package com.ruko.system.data.local.preference.di

import com.ruko.system.data.local.preference.PrefsDataStore
import com.ruko.system.data.local.preference.createDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val preferenceModule: Module = module {
    single<PrefsDataStore> {
        createDataStore (androidContext())
    }
}