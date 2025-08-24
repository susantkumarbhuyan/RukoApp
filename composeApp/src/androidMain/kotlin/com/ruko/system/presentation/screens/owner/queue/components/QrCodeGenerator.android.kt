package com.ruko.system.presentation.screens.owner.queue.components

// androidMain/src/main/kotlin/your/package/qr/QrCodeGenerator.kt

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.createBitmap
import com.ruko.system.presentation.AndroidContextProvider
import com.ruko.system.utils.KmpLog
import kotlin.math.roundToInt

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

actual fun renderComposableToImageBitmap(
    width: Dp,
    height: Dp,
    callback: (ImageBitmap) -> Unit,
    content: @Composable () -> Unit
)  // Create a ComposeView
{
    val context = AndroidContextProvider.appContextAct
    val composeView = ComposeView(context).apply {
        // Dispose of the Composition when the view is detached from the window
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        // Set the Composable content
        setContent {
            content()
        }
        // Set layout parameters for off-screen rendering
        layoutParams = ViewGroup.LayoutParams(width.value.roundToInt(), height.value.roundToInt())
        setLayerType(View.LAYER_TYPE_SOFTWARE, null) // Disable hardware acceleration
    }

// Create a dummy FrameLayout to hold the ComposeView
    val frameLayout = FrameLayout(context).apply {
        addView(composeView)
        // Ensure it's not visible
        visibility = View.INVISIBLE
    }
// Add the dummy FrameLayout to the Activity's content view
    val activity = context.findActivity() as Activity
    activity.runOnUiThread {
        (activity.findViewById<View>(android.R.id.content) as FrameLayout).addView(
            frameLayout
        )
        // Post a runnable to ensure the view is fully laid out before capturing the bitmap
        frameLayout.post {
            try {
                // Measure and layout the ComposeView
                composeView.measure(
                    View.MeasureSpec.makeMeasureSpec(
                        width.value.roundToInt(),
                        View.MeasureSpec.EXACTLY
                    ),
                    View.MeasureSpec.makeMeasureSpec(
                        height.value.roundToInt(),
                        View.MeasureSpec.EXACTLY
                    )
                )
                composeView.layout(
                    0,
                    0,
                    composeView.measuredWidth,
                    composeView.measuredHeight
                )

                // Create a Bitmap with the specified width and height
                val bitmap = createBitmap(width.value.roundToInt(), height.value.roundToInt())
                val canvas = Canvas(bitmap)

                // Draw the ComposeView onto the Canvas
                composeView.draw(canvas)
                // Remove the dummy FrameLayout from the Activity's content view
                frameLayout.post {
                    (activity.findViewById<View>(android.R.id.content) as FrameLayout).removeView(
                        frameLayout
                    )
                    callback(bitmap.asImageBitmap())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                KmpLog.d("IMAGE_UTLS", "captureComposableToBitmap: ${e.message}")
            }
        }
    }
}