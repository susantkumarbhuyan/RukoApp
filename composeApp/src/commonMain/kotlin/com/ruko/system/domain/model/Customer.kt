package com.ruko.system.domain.model

import com.ruko.system.domain.model.enums.AccountStatus
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
data class Customer @OptIn(ExperimentalTime::class) constructor(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val profileImageUrl: String? = null,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val loyaltyPoints: Int = 0,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds(),
    val lastActiveAt: Long? = null,
    val fcmToken: String? = null,
    val language: String? = null,
    val timezone: String? = null,
    val status: AccountStatus = AccountStatus.ACTIVE
)