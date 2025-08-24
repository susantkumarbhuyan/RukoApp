package com.ruko.system.presentation.screens.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun OwnerRegistrationForm(
    viewModel: AuthViewModel,
    focusManager: FocusManager,
    onAuthComplete: () -> Unit
) {
    val state = viewModel.ownerState

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AuthTextField(
            state.name,
            { viewModel.updateOwner { copy(name = it) } },
            "Owner Name",
            Icons.Default.Person,
            focusManager = focusManager
        )
        AuthTextField(
            state.businessName,
            { viewModel.updateOwner { copy(businessName = it) } },
            "Business Name",
            Icons.Default.Store,
            focusManager = focusManager
        )
        AuthTextField(
            state.businessType,
            { viewModel.updateOwner { copy(businessType = it) } },
            "Business Type",
            Icons.Default.Category,
            focusManager = focusManager
        )
        AuthTextField(
            state.phoneNumber,
            { viewModel.updateOwner { copy(phoneNumber = it) } },
            "Phone Number",
            Icons.Default.Phone,
            keyboardType = KeyboardType.Phone,
            focusManager = focusManager
        )
        AuthTextField(
            state.address,
            { viewModel.updateOwner { copy(address = it) } },
            "Business Address",
            Icons.Default.LocationOn,
            focusManager = focusManager
        )
        AuthTextField(
            state.email,
            { viewModel.updateOwner { copy(email = it) } },
            "Email Address",
            Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            focusManager = focusManager
        )
        AuthTextField(
            state.password,
            { viewModel.updateOwner { copy(password = it) } },
            "Password",
            Icons.Default.Lock,
            imeAction = ImeAction.Done,
            isPassword = true,
            focusManager = focusManager
        ) {
            if (viewModel.isOwnerValid) {
                viewModel.registerOwner { onAuthComplete() }
            }
        }

        AuthSubmitButton(viewModel.isOwnerValid, viewModel.isLoading) {
            viewModel.registerOwner { onAuthComplete() }
        }
    }
}

@Composable
fun UserRegistrationForm(
    viewModel: AuthViewModel,
    focusManager: FocusManager,
    onAuthComplete: () -> Unit
) {
    val state = viewModel.userState

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AuthTextField(
            state.name,
            { viewModel.updateUser { copy(name = it) } },
            "User Name",
            Icons.Default.Person,
            focusManager = focusManager
        )
        AuthTextField(
            state.phoneNumber,
            { viewModel.updateUser { copy(phoneNumber = it) } },
            "Phone Number",
            Icons.Default.Phone,
            keyboardType = KeyboardType.Phone,
            focusManager = focusManager
        )
        AuthTextField(
            state.email,
            { viewModel.updateUser { copy(email = it) } },
            "Email Address",
            Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            focusManager = focusManager
        )
        AuthTextField(
            state.password,
            { viewModel.updateUser { copy(password = it) } },
            "Password",
            Icons.Default.Lock,
            imeAction = ImeAction.Done,
            isPassword = true,
            focusManager = focusManager
        ) {
            if (viewModel.isUserValid) {
                viewModel.registerUser { onAuthComplete() }
            }
        }

        AuthSubmitButton(viewModel.isUserValid, viewModel.isLoading) {
            viewModel.registerUser { onAuthComplete() }
        }
    }
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    imeAction: ImeAction = ImeAction.Next,
    keyboardType: KeyboardType = KeyboardType.Text,
    focusManager: FocusManager,
    isPassword: Boolean = false,
    onDone: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(leadingIcon, contentDescription = null) },
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
            onDone = {
                focusManager.clearFocus()
                onDone?.invoke()
            }
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun AuthSubmitButton(
    isValid: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = isValid && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Create Account",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
