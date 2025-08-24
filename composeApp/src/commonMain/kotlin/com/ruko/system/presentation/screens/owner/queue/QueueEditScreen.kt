package com.ruko.system.presentation.screens.owner.queue

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ruko.system.domain.model.QueueStatus
import com.ruko.system.presentation.screens.owner.queue.components.QueueForm
import com.ruko.system.presentation.screens.owner.queue.components.QueueUiState
import com.ruko.system.utils.TimeUtils
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueEditScreen(
    queueId: String,
    navigateBack: (String) -> Unit,
    viewModel: OwnerQueueViewModel = koinViewModel()
) {
    val state by viewModel.queueState.collectAsState()

    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(QueueStatus.ACTIVE) }
    var avgServiceTimeMin by remember { mutableStateOf("0") }

    // Trigger load once
    LaunchedEffect(queueId) {
        viewModel.loadQueue(queueId)
    }

    // Update local form when success arrives
    LaunchedEffect(state) {
        if (state is QueueUiState.Success) {
            val item = (state as QueueUiState.Success).queue
            name = item.name
            type = item.type
            size = item.size.toString()
            status = QueueStatus.valueOf(item.status)
            avgServiceTimeMin = TimeUtils.millisToMinutes(item.avgServiceTime).toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Queue") },
                navigationIcon = {
                    IconButton(onClick = { navigateBack(queueId) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (state) {
            QueueUiState.Idle,
            QueueUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is QueueUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Failed to load queue")
                }
            }

            is QueueUiState.Success -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    QueueForm(
                        name, { name = it },
                        type, { type = it },
                        status, { status = it },
                        size, { size = it },
                        avgServiceTimeMin, { avgServiceTimeMin = it }
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val updated = (state as QueueUiState.Success).queue.copy(
                                name = name,
                                type = type,
                                size = size.toIntOrNull() ?: 0,
                                status = status.name,
                                avgServiceTime = TimeUtils.minutesToMillis(
                                    avgServiceTimeMin.toLongOrNull() ?: 0
                                )
                            )
                            viewModel.editQueue(updated)
                            navigateBack(queueId)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}