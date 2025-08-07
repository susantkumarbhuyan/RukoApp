package com.ruko.system.presentation.navigation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ruko.system.presentation.componnts.ExitApp
import com.ruko.system.presentation.data.QueueState
import com.ruko.system.presentation.owner.LoginScreen
import com.ruko.system.presentation.owner.OwnerHomeScreen
import com.ruko.system.presentation.owner.RegisterOwnerScreen
import com.ruko.system.presentation.user.JoinQueueScreen
import com.ruko.system.presentation.user.QRScannerScreen
import com.ruko.system.presentation.user.QueueStatusScreen
import com.ruko.system.presentation.user.RegisterUserScreen
import com.ruko.system.presentation.user.UserHomeScreen
import com.ruko.system.utils.SessionManager
import kotlinx.serialization.Serializable

@Serializable
sealed class Screen(val route: String) {
    @Serializable
    object Login : Screen("login")
    @Serializable
    object RegisterUser : Screen("register_user")
    @Serializable
    object RegisterOwner : Screen("register_owner")
    @Serializable
    object UserHome : Screen("user_home")
    {
        @Serializable
        object ScanQr : Screen("user_home/scan_qr")

        @Serializable
        object JoinQueue : Screen("user_home/join_queue")
        @Serializable
        object Status : Screen("user_home/status")
    }

    @Serializable
    object OwnerHome : Screen("owner_home") {
        @Serializable
        object GenerateQr : Screen("owner_home/generate_qr")

        @Serializable
        object CreateQueue : Screen("owner_home/create_queue")
    }
}
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun QueueNavHost() {
    val queueState = remember { mutableStateOf(QueueState()) }

    val navController = rememberNavController()
    val startDestination = if (SessionManager.isLoggedIn) {
        when (SessionManager.userType) {
            SessionManager.UserType.USER -> Screen.UserHome.route
            SessionManager.UserType.OWNER ->  Screen.OwnerHome.route
            else -> Screen.Login.route
        }
    } else {
        Screen.Login.route
    }

    var showExitDialog by remember { mutableStateOf(false) }
    var exit by remember { mutableStateOf(false) }


    if(exit){
        ExitApp()
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    exit = true
                }) {
                    Text("Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Exit App") },
            text = { Text("Are you sure you want to exit?") }
        )
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Login.route) {
            LoginScreen(
                onUserSelected = {
                    navController.navigate(Screen.RegisterUser.route)
                },
                onOwnerSelected = {

                    navController.navigate(Screen.RegisterOwner.route)
                }
            )
        }

        composable(Screen.RegisterUser.route) {
            RegisterUserScreen(
                onRegistrationComplete = {
                    navController.navigate(Screen.UserHome.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.UserHome.route) {
            BackHandler {
                showExitDialog = true
            }
            UserHomeScreen(
                onScanClick = { navController.navigate(Screen.UserHome.ScanQr.route) },
                onLogout = {
                    SessionManager.isLoggedIn = false
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                }
            )
        }

        composable(Screen.RegisterOwner.route) {
            RegisterOwnerScreen(
                onRegistrationComplete = {
                    navController.navigate(Screen.OwnerHome.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.OwnerHome.route) {
            BackHandler {
                showExitDialog = true
            }
            OwnerHomeScreen(
                onLogout = {
                    SessionManager.isLoggedIn = false
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                }
            )
        }

        composable(Screen.UserHome.ScanQr.route) {
            QRScannerScreen(
                onCodeScanned = { scannedBusinessName ->
                    // Simulate setting business info
                    queueState.value = QueueState(
                        businessName = scannedBusinessName,
                        queueLength = 5,
                        estimatedWait = 10
                    )
                    navController.navigate(Screen.UserHome.JoinQueue.route)
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(Screen.UserHome.JoinQueue.route) {
            val state = queueState.value

            BackHandler {
                navController.navigate(Screen.UserHome.route) {
                    popUpTo(Screen.UserHome.route) { inclusive = true }
                }
            }

            JoinQueueScreen(
                businessName = state.businessName,
                queueLength = state.queueLength,
                estimatedWait = state.estimatedWait,
                onJoin = { name ->
                    queueState.value = state.copy(position = state.queueLength)
                    navController.navigate(Screen.UserHome.Status.route)
                }
            )
        }

        composable(Screen.UserHome.Status.route) {
            val state = queueState.value

            BackHandler {
                navController.navigate(Screen.UserHome.route) {
                    popUpTo(Screen.UserHome.route) { inclusive = true }
                }
            }

            QueueStatusScreen(
                position = state.position,
                estimatedTime = state.estimatedWait,
                onCancel = {
                    navController.navigate(Screen.UserHome.route) {
                        popUpTo(Screen.UserHome.route) { inclusive = true }
                    }
                }
            )
        }
    }
}