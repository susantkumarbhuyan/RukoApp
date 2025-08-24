package com.ruko.system

import androidx.compose.runtime.Composable
import com.ruko.system.presentation.navigation.RootNavigation
import com.ruko.system.presentation.ui.MainTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MainTheme {
        RootNavigation()
    }
}