package com.ruko.system.utils

// androidMain
actual object KmpLog {
    actual fun d(tag: String, message: String) {
        android.util.Log.d(tag, message)
    }
}