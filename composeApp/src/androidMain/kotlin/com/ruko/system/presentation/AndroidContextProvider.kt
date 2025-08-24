package com.ruko.system.presentation

import android.content.Context

object AndroidContextProvider {
    lateinit var appContextAct: Context
        private set
    lateinit var appContext: Context
        private set

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun initActivity(context: Context) {
        appContextAct = context
    }
}