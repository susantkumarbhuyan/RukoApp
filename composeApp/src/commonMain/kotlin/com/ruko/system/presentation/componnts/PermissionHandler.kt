package com.ruko.system.presentation.componnts


import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.RequestCanceledException

suspend fun checkPermission(
    permission: Permission,
    controller: PermissionsController,
    snackBarHostState: SnackbarHostState
): Boolean {
    val granted = controller.isPermissionGranted(permission)
    if (!granted) {
        try {
            controller.providePermission(permission)
        } catch (_: DeniedException) {
            showPermissionSnackbar("Denied", controller, snackBarHostState)
        } catch (_: DeniedAlwaysException) {
            showPermissionSnackbar("Permanently Denied", controller, snackBarHostState)
        } catch (_: RequestCanceledException) {
            showPermissionSnackbar("Request Canceled", controller, snackBarHostState)
        }
    }
    return true // ✅ Always return after handling
}


private suspend fun showPermissionSnackbar(
    message: String,
    controller: PermissionsController,
    snackbarHostState: SnackbarHostState
) {
    val result = snackbarHostState.showSnackbar(
        message = message,
        actionLabel = "Open Settings",
        duration = SnackbarDuration.Short
    )
    if (result == SnackbarResult.ActionPerformed) {
        controller.openAppSettings()
    }
}