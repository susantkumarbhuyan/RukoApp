package com.ruko.system

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController( configure = {
    initIos()
}) { App() }