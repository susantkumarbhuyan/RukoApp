package com.ruko.system.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CustomerQueue(
    val id: String = "",                  // customerQueueId
    val dailyQueueId: String = "",        // reference to DailyQueue
    val customerId: String = "",          // reference to Customer

    val tokenNumber: Int = 0,             // position in queue
    val status: UserQueueStatus = UserQueueStatus.WAITING,
    val joinedAt: String = "",            // ISO datetime
    val estimatedStartTime: String = "",  // when expected to be served
    val estimatedEndTime: String = "",    // when service ends
    val servedAt: String? = null,
    val cancelledAt: String? = null
)