package com.ruko.system.utils

import kotlin.random.Random


fun generateUUID(): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    return (1..32)
        .map { chars[Random.nextInt(chars.length)] }
        .joinToString("")
}