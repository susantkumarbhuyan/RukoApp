package com.ruko.system.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueStatusScreen(
    position: Int,
    estimatedTime: Int,
    onCancel: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Queue Status") }) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("You're #$position in line", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Estimated wait: ~$estimatedTime minutes")

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = onCancel) {
                Text("Leave Queue")
            }
        }
    }
}
