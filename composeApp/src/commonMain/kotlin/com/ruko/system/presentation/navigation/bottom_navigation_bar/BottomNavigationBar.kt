package com.ruko.system.presentation.navigation.bottom_navigation_bar

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ruko.system.presentation.navigation.BTMNavGraph


/**
 * Bottom navigation bar, uses default [NavigationBar].
 */
@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    bottomNavItems: List<NavigationItem>,
    selectedItem: Int = 0,
    onSelect: (index: Int) -> Unit = {},
    navigate: (graph: BTMNavGraph) -> Unit = {}
) {

    BottomAppBar(
        modifier = modifier.clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEachIndexed { index, it ->
            BottomNavigationBarItem(
                item = it,
                isSelected = selectedItem == index
            ) {
                onSelect(index)
                navigate(it.graph)
            }
        }
    }
}
