package com.ruko.system.presentation.screens.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruko.system.data.local.preference.PreferenceManager
import com.ruko.system.domain.model.User
import com.ruko.system.domain.repository.QueueRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UserHomeState {
    object Loading : UserHomeState()
    data class JoinedQueue(
        val queueId: String,
        val queueName: String,
        val currentPosition: Int,
        val queueLength: Int,
        val estimatedTime: Long
    ) : UserHomeState()

    object NoQueue : UserHomeState()
    data class Error(val message: String) : UserHomeState()
}

class UserHomeViewModel(
    private val preferenceManager: PreferenceManager,
    private val repository: QueueRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    private val _queueState = MutableStateFlow<UserHomeState>(UserHomeState.Loading)
    val queueState: StateFlow<UserHomeState> = _queueState.asStateFlow()

    init {
        loadUserProfile()
        loadQueueState()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            preferenceManager.getUserProfile()?.let { user ->
                _userProfile.value = user.takeIf { it.id.isNotBlank() }
            }
        }
    }

    private fun loadQueueState() {
        viewModelScope.launch {
            _queueState.value = UserHomeState.Loading
            try {
                var queueId = preferenceManager.getJoinedQueue()

                // If no saved queueId, try to fetch from DB
                if (queueId.isNullOrBlank()) {
                    val user = preferenceManager.getUserProfile()
                    if (user != null) {
                        val joinedQueue = repository.fetchUserQByUserId(user.id)

                        queueId = joinedQueue?.queueId
                    }

                    if (queueId.isNullOrBlank()) {
                        _queueState.value = UserHomeState.NoQueue
                        return@launch
                    }
                }

                // Run parallel calls
                coroutineScope {
                    val queueDeferred = async { repository.getQueue(queueId!!) }
                    val sizeDeferred = async { repository.getUserQueueSize(queueId!!) }
                    val avgTimeDeferred = async { repository.getAverageServiceTime(queueId!!) }

                    val queue = queueDeferred.await()
                    val size = sizeDeferred.await()
                    val avgServiceTime = avgTimeDeferred.await() // in ms

                    // For now, assume user is at the end of the queue
                    val currentPosition = size // 0-based size means user is next
                    val estimatedTime = currentPosition * avgServiceTime

                    _queueState.value = UserHomeState.JoinedQueue(
                        queueId = queueId!!,
                        queueName = queue.name,
                        currentPosition = currentPosition,
                        queueLength = size,
                        estimatedTime = estimatedTime
                    )
                }
            } catch (e: Exception) {
                _queueState.value = UserHomeState.Error(e.message ?: "Failed to load queue")
            }
        }
    }
}