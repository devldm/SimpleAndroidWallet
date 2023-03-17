package com.example.ethktprototype.screens

import NetworkDropdown
import TransactionViewModel
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ethktprototype.Network
import com.example.ethktprototype.WalletViewModel
import com.example.ethktprototype.composables.Loading
import com.example.ethktprototype.composables.MyRow
import com.example.ethktprototype.composables.PayDialog
import com.example.ethktprototype.composables.WalletAddressModal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenListScreen(navController: NavHostController, viewModel: WalletViewModel, transactionViewModel: TransactionViewModel) {
    val walletAddress = viewModel.walletAddress.value // Replace with the actual wallet address
    val context = LocalContext.current // get the Context object from the LocalContext
    val networks = remember { Network.values().toList() }
    val selectedNetwork = remember { mutableStateOf(viewModel.selectedNetwork.value) }
    val tokensState = viewModel.getTokens(walletAddress!!, context)
    val tokens by tokensState.observeAsState(emptyList())
    var showPayDialog by remember { mutableStateOf(false) }
    var showWalletModal by remember { mutableStateOf(false) }


    fun onPayConfirmed(amount: Double) {
        val mnemonic = viewModel.getMnemonic(context)

        if (!mnemonic.isNullOrEmpty()) {
            val credentials = viewModel.loadBip44Credentials(mnemonic)
            Log.d("send", "credentials: ${credentials.address}")

            credentials?.let {
                val testAddress = "0x2360BF04Ba25fFeDDA24A519a2283D78FD84f6a6"
                // wrap the sendMatic call in a coroutine
                CoroutineScope(Dispatchers.Default).launch {
                    transactionViewModel.sendMatic(credentials, testAddress, BigDecimal.valueOf(amount))
                }
                showPayDialog = false
            }
        }
    }




    Log.d("network", "selectedNetwork: ${selectedNetwork.value}")

    LaunchedEffect(viewModel.selectedNetwork.value) {
    Log.d("network", "rerunning on network change, fetching ${viewModel.selectedNetwork} data")
        viewModel.getTokens(walletAddress, context)
    }


    Log.d("TokenListScreen", "Tokens size: ${tokens.size}")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Wallet Address",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = walletAddress,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        item{

            NetworkDropdown(
                networks = networks,
                selectedNetwork = viewModel.selectedNetwork,
                updateSelectedNetwork = { viewModel.updateSelectedNetwork(it) }
            )
            Spacer(modifier = Modifier.height(8.dp))

        }

        item {
            MyRow(showPayDialog, setShowPayDialog = { showPayDialog = it }, showWalletModal, setShowWalletModal = { showWalletModal = it})
            Spacer(modifier = Modifier.height(16.dp))
            
            if(showWalletModal) {
                WalletAddressModal(walletAddress = walletAddress, onDismiss = { showWalletModal = false })
            }

            // show the pay dialog if the state variable is true
            if (showPayDialog) {
                PayDialog(
                    onDismiss = { showPayDialog = false },
                    onPay = {address, amount -> onPayConfirmed(amount.toDouble())},
                    selectedNetwork = selectedNetwork.value

                )
            }

        }
        if(tokens.isEmpty()){
            item{
                Loading()
            }

        }
        else {
            items(tokens.size) { token ->
                val t = tokens[token]
                val balanceInEth = t.balance.toBigDecimal().divide(BigDecimal.TEN.pow(18))
                val formatBalance = balanceInEth?.let { String.format("%.2f", it) } ?: "N/A"

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(8.dp)
                        .clickable(onClick = {
                            // TODO: Handle click on token
                        })
                ) {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable(onClick = {
                                // TODO: Handle click on token
                            })
                        )
                        {


                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    t.symbol, style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )

                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = t.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Balance: $formatBalance",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }


    }
}