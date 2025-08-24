package com.ruko.system.presentation.navigation.bottom_navigation_bar

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


/**
 * Bottom Navigation Bar Item, uses default [NavigationBarItem].
 *
 * @param modifier Modifier.
 * @param item [NavigationItem].
 * @param isSelected Whether this screen is currently selected.
 * @param onClick OnClick callback.
 */
@Composable
fun RowScope.BottomNavigationBarItem(
    modifier: Modifier = Modifier,
    item: NavigationItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        colors = NavigationBarItemDefaults.colors(
            selectedTextColor = MaterialTheme.colorScheme.primary,
            selectedIconColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
            unselectedTextColor = MaterialTheme.colorScheme.onBackground,
            unselectedIconColor = MaterialTheme.colorScheme.onBackground
        ),
        label = {
            CustomTooltip(
                text = stringResource(item.tooltip),
                padding = 64.dp
            ) {
                Text(
                    text = stringResource(item.title),
                    maxLines = 1,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        selected = isSelected,
        onClick = { onClick() },
        icon = {
            CustomTooltip(
                text = stringResource(item.tooltip),
                padding = 32.dp
            ) {
                Crossfade(
                    targetState = isSelected,
                    animationSpec = tween(150),
                    label = ""
                ) {
                    if (it) {
                        Icon(
                            painter = painterResource(item.selectedIcon),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            painter = painterResource(item.unselectedIcon),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        },
        modifier = modifier
    )
}