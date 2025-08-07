package com.ruko.system

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform