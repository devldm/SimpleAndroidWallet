package com.example.ethktprototype.composables

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ethktprototype.Network
import com.example.ethktprototype.data.TokenBalance
import com.example.ethktprototype.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenBottomSheet(
    token: TokenBalance,
    network: Network,
    viewModel: WalletViewModel,
    onDismiss: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = token.name,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                fontSize = 25.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
            ) {
                DetailRow(
                    label = "Symbol",
                    value = token.symbol
                )
                DetailRow(
                    label = "Decimals",
                    value = token.decimals.toString()
                )
                DetailRow(
                    label = "Contract address",
                    value = token.contractAddress
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
//                TextButton(
//                    onClick = {
//                        uriHandler.openUri(
//                            buildContractScanUrl(
//                                network,
//                                contractAddress = token.contractAddress
//                            )
//                        )
//                    },
//                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
//                ) {
//                    Text(modifier = Modifier.padding(vertical = 10.dp), fontSize = 20.sp, text = "Polygonscan")
//                    Icon(Icons.AutoMirrored.Filled.ExitToApp, "open external")
//                }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        Log.d("tokenBlock", "$token")
                        viewModel.updateTokenBlockList(token)
                    },
                    enabled = token.contractAddress.isNotEmpty()
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 10.dp),
                        fontSize = 15.sp,
                        text = "Add to Blocklist",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Left,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Left,
            fontSize = 15.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}
