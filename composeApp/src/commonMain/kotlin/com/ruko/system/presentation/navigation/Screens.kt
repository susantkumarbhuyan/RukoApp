package com.ruko.system.presentation.navigation

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable


@Immutable
sealed class Screen {
    @Serializable
    data object Owner : Screen() {
        @Serializable
        data object Home : Screen()

        @Serializable
        data object Profile : Screen()

        @Serializable
        data class CreateQueue(val storeId: String) : Screen()

        @Serializable
        data class UpdateQueue(val queueId: String) : Screen()

        @Serializable
        data class UserQueueList(val queueId: String) : Screen()

        @Serializable
        data class QueueDetails(val queueId: String) : Screen()

        @Serializable
        data object History : Screen()

        @Serializable
        data object Setting : Screen()
    }

    @Serializable
    data object User : Screen() {
        @Serializable
        data object Home : Screen()

        @Serializable
        data object Profile : Screen()

        @Serializable
        data object QRScan : Screen()

        @Serializable
        data class JoinQueue(val qrQueue: String) : Screen()

        @Serializable
        data class CurrentQueue(val queueId: String) : Screen()

        @Serializable
        data object History : Screen()

        @Serializable
        data object Setting : Screen()
    }

    @Serializable
    data object OnBoarding : Screen() {
        @Serializable
        data object Welcome : Screen()

        @Serializable
        data object Feature : Screen()

        @Serializable
        data object Notification : Screen()

        @Serializable
        data object SignUp : Screen() {
            @Serializable
            data object User : Screen()

            @Serializable
            data object Owner : Screen()
        }
    }
}