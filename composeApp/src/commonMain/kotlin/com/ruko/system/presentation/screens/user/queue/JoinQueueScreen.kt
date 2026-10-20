package com.ruko.system.presentation.screens.user.queue

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ruko.system.domain.model.QRQueue
import com.ruko.system.presentation.componnts.QueueMetric
import com.ruko.system.presentation.componnts.QueueNotice
import com.ruko.system.utils.QueueTiming
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinQueueScreen(
    qrQueue: QRQueue,
    onJoinComplete: () -> Unit,
    viewModel: JoinQViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val joining by viewModel.joining.collectAsState()
    LaunchedEffect(qrQueue.queueId) { viewModel.loadQueue(qrQueue) }
    Scaffold(topBar = { TopAppBar(title = { Text("Join the queue") }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)
            .verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)) {
            when (val data = state) {
                JoinQueueState.Loading -> CircularProgressIndicator()
                is JoinQueueState.Error -> {
                    Text(data.message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.loadQueue(qrQueue) }) { Text("Try again") }
                }
                is JoinQueueState.Success -> {
                    Icon(Icons.Default.Groups, null, Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary)
                    Text(data.queue.name, style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold)
                    if (data.queue.description.isNotBlank()) Text(data.queue.description)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QueueMetric("Your estimated position", data.currentPosition.toString(), Modifier.weight(1f))
                        QueueMetric("Estimated wait",
                            "~${QueueTiming.minutesCeiling(data.estimatedTime)} min", Modifier.weight(1f))
                    }
                    QueueNotice("A little less waiting",
                        "Join now and follow your place in line. Check in when you arrive.")
                    Button(onClick = { viewModel.joinQueue(qrQueue, onJoinComplete) },
                        enabled = !joining && data.queue.isActive && data.queue.status == "ACTIVE",
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                        Text(if (joining) "Joining…" else "Join queue")
                    }
                    Text("Position and waiting time may change before you join.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
