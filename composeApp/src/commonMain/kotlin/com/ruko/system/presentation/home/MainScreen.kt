package com.ruko.system.presentation.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

data class QueueState(
    var businessName: String = "",
    var queueLength: Int = 0,
    var estimatedWait: Int = 0,
    var position: Int = 0
)

@Composable
fun InQApp() {
    val navController = rememberNavController()
    val queueState = remember { mutableStateOf(QueueState()) }

    NavHost(navController = navController, startDestination = "home") {

        composable("home") {
            HomeScreen(
                onScanClick = { navController.navigate("scanner") }
            )
        }

        composable("scanner") {
            QRScannerScreen(
                onCodeScanned = { scannedBusinessName ->
                    // Simulate setting business info
                    queueState.value = QueueState(
                        businessName = scannedBusinessName,
                        queueLength = 5,
                        estimatedWait = 10
                    )
                    navController.navigate("join")
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable("join") {
            val state = queueState.value
            JoinQueueScreen(
                businessName = state.businessName,
                queueLength = state.queueLength,
                estimatedWait = state.estimatedWait,
                onJoin = { name ->
                    // Simulate calculating queue position
                    queueState.value = state.copy(position = state.queueLength)
                    navController.navigate("status")
                }
            )
        }

        composable("status") {
            val state = queueState.value
            QueueStatusScreen(
                position = state.position,
                estimatedTime = state.estimatedWait,
                onCancel = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("your_turn") {
            YouAreUpScreen(
                onDone = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}
