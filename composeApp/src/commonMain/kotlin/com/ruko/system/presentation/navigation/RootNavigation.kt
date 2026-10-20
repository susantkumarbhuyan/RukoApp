package com.ruko.system.presentation.navigation

import com.ruko.system.data.local.preference.SessionManager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.ruko.system.notifications.PushNotifications
import com.ruko.system.domain.repository.QueueRepository
import org.koin.compose.koinInject
import com.ruko.system.presentation.AppViewModel
import com.ruko.system.presentation.componnts.ExitAlertDialog
import com.ruko.system.presentation.componnts.ExitApp
import com.ruko.system.presentation.navigation.bottom_navigation_bar.BottomNavigationBar
import com.ruko.system.presentation.navigation.bottom_navigation_bar.NavigationConstants.NAVIGATION_OWNER_ITEMS
import com.ruko.system.presentation.navigation.bottom_navigation_bar.NavigationConstants.NAVIGATION_USER_ITEMS
import com.ruko.system.utils.KmpLog
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import org.koin.compose.viewmodel.koinViewModel

@OptIn(
    ExperimentalSharedTransitionApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun RootNavigation() {
    val snackBarHostState = remember { SnackbarHostState() }
    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) { factory.createPermissionsController() }
    BindEffect(controller)

    val viewModel: AppViewModel = koinViewModel()
    val preferenceManager = viewModel.preferenceManager
    val isLoggedIn by preferenceManager.isLoggedIn.collectAsState(initial = null)
    val isOnBoardingDone by preferenceManager.isOnBoardingDone.collectAsState(initial = null)
    val userType by preferenceManager.userType.collectAsState(initial = null)

    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    val repository: QueueRepository = koinInject()
    val notificationTarget by PushNotifications.target.collectAsState()
    LaunchedEffect(notificationTarget, isLoggedIn, userType) {
        val target = notificationTarget
        if (target != null && isLoggedIn == true && userType != null) {
            try {
                if (userType == SessionManager.UserType.OWNER) {
                    val queue = repository.getQueue(target.queueId)
                    if (queue.storeId == repository.currentUserId) {
                        navController.navigate(Screen.Owner.UserQueueList(target.queueId)) { launchSingleTop = true }
                    }
                } else {
                    val membership = repository.fetchUserQByUserId(repository.currentUserId)
                    if (membership?.queueId == target.queueId) {
                        navController.navigate(Screen.User.CurrentQueue(target.queueId)) { launchSingleTop = true }
                    } else {
                        snackBarHostState.showSnackbar("This queue visit has ended.")
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (_: Exception) { snackBarHostState.showSnackbar("Open your queue from the home screen.") }
            finally { PushNotifications.consume(target.id) }
        }
    }

    var showExitDialog by remember { mutableStateOf(false) }
    var exit by remember { mutableStateOf(false) }

    if (exit) {
        ExitApp()
    }
    if (showExitDialog) {
        ExitAlertDialog(
            onDismiss = { showExitDialog = false },
            onExit = {
                showExitDialog = false
                exit = true
            }
        )
    }


    var showBottomNav by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableIntStateOf(0) }

    val userRoutes = listOf(
        Screen.User.Home::class.qualifiedName,
        Screen.User.History::class.qualifiedName,
        Screen.User.Profile::class.qualifiedName
    )

    val ownerRoutes = listOf(
        Screen.Owner.Home::class.qualifiedName,
        Screen.Owner.History::class.qualifiedName,
        Screen.Owner.Profile::class.qualifiedName
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    LaunchedEffect(currentRoute) {
        currentRoute?.let { route ->
            when {
                userRoutes.contains(route) -> {
                    showBottomNav = true
                    selectedItem = userRoutes.indexOf(route)
                }

                ownerRoutes.contains(route) -> {
                    showBottomNav = true
                    selectedItem = ownerRoutes.indexOf(route)
                }

                else -> {
                    showBottomNav = false
                    selectedItem = -1
                }
            }
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomNav,  // Set this value to control visibility
                enter = fadeIn(),          // Customize the enter animation (e.g., fadeIn, slideInVertically)
                exit = fadeOut()           // Customize the exit animation (e.g., fadeOut, slideOutVertically)
            ) {
                BottomNavigationBar(
                    Modifier,
                    bottomNavItems = if (userType == SessionManager.UserType.OWNER)
                        NAVIGATION_OWNER_ITEMS
                    else
                        NAVIGATION_USER_ITEMS,
                    selectedItem = selectedItem, { index ->
                        selectedItem = index
                    }) {
                    navController.navigate(it) {
                        navController.popBackStack(
                            navController.graph.findStartDestination().id, inclusive = true
                        )
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        },
    ) { innerPadding ->

// Wait until both values are loaded

        if (isLoggedIn != null && isOnBoardingDone != null && (isLoggedIn == false || userType != null)) {
            val startDestination = when {
                isLoggedIn == true || isOnBoardingDone == true -> {
                    when (userType) {
                        SessionManager.UserType.USER -> NavGraph.UserGraph
                        SessionManager.UserType.OWNER -> NavGraph.OwnerGraph
                        else -> NavGraph.SignUpGraph
                    }
                }

                else -> NavGraph.OnboardingGraph
            }
            val noPaddingRoutes = setOf(
                Screen.User.QRScan::class.qualifiedName,
            )
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier
                    .padding(
                        if (currentRoute in noPaddingRoutes) {
                            PaddingValues(0.dp)

//                            PaddingValues(bottom = innerPadding.calculateBottomPadding()) // keep bottom padding
                        } else {
                            innerPadding
                        }
                    )
                    .consumeWindowInsets(innerPadding),
            ) {
                onboardingGraph(
                    navController,
                    controller,
                    snackBarHostState,
                    preferenceManager,
                    scope
                )
                signUpGraph(navController)
                ownerGraph(navController, preferenceManager, scope)
                ownerProfileGraph(navController, preferenceManager, scope)
                userGraph(navController, preferenceManager, scope)
                userHistoryGraph(navController, preferenceManager, scope)
                ownerHistoryGraph(navController, preferenceManager, scope)
                profileGraph(navController, preferenceManager, scope)
            }
        } else {
            // Show splash/loading until preferences are ready
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        BackHandler {
            val currentEntry = navController.currentBackStackEntry
            val route = currentEntry?.destination?.route

            KmpLog.d("BackHandler", "Current route = $route")

            val currentScreen = runCatching { currentEntry?.toRoute<Screen>() }.getOrNull()
            KmpLog.d("BackHandler", "Decoded screen = $currentScreen")

            when (route) {
                Screen.OnBoarding.Welcome::class.qualifiedName,
                Screen.User.Home::class.qualifiedName,
                Screen.Owner.Home::class.qualifiedName -> {
                    KmpLog.d("BackHandler", "Home/Welcome detected → showing exit dialog")
                    showExitDialog = true
                }

                else -> {
                    KmpLog.d("BackHandler", "Default back → popBackStack()")
                    navController.popBackStack()
                }
            }
        }
    }
}
