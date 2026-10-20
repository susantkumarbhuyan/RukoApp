package com.ruko.system.presentation.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.ruko.system.data.local.preference.PreferenceManager
import com.ruko.system.domain.model.QRQueue
import com.ruko.system.presentation.componnts.checkPermission
import com.ruko.system.presentation.screens.onboarding.FeatureScreen
import com.ruko.system.presentation.screens.onboarding.NotificationScreen
import com.ruko.system.presentation.screens.onboarding.WelcomeScreen
import com.ruko.system.presentation.screens.owner.OwnerHomeScreen
import com.ruko.system.presentation.screens.owner.profile.OwnerProfileScreen
import com.ruko.system.presentation.screens.owner.queue.CreateQueueScreen
import com.ruko.system.presentation.screens.owner.queue.QueueDetailsScreen
import com.ruko.system.presentation.screens.owner.queue.QueueEditScreen
import com.ruko.system.presentation.screens.owner.queue.history.QueueHistoryScreen
import com.ruko.system.presentation.screens.owner.queue.userqueue.UserQueueListScreen
import com.ruko.system.presentation.screens.signup.SignUpOwnerScreen
import com.ruko.system.presentation.screens.signup.SignUpScreen
import com.ruko.system.presentation.screens.signup.SignUpUserScreen
import com.ruko.system.presentation.screens.user.UserHomeScreen
import com.ruko.system.presentation.screens.user.current_queue.CurrentQueueScreen
import com.ruko.system.presentation.screens.user.profile.UserProfileScreen
import com.ruko.system.presentation.screens.user.queue.JoinQueueScreen
import com.ruko.system.presentation.screens.user.queue.QRScannerScreen
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.notifications.REMOTE_NOTIFICATION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

fun NavGraphBuilder.onboardingGraph(
    navController: NavHostController,
    controller: PermissionsController,
    snackBarHostState: SnackbarHostState,
    preferenceManager: PreferenceManager,
    scope: CoroutineScope
) {
    navigation<NavGraph.OnboardingGraph>(startDestination = Screen.OnBoarding.Welcome) {
        animatedComposable<Screen.OnBoarding.Welcome> {
            WelcomeScreen { navController.navigate(Screen.OnBoarding.Feature) }
        }
        animatedComposable<Screen.OnBoarding.Feature> {
            var isGranted by remember { mutableStateOf<Boolean?>(null) }

            LaunchedEffect(Unit) {
                isGranted = controller.isPermissionGranted(Permission.REMOTE_NOTIFICATION)
            }

            // Only show UI once we know the permission result
            isGranted?.let { granted ->
                FeatureScreen {
                    navController.navigate(
                        if (!granted) {
                            Screen.OnBoarding.Notification
                        } else {
                            scope.launch {
                                preferenceManager.setOnboarding(true)
                            }
                            Screen.OnBoarding.SignUp
                        }
                    )
                }
            }
        }


        animatedComposable<Screen.OnBoarding.Notification> {
            NotificationScreen({
                scope.launch {
                    // wait until checkPermission is fully done
                    checkPermission(
                        permission = Permission.REMOTE_NOTIFICATION,
                        controller = controller,
                        snackBarHostState = snackBarHostState
                    )

                    preferenceManager.setOnboarding(true)

                    // ✅ navigate after everything is finished
                    navController.navigate(Screen.OnBoarding.SignUp)
                }
            }, {})
        }
    }
}

fun NavGraphBuilder.signUpGraph(
    navController: NavHostController
) {
    navigation<NavGraph.SignUpGraph>(startDestination = Screen.OnBoarding.SignUp) {
        animatedComposable<Screen.OnBoarding.SignUp> {
            SignUpScreen { destination ->
                navController.navigate(destination) {
                    popUpTo(Screen.OnBoarding.SignUp) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
        animatedComposable<Screen.OnBoarding.SignUp.User> {
            SignUpUserScreen {
                navController.navigate(NavGraph.UserGraph) {
                    popUpTo(Screen.OnBoarding.SignUp) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
        animatedComposable<Screen.OnBoarding.SignUp.Owner> {
            SignUpOwnerScreen {
                navController.navigate(NavGraph.OwnerGraph) {
                    popUpTo(Screen.OnBoarding.SignUp) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }
}


fun NavGraphBuilder.ownerGraph(
    navController: NavHostController,
    preferenceManager: PreferenceManager,
    scope: CoroutineScope
) {
    navigation<NavGraph.OwnerGraph>(startDestination = Screen.Owner.Home) {
        animatedComposable<Screen.Owner.Home> {
            OwnerHomeScreen() { screen -> navController.navigate(screen) }
        }
        fadeThroughComposable<Screen.Owner.CreateQueue> {
            CreateQueueScreen(
                storeId = it.toRoute<Screen.Owner.CreateQueue>().storeId,
                { queueId ->
                    if (queueId != null && queueId.isNotBlank()) {
                        navController.navigate(
                            Screen.Owner.QueueDetails(queueId)
                        )
                    } else {
                        navController.navigate(
                            Screen.Owner.Home
                        )
                    }
                })
        }
        fadeThroughComposable<Screen.Owner.UpdateQueue> { entry ->
            val queueId = entry.toRoute<Screen.Owner.UpdateQueue>().queueId
            QueueEditScreen(queueId, { queueId ->
                if (queueId.isNotBlank()) {
                    navController.navigate(
                        Screen.Owner.QueueDetails(queueId)
                    ) {
                        popUpTo(Screen.Owner.UpdateQueue) { inclusive = true }
                        launchSingleTop = true
                    }
                } else {
                    navController.navigate(
                        Screen.Owner.Home
                    )
                }
            })
        }
        fadeThroughComposable<Screen.Owner.QueueDetails> { entry ->
            val queueId = entry.toRoute<Screen.Owner.QueueDetails>().queueId
            QueueDetailsScreen(queueId, { navController.popBackStack() }, onEditQueue = {
                navController.navigate(Screen.Owner.UpdateQueue(queueId))
            }, onUserListClick = {
                navController.navigate(Screen.Owner.UserQueueList(queueId))
            })
        }
        fadeThroughComposable<Screen.Owner.UserQueueList> { entry ->
            val queueId = entry.toRoute<Screen.Owner.UserQueueList>().queueId
            UserQueueListScreen(queueId, { navController.popBackStack() })
        }
        animatedComposable<Screen.Owner.Setting> {
            NotificationScreen({ navController.navigate(Screen.OnBoarding.SignUp) }, {})
        }
    }
}

fun NavGraphBuilder.ownerProfileGraph(
    navController: NavHostController,
    preferenceManager: PreferenceManager,
    scope: CoroutineScope
) {
    navigation<NavGraph.OwnerProfileGraph>(startDestination = Screen.Owner.Profile) {
        animatedComposable<Screen.Owner.Profile> {
            OwnerProfileScreen(onLogout = {
                scope.launch {
                    preferenceManager.setLoggedIn(false)
                }
                navController.navigate(NavGraph.SignUpGraph) {
                    popUpTo(0) { inclusive = true } // clear everything
                    launchSingleTop = true
                }
            })
        }
    }
}

fun NavGraphBuilder.userGraph(
    navController: NavHostController,
    preferenceManager: PreferenceManager,
    scope: CoroutineScope
) {
    navigation<NavGraph.UserGraph>(startDestination = Screen.User.Home) {
        animatedComposable<Screen.User.Home> {
            UserHomeScreen(
                {
                    navController.navigate(Screen.User.QRScan) { }
                },
                onQueueClick = { queueId ->
                    navController.navigate(Screen.User.CurrentQueue(queueId))
                }
            )
        }
        animatedComposable<Screen.User.Profile> {
            FeatureScreen { navController.navigate(Screen.OnBoarding.Notification) }
        }
        animatedComposable<Screen.User.QRScan> {
            QRScannerScreen(
                onCodeScanned = { qrData ->

                    navController.navigate(Screen.User.JoinQueue(qrData)) {
                        // Pop the QRScan screen from the back stack
                        popUpTo(Screen.User.QRScan) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }
        animatedComposable<Screen.User.JoinQueue> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.User.JoinQueue>()
            val qrData = args.qrQueue
            val qrQueue = com.ruko.system.utils.RukoQr.decode(qrData)
            if (qrQueue == null) {
                androidx.compose.material3.Text("Invalid RUKO QR code. Go back and scan again.")
                return@animatedComposable
            }
            JoinQueueScreen(
                qrQueue = qrQueue,
                onJoinComplete = { ->
                    navController.navigate(Screen.User.CurrentQueue(qrQueue.queueId)) {
                        popUpTo(Screen.User.JoinQueue) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        animatedComposable<Screen.User.CurrentQueue> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.User.CurrentQueue>()
            val queueId = args.queueId
            CurrentQueueScreen(queueId, { navController.navigate(Screen.User.Home) })
        }
        animatedComposable<Screen.User.Setting> {
            NotificationScreen({ navController.navigate(Screen.OnBoarding.SignUp) }, {})
        }
    }
}

fun NavGraphBuilder.ownerHistoryGraph(
    navController: NavHostController,
    preferenceManager: PreferenceManager,
    scope: CoroutineScope
) {
    navigation<NavGraph.OwnerHistoryGraph>(startDestination = Screen.Owner.History) {
        animatedComposable<Screen.Owner.History> {
            QueueHistoryScreen()
        }
    }
}


fun NavGraphBuilder.userHistoryGraph(
    navController: NavHostController,
    preferenceManager: PreferenceManager,
    scope: CoroutineScope
) {
    navigation<NavGraph.UserHistoryGraph>(startDestination = Screen.User.History) {
        animatedComposable<Screen.User.History> {
            QueueHistoryScreen()
        }
    }
}


fun NavGraphBuilder.profileGraph(
    navController: NavHostController,
    preferenceManager: PreferenceManager,
    scope: CoroutineScope
) {
    navigation<NavGraph.ProfileGraph>(startDestination = Screen.User.Profile) {

        animatedComposable<Screen.User.Profile> {
            UserProfileScreen {
                scope.launch {
                    preferenceManager.setLoggedIn(false)
                    preferenceManager.logout()
                }

                navController.navigate(NavGraph.SignUpGraph) {
                    popUpTo(NavGraph.SignUpGraph) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
        animatedComposable<Screen.User.Setting> {
            NotificationScreen({ navController.navigate(Screen.OnBoarding.SignUp) }, {})
        }
    }
}