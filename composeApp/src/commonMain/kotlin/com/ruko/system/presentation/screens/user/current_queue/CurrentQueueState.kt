package com.ruko.system.presentation.screens.user.current_queue

import com.ruko.system.domain.model.Queue

sealed class CurrentQueueState {
    object Loading : CurrentQueueState()
    data class Success(
        val queue: Queue,
        val queueId: String,
        val currentPosition: Int,
        val estimatedTime: Long, // milliseconds
        val queueLength: Int = 0,
        val arrived: Boolean = false,
        val serving: Boolean = false
    ) : CurrentQueueState()
    object Finished : CurrentQueueState()
    data class Error(val message: String) : CurrentQueueState()
}
