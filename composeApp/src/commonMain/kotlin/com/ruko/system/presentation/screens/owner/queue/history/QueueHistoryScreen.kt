package com.ruko.system.presentation.screens.owner.queue.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ruko.system.domain.model.QueueHistory
import com.ruko.system.presentation.componnts.AppCard
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun QueueHistoryScreen(viewModel: QueueHistoryViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val s = state) {
        is QueueHistoryState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is QueueHistoryState.Error -> {
            Text("Error: ${s.message}", modifier = Modifier.padding(16.dp))
        }

        is QueueHistoryState.Success -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(s.histories) { history ->
                    QueueHistoryItem(history)
                }
            }
        }
    }
}

@OptIn(FormatStringsInDatetimeFormats::class)
@Composable
fun QueueHistoryItem(history: QueueHistory) {
    // ✅ Use numeric month because MMM is not supported in KMP
    val formatter = LocalDateTime.Format {
        byUnicodePattern("dd MM yyyy, HH:mm")
    }

    val formattedDate = formatter.format(history.date)

    AppCard { // ← using reusable card style
        Text(
            text = history.queueName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text("Total Members: ${history.totalMembers}")
        Text("Total Time: ${history.totalTimeMinutes} min")
        Text(
            text = "Date: $formattedDate",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}