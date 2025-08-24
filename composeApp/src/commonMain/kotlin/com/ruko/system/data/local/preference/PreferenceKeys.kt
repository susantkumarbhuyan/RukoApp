package com.ruko.system.data.local.preference

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey


object PreferenceKeys {
    val LOGGED_IN = booleanPreferencesKey("logged_in")
    val USER_TYPE = stringPreferencesKey("user_type")

    // ----------- User Info ------------
    val USER_NAME = stringPreferencesKey("user_name")
    val USER_NUMBER = stringPreferencesKey("user_number")

    // ----------- Owner Info ------------
    val OWNER_NAME = stringPreferencesKey("owner_name")
    val OWNER_NUMBER = stringPreferencesKey("owner_number")
    val BUSINESS_TYPE = stringPreferencesKey("owner_business_type")
}