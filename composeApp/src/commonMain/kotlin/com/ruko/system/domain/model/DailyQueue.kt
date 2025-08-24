package com.ruko.system.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DailyQueue(
    val id: String = "",                  // dailyQueueId
    val queueId: String = "",             // reference to Queue
    val date: String = "",                // YYYY-MM-DD

    val totalTokensIssued: Int = 0,       // how many issued today
    val servedTokens: Int = 0,            // how many already served
    val maxLimit: Int = 100,              // max allowed today
    val avgServiceTime: Int = 5,          // avg minutes per person

    val isClosed: Boolean = false,        // manually closed
    val createdAt: String = ""            // when started
)