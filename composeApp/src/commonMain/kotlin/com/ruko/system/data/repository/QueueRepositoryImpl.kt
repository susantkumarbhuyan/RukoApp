package com.ruko.system.data.repository

import com.ruko.system.data.local.preference.PreferenceManager
import com.ruko.system.domain.model.LoginCredentials
import com.ruko.system.domain.model.Owner
import com.ruko.system.domain.model.Queue
import com.ruko.system.domain.model.User
import com.ruko.system.domain.model.UserQueue
import com.ruko.system.domain.model.UserQueueHistory
import com.ruko.system.domain.model.UserQueueStatus
import com.ruko.system.domain.model.enums.AccountType
import com.ruko.system.domain.repository.QueueRepository
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.database.FirebaseDatabase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class QueueRepositoryImpl(
    val auth: FirebaseAuth,
    val database: FirebaseDatabase,
    val firestore: FirebaseFirestore,
    val preferenceManager: PreferenceManager
) : QueueRepository {

    // ------------------ 🔹 Properties ------------------
    private val userQueuesRef get() = database.reference("userQueues")

    override val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    override val isAuthenticated: Boolean
        get() = auth.currentUser != null && auth.currentUser?.isAnonymous == false

    override val currentUser: Flow<User>
        get() = auth.authStateChanged.map { it?.let { User(it.uid) } ?: User() }

    // ------------------ 🔹 Auth ------------------
    private suspend fun signIn(loginState: LoginCredentials): String {
        val result = auth.signInWithEmailAndPassword(loginState.email, loginState.password)
        return result.user?.uid ?: throw Exception("Authentication failed")
    }

    override suspend fun <T : Any> authenticate(
        loginState: LoginCredentials,
        accountType: AccountType,
    ): T? {
        val uid = signIn(loginState)

        val snapshot = firestore
            .collection(accountType.collectionName)
            .document(uid)
            .get()

        if (!snapshot.exists) throw Exception("No ${accountType.name} found for UID: $uid")
        val dataMap = when (accountType) {
            AccountType.USER -> snapshot.data<User>().copy(id = uid)
            AccountType.OWNER -> snapshot.data<Owner>().copy(id = uid)
        }
        return dataMap as T?
    }

    private suspend fun createUser(email: String, password: String): String {
        val result = auth.createUserWithEmailAndPassword(email, password)
        return result.user?.uid ?: ""
    }

    override suspend fun createUserAccount(user: User): String {
        val uid = createUser(user.email, user.password)
        firestore.collection(FirestoreCollections.USERS).document(uid).set(user.copy(id = uid, password = ""))
        return uid
    }

    override suspend fun createOwnerAccount(owner: Owner): String {
        val uid = createUser(owner.email, owner.password)
        firestore.collection(FirestoreCollections.OWNERS).document(uid).set(owner.copy(id = uid, password = ""))
        return uid
    }

    override suspend fun signOut() {
        if (auth.currentUser?.isAnonymous == true) {
            auth.currentUser?.delete()
        }
        auth.signOut()
    }

    // ------------------ 🔹 Queue CRUD ------------------
    override suspend fun createQueue(queue: Queue) {
        try {
            require(currentUserId.isNotBlank() && queue.storeId == currentUserId) { "Please sign in as the owner" }
            require(queue.name.isNotBlank() && queue.avgServiceTime > 0) { "Enter a name and valid service time" }
            firestore.collection(FirestoreCollections.QUEUES).document(queue.id).set(queue.copy(businessId = currentUserId))
            preferenceManager.saveQueue(queue)
        } catch (e: Exception) {
            throw Exception("Failed to create queue: ${e.message}", e)
        }
    }

    override suspend fun getQueue(queueId: String): Queue {

        val snapshot = firestore.collection(FirestoreCollections.QUEUES).document(queueId).get()
        if (!snapshot.exists) throw Exception("Queue not found: $queueId")

        val queue = snapshot.data<Queue>().copy(id = queueId)
        preferenceManager.saveQueue(queue)
        return queue
    }

    override suspend fun getQueues(storeId: String): List<Queue> {

        val snapshot = firestore.collection(FirestoreCollections.QUEUES)
            .where { "storeId" equalTo storeId }
            .get()

        val queues = snapshot.documents.map { it.data<Queue>().copy(id = it.id) }
        preferenceManager.saveQueues(queues)
        return queues
    }

    private suspend fun requireQueueOwner(queueId: String) {
        val queue = getQueue(queueId)
        check(currentUserId.isNotBlank() && (queue.storeId == currentUserId || queue.businessId == currentUserId)) {
            "Only the business owner can manage this queue"
        }
    }

    override suspend fun updateQueue(queue: Queue) {
        requireQueueOwner(queue.id)
        if (queue.id.isEmpty()) throw IllegalArgumentException("Queue id is required for update")

        firestore.collection(FirestoreCollections.QUEUES).document(queue.id).set(queue)
        preferenceManager.saveQueue(queue)
    }

    override suspend fun deleteQueue(queueId: String) {
        requireQueueOwner(queueId)
        check(getUserQueues(queueId).isEmpty()) { "Finish the active queue before deleting it" }
        firestore.collection(FirestoreCollections.QUEUES).document(queueId).delete()
        preferenceManager.deleteQueue(queueId)
    }

    // ------------------ 🔹 User Queue (Realtime DB) ------------------
    @OptIn(ExperimentalTime::class)
    override suspend fun joinUserQueue(queueId: String, userId: String) {
        require(userId.isNotBlank() && userId == currentUserId) { "Please sign in again" }
        val queue = getQueue(queueId)
        check(queue.isActive && queue.status == "ACTIVE") { "This queue is closed" }
        val existing = getUserQueues(queueId)
        if (existing.any { it.userId == userId }) return
        val limit = queue.maxQueueSize ?: queue.size.takeIf { it > 0 }
        check(limit == null || existing.size < limit) { "This queue is full" }
        // A stable UID path prevents multiple rows for repeated joins.
        val entry = UserQueue(
            id = userId, queueId = queueId, userId = userId,
            status = UserQueueStatus.JOINED,
            joinedAt = Clock.System.now().toEpochMilliseconds()
        )
        userQueuesRef.child(queueId).child(userId).setValue(entry)
    }

    override suspend fun leaveUserQueue(queueId: String, userId: String) {
        // Also handles entries written by older versions using push IDs.
        require(userId == currentUserId) { "Please sign in again" }
        val matches = getUserQueues(queueId).filter { it.userId == userId }
        check(matches.none { it.startedAt != null }) { "Service is already in progress" }
        for (entry in matches) {
            userQueuesRef.child(queueId).child(entry.id)
                .updateChildren(mapOf("status" to UserQueueStatus.LEFT.name))
        }
        preferenceManager.deleteJoinedQueue()
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun markArrived(queueId: String, userId: String) {
        require(userId == currentUserId) { "Please sign in again" }
        val entry = getUserQueues(queueId).firstOrNull { it.userId == userId }
            ?: error("You are no longer in this queue")
        if (entry.arrivedAt == null) {
            userQueuesRef.child(queueId).child(entry.id)
                .updateChildren(mapOf("arrivedAt" to Clock.System.now().toEpochMilliseconds()))
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun startService(queueId: String, userQueueId: String) {
        requireQueueOwner(queueId)
        val entries = getUserQueues(queueId)
        check(entries.firstOrNull()?.id == userQueueId) { "Serve the first customer in line" }
        val entry = entries.first()
        check(entry.arrivedAt != null) { "Customer must check in first" }
        if (entry.startedAt == null) {
            userQueuesRef.child(queueId).child(entry.id)
                .updateChildren(mapOf("startedAt" to Clock.System.now().toEpochMilliseconds()))
        }
    }

    override suspend fun getUserQueues(queueId: String): List<UserQueue> {
        val snapshot = userQueuesRef.child(queueId).valueEvents.first()
        return snapshot.children.map { it.value<UserQueue>() }
            .filter { it.status == UserQueueStatus.JOINED }
            .sortedWith(compareBy<UserQueue> { it.joinedAt }.thenBy { it.id })
    }

    override suspend fun getUserQueueSize(queueId: String): Int {
        val snapshot = userQueuesRef.child(queueId).valueEvents.first()
        return snapshot.children.count { it.value<UserQueue>().status == UserQueueStatus.JOINED }
    }

    override suspend fun getUserQueuePosition(queueId: String, userId: String): Int {
        val snapshot = userQueuesRef.child(queueId).valueEvents.first()
        val sortedQueues = snapshot.children
            .map { it.value<UserQueue>() }
            .filter { it.status == UserQueueStatus.JOINED }
            .sortedWith(compareBy<UserQueue> { it.joinedAt }.thenBy { it.id })

        return sortedQueues.indexOfFirst { it.userId == userId } + 1
    }

    // ------------------ 🔹 Queue Progress ------------------
    @OptIn(ExperimentalTime::class)
    override suspend fun markUserQueueCompleted(queueId: String, userQueueId: String) {
        requireQueueOwner(queueId)
        val ref = userQueuesRef.child(queueId).child(userQueueId)
        val entry = ref.valueEvents.first().value<UserQueue>()
        // Keep the timestamp stable across retries so history writes are idempotent.
        val completedAt = entry.completedAt ?: Clock.System.now().toEpochMilliseconds()
        val startedAt = entry.startedAt ?: error("Start service before completing it")
        if (entry.status != UserQueueStatus.COMPLETED) {
            check(getUserQueues(queueId).firstOrNull()?.id == userQueueId) {
                "Complete the current customer first"
            }
            ref.updateChildren(mapOf(
                "status" to UserQueueStatus.COMPLETED.name,
                "completedAt" to completedAt
            ))
        }
        val history = UserQueueHistory(
            id = entry.id, queueId = queueId, userId = entry.userId,
            joinedAt = entry.joinedAt, startedAt = startedAt,
            completedAt = completedAt, serviceTime = (completedAt - startedAt).coerceAtLeast(1)
        )
        firestore.collection(FirestoreCollections.QUEUE_HISTORY).document(queueId)
            .collection(FirestoreCollections.USERS)
            .document(entry.id + "_" + entry.joinedAt).set(history)
    }

    // ------------------ 🔹 Observers ------------------
    override fun observeUserQueue(queueId: String): Flow<List<UserQueue>> {
        return userQueuesRef.child(queueId).valueEvents
            .map { snapshot ->
                snapshot.children.map { it.value<UserQueue>() }
                    .filter { it.status == UserQueueStatus.JOINED }
                    .sortedWith(compareBy<UserQueue> { it.joinedAt }.thenBy { it.id })
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeUserQueueWithUserInfo(queueId: String): Flow<List<Pair<UserQueue, User?>>> {
        return userQueuesRef.child(queueId).valueEvents
            .flatMapLatest { snapshot ->
                val userQueues = snapshot.children.map { it.value<UserQueue>() }
                    .filter { it.status == UserQueueStatus.JOINED }
                    .sortedWith(compareBy<UserQueue> { it.joinedAt }.thenBy { it.id })

                flow {
                    val list = userQueues.map { queue ->
                        val userSnapshot = firestore.collection(FirestoreCollections.USERS)
                            .where { "id" equalTo queue.userId }
                            .get()
                        val queues = userSnapshot.documents.map { it.data<User>() }
                        queue to queues.firstOrNull()
                    }
                    emit(list)
                }
            }
    }

    override fun observeUserQueuePosition(queueId: String, userId: String): Flow<Int> {
        return userQueuesRef.child(queueId).valueEvents
            .map { snapshot ->
                snapshot.children
                    .map { it.value<UserQueue>() }
                    .filter { it.status == UserQueueStatus.JOINED }
                    .sortedWith(compareBy<UserQueue> { it.joinedAt }.thenBy { it.id })
                    .indexOfFirst { it.userId == userId } + 1
            }
    }

    // ------------------ 🔹 Stats ------------------
    override suspend fun getAverageServiceTime(queueId: String): Long {
        val snapshot = firestore.collection(FirestoreCollections.QUEUE_HISTORY)
            .document(queueId)
            .collection(FirestoreCollections.USERS)
            .get()

        val histories = snapshot.documents.map { it.data<UserQueueHistory>() }
        if (histories.isEmpty()) return getQueue(queueId).avgServiceTime

        val avgServiceTime = histories.map { it.serviceTime }.filter { it > 0 }.takeIf { it.isNotEmpty() }?.average()
            ?: return getQueue(queueId).avgServiceTime
        return avgServiceTime.toLong()
    }

    // ------------------ 🔹 Misc ------------------
    override suspend fun fetchUserQByUserId(userId: String): UserQueue? {
        // The saved pointer avoids downloading every business queue.
        val queueId = preferenceManager.getJoinedQueue() ?: return null
        return getUserQueues(queueId).firstOrNull { it.userId == userId }
    }
}