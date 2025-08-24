package com.ruko.system.utils
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionManager() {

    enum class UserType {
        USER, OWNER
    }
}
