package com.ruko.system.data.local.preference


import com.ruko.system.domain.model.Owner
import com.ruko.system.domain.model.Queue
import com.ruko.system.domain.model.User
import com.ruko.system.utils.SessionManager
import kotlinx.coroutines.flow.Flow

interface PreferenceManager {
    val isOnBoardingDone: Flow<Boolean>
    suspend fun setOnboarding(value: Boolean)
    val isLoggedIn: Flow<Boolean>
    suspend fun setLoggedIn(value: Boolean)

    val userType: Flow<SessionManager.UserType?>
    suspend fun setUserType(type: SessionManager.UserType?)

    // Complete owner profile setup
    suspend fun saveOwnerProfile(owner: Owner)

    // Get complete owner profile
    suspend fun getOwnerProfile(): Owner?

    // Complete owner profile setup
    suspend fun saveUserProfile(user: User)

    // Get complete owner profile
    suspend fun getUserProfile(): User?

    // Logout
    suspend fun logout()

    suspend fun hasSeenOnboarding(): Boolean
    suspend fun setSeenOnboarding()

    suspend fun saveQueue(queue: Queue)
    suspend fun getQueue(queueId: String): Queue?

    // New
    suspend fun saveQueues(queues: List<Queue>)
    suspend fun getQueues(): List<Queue>?
    suspend fun deleteQueue(queueId: String)

    suspend fun setJoinedQueue(queueId: String)
    suspend fun getJoinedQueue(): String?

    suspend fun deleteJoinedQueue()

}