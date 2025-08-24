package com.ruko.system.presentation.screens.signup

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ruko.system.domain.model.AccountType
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import ruko.composeapp.generated.resources.Res
import ruko.composeapp.generated.resources.icon_customer
import ruko.composeapp.generated.resources.icon_store_owner

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignUpOwnerScreen(
    viewModel: AuthViewModel = koinViewModel(),
    onAuthComplete: () -> Unit
) {
    SignUpScreen(
        viewModel = viewModel,
        onAuthComplete = onAuthComplete,
        accountType = AccountType.OWNER,
        icon = painterResource(Res.drawable.icon_store_owner), // Use a dedicated icon
        registrationTitle = "Join as Owner",
        registrationSubtitle = "Enter your business details to get started",
        loginSubtitle = "Sign in to manage your business",
        registrationForm = { vm, fm, complete ->
            OwnerRegistrationForm(viewModel = vm, focusManager = fm, onAuthComplete = complete)
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignUpUserScreen(
    viewModel: AuthViewModel = koinViewModel(),
    onAuthComplete: () -> Unit
) {
    SignUpScreen(
        viewModel = viewModel,
        onAuthComplete = onAuthComplete,
        accountType = AccountType.USER,
        icon = painterResource(Res.drawable.icon_customer), // Use a dedicated icon
        registrationTitle = "Join as a User",
        registrationSubtitle = "Fill in your details to get started",
        loginSubtitle = "Sign in to join the queue",
        registrationForm = { vm, fm, complete ->
            UserRegistrationForm(viewModel = vm, focusManager = fm, onAuthComplete = complete)
        }
    )
}


@OptIn(
    ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun SignUpScreen(
    viewModel: AuthViewModel = koinViewModel(),
    onAuthComplete: () -> Unit,
    accountType: AccountType,
    icon: Painter,
    registrationTitle: String,
    registrationSubtitle: String,
    loginSubtitle: String,
    registrationForm: @Composable (
        viewModel: AuthViewModel,
        focusManager: FocusManager,
        onAuthComplete: () -> Unit
    ) -> Unit
) {
    var isRegistrationMode by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    // Intercept system back press
    BackHandler(enabled = isRegistrationMode) {
        // If we’re in registration, override system back to just switch mode
        isRegistrationMode = false
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back Button
        AnimatedVisibility(
            visible = isRegistrationMode,
            enter = fadeIn(tween(300)) + slideInHorizontally(),
            exit = fadeOut(tween(300)) + slideOutHorizontally()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                IconButton(onClick = { isRegistrationMode = false }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Login",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Header Section
        AnimatedContent(
            targetState = isRegistrationMode,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) }
        ) { showRegistration ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
//                Icon(
//                    imageVector = icon,
//                    contentDescription = "Account Icon",
//                    tint = MaterialTheme.colorScheme.onBackground,
//                    modifier = Modifier.size(80.dp)
//                )
                Image(
                    painter = icon,
                    contentDescription = "Account Icon",
                    modifier = Modifier.size(80.dp)
                )
                Text(
                    text = if (showRegistration) registrationTitle else "Welcome Back!",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (showRegistration) registrationSubtitle else loginSubtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Form Content
        AnimatedContent(
            targetState = isRegistrationMode,
            transitionSpec = {
                if (targetState) {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300)
                    )
                } else {
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(300)
                    ) togetherWith (
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(300)
                            )
                            )
                }
            },
            label = "auth_content",
            modifier = Modifier.fillMaxWidth()
        ) { showRegistration ->
            if (showRegistration) {
                registrationForm(viewModel, focusManager, onAuthComplete)
            } else {
                LoginForm(
                    viewModel = viewModel,
                    focusManager = focusManager,
                    accountType = accountType,
                    onAuthComplete = onAuthComplete,
                    onSwitchToRegistration = { isRegistrationMode = true }
                )
            }
        }

        // Error Message
        viewModel.errorMessage?.let { error ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
