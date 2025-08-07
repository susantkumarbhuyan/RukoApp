package com.ruko.system

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.ruko.system.presentation.navigation.QueueNavHost
import com.ruko.system.presentation.ui.theme.QueueTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    QueueTheme {
        QueueNavHost()
    }
}