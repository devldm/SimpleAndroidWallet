package com.example.ethktprototype.composables

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.ethktprototype.R
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@Composable
fun BarcodeScanner(
    onScanResult: (String) -> Unit
) {
    val context = LocalContext.current
    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            if (result.contents != null) {
                onScanResult(result.contents)
            } else {
                Toast.makeText(context, "Scan cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val options = ScanOptions()
    options.setPrompt("Scan a QR code")
    options.setOrientationLocked(false)
    options.setBeepEnabled(false)

    IconButton(
        onClick = {
            scanLauncher.launch(options)
        },

    ) {
        Icon(
            painter = painterResource(id = R.drawable.baseline_qr_code_scanner_24),
            contentDescription = "Scan QR Code"
        )
    }
}
