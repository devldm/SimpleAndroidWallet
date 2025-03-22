package com.example.ethktprototype.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ethktprototype.Network
import com.example.ethktprototype.data.TokenBalance
import com.example.ethktprototype.WalletViewModel
import java.math.BigDecimal


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendBottomSheet(
    onDismiss: () -> Unit,
    onPay: (toAddress: String, amount: BigDecimal, contractAddress: String) -> Unit,
    selectedNetwork: Network,
    tokens: List<TokenBalance>,
    viewModel: WalletViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    var toAddress by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("0") }

    val nonBlockedTokens: List<TokenBalance> =
        tokens.filter { token -> !uiState.tokensBlocked.contains(token) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Send",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            OutlinedTextField(
                value = toAddress.lowercase(),
                onValueChange = { toAddress = it.lowercase() },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                trailingIcon = {
                    BarcodeScanner(onScanResult = { result -> toAddress = result })
                }
            )

            TokenDropdown(
                tokens = nonBlockedTokens,
                selectedToken = uiState.selectedToken,
                updateSelectedToken = { viewModel.updateSelectedToken(it) }
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                maxLines = 1
            )

            Text(
                text = "Chain: ${selectedNetwork.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(), // Increased padding,
                    onClick = {
                        uiState.selectedToken?.let {
                            onPay(
                                toAddress.lowercase().trim(),
                                amount.toBigDecimal(),
                                it.contractAddress
                            )
                        }
                    },
                    enabled = toAddress.isNotBlank() && amount.isNotBlank() && amount != "0",
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 10.dp),
                        fontSize = 20.sp,
                        text = "Send"
                    )
                }
            }

        }
    }
}


