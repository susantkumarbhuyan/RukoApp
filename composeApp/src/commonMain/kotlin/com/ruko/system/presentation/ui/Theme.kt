package com.ruko.system.presentation.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.ruko.system.presentation.ui.themes.DarkColors
import com.ruko.system.presentation.ui.themes.LightColors

@Composable
fun MainTheme(
    useDarkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors, // use default or customize
        content = content
    )
}