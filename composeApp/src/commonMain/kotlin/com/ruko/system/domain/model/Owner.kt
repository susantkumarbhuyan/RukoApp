package com.ruko.system.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Owner(
    val id: String = "",
    val name: String = "",
    val businessName: String = "",
    val businessType: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val email: String = "",
    val isVerified: Boolean = false,
    val registrationDate: String? = null,
    @Transient val password: String = ""
)
