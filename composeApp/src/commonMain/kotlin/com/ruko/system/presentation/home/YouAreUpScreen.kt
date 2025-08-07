package com.ruko.system.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
fun YouAreUpScreen(onDone: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("It’s Your Turn!") }) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(96.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text("🎉 It's your turn!", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Please proceed to the counter.")

            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onDone) {
                Text("OK")
            }
        }
    }
}
