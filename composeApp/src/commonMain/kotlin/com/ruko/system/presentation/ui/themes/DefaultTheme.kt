package com.ruko.system.presentation.ui.themes

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val LightColors = lightColorScheme(
    primary = Color(0xFF006B60), onPrimary = Color.White,
    primaryContainer = Color(0xFFC8F2E7), onPrimaryContainer = Color(0xFF00382F),
    secondary = Color(0xFF49645E), onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCEAE4), onSecondaryContainer = Color(0xFF19352E),
    tertiary = Color(0xFF795900), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE5A6), onTertiaryContainer = Color(0xFF402D00),
    background = Color(0xFFF5F8F7), onBackground = Color(0xFF172D28),
    surface = Color.White, onSurface = Color(0xFF172D28),
    surfaceVariant = Color(0xFFE8EFEC), onSurfaceVariant = Color(0xFF50635D),
    outline = Color(0xFF71857D), outlineVariant = Color(0xFFD4E0DA),
    error = Color(0xFFBA1A1A), onError = Color.White,
    errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF410002)
)

val DarkColors = darkColorScheme(
    primary = Color(0xFF83D6C3), onPrimary = Color(0xFF00382F),
    primaryContainer = Color(0xFF005047), onPrimaryContainer = Color(0xFFA5F2DF),
    secondary = Color(0xFFB3CCC3), onSecondary = Color(0xFF20372F),
    secondaryContainer = Color(0xFF354D44), onSecondaryContainer = Color(0xFFCFE9DF),
    tertiary = Color(0xFFE8C36B), onTertiary = Color(0xFF402D00),
    background = Color(0xFF101C18), onBackground = Color(0xFFE0ECE6),
    surface = Color(0xFF182721), onSurface = Color(0xFFE0ECE6),
    surfaceVariant = Color(0xFF30443B), onSurfaceVariant = Color(0xFFB9CCC2),
    outline = Color(0xFF879E91), outlineVariant = Color(0xFF3D5147),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005)
)
