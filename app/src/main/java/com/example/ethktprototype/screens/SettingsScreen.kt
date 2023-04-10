package com.example.ethktprototype.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ethktprototype.WalletViewModel

@Composable
fun SettingsScreen(navController: NavHostController, viewModel: WalletViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(22.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Surface(tonalElevation = 8.dp,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()) {
            Column(

                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Remove wallet",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "This will remove all your data from the SimpleWallet app. Be sure you backed up your mnemonic before continuing",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                // Button to import wallet
                Button(
                    onClick = {
                        viewModel.removeAllWalletData()
                    },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 16.dp)
                ) {
                    Text("Remove Wallet")
                }
            }

        }

    }
}