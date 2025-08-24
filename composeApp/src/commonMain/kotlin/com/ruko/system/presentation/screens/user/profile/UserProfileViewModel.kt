package com.ruko.system.presentation.screens.user.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruko.system.data.local.preference.PreferenceManager
import com.ruko.system.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UserProfileState {
    object Loading : UserProfileState()
    data class Success(val user: User) : UserProfileState()
    data class Error(val message: String) : UserProfileState()
}

class UserProfileViewModel(
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _state = MutableStateFlow<UserProfileState>(UserProfileState.Loading)
    val state: StateFlow<UserProfileState> = _state.asStateFlow()

    init {
        fetchUser()
    }

    private fun fetchUser() {
        viewModelScope.launch {
            try {
                val user = preferenceManager.getUserProfile() // suspend function
                if (user == null || user.id.isBlank()) {
                    _state.value = UserProfileState.Error("User not found")
                } else {
                    _state.value = UserProfileState.Success(user)
                }
            } catch (e: Exception) {
                _state.value = UserProfileState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun refresh() {
        _state.value = UserProfileState.Loading
        fetchUser()
    }
}