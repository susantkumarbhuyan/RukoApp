package com.ruko.system.domain.model

import kotlinx.datetime.LocalDateTime

data class QueueHistory(
    val id: String,
    val queueName: String,
    val totalMembers: Int,
    val totalTimeMinutes: Int, // total time taken in minutes
    val date: LocalDateTime
)