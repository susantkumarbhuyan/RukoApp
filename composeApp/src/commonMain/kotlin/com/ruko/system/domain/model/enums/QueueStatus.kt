package com.ruko.system.domain.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class QueueStatus {
    ACTIVE,
    IN_ACTIVE,
    DELETED
}