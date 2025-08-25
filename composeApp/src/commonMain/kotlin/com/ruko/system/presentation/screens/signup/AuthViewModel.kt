package com.ruko.system.presentation.screens.signup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruko.system.data.local.preference.PreferenceManager
import com.ruko.system.domain.model.LoginCredentials
import com.ruko.system.domain.model.Owner
import com.ruko.system.domain.model.User
import com.ruko.system.domain.model.enums.AccountType
import com.ruko.system.domain.repository.QueueRepository
import com.ruko.system.utils.generateUUID
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: QueueRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {
    var loginState by mutableStateOf(LoginCredentials())
        private set

    // States
    var ownerState by mutableStateOf(Owner())
        private set
    var userState by mutableStateOf(User())
        private set
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // ---------------- Validation ----------------
    private fun isValidEmail(email: String) =
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex().matches(email)

    val isOwnerValid: Boolean
        get() = ownerState.name.isNotBlank() &&
                ownerState.businessName.isNotBlank() &&
                ownerState.businessType.isNotBlank() &&
                ownerState.phoneNumber.length >= 10 &&
                ownerState.address.isNotBlank() &&
                isValidEmail(ownerState.email) &&
                ownerState.password.length >= 6

    val isUserValid: Boolean
        get() = userState.name.isNotBlank() &&
                isValidEmail(userState.email) &&
                userState.password.length >= 6


    // -------- Owner Registration --------
    fun registerOwner(onSuccess: () -> Unit) {
        if (!isOwnerValid) {
            errorMessage = "Please fill in all fields correctly"
            return
        }

        isLoading = true
        errorMessage = null

        val ownerId = generateUUID()
        val owner = ownerState.copy(id = ownerId)
        performAction(
            action = { repository.createOwnerAccount(owner) },
            onSuccess = {
                viewModelScope.launch {
                    try {
                        preferenceManager.saveOwnerProfile(owner)
                        preferenceManager.setLoggedIn(true)
                        preferenceManager.setUserType(SessionManager.UserType.OWNER)
                        onSuccess()
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Registration failed"
                    } finally {
                        isLoading = false
                    }
                }
            }
        )
    }

    // ---------------- User Registration ----------------
    fun registerUser(onSuccess: () -> Unit) {
        if (!isUserValid) {
            errorMessage = "Please fill all fields correctly"
            return
        }

        val userId = generateUUID()
        val user = userState.copy(id = userId)
        performAction(
            action = { repository.createUserAccount(user) },
            onSuccess = {
                viewModelScope.launch {
                    try {
                        preferenceManager.saveUserProfile(user)
                        preferenceManager.setLoggedIn(true)
                        preferenceManager.setUserType(SessionManager.UserType.USER)
                        onSuccess()
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Registration failed"
                    } finally {
                        isLoading = false
                    }
                }
            }
        )
    }

    // -------- Login (Owner/User) --------
    fun login(
        accountType: AccountType,
        onSuccess: () -> Unit
    ) {
        if (!isValidEmail(loginState.email) || loginState.password.length < 6) {
            errorMessage = "Invalid email or password format"
            return
        }

        viewModelScope.launch {
            try {
                isLoading = true
                val profile = when (accountType) {
                    AccountType.USER -> repository.authenticate<Owner>(loginState, accountType)
                    AccountType.OWNER -> repository.authenticate<User>(loginState, accountType)
                }
                if (profile != null) {
                    when (profile) {
                        is Owner -> {
                            preferenceManager.saveOwnerProfile(profile)
                            preferenceManager.setUserType(SessionManager.UserType.OWNER)
                        }

                        is User -> {
                            preferenceManager.saveUserProfile(profile)
                            preferenceManager.setUserType(SessionManager.UserType.USER)
                        }
                    }
                    preferenceManager.setLoggedIn(true)
                    onSuccess()
                } else {
                    errorMessage = "Invalid email or password"
                }
            } catch (e: Exception) {
                errorMessage = "Login failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // ---------------- Utilities ----------------
    private fun performAction(action: suspend () -> Unit, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null
                action()
                onSuccess?.invoke()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Operation failed"
            } finally {
                isLoading = false
            }
        }
    }

    // -------- Utilities --------
    fun clearOwnerFields() {
        ownerState = Owner()
    }

    fun clearUserFields() {
        userState = User()
    }

    // Update fields
    fun updateOwner(update: Owner.() -> Owner) {
        ownerState = ownerState.update()
    }

    fun updateUser(update: User.() -> User) {
        userState = userState.update()
    }

    fun updateLogin(update: LoginCredentials.() -> LoginCredentials) {
        loginState = loginState.update()
    }


    fun validateOwnerFields(): List<String> {
        val errors = mutableListOf<String>()
        if (ownerState.name.isBlank()) errors.add("Name is required")
        if (ownerState.name.isBlank()) errors.add("Email is required")
        if (ownerState.name.isBlank() || ownerState.name.length < 6) errors.add("Password must be at least 6 characters")
        if (!isValidEmail(ownerState.name)) errors.add("Invalid email format")
        if (ownerState.name.isNotBlank() && ownerState.name.length < 10) errors.add("Phone number must be at least 10 digits")
        return errors
    }

    fun validateUserFields(): List<String> {
        val errors = mutableListOf<String>()
        if (userState.name.isBlank()) errors.add("Name is required")
        if (userState.email.isBlank()) errors.add("Email is required")
        if (userState.password.isBlank() || userState.password.length < 6) errors.add("Password must be at least 6 characters")
        if (!isValidEmail(userState.email)) errors.add("Invalid email format")
        if (userState.phoneNumber.isNotBlank() && userState.phoneNumber.length < 10) errors.add("Phone number must be at least 10 digits")
        return errors
    }

    // Check login status
    fun checkLoginStatus(onLoggedIn: () -> Unit) {
        viewModelScope.launch {
            preferenceManager.isLoggedIn.collect { loggedIn ->
                if (loggedIn) onLoggedIn()
            }
        }
    }

    // Load existing profile
    fun loadExistingOwnerProfile() {
        viewModelScope.launch {
            val ownerProfile = preferenceManager.getOwnerProfile()
            ownerProfile?.let {
                ownerState = it
            }
        }
    }

    fun loadExistingUserProfile() {
        viewModelScope.launch {
            val userProfile = preferenceManager.getUserProfile()
            userProfile?.let {
                userState = it
            }
        }
    }
}