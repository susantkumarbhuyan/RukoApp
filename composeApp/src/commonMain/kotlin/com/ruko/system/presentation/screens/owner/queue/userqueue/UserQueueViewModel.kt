package com.ruko.system.presentation.screens.owner.queue.userqueue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruko.system.domain.repository.QueueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class UserQueueViewModel(
    private val repository: QueueRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UserQueueUiState>(UserQueueUiState.Loading)
    val uiState: StateFlow<UserQueueUiState> = _uiState.asStateFlow()

    private var observer: kotlinx.coroutines.Job? = null
    private val _busy = MutableStateFlow(false)
    val busy = _busy.asStateFlow()
    private val _actionError = MutableStateFlow<String?>(null)
    val actionError = _actionError.asStateFlow()

    fun observeUserQueues(queueId: String) {
        observer?.cancel()
        observer = viewModelScope.launch {
            repository.observeUserQueueWithUserInfo(queueId)
                .onStart { _uiState.value = UserQueueUiState.Loading }
                .catch { e ->
                    _uiState.value = UserQueueUiState.Error(e.message ?: "Unknown error")
                }
                .collect { queues ->
                    _uiState.value = if (queues.isEmpty()) {
                        UserQueueUiState.Empty
                    } else {
                        UserQueueUiState.Success(queues)
                    }
                }
        }
    }

    fun startService(queueId: String, entryId: String) = action {
        repository.startService(queueId, entryId)
    }

    fun markCompleted(queueId: String, userQueueId: String) = action {
        repository.markUserQueueCompleted(queueId, userQueueId)
    }

    private fun action(block: suspend () -> Unit) {
        if (_busy.value) return
        _busy.value = true
        _actionError.value = null
        viewModelScope.launch {
            try { block() }
            catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (e: Exception) { _actionError.value = e.message ?: "Could not update queue" }
            finally { _busy.value = false }
        }
    }
}
