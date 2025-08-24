package com.ruko.system.presentation.screens.owner.queue.components

import com.ruko.system.domain.model.Queue

sealed class QueueUiState {
    object Idle : QueueUiState()
    object Loading : QueueUiState()
    data class Success(val queue: Queue) : QueueUiState()
    data class Error(val message: String) : QueueUiState()
}