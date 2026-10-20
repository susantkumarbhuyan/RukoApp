package com.ruko.system.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val isAnonymous: Boolean = false,
    val fcmToken: String? = null,
    @Transient val password: String = ""
)
