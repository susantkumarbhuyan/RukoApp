package com.ruko.system

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import com.ruko.system.domain.model.PosterData

expect fun imageBitmapToBase64(imageBitmap: ImageBitmap): String

// commonMain
@Composable
expect fun WebViewShareScreen(data: PosterData, onShared: () -> Unit)