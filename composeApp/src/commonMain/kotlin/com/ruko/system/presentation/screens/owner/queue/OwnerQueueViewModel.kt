package com.ruko.system.presentation.screens.owner.queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruko.system.domain.model.Queue
import com.ruko.system.domain.repository.QueueRepository
import com.ruko.system.presentation.screens.owner.queue.components.QueueUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OwnerQueueViewModel(
    private val repository: QueueRepository,  // Your repository,
) : ViewModel() {
    private val _queueState = MutableStateFlow<QueueUiState>(QueueUiState.Idle)
    val queueState: StateFlow<QueueUiState> = _queueState.asStateFlow()

    // Fetch queue
    fun loadQueue(queueId: String) {
        viewModelScope.launch {
            _queueState.value = QueueUiState.Loading
            try {
                val queue = repository.getQueue(queueId)
                _queueState.value = QueueUiState.Success(queue)
            } catch (e: Exception) {
                _queueState.value = QueueUiState.Error("Failed to fetch queue: ${e.message}")
            }
        }
    }

    // Create queue
    fun createQueue(queue: Queue) {
        viewModelScope.launch {
            _queueState.value = QueueUiState.Loading
            try {
                repository.createQueue(queue)
                _queueState.value = QueueUiState.Success(queue)
            } catch (e: Exception) {
                _queueState.value = QueueUiState.Error("Failed to create queue: ${e.message}")
            }
        }
    }

    // Edit queue
    fun editQueue(updatedQueue: Queue) {
        viewModelScope.launch {
            try {
                repository.updateQueue(updatedQueue)
                _queueState.value = QueueUiState.Success(updatedQueue)
            } catch (e: Exception) {
                _queueState.value = QueueUiState.Error("Failed to update queue: ${e.message}")
            }
        }
    }

    // Delete queue
    fun deleteQueue(queueId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.deleteQueue(queueId)
                _queueState.value = QueueUiState.Idle
                onSuccess()
            } catch (e: Exception) {
                _queueState.value = QueueUiState.Error("Failed to delete queue: ${e.message}")
            }
        }
    }

    // Reset state after handling (useful to avoid re-navigation)
    fun resetState() {
        _queueState.value = QueueUiState.Idle
    }
}