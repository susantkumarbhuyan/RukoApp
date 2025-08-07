package com.ruko.system.presentation.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ruko.system.presentation.componnts.QrScannerView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(onCodeScanned: (String) -> Unit, onCancel: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Scan QR Code") }) }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Point the camera at the QR code", modifier = Modifier.padding(16.dp))

            QrScannerView(onCodeScanned)

            Button(
                onClick = onCancel,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    }
}
