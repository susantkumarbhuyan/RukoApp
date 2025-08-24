package com.ruko.system.presentation

import android.app.Application
import com.google.firebase.FirebaseApp
import com.ruko.system.di.initKoin
import com.ruko.system.utils.MAppContext
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level


class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidContextProvider.init(this)
        MAppContext.set(this)
        initKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@MyApp)
        }
        FirebaseApp.initializeApp(this)
    }
}