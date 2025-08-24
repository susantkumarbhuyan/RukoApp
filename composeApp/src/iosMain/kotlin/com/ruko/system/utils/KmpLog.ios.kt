package com.ruko.system.utils

// iosMain (or other)
actual object KmpLog {
    actual fun d(tag: String, message: String) {
        println("[$tag] $message")
    }
}