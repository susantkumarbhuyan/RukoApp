package com.ruko.system.notifications

import com.ruko.system.utils.generateUUID
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.functions.functions
import dev.gitlive.firebase.messaging.messaging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class NotificationTarget(val id: String, val queueId: String, val kind: String)

object PushNotifications {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()
    private var started = false
    private var cachedToken: String? = null
    private var registeredBinding: String? = null
    private var logoutUid: String? = null
    private var lastRegisteredAt: Long = 0
    private val _target = MutableStateFlow<NotificationTarget?>(null)
    val target = _target.asStateFlow()

    fun start() {
        if (started) return
        started = true
        scope.launch {
            Firebase.auth.authStateChanged.collectLatest { user ->
                if (user == null) {
                    mutex.withLock { logoutUid = null; registeredBinding = null }
                    return@collectLatest
                }
                while (isActive) {
                    try { registerCurrentToken() }
                    catch (e: CancellationException) { throw e }
                    catch (_: Exception) { /* Retry after APNs, connectivity, or auth is ready. */ }
                    delay(60_000)
                }
            }
        }
    }

    fun tokenChanged(token: String) {
        scope.launch {
            mutex.withLock { cachedToken = token; registeredBinding = null }
            try { registerCurrentToken() }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { /* Startup loop and Android worker retry registration. */ }
        }
    }

    suspend fun registerCurrentToken() {
        mutex.withLock {
            val uid = Firebase.auth.currentUser?.uid ?: return
            if (uid == logoutUid) return
            val token = cachedToken ?: Firebase.messaging.getToken()
            if (token.isBlank()) return
            val binding = uid + ":" + token
            val now = com.ruko.system.utils.TimeUtils.currentTimeInMilli()
            if (registeredBinding == binding && now - lastRegisteredAt < 86_400_000) return
            Firebase.functions("asia-south1").httpsCallable("rukoRegisterDevice")(mapOf("token" to token))
            registeredBinding = binding
            lastRegisteredAt = now
            cachedToken = token
        }
    }

    suspend fun unregisterForLogout() {
        mutex.withLock {
            logoutUid = Firebase.auth.currentUser?.uid
            val token = cachedToken ?: runCatching { Firebase.messaging.getToken() }.getOrNull()
            // Attempt token invalidation even when server detachment fails.
            try {
                if (token != null) Firebase.functions("asia-south1")
                    .httpsCallable("rukoUnregisterDevice")(mapOf("token" to token))
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { /* Token deletion invalidates stale server registrations. */ }
            finally {
                runCatching { Firebase.messaging.deleteToken() }
                cachedToken = null
                registeredBinding = null
                _target.value = null
            }
        }
    }

    fun openQueue(queueId: String, kind: String) {
        if (!Regex("[A-Za-z0-9_-]{1,100}").matches(queueId)) return
        _target.value = NotificationTarget(generateUUID(), queueId, kind)
    }

    fun consume(id: String) {
        if (_target.value?.id == id) _target.value = null
    }
}
