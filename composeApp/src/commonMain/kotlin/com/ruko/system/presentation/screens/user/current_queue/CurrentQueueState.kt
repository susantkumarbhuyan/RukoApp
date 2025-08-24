package com.ruko.system.presentation.screens.user.current_queue

import com.ruko.system.domain.model.Queue

sealed class CurrentQueueState {
    object Loading : CurrentQueueState()
    data class Success(
        val queue: Queue,
        val queueId: String,
        val currentPosition: Int,
        val estimatedTime: Long // minutes
    ) : CurrentQueueState()

    data class Error(val message: String) : CurrentQueueState()
}