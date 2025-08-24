package com.ruko.system.presentation.navigation.bottom_navigation_bar

import androidx.compose.runtime.Immutable
import com.ruko.system.presentation.navigation.BTMNavGraph
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

@Immutable
data class NavigationItem(
    val graph: BTMNavGraph,
    val title: StringResource,
    val tooltip: StringResource,
    val selectedIcon: DrawableResource,
    val unselectedIcon: DrawableResource
)