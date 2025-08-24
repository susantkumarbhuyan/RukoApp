package com.ruko.system.presentation.screens.owner.queue.userqueue

import com.ruko.system.domain.model.User
import com.ruko.system.domain.model.UserQueue

sealed class UserQueueUiState {
    object Loading : UserQueueUiState()
    data class Success(val queues: List<Pair<UserQueue, User?>>) : UserQueueUiState()
    object Empty : UserQueueUiState()
    data class Error(val message: String) : UserQueueUiState()
}