package com.ruko.system.presentation.screens.owner.queue

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ruko.system.WebViewShareScreen
import com.ruko.system.domain.model.PosterData
import com.ruko.system.domain.model.QRQueue
import com.ruko.system.domain.model.Queue
import com.ruko.system.imageBitmapToBase64
import com.ruko.system.presentation.screens.owner.queue.components.QueueUiState
import com.ruko.system.utils.TimeUtils
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel
import qrgenerator.QRCodeImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueDetailsScreen(
    queueId: String,
    navigateBack: () -> Unit,
    onEditQueue: (String) -> Unit,
    onUserListClick: (String) -> Unit,
    viewModel: OwnerQueueViewModel = koinViewModel()
) {
    val state by viewModel.queueState.collectAsState()

    LaunchedEffect(queueId) {
        viewModel.loadQueue(queueId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Queue Details") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        when (state) {
            QueueUiState.Idle,
            QueueUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is QueueUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (state as QueueUiState.Error).message,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            is QueueUiState.Success -> {
                val queue = (state as QueueUiState.Success).queue

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // Queue Info Card
                    item {
                        QueueInfoCard(queue = queue)
                    }

                    // QR Code Section
                    item {
                        GenerateQueueQR(queue)
                    }

                    // Action Buttons
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ActionButton(
                                text = "View Users",
                                icon = Icons.Default.People,
                                onClick = { onUserListClick(queue.id) },
                                isPrimary = true
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp) // spacing between buttons
                            ) {
                                ActionButton(
                                    text = "Edit Queue",
                                    icon = Icons.Default.Edit,
                                    onClick = { onEditQueue(queue.id) },
                                    isPrimary = false,
                                    modifier = Modifier.weight(1f) // 50% width
                                )

                                ActionButton(
                                    text = "Delete Queue",
                                    icon = Icons.Default.Delete,
                                    onClick = { viewModel.deleteQueue(queue.id) { navigateBack() } },
                                    isPrimary = false,
                                    isDestructive = true,
                                    modifier = Modifier.weight(1f) // 50% width
                                )
                            }

                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QueueInfoCard(queue: Queue) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = queue.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            InfoRow(
                icon = Icons.Rounded.Category,
                label = "Type",
                value = queue.type
            )
            InfoRow(
                icon = Icons.Rounded.CheckCircle,
                label = "Status",
                value = queue.status
            )
            InfoRow(
                icon = Icons.Rounded.People,
                label = "Size",
                value = "${queue.size}"
            )
            InfoRow(
                icon = Icons.Rounded.Schedule,
                label = "Estimated Wait Time",
                value = "${queue.time} sec"
            )
            InfoRow(
                icon = Icons.Rounded.Timer,
                label = "Avg Service Time",
                value = TimeUtils.formatMillisToTimeString(queue.avgServiceTime)
            )
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// Reusable Action Button
@Composable
fun ActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isPrimary: Boolean,
    isDestructive: Boolean = false,
    modifier: Modifier = Modifier // no fillMaxWidth here
) {
    val buttonColors = when {
        isDestructive -> ButtonDefaults.buttonColors(
            containerColor = Color(0xFFEF5350), // Red
            contentColor = Color.White
        )

        isPrimary -> ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4CAF50), // Green
            contentColor = Color.White
        )

        else -> ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = Color(0xFF2196F3) // Blue
        )
    }

    if (isDestructive || isPrimary) {
        Button(
            onClick = onClick,
            modifier = modifier, // use caller's modifier
            shape = RoundedCornerShape(12.dp),
            colors = buttonColors,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text)
            }
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier, // use caller's modifier
            shape = RoundedCornerShape(12.dp),
            colors = buttonColors,
            border = BorderStroke(1.dp, Color(0xFF2196F3))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text)
            }
        }
    }
}


@Composable
private fun GenerateQueueQR(queue: Queue) {
    // QR Code Section
    var qrImageBitmap: ImageBitmap? by remember { mutableStateOf(null) }
    var showQrCode by remember { mutableStateOf(true) }
    var showShareScreen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    if (showShareScreen) {
        qrImageBitmap?.let {
            val posterData = PosterData(
                brandTitle = queue.name,
                shortDescription = "Scan to get your spot in line.",
                qrImageBase64 = imageBitmapToBase64(it),
                footerNote = "Join the queue instantly!"
            )
            WebViewShareScreen(posterData) {
                // Callback when sharing is done
                showShareScreen = false
            }
        }
    }
    AnimatedVisibility(
        visible = showQrCode,
        enter = fadeIn(tween(300)),
        exit = fadeOut(tween(300))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF0F4C3)) // Light yellow/lime color
                .padding(24.dp)
        ) {
            Text(
                text = "Scan to Join Queue",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF689F38) // Darker green
            )

            queue.let { que ->
                val qrData = QRQueue(
                    businessId = que.storeId,
                    queueId = que.id,
                    queueName = que.name,
                    ownerName = "Empty"
                )

                QRCodeImage(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            BorderStroke(4.dp, Color(0xFF8BC34A)), // Green border
                            RoundedCornerShape(16.dp)
                        ),
                    url = "RUKO_${Json.encodeToString(qrData)}",
                    contentDescription = "Business QR Code",
                    onSuccess = { qrImageBitmap = it }
                )

                OutlinedButton(
                    onClick = {
                        showShareScreen = true
                    },
                    enabled = qrImageBitmap != null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF388E3C) // Dark green
                    ),
                    border = BorderStroke(1.dp, Color(0xFF388E3C))
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share QR Code")
                }
            }
        }
    }
}
