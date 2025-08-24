package com.ruko.system.presentation.screens.owner.queue.userqueue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruko.system.domain.repository.QueueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class UserQueueViewModel(
    private val repository: QueueRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UserQueueUiState>(UserQueueUiState.Loading)
    val uiState: StateFlow<UserQueueUiState> = _uiState.asStateFlow()

    fun observeUserQueues(queueId: String) {
        viewModelScope.launch {
            repository.observeUserQueueWithUserInfo(queueId)
                .onStart { _uiState.value = UserQueueUiState.Loading }
                .catch { e ->
                    _uiState.value = UserQueueUiState.Error(e.message ?: "Unknown error")
                }
                .collect { queues ->
                    _uiState.value = if (queues.isEmpty()) {
                        UserQueueUiState.Empty
                    } else {
                        UserQueueUiState.Success(queues)
                    }
                }
        }
    }

    fun markCompleted(queueId: String, userQueueId: String) {
        viewModelScope.launch {
            try {
                repository.markUserQueueCompleted(queueId, userQueueId)
            } catch (e: Exception) {
                _uiState.value = UserQueueUiState.Error("Failed to update queue")
            }
        }
    }
}