package com.ruko.system.presentation.componnts

import android.app.Activity
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import com.ruko.system.presentation.AndroidContextProvider
import java.io.File
import java.io.FileOutputStream

@Composable
actual fun ExitApp() {
    val context  = LocalContext.current
    (context as? Activity)?.finish()
}

actual fun shareQrCodeImage( image: ImageBitmap?, title: String) {
    if (image == null) return
    val context = AndroidContextProvider.appContext
    val bitmap = image.asAndroidBitmap()
    val file = File(context.cacheDir, "shared_qr.png")
    val outputStream = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    outputStream.flush()
    outputStream.close()

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TITLE, title)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, "Share via").addFlags(FLAG_ACTIVITY_NEW_TASK))
}