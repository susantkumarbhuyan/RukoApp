package com.ruko.system.presentation.navigation

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

sealed class BTMNavGraph

@Immutable
sealed class NavGraph : BTMNavGraph() {
    @Serializable
    data object OnboardingGraph : NavGraph()

    @Serializable
    data object SignUpGraph : NavGraph()

    @Serializable
    data object UserGraph : NavGraph()

    @Serializable
    data object OwnerProfileGraph : NavGraph()

    @Serializable
    data object ProfileGraph : NavGraph()

    @Serializable
    data object OwnerHistoryGraph : NavGraph()

    @Serializable
    data object UserHistoryGraph : NavGraph()

    @Serializable
    data object OwnerGraph : NavGraph()

}