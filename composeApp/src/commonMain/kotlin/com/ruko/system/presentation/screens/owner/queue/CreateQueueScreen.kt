package com.ruko.system.presentation.screens.owner.queue

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ruko.system.domain.model.Queue
import com.ruko.system.domain.model.QueueStatus
import com.ruko.system.presentation.screens.owner.queue.components.QueueForm
import com.ruko.system.presentation.screens.owner.queue.components.QueueUiState
import com.ruko.system.utils.generateUUID
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQueueScreen(
    storeId: String,
    navigateBack: (String?) -> Unit,
    viewModel: OwnerQueueViewModel = koinViewModel()
) {
    val queueState by viewModel.queueState.collectAsState()

    val newQueueId = remember { generateUUID() }
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(QueueStatus.ACTIVE) }
    var size by remember { mutableStateOf("5") }
    var avgServiceTime by remember { mutableStateOf("10") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Queue") },
                navigationIcon = {
                    IconButton(onClick = { navigateBack(null) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            QueueForm(
                name, { name = it },
                type, { type = it },
                status, { status = it },
                size, { size = it },
                avgServiceTime, { avgServiceTime = it }
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val newQueue = Queue(
                        id = newQueueId,
                        name = name,
                        type = type,
                        status = status.name,
                        isActive = status == QueueStatus.ACTIVE,
                        size = size.toIntOrNull() ?: 5,
                        maxQueueSize = size.toIntOrNull() ?: 5,
                        avgServiceTime = (avgServiceTime.toLongOrNull() ?: 10) * 60_000,
                        businessId = storeId,
                        storeId = storeId
                    )
                    viewModel.createQueue(newQueue)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = queueState !is QueueUiState.Loading && name.isNotBlank() &&
                    storeId.isNotBlank() && (avgServiceTime.toLongOrNull() ?: 0) in 1L..1440L &&
                    (size.toIntOrNull() ?: 0) in 1..200
            ) {
                if (queueState is QueueUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                } else {
                    Icon(Icons.Default.Add, contentDescription = "Create Queue")
                    Spacer(Modifier.width(8.dp))
                }
                Text("Create Queue")
            }

            // Handle state changes
            when (queueState) {
                is QueueUiState.Success -> {
                    val createdQueue = (queueState as QueueUiState.Success).queue
                    LaunchedEffect(createdQueue.id) {
                        navigateBack(createdQueue.id)
                        viewModel.resetState()
                    }
                }

                is QueueUiState.Error -> {
                    val message = (queueState as QueueUiState.Error).message
                    Text(message, color = MaterialTheme.colorScheme.error)
                }

                else -> {}
            }
        }
    }
}