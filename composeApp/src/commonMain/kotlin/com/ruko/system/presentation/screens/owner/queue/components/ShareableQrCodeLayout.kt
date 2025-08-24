package com.ruko.system.presentation.screens.owner.queue.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ShareableQrCodeLayout(
    title: String,
    description: String,
    qrImageBitmap: ImageBitmap
) {
    Box(
        modifier = Modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFA5D6A7), // Mint
                        Color(0xFF66BB6A)   // Sky Blue
                    )
                )
            )
            .wrapContentSize()

    ) {
        Card(
            modifier = Modifier
                .padding(12.dp), // Outer breathing space
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp), // Soft shadow for depth
            border = BorderStroke(
                1.5.dp,
                Color(0xFF304FFE).copy(alpha = 0.4f) // Subtle themed border
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 32.sp, // Large, but not oversized
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF2E7D32),// Deep Green, // Medium soft violet
                    textAlign = TextAlign.Center
                )

                // Description
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 25.sp,
                        lineHeight = 22.sp
                    ),
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(26.dp))
                // QR Code
                Image(
                    painter = BitmapPainter(image = qrImageBitmap),
                    contentDescription = "QR Code for $title",
                    modifier = Modifier
                        .size(290.dp)
                        .border(5.dp, Color(0xFF66BB6A), RoundedCornerShape(12.dp))
                        .padding(6.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}