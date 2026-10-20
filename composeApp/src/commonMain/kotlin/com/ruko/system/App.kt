package com.ruko.system

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.ruko.system.notifications.PushNotifications
import com.ruko.system.presentation.navigation.RootNavigation
import com.ruko.system.presentation.ui.MainTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    LaunchedEffect(Unit) { PushNotifications.start() }
    MainTheme {
        RootNavigation()
    }
}