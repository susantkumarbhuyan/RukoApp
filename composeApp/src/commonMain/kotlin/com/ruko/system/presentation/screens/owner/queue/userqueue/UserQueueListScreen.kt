package com.ruko.system.presentation.screens.owner.queue.userqueue

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserQueueListScreen(
    queueId: String,
    navigateBack: () -> Unit,
    viewModel: UserQueueViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val busy by viewModel.busy.collectAsState()
    val error by viewModel.actionError.collectAsState()
    LaunchedEffect(queueId) { viewModel.observeUserQueues(queueId) }
    Scaffold(topBar = {
        TopAppBar(title = { Text("Live queue") }, navigationIcon = {
            IconButton(onClick = navigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
        })
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            error?.let { message -> item { Text(message, color = MaterialTheme.colorScheme.error) } }
            when (val data = state) {
                UserQueueUiState.Loading -> item { CircularProgressIndicator() }
                UserQueueUiState.Empty -> item {
                    Text("All caught up", style = MaterialTheme.typography.headlineSmall)
                    Text("New customers appear here as they join.")
                }
                is UserQueueUiState.Error -> item {
                    Text(data.message, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { viewModel.observeUserQueues(queueId) }) { Text("Try again") }
                }
                is UserQueueUiState.Success -> {
                    item { Text("${data.queues.size} people in line",
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                    itemsIndexed(data.queues, key = { _, pair -> pair.first.id }) { index, (entry, user) ->
                        OutlinedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
                            Column(Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(if (index == 0) "UP NEXT" else "POSITION ${index + 1}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary)
                                Text(user?.name?.ifBlank { "Customer" } ?: "Customer",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold)
                                Text(when {
                                    entry.startedAt != null -> "Service in progress"
                                    entry.arrivedAt != null -> "Checked in"
                                    else -> "Not checked in"
                                }, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (index == 0) {
                                    Button(onClick = {
                                        if (entry.startedAt == null) viewModel.startService(queueId, entry.id)
                                        else viewModel.markCompleted(queueId, entry.id)
                                    }, enabled = !busy && entry.arrivedAt != null,
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                                        Text(if (entry.startedAt == null) "Start service" else "Complete service")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
