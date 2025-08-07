package com.ruko.system.utils

import com.russhwolf.settings.Settings
object SessionManager {
    private val settings: Settings = Settings()

    private const val KEY_LOGGED_IN = "logged_in"
    private const val KEY_USER_TYPE = "user_type"

    // User keys
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_NUMBER = "user_number"

    // Owner keys
    private const val KEY_OWNER_NAME = "owner_name"
    private const val KEY_OWNER_NUMBER = "owner_number"
    private const val KEY_BUSINESS_TYPE = "owner_business_type"

    enum class UserType {
        USER, OWNER
    }

    var isLoggedIn: Boolean
        get() = settings.getBoolean(KEY_LOGGED_IN, false)
        set(value) {
            settings.putBoolean(KEY_LOGGED_IN, value)
        }

    var userType: UserType?
        get() = settings.getStringOrNull(KEY_USER_TYPE)?.let { UserType.valueOf(it) }
        set(value) {
            if (value == null) {
                settings.remove(KEY_USER_TYPE)
            } else {
                settings.putString(KEY_USER_TYPE, value.name)
            }
        }

    // ----------- User Info ------------
    var userName: String?
        get() = settings.getStringOrNull(KEY_USER_NAME)
        set(value) {
            if (value == null) settings.remove(KEY_USER_NAME)
            else settings.putString(KEY_USER_NAME, value)
        }

    var userPhoneNumber: String?
        get() = settings.getStringOrNull(KEY_USER_NUMBER)
        set(value) {
            if (value == null) settings.remove(KEY_USER_NUMBER)
            else settings.putString(KEY_USER_NUMBER, value)
        }

    // ----------- Owner Info ------------
    var ownerName: String?
        get() = settings.getStringOrNull(KEY_OWNER_NAME)
        set(value) {
            if (value == null) settings.remove(KEY_OWNER_NAME)
            else settings.putString(KEY_OWNER_NAME, value)
        }

    var ownerPhoneNumber: String?
        get() = settings.getStringOrNull(KEY_OWNER_NUMBER)
        set(value) {
            if (value == null) settings.remove(KEY_OWNER_NUMBER)
            else settings.putString(KEY_OWNER_NUMBER, value)
        }

    var businessType: String?
        get() = settings.getStringOrNull(KEY_BUSINESS_TYPE)
        set(value) {
            if (value == null) settings.remove(KEY_BUSINESS_TYPE)
            else settings.putString(KEY_BUSINESS_TYPE, value)
        }

    fun logout() {
        settings.remove(KEY_LOGGED_IN)
        settings.remove(KEY_USER_TYPE)

        settings.remove(KEY_USER_NAME)
        settings.remove(KEY_USER_NUMBER)

        settings.remove(KEY_OWNER_NAME)
        settings.remove(KEY_OWNER_NUMBER)
        settings.remove(KEY_BUSINESS_TYPE)
    }
}