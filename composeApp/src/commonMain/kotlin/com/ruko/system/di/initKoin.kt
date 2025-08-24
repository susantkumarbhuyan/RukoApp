package com.ruko.system.di


import com.ruko.system.data.local.preference.di.corePreferenceModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

private var koinInitialized = false

fun initKoin(config: KoinAppDeclaration? = null) {
    if (koinInitialized) return
    koinInitialized = true

    startKoin {
        config?.invoke(this)
        modules(
            corePreferenceModule,
            koinModules
        )
    }
}
