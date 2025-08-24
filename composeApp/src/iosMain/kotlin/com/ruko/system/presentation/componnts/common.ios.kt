package com.ruko.system.presentation.componnts

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import qrscanner.toUIImage


@Composable
actual fun ExitApp() {
}

actual fun shareQrCodeImage(image: ImageBitmap?, title: String) {
    if (image == null) return

    val uiImage = image.toUIImage()

    val activityItems = listOf(uiImage)
    val activityViewController = UIActivityViewController(activityItems, null)

    // Presenting the share sheet
    val window = UIApplication.sharedApplication.keyWindow
    val rootViewController = window?.rootViewController

    rootViewController?.presentViewController(
        activityViewController,
        animated = true,
        completion = null
    )
}

