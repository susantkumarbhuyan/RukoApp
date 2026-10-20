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
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.functions.FirebaseFunctions
import com.ruko.system.notifications.PushNotifications
import com.ruko.system.utils.generateUUID
import kotlinx.serialization.Serializable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class QueueRepositoryImpl(
    val auth: FirebaseAuth,
    val firestore: FirebaseFirestore,
    val functions: FirebaseFunctions,
    val preferenceManager: PreferenceManager
) : QueueRepository {

    // ------------------ 🔹 Properties ------------------


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
        PushNotifications.unregisterForLogout()
        auth.signOut()
        preferenceManager.logout()
    }


    @Serializable
    private data class Command(
        val action: String, val queueId: String, val requestId: String,
        val ticketId: String? = null
    )
    @Serializable
    private data class SaveQueue(val queue: Queue)
    @Serializable
    private data class Membership(
        val userId: String = "", val queueId: String = "",
        val ticketId: String = "", val active: Boolean = false
    )

    private fun tickets(queueId: String) = firestore.collection("queues").document(queueId)
        .collection("tickets")
    private suspend fun membership(userId: String): Membership? {
        val snapshot = firestore.collection("activeMemberships").document(userId).get()
        return if (snapshot.exists) snapshot.data<Membership>() else null
    }
    private val commandMutex = Mutex()
    private suspend fun command(action: String, queueId: String, ticketId: String? = null) {
        commandMutex.withLock {
            val key = listOf(currentUserId, queueId, action, ticketId.orEmpty()).joinToString("_")
            val requestId = preferenceManager.getPendingRequest(key) ?: generateUUID().also {
                preferenceManager.putPendingRequest(key, it)
            }
            functions.httpsCallable("rukoQueueCommand")(Command(action, queueId, requestId, ticketId))
            preferenceManager.clearPendingRequest(key)
        }
    }

    override suspend fun createQueue(queue: Queue) {
        functions.httpsCallable("rukoSaveQueue")(SaveQueue(queue))
        preferenceManager.saveQueue(getQueue(queue.id))
    }

    override suspend fun getQueue(queueId: String): Queue {
        val snapshot = firestore.collection("queues").document(queueId).get()
        check(snapshot.exists) { "Queue not found" }
        val queue = snapshot.data<Queue>().copy(id = queueId)
        check(queue.status != "DELETED") { "Queue was deleted" }
        return queue
    }

    override suspend fun getQueues(storeId: String): List<Queue> =
        firestore.collection("queues").where { "storeId" equalTo storeId }.get()
            .documents.map { it.data<Queue>().copy(id = it.id) }
            .filter { it.status != "DELETED" }

    override suspend fun updateQueue(queue: Queue) {
        functions.httpsCallable("rukoSaveQueue")(SaveQueue(queue))
        preferenceManager.saveQueue(getQueue(queue.id))
    }

    override suspend fun deleteQueue(queueId: String) {
        functions.httpsCallable("rukoDeleteQueue")(mapOf("queueId" to queueId))
        preferenceManager.deleteQueue(queueId)
    }

    override suspend fun joinUserQueue(queueId: String, userId: String) {
        require(userId == currentUserId && userId.isNotBlank()) { "Please sign in again" }
        command("JOIN", queueId)
        preferenceManager.setJoinedQueue(queueId)
    }

    override suspend fun leaveUserQueue(queueId: String, userId: String) {
        require(userId == currentUserId) { "Please sign in again" }
        val own = membership(userId)
        if (own?.active == true) {
            check(own.queueId == queueId) { "Queue membership changed" }
            command("LEAVE", queueId, own.ticketId)
        }
        preferenceManager.deleteJoinedQueue()
    }

    override suspend fun markArrived(queueId: String, userId: String) {
        require(userId == currentUserId) { "Please sign in again" }
        val own = membership(userId)
        check(own?.active == true && own.queueId == queueId) { "You are no longer in this queue" }
        command("ARRIVE", queueId, own.ticketId)
    }

    override suspend fun startService(queueId: String, userQueueId: String) {
        command("START", queueId, userQueueId)
    }

    override suspend fun markUserQueueCompleted(queueId: String, userQueueId: String) {
        command("COMPLETE", queueId, userQueueId)
    }

    override suspend fun getUserQueues(queueId: String): List<UserQueue> =
        tickets(queueId).where { "status" equalTo "JOINED" }.orderBy("sequence").get()
            .documents.map { it.data<UserQueue>() }

    override suspend fun getUserQueueSize(queueId: String): Int {
        // Join previews are available before membership. The backend remains authoritative.
        val snapshot = firestore.collection("queueRuntime").document(queueId).get()
        return if (snapshot.exists) snapshot.get<List<String>>("order").size else 0
    }

    override suspend fun getUserQueuePosition(queueId: String, userId: String): Int {
        val own = membership(userId)
        if (own?.active != true || own.queueId != queueId) return 0
        return getUserQueues(queueId).indexOfFirst { it.id == own.ticketId } + 1
    }

    override fun observeUserQueue(queueId: String): Flow<List<UserQueue>> =
        tickets(queueId).where { "status" equalTo "JOINED" }.orderBy("sequence").snapshots
            .map { snapshot -> snapshot.documents.map { it.data<UserQueue>() } }
            .catch { error ->
                if (error is CancellationException) throw error
                val own = membership(currentUserId)
                // Completion atomically revokes the former member's stream permission.
                if (own != null && !own.active && own.queueId == queueId) emit(emptyList())
                else throw error
            }

    override fun observeUserQueueWithUserInfo(queueId: String): Flow<List<Pair<UserQueue, User?>>> =
        observeUserQueue(queueId).map { entries ->
            entries.map { entry ->
                val user = try {
                    val profile = firestore.collection(FirestoreCollections.USERS).document(entry.userId).get()
                    if (profile.exists) profile.data<User>() else null
                } catch (e: CancellationException) { throw e }
                catch (_: Exception) { null }
                entry to user
            }
        }

    override fun observeUserQueuePosition(queueId: String, userId: String): Flow<Int> =
        observeUserQueue(queueId).map { entries -> entries.indexOfFirst { it.userId == userId } + 1 }

    override fun observeAverageServiceTime(queueId: String): Flow<Long> =
        firestore.collection("queueRuntime").document(queueId).snapshots.map {
            if (it.exists) it.get<Long>("averageMillis").coerceAtLeast(1) else 600_000L
        }

    override suspend fun getAverageServiceTime(queueId: String): Long {
        val runtime = firestore.collection("queueRuntime").document(queueId).get()
        return if (runtime.exists) runtime.get<Long>("averageMillis").coerceAtLeast(1)
            else getQueue(queueId).avgServiceTime
    }

    override suspend fun fetchUserQByUserId(userId: String): UserQueue? {
        require(userId == currentUserId) { "Please sign in again" }
        val own = membership(userId)
        if (own?.active != true) return null
        val ticket = tickets(own.queueId).document(own.ticketId).get()
        return if (ticket.exists) ticket.data<UserQueue>() else null
    }
}
