package com.ruko.system.presentation.screens.user.current_queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruko.system.data.local.preference.PreferenceManager
import com.ruko.system.domain.repository.QueueRepository
import com.ruko.system.utils.QueueTiming
import com.ruko.system.utils.TimeUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CurrentQueueViewModel(
    private val repository: QueueRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {
    private val _state = MutableStateFlow<CurrentQueueState>(CurrentQueueState.Loading)
    val state = _state.asStateFlow()
    private val _busy = MutableStateFlow(false)
    val busy = _busy.asStateFlow()
    private val _actionError = MutableStateFlow<String?>(null)
    val actionError = _actionError.asStateFlow()
    private var observer: Job? = null

    fun loadQueue(queueId: String) {
        observer?.cancel()
        observer = viewModelScope.launch {
            _state.value = CurrentQueueState.Loading
            try {
                val userId = repository.currentUserId.takeIf { it.isNotBlank() }
                    ?: error("Please sign in again")
                val queue = repository.getQueue(queueId)
                val average = repository.getAverageServiceTime(queueId)
                val clock = flow {
                    while (true) {
                        emit(TimeUtils.currentTimeInMilli())
                        delay(15_000)
                    }
                }
                combine(repository.observeUserQueue(queueId), clock) { entries, now ->
                    val position = QueueTiming.position(entries, userId)
                    val own = entries.firstOrNull { it.userId == userId }
                    if (position == 0) CurrentQueueState.Finished
                    else CurrentQueueState.Success(
                        queue, queueId, position,
                        QueueTiming.waitMillis(entries, userId, average, now),
                        entries.size, own?.arrivedAt != null, own?.startedAt != null
                    )
                }.collect { result ->
                    if (result is CurrentQueueState.Finished) preferenceManager.deleteJoinedQueue()
                    _state.value = result
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = CurrentQueueState.Error(e.message ?: "Could not load your queue")
            }
        }
    }

    fun arrive(queueId: String) = action {
        repository.markArrived(queueId, repository.currentUserId)
    }

    fun leaveQueue(queueId: String, onLeft: () -> Unit) = action {
        repository.leaveUserQueue(queueId, repository.currentUserId)
        preferenceManager.deleteJoinedQueue()
        onLeft()
    }

    private fun action(block: suspend () -> Unit) {
        if (_busy.value) return
        _busy.value = true
        _actionError.value = null
        viewModelScope.launch {
            try { block() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { _actionError.value = e.message ?: "Please try again" }
            finally { _busy.value = false }
        }
    }
}
