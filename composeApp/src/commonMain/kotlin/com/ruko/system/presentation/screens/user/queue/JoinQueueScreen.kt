package com.ruko.system.presentation.screens.user.queue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ruko.system.domain.model.QRQueue
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinQueueScreen(
    qrQueue: QRQueue,
    onJoinComplete: () -> Unit,
    viewModel: JoinQViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(qrQueue.queueId) {
        viewModel.loadQueue(qrQueue)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Join Queue") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when (state) {
                is JoinQueueState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is JoinQueueState.Error -> {
                    val message = (state as JoinQueueState.Error).message
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadQueue(qrQueue) }) {
                            Text("Retry")
                        }
                    }
                }

                is JoinQueueState.Success -> {
                    val queueState = state as JoinQueueState.Success
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = "Queue Icon",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                queueState.queue.name,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                "Business: ${qrQueue.ownerName}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Queue Size: ${queueState.queue.size}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Current Position: ${queueState.currentPosition}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Estimated Wait: ~${queueState.estimatedTime} min",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Button(
                                onClick = {
                                    viewModel.joinQueue(
                                        qrQueue,
                                        onJoined = { onJoinComplete() }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Login,
                                    contentDescription = "Join Queue"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Join Queue")
                            }
                        }
                    }
                }
            }
        }
    }
}