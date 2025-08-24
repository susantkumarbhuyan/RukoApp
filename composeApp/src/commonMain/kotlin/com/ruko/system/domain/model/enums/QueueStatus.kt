package com.ruko.system.domain.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class QueueStatus {
    WAITING,
    SERVED,
    CANCELLED,
    NO_SHOW
}