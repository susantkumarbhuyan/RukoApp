package com.ruko.system.presentation.ui.themes

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val LightColors = lightColorScheme(
    primary = Color(0xFF4285F4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD2E3FC),
    onPrimaryContainer = Color(0xFF0C1F42),

    secondary = Color(0xFF34A853),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB9F1CA),
    onSecondaryContainer = Color(0xFF082E11),

    tertiary = Color(0xFFFBBC05),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFFFBEBC7),
    onTertiaryContainer = Color(0xFF2E2208),

    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1E1E1E),

    // Revised surface and surfaceVariant colors
    surface = Color(0xFFF8FAFC),      // A very subtle, cool-toned white
    onSurface = Color(0xFF1E1E1E),

    surfaceVariant = Color(0xFFE5E9EC), // A soft grey for subtle contrast
    onSurfaceVariant = Color(0xFF5E6773),

    error = Color(0xFFEA4335),
    onError = Color.White,
    errorContainer = Color(0xFFFDE8E7),
    onErrorContainer = Color(0xFF410002)
)
val DarkColors = darkColorScheme(
    primary = Color(0xFF8AB4F8),      // Lighter blue for dark background
    onPrimary = Color.Black,          // Ensures visibility on light primary
    primaryContainer = Color(0xFF29467C),
    onPrimaryContainer = Color(0xFFD2E3FC),

    secondary = Color(0xFF57B76F),    // A more vibrant green for dark background
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF1A4725),
    onSecondaryContainer = Color(0xFFB9F1CA),

    tertiary = Color(0xFFFDE1A3),     // Lighter yellow for dark background
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF4C3B0E),
    onTertiaryContainer = Color(0xFFFBEBC7),

    background = Color(0xFF121212),   // True black for deep contrast and power efficiency on OLED screens
    onBackground = Color(0xFFF5F5F5),
    surface = Color(0xFF1E1E1E),      // Darker grey for surfaces and cards
    onSurface = Color(0xFFF5F5F5),
    surfaceVariant = Color(0xFF424242), // Slightly lighter grey for dividers
    onSurfaceVariant = Color(0xFFBDBDBD),

    error = Color(0xFFE6554D),        // A slightly desaturated red
    onError = Color.Black,
    errorContainer = Color(0xFF5A1C17),
    onErrorContainer = Color(0xFFFDE8E7)
)


