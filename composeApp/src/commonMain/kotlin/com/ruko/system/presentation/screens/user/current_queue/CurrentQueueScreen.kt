package com.ruko.system.presentation.screens.user.current_queue

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ruko.system.presentation.componnts.QueueMetric
import com.ruko.system.presentation.componnts.QueueNotice
import com.ruko.system.utils.QueueTiming
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentQueueScreen(
    queueId: String,
    onLeftQueue: () -> Unit,
    viewModel: CurrentQueueViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val busy by viewModel.busy.collectAsState()
    val actionError by viewModel.actionError.collectAsState()
    var confirmLeave by remember { mutableStateOf(false) }
    LaunchedEffect(queueId) { viewModel.loadQueue(queueId) }

    if (confirmLeave) {
        AlertDialog(onDismissRequest = { confirmLeave = false },
            title = { Text("Leave this queue?") },
            text = { Text("Your place will be released. Joining again puts you at the end.") },
            confirmButton = {
                TextButton(enabled = !busy, onClick = {
                    confirmLeave = false
                    viewModel.leaveQueue(queueId, onLeftQueue)
                }) { Text("Leave queue") }
            },
            dismissButton = { TextButton(onClick = { confirmLeave = false }) { Text("Stay") } })
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Your place in line") }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)
            .verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            when (val data = state) {
                CurrentQueueState.Loading -> CircularProgressIndicator()
                is CurrentQueueState.Error -> {
                    Text(data.message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.loadQueue(queueId) }) { Text("Try again") }
                }
                CurrentQueueState.Finished -> {
                    QueueNotice("You're no longer in line",
                        "Your visit may have been completed or your place removed.")
                    Button(onClick = onLeftQueue) { Text("Back to home") }
                }
                is CurrentQueueState.Success -> {
                    Text(data.queue.name, style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.primaryContainer) {
                        Column(Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(if (data.serving) "SERVICE IN PROGRESS" else "YOUR POSITION",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(data.currentPosition.toString(),
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(if (data.currentPosition == 1) "You're at the front"
                                else "${data.currentPosition - 1} people ahead",
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    QueueDetails(data)
                    QueueNotice(
                        if (data.arrived) "You're checked in" else "Let the business know you've arrived",
                        if (data.arrived) "Keep this screen handy. The business will start your service."
                        else "Tap below once you are at the salon or clinic."
                    )
                    actionError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    if (!data.arrived) {
                        Button(onClick = { viewModel.arrive(queueId) }, enabled = !busy,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                            Text(if (busy) "Updating…" else "I've arrived")
                        }
                    }
                    Text("Waiting times are estimates and can change as service progresses.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center)
                    OutlinedButton(onClick = { confirmLeave = true }, enabled = !busy && !data.serving,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                        Text("Leave queue")
                    }
                }
            }
        }
    }
}

@Composable
fun QueueDetails(data: CurrentQueueState.Success) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        QueueMetric("Estimated wait",
            if (data.serving) "In service" else "~${QueueTiming.minutesCeiling(data.estimatedTime)} min",
            Modifier.weight(1f))
        QueueMetric("People in line", data.queueLength.toString(), Modifier.weight(1f))
    }
}
