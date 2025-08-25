package com.ruko.system.domain.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class UserQueueStatus {
    JOINED,
    WAITING,
    LEFT,
    CANCELLED,
    COMPLETED
}