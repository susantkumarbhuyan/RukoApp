package com.ruko.system.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Account(
    val email: String = "",
    val password: String = ""
)