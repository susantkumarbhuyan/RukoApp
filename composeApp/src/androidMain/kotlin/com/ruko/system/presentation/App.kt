package com.ruko.system.presentation

import android.app.Application
import android.content.Context


class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidContextProvider.init(this)
    }
}