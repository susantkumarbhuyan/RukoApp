package com.ruko.system.presentation.screens.user.queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruko.system.data.local.preference.PreferenceManager
import com.ruko.system.domain.model.QRQueue
import com.ruko.system.domain.model.Queue
import com.ruko.system.domain.repository.QueueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class JoinQueueState {
    object Loading : JoinQueueState()
    data class Success(
        val queue: Queue,
        val currentPosition: Int, // 1-based
        val estimatedTime: Long // milliseconds
    ) : JoinQueueState()

    data class Error(val message: String) : JoinQueueState()
}

class JoinQViewModel(
    private val repository: QueueRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _state = MutableStateFlow<JoinQueueState>(JoinQueueState.Loading)
    val state: StateFlow<JoinQueueState> = _state.asStateFlow()

    fun loadQueue(qrQueue: QRQueue) {
        viewModelScope.launch {
            _state.value = JoinQueueState.Loading
            try {
                // Fetch queue from repository
                val queue = repository.getQueue(qrQueue.queueId)
                val currentPosition = (repository.getUserQueueSize(qrQueue.queueId) + 1)
                val estimatedTime =
                    (currentPosition - 1).coerceAtLeast(0) * queue.avgServiceTime
                _state.value = JoinQueueState.Success(
                    queue = queue,
                    currentPosition = currentPosition,
                    estimatedTime = estimatedTime
                )
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = JoinQueueState.Error(
                    e.message ?: "Failed to load queue"
                )
            }
        }
    }

    private val _joining = MutableStateFlow(false)
    val joining = _joining.asStateFlow()

    fun joinQueue(qrQueue: QRQueue, onJoined: () -> Unit) {
        if (_joining.value) return
        _joining.value = true
        viewModelScope.launch {
            try {
                val joinedQueueId = preferenceManager.getJoinedQueue()
                if (joinedQueueId != null && repository.getUserQueuePosition(
                        joinedQueueId, repository.currentUserId) > 0) {
                    _state.value = JoinQueueState.Error(
                        "You are already in a queue"
                    )
                    return@launch
                }

                val userId = repository.currentUserId.takeIf { it.isNotBlank() }
                    ?: throw IllegalStateException("Please sign in again")

                repository.joinUserQueue(
                    queueId = qrQueue.queueId,
                    userId = userId
                )
                preferenceManager.setJoinedQueue(qrQueue.queueId)

                onJoined()
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = JoinQueueState.Error(
                    e.message ?: "Failed to join queue"
                )
            } finally {
                _joining.value = false
            }
        }
    }
}