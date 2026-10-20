package com.ruko.system.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.work.*
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ruko.system.MainActivity
import com.ruko.system.R

class RukoMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        PushNotifications.tokenChanged(token)
        enqueueTokenSync(applicationContext)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val queueId = message.data["queueId"] ?: return
        if (!Regex("[A-Za-z0-9_-]{1,100}").matches(queueId)) return
        val eventId = message.data["eventId"] ?: message.messageId ?: return
        val prefs = getSharedPreferences("ruko_push", Context.MODE_PRIVATE)
        val seen = prefs.getStringSet("seen", emptySet()).orEmpty()
        if (eventId in seen) return
        val updated = (seen.toList().takeLast(99) + eventId).toSet()
        prefs.edit().putStringSet("seen", updated).apply()
        val manager = getSystemService(NotificationManager::class.java)
        ensureChannel(this)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("queueId", queueId)
            putExtra("kind", message.data["kind"] ?: "")
        }
        val ticket = message.data["ticketId"] ?: queueId
        val pending = PendingIntent.getActivity(this, ticket.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = Notification.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_queue_notification)
            .setContentTitle(message.notification?.title ?: "RUKO queue update")
            .setContentText(message.notification?.body ?: "Open RUKO for your latest queue status.")
            .setVisibility(Notification.VISIBILITY_PRIVATE)
            .setAutoCancel(true).setContentIntent(pending).build()
        manager.notify(ticket, 1, notification)
    }

    companion object {
        const val CHANNEL = "ruko_queue"
        fun ensureChannel(context: Context) {
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL, "Queue updates", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Queue progress, arrival, and service updates"
                    lockscreenVisibility = Notification.VISIBILITY_PRIVATE
                }
            )
        }
        fun enqueueTokenSync(context: Context) {
            val work = OneTimeWorkRequestBuilder<PushTokenWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork("ruko_push_token",
                ExistingWorkPolicy.REPLACE, work)
        }
    }
}

class PushTokenWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = try {
        PushNotifications.registerCurrentToken()
        Result.success()
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (_: Exception) {
        if (runAttemptCount < 8) Result.retry() else Result.failure()
    }
}
