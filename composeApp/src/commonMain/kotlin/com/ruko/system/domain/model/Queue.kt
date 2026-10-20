package com.ruko.system.domain.model

import com.ruko.system.utils.TimeUtils.currentTimeInMilli
import kotlinx.serialization.Serializable

@Serializable
data class Queue(
    val id: String = "",                  // queueId
    val businessId: String = "",          // which business created it
    val name: String = "",                // e.g., "Doctor OPD"
    val description: String = "",
    val timezone: String = "Asia/Kolkata",
    val schemaVersion: Int = 2,
    // Compatibility with the existing owner forms and stored queue documents.
    val storeId: String = businessId,
    val type: String = "",
    val status: String = "ACTIVE",
    val size: Int = 0,
    val time: Int = 0,
    val createdAt: Long = currentTimeInMilli(),           // ISO datetime
    val isActive: Boolean = true,
    val dailyLimit: Int = 100,            // default tokens per day
    val avgServiceTime: Long = 600000, // 10 min default
    val maxQueueSize: Int? = null,
    val endTime: String = "",              // ISO datetime
    val closingHour: Long? = null, // until which queue is active today
    val servicesOffered: List<String> = emptyList(),
)