package com.ruko.system

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import com.ruko.system.domain.model.PosterData


@Composable
actual fun WebViewShareScreen(data: PosterData, onShared: () -> Unit) {
}

actual fun imageBitmapToBase64(imageBitmap: ImageBitmap): String {
    TODO("Not yet implemented")
}