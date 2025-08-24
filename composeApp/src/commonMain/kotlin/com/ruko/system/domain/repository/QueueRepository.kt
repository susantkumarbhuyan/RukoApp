package com.ruko.system.domain.repository

import com.ruko.system.domain.model.Account
import com.ruko.system.domain.model.AccountType
import com.ruko.system.domain.model.Owner
import com.ruko.system.domain.model.Queue
import com.ruko.system.domain.model.User
import com.ruko.system.domain.model.UserQueue
import kotlinx.coroutines.flow.Flow

interface QueueRepository {
    val currentUserId: String
    val isAuthenticated: Boolean
    val currentUser: Flow<User>

    suspend fun <T : Any> authenticate(
        loginState: Account,
        accountType: AccountType
    ): T?

    suspend fun createUserAccount(user: User): String
    suspend fun createOwnerAccount(owner: Owner): String
    suspend fun signOut()

    suspend fun createQueue(queue: Queue)
    suspend fun getQueue(queueId: String): Queue
    suspend fun getQueues(storeId: String): List<Queue>
    suspend fun updateQueue(queue: Queue)
    suspend fun deleteQueue(queueId: String)

    suspend fun getUserQueues(queueId: String): List<UserQueue>
    suspend fun leaveUserQueue(queueId: String, userId: String)
    suspend fun getUserQueueSize(queueId: String): Int
    suspend fun getUserQueuePosition(queueId: String, userId: String): Int
    suspend fun joinUserQueue(queueId: String, userId: String)
    fun observeUserQueue(queueId: String): Flow<List<UserQueue>>
    fun observeUserQueueWithUserInfo(queueId: String): Flow<List<Pair<UserQueue, User?>>>
    suspend fun markUserQueueCompleted(queueId: String, userQueueId: String)
    fun observeUserQueuePosition(queueId: String, userId: String): Flow<Int>
    suspend fun getAverageServiceTime(queueId: String): Long
    suspend fun fetchUserQByUserId(userId: String): UserQueue?
}
