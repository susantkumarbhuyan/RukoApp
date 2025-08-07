package com.ruko.system.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ui/theme/Theme.kt
private val LightColors = lightColorScheme(
    primary = Color(0xFF4CAF50),      // Green (friendly)
    onPrimary = Color.White,
    secondary = Color(0xFF03A9F4),    // Light Blue (calm)
    onSecondary = Color.White,
    background = Color(0xFFFDFDFD),   // Soft white
    onBackground = Color(0xFF212121),
    surface = Color.White,
    onSurface = Color(0xFF212121),
    error = Color(0xFFD32F2F),
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color.Black,
    secondary = Color(0xFF4FC3F7),
    onSecondary = Color.Black,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    error = Color(0xFFCF6679),
    onError = Color.Black
)

@Composable
fun QueueTheme(
    useDarkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors, // use default or customize
        content = content
    )
}
