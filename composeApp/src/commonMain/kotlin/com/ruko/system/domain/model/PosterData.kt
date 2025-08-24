package com.ruko.system.domain.model

data class PosterData(
    val brandTitle: String,
    val shortDescription: String,
    val qrImageBase64: String, // QR code as Base64 string
    val footerNote: String
)