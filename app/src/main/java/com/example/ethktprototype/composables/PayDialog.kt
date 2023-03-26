package com.example.ethktprototype.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ethktprototype.Network
import com.example.ethktprototype.TokenBalance
import com.example.ethktprototype.WalletViewModel
import java.math.BigDecimal


@Composable
fun PayDialog(
    onDismiss: () -> Unit,
    onPay: (toAddress: String, amount: BigDecimal, contractAddress: String) -> Unit,
    selectedNetwork: Network,
    tokens: List<TokenBalance>,
    viewModel: WalletViewModel,
) {

    var toAddress by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("0") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .height(450.dp),

            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,

        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Pay", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = toAddress,
                        onValueChange = { toAddress = it },
                        label = { Text("To Address") },
                        modifier = Modifier.weight(1f),
                    )
                    BarcodeScanner(onScanResult = {result -> toAddress =result})
                }
                Spacer(modifier = Modifier.height(16.dp))
                TokenDropdown(tokens = tokens, selectedToken = viewModel.selectedToken.value, updateSelectedToken = {viewModel.updateSelectedToken(it)} )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Chain: ${selectedNetwork.displayName}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = "Cancel")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { viewModel.selectedToken.value?.let {
                            onPay(toAddress, amount.toBigDecimal(),
                                it.contractAddress)
                        } },
                        enabled = toAddress.isNotBlank() && amount.isNotBlank() && amount != "0",
                    ) {
                        Text(text = "Send")
                    }
                }
            }
        }
    }
}

