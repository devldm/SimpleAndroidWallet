package com.example.ethktprototype.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ethktprototype.WalletViewModel
import io.sentry.Sentry
import utils.isValidMnemonic
import utils.loadBip44Credentials


@Composable
fun ImportWallet(
    navController: NavHostController,
    onWalletImported: (String) -> Unit,
    viewModel: WalletViewModel
) {
    var errorMessage by remember { mutableStateOf("") }

    Surface(
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            var mnemonic by remember { mutableStateOf("") }

            // Input field for the mnemonic
            OutlinedTextField(
                value = mnemonic,
                onValueChange = { mnemonic = it },
                label = { Text("Enter your wallet mnemonic") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = false,
                maxLines = Int.MAX_VALUE,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                ),
            )

            // Display any error message
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Button to import wallet
            Button(
                onClick = {
                    if (isValidMnemonic(mnemonic)) {
                        mnemonic = mnemonic.trim()
                        try {
                            // TODO: See if other wallets work with Bip44
                            //val wallet = WalletUtils.loadBip39Credentials("", mnemonic)
                            val wallet = loadBip44Credentials(mnemonic)
                            onWalletImported(wallet.address)
                            viewModel.storeMnemonic(mnemonic)
                        } catch (e: Exception) {
                            errorMessage = "Failed to import wallet: ${e.message}"
                            Sentry.captureException(e)
                        }
                    } else {
                        errorMessage = "Invalid mnemonic"
                    }
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 16.dp)
            ) {
                Text("Import Wallet")
            }

        }
    }
}