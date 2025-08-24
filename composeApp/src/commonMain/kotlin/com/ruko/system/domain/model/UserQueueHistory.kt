package com.ruko.system.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserQueueHistory(
    val id: String = "",
    val queueId: String = "",
    val userId: String = "",
    val joinedAt: Long = 0,           // when user joined the queue
    val startedAt: Long = 0,          // when user reached position 1
    val completedAt: Long = 0,        // when service finished
    val serviceTime: Long = 0         // completedAt - startedAt
)
