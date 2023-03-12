package com.example.ethktprototype.composables

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@Composable
fun MyWalletTitle() {
    Text(
        text = "My Wallet",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,

        )
}