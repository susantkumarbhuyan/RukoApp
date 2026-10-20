package com.ruko.system.presentation.componnts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import qrscanner.CameraLens
import qrscanner.QrScanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerView(
    onCodeScanned: (String) -> Unit,
    onClose: () -> Unit
) {
    var qrCodeURL by remember { mutableStateOf("") }
    var flashlightOn by remember { mutableStateOf(false) }
    var openImagePicker by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Pulsing animation for QR overlay
    val boxScale = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        boxScale.animateTo(
            targetValue = 0.85f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = FastOutLinearInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    val cornerScale = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        cornerScale.animateTo(
            targetValue = 0.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "QR Line Animation")
    val lineProgress by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "QR Line Progress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // camera preview or black background'

    ) {
        // Camera Scanner
        QrScanner(
            modifier = Modifier.fillMaxSize(),
            flashlightOn = flashlightOn,
            cameraLens = CameraLens.Back,
            openImagePicker = openImagePicker,
            onCompletion = { result ->
                coroutineScope.launch {
                    if (qrCodeURL.isEmpty() && result.startsWith("RUKO_") &&
                        com.ruko.system.utils.RukoQr.decode(result) != null) {
                        val cleanedQr = result.removePrefix("RUKO_") // remove the "RUKO_" prefix
                        qrCodeURL = cleanedQr
                        onCodeScanned(cleanedQr)
                    } else {
                        // Show snackbar for invalid QR
                        snackBarHostState.showSnackbar(
                            message = "Invalid QR. Please scan a RUKO QR.",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            },
            imagePickerHandler = { openImagePicker = it },
            onFailure = {
                coroutineScope.launch {
                    snackBarHostState.showSnackbar(
                        message = it.ifEmpty { "Invalid QR code. Please scan a RUKO QR." },
                        duration = SnackbarDuration.Short
                    )
                }
            },
            customOverlay = getAnimatedQrOverlay(
                lineProgress = lineProgress,
                cornerScale = cornerScale.value,
                boxScale = boxScale.value
            )
        )

        // Transparent TopBar overlay
        TopAppBar(
            title = {
                Text(
                    "Scan the QR",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
                titleContentColor = Color.White
            ),
            modifier = Modifier.align(Alignment.TopCenter)
                .padding(WindowInsets.statusBars.asPaddingValues())
        )

        // Bottom buttons (Flashlight + Gallery)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp).padding(
                    WindowInsets.navigationBars
                        .asPaddingValues()
                ),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = { flashlightOn = !flashlightOn }) {
                Icon(
                    imageVector = if (flashlightOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Flashlight",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(32.dp))
            IconButton(onClick = { openImagePicker = true }) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Gallery",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        androidx.compose.material3.SnackbarHost(
            hostState = snackBarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp)
        )

        // Brand overlay
        Text(
            text = "RUKO",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .padding(
                    WindowInsets.navigationBars
                        .asPaddingValues()
                ),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.2f)
        )

        // Scanned result panel
        AnimatedVisibility(
            visible = qrCodeURL.isNotEmpty(),
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Scanned QR Code:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Text(
                        text = qrCodeURL,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.DarkGray,
                        fontSize = 13.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = {
                            clipboardManager.setText(AnnotatedString(qrCodeURL))
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar("Copied to clipboard")
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun getAnimatedQrOverlay(
    lineProgress: Float,
    cornerScale: Float,
    boxScale: Float // New parameter
): ContentDrawScope.() -> Unit {
    return {
        val canvasWidth = this.size.width
        val canvasHeight = this.size.height
        val baseSize = minOf(300.dp.toPx(), canvasWidth - 48.dp.toPx())
        val size = baseSize * boxScale // Animate the size
        val topOffset = -50.dp.toPx()
        val top = (this.size.height - size) / 2 + topOffset
        val left = (this.size.width - size) / 2
        val rect = Rect(left, top, left + size, top + size)
        val cornerRadius = 16.dp.toPx()
        val borderWidth = 4.dp.toPx()

        // 1. Create a Path for the full screen
        val fullScreenPath = Path().apply {
            addRect(Rect(0f, 0f, canvasWidth, canvasHeight))
        }

        // 2. Create a Path for the rounded hole
        val qrBoxPath = Path().apply {
            addRoundRect(RoundRect(rect, CornerRadius(cornerRadius)))
        }
        // 3. Subtract the qrBoxPath from the fullScreenPath
        fullScreenPath.op(fullScreenPath, qrBoxPath, PathOperation.Difference)

        // 4. Draw the resulting path with the semi-transparent black color
        drawPath(
            path = fullScreenPath,
            color = Color.Black.copy(alpha = 0.6f)
        )
        // Animated corner borders
        val animatedBorderWidth = borderWidth * cornerScale
        val cornerLength = 60.dp.toPx()
        val innerOffset = 20.dp.toPx() // The padding from the border

// Top-left corner
        drawLine(
            color = Color.White,
            start = Offset(
                rect.left + innerOffset + cornerLength * cornerScale,
                rect.top + innerOffset
            ),
            end = Offset(rect.left + innerOffset + cornerRadius, rect.top + innerOffset),
            strokeWidth = animatedBorderWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White,
            start = Offset(
                rect.left + innerOffset,
                rect.top + innerOffset + cornerLength * cornerScale
            ),
            end = Offset(rect.left + innerOffset, rect.top + innerOffset + cornerRadius),
            strokeWidth = animatedBorderWidth,
            cap = StrokeCap.Round
        )

// Top-right corner
        drawLine(
            color = Color.White,
            start = Offset(
                rect.right - innerOffset - cornerLength * cornerScale,
                rect.top + innerOffset
            ),
            end = Offset(rect.right - innerOffset - cornerRadius, rect.top + innerOffset),
            strokeWidth = animatedBorderWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White,
            start = Offset(
                rect.right - innerOffset,
                rect.top + innerOffset + cornerLength * cornerScale
            ),
            end = Offset(rect.right - innerOffset, rect.top + innerOffset + cornerRadius),
            strokeWidth = animatedBorderWidth,
            cap = StrokeCap.Round
        )

// Bottom-left corner
        drawLine(
            color = Color.White,
            start = Offset(
                rect.left + innerOffset + cornerLength * cornerScale,
                rect.bottom - innerOffset
            ),
            end = Offset(rect.left + innerOffset + cornerRadius, rect.bottom - innerOffset),
            strokeWidth = animatedBorderWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White,
            start = Offset(
                rect.left + innerOffset,
                rect.bottom - innerOffset - cornerLength * cornerScale
            ),
            end = Offset(rect.left + innerOffset, rect.bottom - innerOffset - cornerRadius),
            strokeWidth = animatedBorderWidth,
            cap = StrokeCap.Round
        )

// Bottom-right corner
        drawLine(
            color = Color.White,
            start = Offset(
                rect.right - innerOffset - cornerLength * cornerScale,
                rect.bottom - innerOffset
            ),
            end = Offset(rect.right - innerOffset - cornerRadius, rect.bottom - innerOffset),
            strokeWidth = animatedBorderWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White,
            start = Offset(
                rect.right - innerOffset,
                rect.bottom - innerOffset - cornerLength * cornerScale
            ),
            end = Offset(rect.right - innerOffset, rect.bottom - innerOffset - cornerRadius),
            strokeWidth = animatedBorderWidth,
            cap = StrokeCap.Round
        )
//// Animated scanning line
//        val lineRectHeight = rect.height - 2 * borderWidth
//        val linePosition = rect.top + borderWidth + lineRectHeight * lineProgress
//
//// New value to control the line's horizontal padding
//        val horizontalPadding = 40.dp.toPx()
//
//        drawLine(
//            color = Color.White.copy(alpha = 0.8f),
//            start = Offset(rect.left + borderWidth + horizontalPadding, linePosition),
//            end = Offset(rect.right - borderWidth - horizontalPadding, linePosition),
//            strokeWidth = 4.dp.toPx(),
//            cap = StrokeCap.Round
//        )
    }
}

fun isValidRukoQr(qr: String): Boolean {
    return qr.startsWith("RUKO_") && com.ruko.system.utils.RukoQr.decode(qr) != null
}