package com.ruko.system.presentation.navigation.bottom_navigation_bar


import com.ruko.system.presentation.navigation.NavGraph
import ruko.composeapp.generated.resources.Res
import ruko.composeapp.generated.resources.bottom_nav_home
import ruko.composeapp.generated.resources.bottom_nav_my_queue
import ruko.composeapp.generated.resources.bottom_nav_profile
import ruko.composeapp.generated.resources.btm_nav_home_selected
import ruko.composeapp.generated.resources.btm_nav_home_unselected
import ruko.composeapp.generated.resources.btm_nav_profile_selected
import ruko.composeapp.generated.resources.btm_nav_profile_unselected
import ruko.composeapp.generated.resources.btm_nav_queue_selected
import ruko.composeapp.generated.resources.btm_nav_queue_unselected

object NavigationConstants {
    // Navigation items for NavigationBars.
    val NAVIGATION_USER_ITEMS: List<NavigationItem> = buildList {
        // Always show Calendar tab
        add(
            NavigationItem(
                graph = NavGraph.UserGraph,
                title = Res.string.bottom_nav_home,
                tooltip = Res.string.bottom_nav_home,
                selectedIcon = Res.drawable.btm_nav_home_selected,
                unselectedIcon = Res.drawable.btm_nav_home_unselected
            )
        )
        // Always show More tab
        add(
            NavigationItem(
                graph = NavGraph.UserHistoryGraph,
                title = Res.string.bottom_nav_my_queue,
                tooltip = Res.string.bottom_nav_my_queue,
                selectedIcon = Res.drawable.btm_nav_queue_selected,
                unselectedIcon = Res.drawable.btm_nav_queue_unselected
            )
        )
        add(
            NavigationItem(
                graph = NavGraph.ProfileGraph,
                title = Res.string.bottom_nav_profile,
                tooltip = Res.string.bottom_nav_profile,
                selectedIcon = Res.drawable.btm_nav_profile_selected,
                unselectedIcon = Res.drawable.btm_nav_profile_unselected
            )
        )
    }
    val NAVIGATION_OWNER_ITEMS: List<NavigationItem> = buildList {
        // Always show Calendar tab
        add(
            NavigationItem(
                graph = NavGraph.OwnerGraph,
                title = Res.string.bottom_nav_home,
                tooltip = Res.string.bottom_nav_home,
                selectedIcon = Res.drawable.btm_nav_home_selected,
                unselectedIcon = Res.drawable.btm_nav_home_unselected
            )
        )
        // Always show More tab
        add(
            NavigationItem(
                graph = NavGraph.OwnerHistoryGraph,
                title = Res.string.bottom_nav_my_queue,
                tooltip = Res.string.bottom_nav_my_queue,
                selectedIcon = Res.drawable.btm_nav_queue_selected,
                unselectedIcon = Res.drawable.btm_nav_queue_unselected
            )
        )
        add(
            NavigationItem(
                graph = NavGraph.OwnerProfileGraph,
                title = Res.string.bottom_nav_profile,
                tooltip = Res.string.bottom_nav_profile,
                selectedIcon = Res.drawable.btm_nav_profile_selected,
                unselectedIcon = Res.drawable.btm_nav_profile_unselected
            )
        )
    }
}