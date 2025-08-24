package com.ruko.system.presentation.screens.owner.queue.components

// commonMain/src/main/kotlin/your/package/qr/QrCodeGenerator.kt

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

expect fun renderComposableToImageBitmap(
    width: Dp = 300.dp,
    height: Dp = 400.dp,
    callback: (ImageBitmap) -> Unit,
    content: @Composable () -> Unit
)