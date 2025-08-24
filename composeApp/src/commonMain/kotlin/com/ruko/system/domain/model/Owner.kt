package com.ruko.system.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Owner(
    val id: String = "",
    val name: String = "",
    val businessName: String = "",
    val businessType: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val isVerified: Boolean = false,
    val registrationDate: String? = null,
    val email: String = "",
    val password: String = ""
    // Account properties are inherited: email, password
)