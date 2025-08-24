package com.ruko.system.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Queue(
    val id: String = "",
    val name: String = "",
    val size: Int = 5,
    val time: Int = 100,
    val type: String = "",
    val status: String = "",
    val avgServiceTime: Long = 10,
    val storeId: String = "", // Reference to the Owner ID
)