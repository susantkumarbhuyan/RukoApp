package com.ruko.system.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserQueue(
    val id: String = "",
    val queueId: String = "",
    val userId: String = "",
    val status: UserQueueStatus = UserQueueStatus.WAITING,
    val joinedAt: Long,
    val startedAt: Long? = null
)
