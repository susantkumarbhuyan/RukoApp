package com.ruko.system

import android.os.Bundle
import android.content.Intent
import com.ruko.system.notifications.PushNotifications
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import com.ruko.system.presentation.AndroidContextProvider
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize

class MainActivity : ComponentActivity() {
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotification(intent)
    }

    private fun handleNotification(intent: Intent?) {
        val queueId = intent?.getStringExtra("queueId") ?: return
        PushNotifications.openQueue(queueId, intent.getStringExtra("kind") ?: "")
        intent.removeExtra("queueId")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.initialize(
            applicationContext
        )
        AndroidContextProvider.initActivity(this)
        handleNotification(intent)
        setContent {
            val transparent = android.graphics.Color.TRANSPARENT
            val isDarkTheme = isSystemInDarkTheme()

            enableEdgeToEdge(
                statusBarStyle = if (isDarkTheme) {
                    // Dark theme → light icons
                    SystemBarStyle.dark(transparent)
                } else {
                    // Light theme → dark icons
                    SystemBarStyle.light(transparent, transparent)
                },
                navigationBarStyle = if (isDarkTheme) {
                    // Dark theme → light icons
                    SystemBarStyle.dark(transparent)
                } else {
                    // Light theme → dark icons
                    SystemBarStyle.light(transparent, transparent)
                }
            )

            App()
        }
    }
}