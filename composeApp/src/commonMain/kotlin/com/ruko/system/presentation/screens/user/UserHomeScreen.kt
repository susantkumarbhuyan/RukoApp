package com.ruko.system.presentation.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ruko.system.domain.model.User
import com.ruko.system.presentation.componnts.QueueMetric
import com.ruko.system.presentation.componnts.QueueNotice
import com.ruko.system.utils.QueueTiming
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UserHomeScreen(
    onScanClick: () -> Unit,
    onQueueClick: (queueId: String) -> Unit,
    viewModel: UserHomeViewModel = koinViewModel()
) {
    val user by viewModel.userProfile.collectAsState()
    val state by viewModel.queueState.collectAsState()
    LaunchedEffect(Unit) { viewModel.refresh() }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text("RUKO", style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        user?.let { UserWelcome(it) }
        Text("Your time, back in your hands.",
            style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        QueueSection(state, onQueueClick)
        if (state !is UserHomeState.JoinedQueue) ScanQrButton(onScanClick)
        if (state is UserHomeState.Error) {
            TextButton(onClick = viewModel::refresh) { Text("Try again") }
        }
        QueueNotice("How it works", "Scan the business QR code, join the line, and check in when you arrive.")
    }
}

@Composable
fun UserWelcome(user: User) {
    Text("Hi, ${user.name.ifBlank { "there" }}",
        style = MaterialTheme.typography.titleLarge)
}

@Composable
fun QueueSection(queueState: UserHomeState, onQueueClick: (String) -> Unit) {
    when (queueState) {
        UserHomeState.Loading -> CircularProgressIndicator()
        UserHomeState.NoQueue -> NoQueueMessage()
        is UserHomeState.Error -> Text(queueState.message, color = MaterialTheme.colorScheme.error)
        is UserHomeState.JoinedQueue -> QueueCard(queueState) { onQueueClick(queueState.queueId) }
    }
}

@Composable
fun QueueCard(queue: UserHomeState.JoinedQueue, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick, shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("YOU'RE IN LINE", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary)
            Text(queue.queueName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            QueueDetailsRow(queue)
            Text("View live queue →", color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun QueueDetailsRow(queue: UserHomeState.JoinedQueue) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        QueueMetric("Your position", queue.currentPosition.toString(), Modifier.weight(1f))
        QueueMetric("Estimated wait", "~${QueueTiming.minutesCeiling(queue.estimatedTime)} min",
            Modifier.weight(1f))
    }
}

@Composable
fun NoQueueMessage() {
    Column(Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(Icons.Default.QrCodeScanner, null, Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary)
        Text("Skip the standing around.", style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold)
        Text("Scan a QR code at your salon or clinic to join its queue.",
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ScanQrButton(onScanClick: () -> Unit) {
    Button(onClick = onScanClick, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
        Icon(Icons.Default.QrCodeScanner, null)
        Spacer(Modifier.width(10.dp))
        Text("Scan to join")
    }
}
