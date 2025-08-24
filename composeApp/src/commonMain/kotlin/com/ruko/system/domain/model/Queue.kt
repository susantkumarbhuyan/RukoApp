package com.ruko.system.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Queue(
    val id: String = "",                  // queueId
    val businessId: String = "",          // which business created it
    val name: String = "",                // e.g., "Doctor OPD"
    val description: String = "",
    val createdAt: String = "",           // ISO datetime
    val isActive: Boolean = true,
    val dailyLimit: Int = 100,            // default tokens per day
    val avgServiceTime: Long = 10,
    val endTime: String = ""              // ISO datetime until which queue is active today
)