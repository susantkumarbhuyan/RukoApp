package com.ruko.system.data.repository

import com.ruko.system.data.local.preference.PreferenceManager
import com.ruko.system.domain.model.Account
import com.ruko.system.domain.model.AccountType
import com.ruko.system.domain.model.Owner
import com.ruko.system.domain.model.Queue
import com.ruko.system.domain.model.User
import com.ruko.system.domain.model.UserQueue
import com.ruko.system.domain.model.UserQueueHistory
import com.ruko.system.domain.model.UserQueueStatus
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
    private suspend fun signIn(loginState: Account): String {
        val result = auth.signInWithEmailAndPassword(loginState.email, loginState.password)
        return result.user?.uid ?: throw Exception("Authentication failed")
    }

    override suspend fun <T : Any> authenticate(
        loginState: Account,
        accountType: AccountType,
    ): T? {
        val uid = signIn(loginState)

        val snapshot = firestore
            .collection(accountType.collectionName)
            .document(uid)
            .get()

        if (!snapshot.exists) throw Exception("No ${accountType.name} found for UID: $uid")
        val dataMap = when (accountType) {
            AccountType.USER -> snapshot.data<User>()
            AccountType.OWNER -> snapshot.data<Owner>()
        }
        return dataMap as T?
    }

    private suspend fun createUser(email: String, password: String): String {
        val result = auth.createUserWithEmailAndPassword(email, password)
        return result.user?.uid ?: ""
    }

    override suspend fun createUserAccount(user: User): String {
        val uid = createUser(user.email, user.password)
        firestore.collection(FirestoreCollections.USERS).document(uid).set(user.copy(id = uid))
        return uid
    }

    override suspend fun createOwnerAccount(owner: Owner): String {
        val uid = createUser(owner.email, owner.password)
        firestore.collection(FirestoreCollections.OWNERS).document(uid).set(owner)
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
            firestore.collection(FirestoreCollections.QUEUES).document(queue.id).set(queue)
            preferenceManager.saveQueue(queue)
        } catch (e: Exception) {
            throw Exception("Failed to create queue: ${e.message}", e)
        }
    }

    override suspend fun getQueue(queueId: String): Queue {
        preferenceManager.getQueue(queueId)?.let { return it }

        val snapshot = firestore.collection(FirestoreCollections.QUEUES).document(queueId).get()
        if (!snapshot.exists) throw Exception("Queue not found: $queueId")

        val queue = snapshot.data<Queue>()
        preferenceManager.saveQueue(queue)
        return queue
    }

    override suspend fun getQueues(storeId: String): List<Queue> {
        preferenceManager.getQueues()?.takeIf { it.isNotEmpty() }?.let { return it }

        val snapshot = firestore.collection(FirestoreCollections.QUEUES)
            .where { "storeId" equalTo storeId }
            .get()

        val queues = snapshot.documents.map { it.data<Queue>() }
        preferenceManager.saveQueues(queues)
        return queues
    }

    override suspend fun updateQueue(queue: Queue) {
        if (queue.id.isEmpty()) throw IllegalArgumentException("Queue id is required for update")

        firestore.collection(FirestoreCollections.QUEUES).document(queue.id).set(queue)
        preferenceManager.saveQueue(queue)
    }

    override suspend fun deleteQueue(queueId: String) {
        firestore.collection(FirestoreCollections.QUEUES).document(queueId).delete()
        preferenceManager.deleteQueue(queueId)
    }

    // ------------------ 🔹 User Queue (Realtime DB) ------------------
    @OptIn(ExperimentalTime::class)
    override suspend fun joinUserQueue(queueId: String, userId: String) {
        val userQueueId = userQueuesRef.child(queueId).push().key ?: userId
        val userQueue = UserQueue(
            id = userQueueId,
            queueId = queueId,
            userId = userId,
            status = UserQueueStatus.JOINED,
            joinedAt = Clock.System.now().toEpochMilliseconds()
        )
        userQueuesRef.child(queueId).child(userQueueId).setValue(userQueue)
    }

    override suspend fun leaveUserQueue(queueId: String, userId: String) {
        userQueuesRef.child(queueId).child(userId).removeValue()
    }

    override suspend fun getUserQueues(queueId: String): List<UserQueue> {
        val snapshot = userQueuesRef.child(queueId).valueEvents.first()
        return snapshot.children.map { it.value<UserQueue>() }
            .filter { it.status == UserQueueStatus.JOINED }
            .sortedBy { it.joinedAt }
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
            .sortedBy { it.joinedAt }

        return sortedQueues.indexOfFirst { it.userId == userId } + 1
    }

    // ------------------ 🔹 Queue Progress ------------------
    @OptIn(ExperimentalTime::class)
    private suspend fun markNextUserStarted(queueId: String) {
        val snapshot = userQueuesRef.child(queueId).valueEvents.first()
        val nextUser = snapshot.children
            .map { it.value<UserQueue>() }
            .filter { it.status == UserQueueStatus.JOINED && it.startedAt == null }
            .minByOrNull { it.joinedAt }

        nextUser?.let { user ->
            val timestamp = Clock.System.now().toEpochMilliseconds()
            userQueuesRef.child(queueId).child(user.id)
                .updateChildren(mapOf("startedAt" to timestamp))
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun markUserQueueCompleted(queueId: String, userQueueId: String) {
        val snapshot = userQueuesRef.child(queueId).child(userQueueId).valueEvents
        val userQueue = snapshot.first().value<UserQueue>()

        val completedAt = Clock.System.now().toEpochMilliseconds()
        val startedAt = userQueue.startedAt ?: userQueue.joinedAt
        val serviceTime = completedAt - startedAt

        userQueuesRef.child(queueId).child(userQueueId)
            .updateChildren(mapOf("status" to UserQueueStatus.COMPLETED.name))

        val historyDoc = firestore.collection(FirestoreCollections.QUEUE_HISTORY)
            .document(queueId)
            .collection(FirestoreCollections.USERS)
            .document(userQueue.userId)

        val history = UserQueueHistory(
            id = userQueue.id,
            queueId = queueId,
            userId = userQueue.userId,
            joinedAt = userQueue.joinedAt,
            startedAt = startedAt,
            completedAt = completedAt,
            serviceTime = serviceTime
        )

        historyDoc.set(history)
        markNextUserStarted(queueId)
    }

    // ------------------ 🔹 Observers ------------------
    override fun observeUserQueue(queueId: String): Flow<List<UserQueue>> {
        return userQueuesRef.child(queueId).valueEvents
            .map { snapshot ->
                snapshot.children.map { it.value<UserQueue>() }
                    .filter { it.status == UserQueueStatus.JOINED }
                    .sortedBy { it.joinedAt }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeUserQueueWithUserInfo(queueId: String): Flow<List<Pair<UserQueue, User?>>> {
        return userQueuesRef.child(queueId).valueEvents
            .flatMapLatest { snapshot ->
                val userQueues = snapshot.children.map { it.value<UserQueue>() }
                    .filter { it.status == UserQueueStatus.JOINED }
                    .sortedBy { it.joinedAt }

                flow {
                    val list = userQueues.map { queue ->
                        val userSnapshot = firestore.collection(FirestoreCollections.USERS)
                            .where { "id" equalTo queue.userId }
                            .get()
                        val queues = userSnapshot.documents.map { it.data<User>() }
                        queue to queues.first()
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
                    .sortedBy { it.joinedAt }
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

        val avgServiceTime = histories.map { it.serviceTime }.average()
        return avgServiceTime.toLong()
    }

    // ------------------ 🔹 Misc ------------------
    override suspend fun fetchUserQByUserId(userId: String): UserQueue? {
        return null
    }
}