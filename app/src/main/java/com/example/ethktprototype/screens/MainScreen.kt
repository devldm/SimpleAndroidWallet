package com.example.ethktprototype.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ethktprototype.WalletViewModel
import com.example.ethktprototype.composables.ImportWallet

@Composable
fun MyMainScreen(navController: NavHostController, viewModel: WalletViewModel) {
    val mnemonicLoaded = viewModel.mnemonicLoaded.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(22.dp)
    ) {
        Text(
            text = "Import Wallet",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "To get started, import your wallet by entering your seed phrase or private key below:",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (!mnemonicLoaded) {
            ImportWallet(
                navController,
                onWalletImported = { address -> viewModel.storeWallet(address) },
                viewModel,
            )
        }
    }
}
