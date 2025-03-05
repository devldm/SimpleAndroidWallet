package com.example.ethktprototype.composables

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import qrcode.QRCode


@Composable
fun QRCodeDisplay(data: String, modifier: Modifier = Modifier) {
    val intColor = MaterialTheme.colorScheme.primary.toArgb()
    fun generateQRCode(text: String): Bitmap? {
        return try {
            val qrCode = QRCode.ofRoundedSquares()
                .withColor(intColor)
                .withBackgroundColor(0)
                .withSize(40)
                .build(text)

            val pngBytes = qrCode.renderToBytes()
            BitmapFactory.decodeStream(pngBytes.inputStream())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    val bitmap = generateQRCode(data)
    bitmap?.let {
        Box(
            modifier = modifier
                .size(250.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "QR Code",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}




