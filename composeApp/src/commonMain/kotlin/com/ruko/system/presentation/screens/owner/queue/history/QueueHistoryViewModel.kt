package com.ruko.system.presentation.screens.owner.queue.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruko.system.domain.model.QueueHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

sealed class QueueHistoryState {
    object Loading : QueueHistoryState()
    data class Success(val histories: List<QueueHistory>) : QueueHistoryState()
    data class Error(val message: String) : QueueHistoryState()
}

class QueueHistoryViewModel : ViewModel() {

    private val _state = MutableStateFlow<QueueHistoryState>(QueueHistoryState.Loading)
    val state: StateFlow<QueueHistoryState> = _state

    init {
        loadDummyHistory()
    }

    @OptIn(ExperimentalTime::class)
    private fun loadDummyHistory() {
        viewModelScope.launch {
            val dummy = listOf(
                QueueHistory(
                    id = "1",
                    queueName = "Doctor Appointment",
                    totalMembers = 15,
                    totalTimeMinutes = 120,
                    date = Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                ),
                QueueHistory(
                    id = "2",
                    queueName = "Salon Queue",
                    totalMembers = 8,
                    totalTimeMinutes = 90,
                    date = (Clock.System.now() - 1.days)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                ),
                QueueHistory(
                    id = "3",
                    queueName = "Restaurant Waiting",
                    totalMembers = 20,
                    totalTimeMinutes = 200,
                    date = (Clock.System.now() - 3.days)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                )
            )
            _state.value = QueueHistoryState.Success(dummy)
        }
    }
}