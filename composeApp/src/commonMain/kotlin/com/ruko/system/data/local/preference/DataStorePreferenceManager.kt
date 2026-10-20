package com.ruko.system.data.local.preference

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ruko.system.domain.model.Owner
import com.ruko.system.domain.model.Queue
import com.ruko.system.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class DataStorePreferenceManager(
    private val dataStore: DataStore<Preferences>,
) : PreferenceManager {
    companion object {
        private val IS_ONBOARDING_DONE = booleanPreferencesKey("is_onboarding_done")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")

        // Owner Keys
        private val OWNER_ID = stringPreferencesKey("owner_id")
        private val OWNER_NAME = stringPreferencesKey("owner_name")
        private val OWNER_PHONE_NUMBER = stringPreferencesKey("owner_phone_number")
        private val OWNER_EMAIL = stringPreferencesKey("owner_email")
        private val BUSINESS_NAME = stringPreferencesKey("business_name")
        private val BUSINESS_TYPE = stringPreferencesKey("business_type")
        private val BUSINESS_ADDRESS = stringPreferencesKey("business_address")
        private val IS_BUSINESS_VERIFIED = booleanPreferencesKey("is_business_verified")
        private val REGISTRATION_DATE = stringPreferencesKey("registration_date")
        private val LAST_LOGIN_DATE = stringPreferencesKey("last_login_date")

        private val ONBOARDING_KEY = booleanPreferencesKey("seen_onboarding")

        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_PASSWORD = stringPreferencesKey("user_password")
        private val USER_PHONE_NUMBER = stringPreferencesKey("user_phone_number")
        private val USER_IS_ANONYMOUS = booleanPreferencesKey("user_is_anonymous")
        private val USER_TYPE = stringPreferencesKey("user_type")
        private val QUEUE_PREFIX = stringPreferencesKey("queue_") // dynamic keys
        private const val QUEUE_CACHE_PREFIX = "queue_"
        private const val QUEUE_LIST_PREFIX = "queues_"
        private val JOINED_QUEUE = stringPreferencesKey("joined_queue") // d
    }

    // ---------------- Session ----------------
    override val isOnBoardingDone: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[IS_ONBOARDING_DONE] ?: false }

    override suspend fun setOnboarding(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[IS_ONBOARDING_DONE] = value
        }
    }

    override val isLoggedIn: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[PreferenceKeys.LOGGED_IN] ?: false }

    override suspend fun setLoggedIn(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.LOGGED_IN] = value
        }
    }

    override val userType: Flow<SessionManager.UserType?> =
        dataStore.data.map { prefs ->
            prefs[PreferenceKeys.USER_TYPE]?.let { SessionManager.UserType.valueOf(it) }
        }

    override suspend fun setUserType(type: SessionManager.UserType?) {
        dataStore.edit { prefs ->
            if (type == null) prefs.remove(PreferenceKeys.USER_TYPE)
            else prefs[PreferenceKeys.USER_TYPE] = type.name
        }
    }


    // Complete owner profile operations
    override suspend fun saveOwnerProfile(owner: Owner) {
//        val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
//            .format(Date())

        dataStore.edit { preferences ->
            preferences[OWNER_ID] = owner.id
            preferences[OWNER_NAME] = owner.name
            preferences[BUSINESS_NAME] = owner.businessName
            preferences[BUSINESS_TYPE] = owner.businessType
            preferences[OWNER_PHONE_NUMBER] = owner.phoneNumber
            preferences[BUSINESS_ADDRESS] = owner.address
            preferences[OWNER_EMAIL] = owner.email
            preferences[REGISTRATION_DATE] = "uniynyuy"
            preferences[IS_LOGGED_IN] = true
            preferences[USER_TYPE] = SessionManager.UserType.OWNER.name
        }
    }

    override suspend fun getOwnerProfile(): Owner? {
        val preferences = dataStore.data.first()

        val ownerId = preferences[OWNER_ID] ?: return null
        val ownerName = preferences[OWNER_NAME] ?: return null
        val businessName = preferences[BUSINESS_NAME] ?: return null
        val businessType = preferences[BUSINESS_TYPE] ?: return null
        val phoneNumber = preferences[OWNER_PHONE_NUMBER] ?: return null
        val address = preferences[BUSINESS_ADDRESS] ?: return null
        val email = preferences[OWNER_EMAIL] ?: return null

        return Owner(
            id = ownerId,
            name = ownerName,
            businessName = businessName,
            businessType = businessType,
            phoneNumber = phoneNumber,
            address = address,
            email = email,
            isVerified = preferences[IS_BUSINESS_VERIFIED] ?: false
        )
    }

    // Complete owner profile operations
    override suspend fun saveUserProfile(user: User) {
        dataStore.edit { preferences ->
            preferences[USER_ID] = user.id
            preferences[USER_NAME] = user.name
            preferences[USER_EMAIL] = user.email
            preferences.remove(USER_PASSWORD)
            preferences[USER_PHONE_NUMBER] = user.phoneNumber
            preferences[IS_LOGGED_IN] = true
            preferences[USER_TYPE] = SessionManager.UserType.USER.name
        }
    }

    override suspend fun getUserProfile(): User? {
        val preferences = dataStore.data.first()

        val id = preferences[USER_ID] ?: return null
        val name = preferences[USER_NAME] ?: ""
        val email = preferences[USER_EMAIL] ?: ""
        val password = ""
        val phoneNumber = preferences[USER_PHONE_NUMBER] ?: ""

        return User(
            id = id,
            name = name,
            email = email,
            password = password,
            phoneNumber = phoneNumber
        )
    }

    // ---------------- Logout ----------------
    override suspend fun logout() {
        dataStore.edit { prefs ->
            val valueToKeep = prefs[IS_ONBOARDING_DONE]  // save the value
            prefs.clear()                     // remove everything
            if (valueToKeep != null) {
                prefs[IS_ONBOARDING_DONE] = valueToKeep  // restore the kept key
            }
        }
    }


    override suspend fun hasSeenOnboarding(): Boolean {
        return dataStore.data.map { prefs ->
            prefs[ONBOARDING_KEY] ?: false
        }.first()
    }

    override suspend fun setSeenOnboarding() {
        dataStore.edit { prefs ->
            prefs[ONBOARDING_KEY] = true
        }
    }

    // ---------------- Queue Caching ----------------
    override suspend fun saveQueue(queue: Queue) {
        dataStore.edit { prefs ->
            val key = stringPreferencesKey(QUEUE_LIST_PREFIX)
            val existingJson = prefs[key]
            val existingList: MutableList<Queue> = if (existingJson != null) {
                runCatching {
                    Json.decodeFromString(
                        ListSerializer(Queue.serializer()),
                        existingJson
                    )
                }
                    .getOrDefault(emptyList())
                    .toMutableList()
            } else {
                mutableListOf()
            }

            // Update or add
            val index = existingList.indexOfFirst { it.id == queue.id }
            if (index >= 0) {
                existingList[index] = queue
            } else {
                existingList.add(queue)
            }

            prefs[key] = Json.encodeToString(ListSerializer(Queue.serializer()), existingList)
        }
    }

    override suspend fun getQueue(queueId: String): Queue? {
        val prefs = dataStore.data.first()
        val key = stringPreferencesKey(QUEUE_LIST_PREFIX)
        val jsonString = prefs[key] ?: return null
        val list =
            runCatching { Json.decodeFromString(ListSerializer(Queue.serializer()), jsonString) }
                .getOrElse { emptyList() }
        return list.firstOrNull { it.id == queueId }
    }

    override suspend fun saveQueues(queues: List<Queue>) {
        dataStore.edit { prefs ->
            val key = stringPreferencesKey(QUEUE_LIST_PREFIX)
            prefs[key] = Json.encodeToString(ListSerializer(Queue.serializer()), queues)
        }
    }

    override suspend fun getQueues(): List<Queue> {
        val prefs = dataStore.data.first()
        val key = stringPreferencesKey(QUEUE_LIST_PREFIX)
        val jsonStr = prefs[key] ?: return emptyList()
        return runCatching { Json.decodeFromString(ListSerializer(Queue.serializer()), jsonStr) }
            .getOrElse { emptyList() }
    }

    override suspend fun deleteQueue(queueId: String) {
        dataStore.edit { prefs ->
            val key = stringPreferencesKey(QUEUE_LIST_PREFIX)
            val existingJson = prefs[key] ?: return@edit
            val existingList: MutableList<Queue> = runCatching {
                Json.decodeFromString(ListSerializer(Queue.serializer()), existingJson)
            }.getOrDefault(emptyList()).toMutableList()

            val newList = existingList.filterNot { it.id == queueId }

            prefs[key] = Json.encodeToString(ListSerializer(Queue.serializer()), newList)
        }
    }

    override suspend fun setJoinedQueue(queueId: String) {
        dataStore.edit { prefs ->
            prefs[JOINED_QUEUE] = queueId
        }
    }

    override suspend fun getJoinedQueue(): String? {
        return dataStore.data.first()[JOINED_QUEUE]
    }

    override suspend fun getPendingRequest(key: String): String? =
        dataStore.data.first()[stringPreferencesKey("pending_request_" + key)]

    override suspend fun putPendingRequest(key: String, id: String) {
        dataStore.edit { it[stringPreferencesKey("pending_request_" + key)] = id }
    }

    override suspend fun clearPendingRequest(key: String) {
        dataStore.edit { it.remove(stringPreferencesKey("pending_request_" + key)) }
    }

    override suspend fun deleteJoinedQueue() {
        dataStore.edit { prefs ->
            prefs.remove(JOINED_QUEUE)
        }
    }
}
