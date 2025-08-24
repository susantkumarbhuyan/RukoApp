package com.ruko.system.data.local.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

actual fun createDataStore(context: Any?): DataStore<Preferences> = createDataStore(
    producePath = { (context as Context).filesDir.resolve(dataStoreFileName).absolutePath }
)
