package com.ruko.system

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ruko.system.domain.model.PosterData
import java.io.ByteArrayOutputStream

// Convert Drawable resource to Bitmap
fun getBitmapFromDrawable(context: Context, drawableResId: Int): Bitmap {
    return BitmapFactory.decodeResource(context.resources, drawableResId)
}

// Convert Bitmap to Base64 string
fun bitmapToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.NO_WRAP)
}

actual fun imageBitmapToBase64(imageBitmap: ImageBitmap): String {
    return bitmapToBase64(imageBitmap.asAndroidBitmap())
}

@Composable
actual fun WebViewShareScreen(data: PosterData, onShared: () -> Unit) {
    val context = LocalContext.current
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isPageLoaded by remember { mutableStateOf(false) }
    val bitmap = getBitmapFromDrawable(context, R.drawable.qr_sample)

    val htmlContent = buildPosterHtml(
        brandTitle = data.brandTitle,
        shortDescription = data.shortDescription,
        qrImageBase64 = data.qrImageBase64,
        footerNote = data.footerNote
    )

    Box(

        modifier = Modifier
            .size(1.dp) // tiny but measurable
            .graphicsLayer { alpha = 0f },// invisible but measurable
        contentAlignment = Alignment.Center // center content inside
    ) {
        // Use AndroidView to embed the WebView.
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(),
            factory = {
                WebView(it).apply {
                    // Save the instance to our state variable.
                    webViewInstance = this
                    // Configure WebView settings.
                    settings.apply {
                        builtInZoomControls = true
                        useWideViewPort = false
                        javaScriptEnabled = true
                        setSupportMultipleWindows(false)
                        loadsImagesAutomatically = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                    }
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isPageLoaded = true
                        }
                    }
                    // Load the HTML data.
                    loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                }
            },
            // This 'update' block is called when the composable is recomposed.
            // You can use it to update the WebView's content if needed.
            update = {
                // If the HTML content were to change, you'd load it here.
                // it.loadData(newHtmlContent, "text/html", null)
            }
        )
    }

    LaunchedEffect(isPageLoaded) {
        // Check if the WebView instance is available.
        webViewInstance?.let { webView ->
            // Get the bitmap of the WebView.
            val bitmap = getBitmapOfView(webView)
            // Share the bitmap.
            shareBitmapAsImage(context, bitmap)
            if (isPageLoaded) {
                onShared()
            }
        }
    }
}

fun buildPosterHtml(
    brandTitle: String,
    shortDescription: String,
    qrImageBase64: String, // Pass base64 string (without "data:image/png;base64,")
    footerNote: String
): String {
    return """
  <!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --primary-color: #4285F4; /* Google Pay blue */
            --secondary-color: #34A853; /* Google Pay green */
            --background-start: #f8f9fa;
            --background-end: #e9ecef;
            --card-bg: #ffffff;
            --text-color: #202124;
            --subtle-text: #5f6368;
            --qr-box-bg: #f1f3f4;
            --glow-color: rgba(66, 133, 244, 0.5);
        }

        body {
            margin: 0;
            padding: 0;
            font-family: 'Roboto', sans-serif;
            background: linear-gradient(180deg, var(--background-start), var(--background-end));
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            color: var(--text-color);
        }
        .poster {
            background: var(--card-bg);
            border-radius: 24px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15), 0 0 20px var(--glow-color);
            padding: 80px 32px 40px; /* Increased padding-top to add space from the top */
            max-width: 400px;
            width: 90%;
            text-align: center;
            border: 1px solid #ddd;
        }
        .brand {
            font-size: 32px;
            font-weight: 700;
            color: var(--primary-color);
            margin-bottom: 20px;
            letter-spacing: -0.5px;
        }
        .description {
            font-size: 18px;
            color: var(--subtle-text);
            margin-bottom: 32px;
            line-height: 1.4;
        }
        .qr-box img {
            width: 220px;
            height: 220px;
            border-radius: 16px;
            border: 4px solid var(--primary-color); /* Added solid border to the image itself */
        }
        .footer-note {
            font-size: 14px;
            color: var(--subtle-text);
            margin-top: 16px;
        }
        .powered-by {
            font-size: 12px;
            color: var(--subtle-text);
            margin-top: 8px;
            opacity: 0.7;
        }
    </style>
</head>
<body>
    <div class="poster">
        <div class="brand">$brandTitle</div>
        <div class="description">$shortDescription</div>
        <div class="qr-box">
            <img src="data:image/png;base64,$qrImageBase64" alt="QR Code" />
        </div>
        <div class="footer-note">$footerNote</div>
        <div class="powered-by">Powered by RUKO</div>
    </div>
</body>
</html>
    """.trimIndent()
}