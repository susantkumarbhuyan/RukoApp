package com.ruko.system.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class UserQueueStatus {
    JOINED,
    WAITING,
    LEFT,
    CANCELLED,
    COMPLETED
}

@Serializable
enum class QueueStatus {
    ACTIVE,
    IN_ACTIVE,
    DELETED
}