package com.ruko.system.presentation.screens.user.current_queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruko.system.data.local.preference.PreferenceManager
import com.ruko.system.domain.repository.QueueRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CurrentQueueViewModel(
    private val repository: QueueRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _state = MutableStateFlow<CurrentQueueState>(CurrentQueueState.Loading)
    val state: StateFlow<CurrentQueueState> = _state.asStateFlow()

    fun loadQueue(queueId: String) {
        viewModelScope.launch {
            _state.value = CurrentQueueState.Loading
            try {
                // Run both calls in parallel for speed
                val queueDeferred = async { repository.getQueue(queueId) }
                val sizeDeferred = async { repository.getUserQueueSize(queueId) }
                val avgTimeDeferred = async { repository.getAverageServiceTime(queueId) }

                val queue = queueDeferred.await()
                val size = sizeDeferred.await()
                val avgServiceTime = avgTimeDeferred.await() // in ms

                val currentPosition = size + 1
                val estimatedTime = currentPosition * avgServiceTime

                _state.value = CurrentQueueState.Success(
                    queue = queue,
                    queueId = queueId,
                    currentPosition = currentPosition,
                    estimatedTime = estimatedTime
                )
            } catch (e: Exception) {
                _state.value = CurrentQueueState.Error(e.message ?: "Failed to load queue")
            }
        }
    }

    fun leaveQueue(queueId: String, onLeft: () -> Unit) {
        viewModelScope.launch {
            try {
                val userId = _cachedUserId()
                    ?: throw IllegalStateException("User not found")

                withContext(Dispatchers.IO) {
                    repository.leaveUserQueue(queueId, userId)
                }
                onLeft()
            } catch (e: Exception) {
                _state.value = CurrentQueueState.Error(e.message ?: "Failed to leave queue")
            }
        }
    }

    // Cache userId so we don’t fetch every time
    private var cachedUserId: String? = null
    private suspend fun _cachedUserId(): String? {
        return cachedUserId ?: preferenceManager.getUserProfile()?.id.also {
            cachedUserId = it
        }
    }
}