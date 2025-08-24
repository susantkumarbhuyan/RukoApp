package com.ruko.system

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.View
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import java.io.File
import java.io.FileOutputStream

// A helper function to create a Bitmap from any Android View.
// This is the modern replacement for WebView.capturePicture().
fun getBitmapOfView(view: View): Bitmap {
    if (view.width == 0 || view.height == 0) {
        // Assign default size if view is not measured yet
        view.measure(
            View.MeasureSpec.makeMeasureSpec(900, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1446, View.MeasureSpec.EXACTLY)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }
    val bitmap = createBitmap(view.width, view.height)
    val canvas = Canvas(bitmap)
    view.draw(canvas)
    return bitmap
}

// A helper function to share a Bitmap.
// This is the recommended way to share files securely.
fun shareBitmapAsImage(context: Context, bitmap: Bitmap) {
    // 1. Save the bitmap to a temporary cache file.
    val cachePath = File(context.cacheDir, "images")
    cachePath.mkdirs()
    val file = File(cachePath, "shared_image.png")
    val fileOutputStream = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
    fileOutputStream.flush()
    fileOutputStream.close()

    // 2. Get the URI of the temporary file using FileProvider.
    val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider", // 👈 matches manifest
        file
    )
    // 3. Create and launch the share intent.
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share via"))
}





