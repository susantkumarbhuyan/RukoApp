package com.ruko.system.utils

import android.content.Context
import java.lang.ref.WeakReference

actual object MAppContext {
    private var value: WeakReference<Context?>? = null
    fun set(context: Context) {
        value = WeakReference(context)
    }

    internal fun get(): Context {
        return value?.get() ?: throw RuntimeException("Context Error")
    }
}