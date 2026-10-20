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

    fun refresh() {
        loadQueueState()
    }

    private var queueJob: kotlinx.coroutines.Job? = null
    private fun loadQueueState() {
        queueJob?.cancel()
        queueJob = viewModelScope.launch {
            _queueState.value = UserHomeState.Loading
            try {
                val active = repository.fetchUserQByUserId(repository.currentUserId)
                val queueId = active?.queueId
                if (queueId.isNullOrBlank()) {
                    preferenceManager.deleteJoinedQueue()
                    _queueState.value = UserHomeState.NoQueue
                    return@launch
                }
                preferenceManager.setJoinedQueue(queueId)
                val userId = repository.currentUserId
                val queue = repository.getQueue(queueId)
                kotlinx.coroutines.flow.combine(
                    repository.observeUserQueue(queueId),
                    repository.observeAverageServiceTime(queueId)
                ) { entries, average -> entries to average }.collect { (entries, average) ->
                    val position = com.ruko.system.utils.QueueTiming.position(entries, userId)
                    if (position == 0) {
                        preferenceManager.deleteJoinedQueue()
                        _queueState.value = UserHomeState.NoQueue
                    } else {
                        _queueState.value = UserHomeState.JoinedQueue(
                            queueId, queue.name, position, entries.size,
                            com.ruko.system.utils.QueueTiming.waitMillis(
                                entries, userId, average,
                                com.ruko.system.utils.TimeUtils.currentTimeInMilli()
                            )
                        )
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _queueState.value = UserHomeState.Error(e.message ?: "Could not load queue")
            }
        }
    }
}
