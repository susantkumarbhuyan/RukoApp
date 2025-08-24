package com.ruko.system.presentation.screens.owner.queue

import com.ruko.system.domain.model.Queue

sealed class QueueListUiState {
    object Idle : QueueListUiState()
    object Loading : QueueListUiState()
    data class Success(val queues: List<Queue>) : QueueListUiState()
    object Empty : QueueListUiState()
    data class Error(val message: String) : QueueListUiState()
    object NoOwner : QueueListUiState()
}