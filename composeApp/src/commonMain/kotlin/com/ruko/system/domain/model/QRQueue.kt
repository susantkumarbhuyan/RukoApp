package com.ruko.system.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class QRQueue(
    val businessId: String = "",
    val queueId: String = "",
    val queueName: String = "",
    val ownerName: String = ""
)
