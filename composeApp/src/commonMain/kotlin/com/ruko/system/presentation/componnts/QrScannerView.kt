package com.ruko.system.presentation.componnts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import qrscanner.CameraLens
import qrscanner.QrScanner
import kotlin.text.ifEmpty


@Composable
fun QrScannerView(
    onCodeScanned: (String) -> Unit,
    onClose: () -> Unit
) {
    var qrCodeURL by remember { mutableStateOf("") }
    var flashlightOn by remember { mutableStateOf(false) }
    var openImagePicker by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camera Scanner
        QrScanner(
            modifier = Modifier.fillMaxSize(),
            flashlightOn = flashlightOn,
            cameraLens = CameraLens.Back,
            openImagePicker = openImagePicker,
            onCompletion = {
                qrCodeURL = it
                onCodeScanned(it)
            },
            imagePickerHandler = { openImagePicker = it },
            onFailure = {
                coroutineScope.launch {
                    val message = it.ifEmpty { "Invalid QR code" }
                    snackBarHostState.showSnackbar(message)
                }
            },
//            customOverlay = {
//                val size = 80.dp.toPx()
//                val cornerRadius = 32.dp.toPx()
//                val left = (size - size) / 2f
//                val top = (size - size) / 2f
//                val rect = androidx.compose.ui.geometry.Rect(
//                    center.x - size / 2,
//                    center.y - size / 2,
//                    center.x + size / 2,
//                    center.y + size / 2
//                )
//
//                drawRoundRect(
//                    color = Color.Transparent,
//                    topLeft = rect.topLeft,
//                    size = androidx.compose.ui.geometry.Size(size, size),
//                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
//                    style = Stroke(width = 4.dp.toPx(), pathEffect = null)
//                )
//            }
        )

        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Scan QR Code",
                modifier = Modifier.weight(1f),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onClose() }
            )
        }
        // Bottom buttons (flash & gallery)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            IconButton(onClick = { flashlightOn = !flashlightOn }) {
                Icon(
                    imageVector = if (flashlightOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Flashlight",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(onClick = { openImagePicker = true }) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Gallery",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Scanned result panel
        AnimatedVisibility(
            visible = qrCodeURL.isNotEmpty(),
            modifier = Modifier.align(Alignment.BottomCenter)
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

        SnackbarHost(
            hostState = snackBarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}