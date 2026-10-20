package com.ruko.system.presentation.screens.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruko.system.data.local.preference.PreferenceManager
import com.ruko.system.domain.model.Owner
import com.ruko.system.domain.repository.QueueRepository
import com.ruko.system.presentation.screens.owner.queue.QueueListUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class OwnerHomeViewModel(
    private val preferenceManager: PreferenceManager,
    private val repository: QueueRepository
) : ViewModel() {
    private val _queueListState = MutableStateFlow<QueueListUiState>(QueueListUiState.Idle)
    val queueListState: StateFlow<QueueListUiState> = _queueListState.asStateFlow()

    private val _ownerProfile = MutableStateFlow<Owner?>(null)
    val ownerProfile: StateFlow<Owner?> = _ownerProfile.asStateFlow()

    init {
        observeOwnerAndQueues()
    }

    private fun observeOwnerAndQueues() {
        viewModelScope.launch {
            val owner = preferenceManager.getOwnerProfile()
            if (owner == null || owner.id.isBlank()) {
                _ownerProfile.value = null
                _queueListState.value = QueueListUiState.NoOwner
            } else {
                _ownerProfile.value = owner
                _queueListState.value = QueueListUiState.Loading
                fetchQueues(owner.id)
            }
        }
    }

    private suspend fun fetchQueues(ownerId: String) {
        try {
            val queues = repository.getQueues(ownerId)
            _queueListState.value = if (queues.isEmpty()) {
                QueueListUiState.Empty
            } else {
                QueueListUiState.Success(queues)
            }
        } catch (e: Exception) {
            _queueListState.value = QueueListUiState.Error(e.message ?: "Something went wrong")
        }
    }

    fun refreshQueues() {
        viewModelScope.launch {
            val owner = preferenceManager.getOwnerProfile()
            _ownerProfile.value = owner
            val ownerId = owner?.id
            if (ownerId.isNullOrBlank()) {
                _queueListState.value = QueueListUiState.NoOwner
            } else {
                _queueListState.value = QueueListUiState.Loading
                fetchQueues(ownerId)
            }
        }
    }
}
