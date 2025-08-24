package com.ruko.system.presentation.screens.user.queue

import androidx.compose.runtime.Composable
import com.ruko.system.presentation.componnts.QrScannerView

@Composable
fun QRScannerScreen(
    onCodeScanned: (String) -> Unit,
    onCancel: () -> Unit
) {
    QrScannerView(
        onCodeScanned = onCodeScanned,
        onClose = onCancel
    )
}