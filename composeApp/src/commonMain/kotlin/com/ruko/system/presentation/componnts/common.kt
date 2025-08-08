package com.ruko.system.presentation.componnts

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

@Composable
expect fun ExitApp ()

expect fun shareQrCodeImage ( image: ImageBitmap?, title: String)
@Composable
expect fun Reply ()