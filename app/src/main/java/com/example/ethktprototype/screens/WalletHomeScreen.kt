package com.example.ethktprototype.screens

import NetworkDropdown
import TransactionViewModel
import android.app.Application
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
import com.example.ethktprototype.composables.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenListScreen(
    navController: NavHostController,
    viewModel: WalletViewModel,
    transactionViewModel: TransactionViewModel,
    application: Application
) {
    val walletAddress = viewModel.walletAddress.value // Replace with the actual wallet address
    val context = LocalContext.current // get the Context object from the LocalContext
    val networks = remember { Network.values().toList() }
    val contractAddresses = viewModel.getTokenContractAddresses()
    val tokensState = viewModel.getTokens(walletAddress!!, contractAddresses, context, application)
    val tokens by tokensState.observeAsState(emptyList())
    var showPayDialog by remember { mutableStateOf(false) }
    var showWalletModal by remember { mutableStateOf(false) }
    var showSuccessModal by remember { mutableStateOf(false) }


    var toAddress by remember {
        mutableStateOf("")
    }
    var sentAmount by remember {
        mutableStateOf(0.0)
    }


    fun onPayConfirmed(address: String, amount: Double, contractAddress: String) {
        val mnemonic = viewModel.getMnemonic(context)
        toAddress = address
        sentAmount = amount


        if (!mnemonic.isNullOrEmpty()) {
            val credentials = viewModel.loadBip44Credentials(mnemonic)
            Log.d("send", "credentials: ${credentials.address}")

            credentials.let {
                // wrap the sendToken call in a coroutine
                CoroutineScope(Dispatchers.Default).launch {
                    val hash = transactionViewModel.sendTokens(
                        credentials, contractAddress, address, BigDecimal.valueOf(0.5)
                    )
                    if (!hash.isNullOrEmpty()) {
                        showSuccessModal = true
                    }
                }
                showPayDialog = false
            }
        }
    }




    Log.d("network", "selectedNetwork: ${viewModel.selectedNetwork.value}")

    LaunchedEffect(viewModel.selectedNetwork.value) {
        Log.d("network", "rerunning on network change, fetching ${viewModel.selectedNetwork} data")
        viewModel.getTokens(walletAddress, contractAddresses, context, application)
    }


    Log.d("TokenListScreen", "Tokens size: ${tokens.size}")

    LazyColumn(
        modifier = Modifier.fillMaxSize().fillMaxHeight(),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp).fillMaxHeight()
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
        item {

            NetworkDropdown(networks = networks,
                selectedNetwork = viewModel.selectedNetwork,
                updateSelectedNetwork = { viewModel.updateSelectedNetwork(it) })
            Spacer(modifier = Modifier.height(8.dp))

        }

        item {
            MyRow(showPayDialog,
                setShowPayDialog = { showPayDialog = it },
                showWalletModal,
                setShowWalletModal = { showWalletModal = it })
            Spacer(modifier = Modifier.height(16.dp))

            if (showWalletModal) {
                WalletAddressModal(walletAddress = walletAddress,
                    onDismiss = { showWalletModal = false })
            }

            if (showSuccessModal) {
                SuccessDialogModal(value = sentAmount.toString(),
                    address = toAddress,
                    onDismiss = { showSuccessModal = false })
            }

            // show the pay dialog if the state variable is true
            if (showPayDialog) {
                PayDialog(onDismiss = { showPayDialog = false },
                    onPay = { address, amount, contractAddress -> onPayConfirmed(address, amount.toDouble(), contractAddress) },
                    selectedNetwork = viewModel.selectedNetwork.value,
                    tokens = tokens,
                    transactionViewModel = transactionViewModel
                )
            }

        }
        if (viewModel.loading.value) {
            item {
                Loading()
            }
        } else if (!viewModel.loading.value && tokens.isEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxSize().fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally,) {
                    Text("No tokens found for this wallet")
                }

            }
        } else {
            items(tokens.size) { token ->
                Log.d("tokens UI", "$tokens")
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
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clickable(onClick = {
                                    // TODO: Handle click on token
                                })
                        ) {


                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    t.symbol,
                                    style = MaterialTheme.typography.bodyMedium,
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