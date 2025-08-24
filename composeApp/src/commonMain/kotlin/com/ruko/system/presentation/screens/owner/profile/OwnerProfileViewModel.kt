package com.ruko.system.presentation.screens.owner.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruko.system.data.local.preference.PreferenceManager
import com.ruko.system.domain.model.Owner
import com.ruko.system.domain.model.QRQueue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

sealed class OwnerHomeUiState {
    object Loading : OwnerHomeUiState()
    data class Success(
        val profile: Owner,
        val qrCodeData: String? = null
    ) : OwnerHomeUiState()

    data class Error(val message: String) : OwnerHomeUiState()
}

class OwnerProfileViewModel(
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<OwnerHomeUiState>(OwnerHomeUiState.Loading)
    val uiState: StateFlow<OwnerHomeUiState> = _uiState.asStateFlow()

    fun loadOwnerProfile() {
        viewModelScope.launch {
            try {
                _uiState.value = OwnerHomeUiState.Loading

                val profile = preferenceManager.getOwnerProfile()

                if (profile != null) {
                    _uiState.value = OwnerHomeUiState.Success(profile = profile)
                } else {
                    _uiState.value = OwnerHomeUiState.Error(
                        message = "Profile not found. Please log in again."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = OwnerHomeUiState.Error(
                    message = "Failed to load profile: ${e.message}"
                )
            }
        }
    }

    fun generateQrCodeData(
        qrQueue: QRQueue
    ) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is OwnerHomeUiState.Success) {
                    // Generate QR code data with business information
                    val qrData = Json.encodeToString(qrQueue)

                    _uiState.value = currentState.copy(qrCodeData = qrData)
                }
            } catch (e: Exception) {
                _uiState.value = OwnerHomeUiState.Error(
                    message = "Failed to generate QR code: ${e.message}"
                )
            }
        }
    }

    fun refreshProfile() {
        loadOwnerProfile()
    }

    fun logout() {
        viewModelScope.launch {
            try {
                preferenceManager.logout()
            } catch (e: Exception) {
                // Handle logout error if needed
                _uiState.value = OwnerHomeUiState.Error(
                    message = "Failed to logout: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        val currentState = _uiState.value
        if (currentState is OwnerHomeUiState.Error) {
            // Try to reload profile when clearing error
            loadOwnerProfile()
        }
    }

    // Additional utility functions

    fun updateProfileInMemory(updatedProfile: Owner) {
        val currentState = _uiState.value
        if (currentState is OwnerHomeUiState.Success) {
            _uiState.value = currentState.copy(profile = updatedProfile)
        }
    }

    fun isProfileComplete(): Boolean {
        val currentState = _uiState.value
        return if (currentState is OwnerHomeUiState.Success) {
            with(currentState.profile) {
                name.isNotBlank() &&
                        businessName.isNotBlank() &&
                        businessType.isNotBlank() &&
                        phoneNumber.isNotBlank() &&
                        email.isNotBlank() &&
                        address.isNotBlank()
            }
        } else false
    }

    fun getBusinessSummary(): String? {
        val currentState = _uiState.value
        return if (currentState is OwnerHomeUiState.Success) {
            "${currentState.profile.businessName} - ${currentState.profile.businessType}"
        } else null
    }
}