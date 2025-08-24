package com.ruko.system.data.local.preference.di


import com.ruko.system.data.local.preference.DataStorePreferenceManager
import com.ruko.system.data.local.preference.PreferenceManager
import org.koin.core.module.Module
import org.koin.dsl.module


val corePreferenceModule = module {
    single<PreferenceManager> { DataStorePreferenceManager(get()) }
    includes(preferenceModule)
}
expect val preferenceModule: Module

