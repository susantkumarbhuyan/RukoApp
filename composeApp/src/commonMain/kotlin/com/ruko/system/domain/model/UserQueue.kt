package com.ruko.system.domain.model

import com.ruko.system.domain.model.enums.UserQueueStatus
import kotlinx.serialization.Serializable

@Serializable
data class UserQueue(
    val id: String = "",
    val queueId: String = "",
    val userId: String = "",
    val status: UserQueueStatus = UserQueueStatus.WAITING,
    val joinedAt: Long,
    val sequence: Long = 0,
    val startedAt: Long? = null,
    val arrivedAt: Long? = null,
    val completedAt: Long? = null
)
