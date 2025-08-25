package com.ruko.system.domain.model

import com.ruko.system.domain.model.enums.AccountStatus
import com.ruko.system.utils.TimeUtils.currentTimeInMilli
import kotlinx.serialization.Serializable
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
    val createdAt: Long = currentTimeInMilli(),
    val updatedAt: Long = currentTimeInMilli(),
    val lastActiveAt: Long? = null,
    val fcmToken: String? = null,
    val language: String? = null,
    val timezone: String? = null,
    val status: AccountStatus = AccountStatus.ACTIVE
)