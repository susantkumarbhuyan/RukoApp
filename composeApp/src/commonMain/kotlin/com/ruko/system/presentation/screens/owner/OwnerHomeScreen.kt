package com.ruko.system.presentation.screens.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ruko.system.domain.model.Queue
import com.ruko.system.presentation.componnts.QueueNotice
import com.ruko.system.presentation.navigation.Screen
import com.ruko.system.presentation.screens.owner.queue.QueueListUiState
import com.ruko.system.utils.TimeUtils
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerHomeScreen(
    viewModel: OwnerHomeViewModel = koinViewModel(),
    navigate: (Screen) -> Unit
) {
    val state by viewModel.queueListState.collectAsState()
    val owner by viewModel.ownerProfile.collectAsState()
    LaunchedEffect(Unit) { viewModel.refreshQueues() }
    Scaffold(topBar = {
        TopAppBar(title = { Text("RUKO", fontWeight = FontWeight.Bold) },
            actions = {
                IconButton(onClick = { navigate(Screen.Owner.Profile) }) {
                    Icon(Icons.Default.AccountCircle, "Business profile")
                }
            })
    }, bottomBar = {
        Surface {
            Button(
                onClick = { owner?.id?.takeIf { it.isNotBlank() }?.let {
                    navigate(Screen.Owner.CreateQueue(storeId = it))
                } },
                enabled = !owner?.id.isNullOrBlank(),
                modifier = Modifier.fillMaxWidth().padding(20.dp).heightIn(min = 56.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Create a queue")
            }
        }
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)) {
            item {
                Text(owner?.businessName?.ifBlank { "Your business" } ?: "Your business",
                    style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("A smoother day starts here", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item { QueueNotice("Your queues",
                "Open a queue to share its QR code, check arrivals, and serve the next customer.") }
            when (val data = state) {
                QueueListUiState.Loading, QueueListUiState.Idle -> item { CircularProgressIndicator() }
                QueueListUiState.Empty -> item {
                    Text("Ready for your first customer?",
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Create a queue, display its QR code, and let customers join from their phones.")
                }
                QueueListUiState.NoOwner -> item { Text("Sign in with your business account to continue.") }
                is QueueListUiState.Error -> item {
                    Text(data.message, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = viewModel::refreshQueues) { Text("Try again") }
                }
                is QueueListUiState.Success -> {
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${data.queues.size} queues", style = MaterialTheme.typography.titleMedium)
                            TextButton(onClick = viewModel::refreshQueues) { Text("Refresh") }
                        }
                    }
                    items(data.queues, key = { it.id }) { queue ->
                        QueueCard(queue) { navigate(Screen.Owner.QueueDetails(queue.id)) }
                    }
                }
            }
        }
    }
}

@Composable
fun QueueCard(queue: Queue, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick, modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (queue.isActive && queue.status == "ACTIVE") "OPEN FOR JOINING" else "CLOSED",
                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(queue.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(queue.type.ifBlank { "Customer queue" }, color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider()
            Text("${TimeUtils.formatMillisToTimeString(queue.avgServiceTime)} per customer",
                style = MaterialTheme.typography.bodyMedium)
            Text("Manage queue →", color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold)
        }
    }
}
