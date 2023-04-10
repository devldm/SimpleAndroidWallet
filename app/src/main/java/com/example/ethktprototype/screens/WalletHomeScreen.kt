package com.example.ethktprototype.screens

import NetworkDropdown
import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavHostController
import com.example.ethktprototype.Network
import com.example.ethktprototype.WalletViewModel
import com.example.ethktprototype.composables.*
import com.example.ethktprototype.getUserBalances
import utils.loadBip44Credentials
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenListScreen(
    navController: NavHostController,
    viewModel: WalletViewModel,
    application: Application
) {
    val walletAddress = viewModel.walletAddress.value // Replace with the actual wallet address
    val networks = remember { Network.values().toList() }
    val tokensState = viewModel.getTokens(application)
    val tokens by tokensState.observeAsState(emptyList())
    val hashState = viewModel.hash.observeAsState()
    var hash by remember { mutableStateOf("") }
    val initialSelectedNetwork = remember { viewModel.selectedNetwork.value }
    var showPayDialog by remember { mutableStateOf(false) }
    var showWalletModal by remember { mutableStateOf(false) }
    var showSuccessModal by remember { mutableStateOf(false) }


    var toAddress by remember {
        mutableStateOf("")
    }
    var sentAmount by remember {
        mutableStateOf(0.0)
    }
    var sentCurrency by remember {
        mutableStateOf("")
    }


    fun onPayConfirmed(address: String, amount: Double, contractAddress: String) {
        val mnemonic = viewModel.getMnemonic()
        toAddress = address
        sentAmount = amount
        sentCurrency = viewModel.selectedToken.value?.symbol ?: ""

        if (!mnemonic.isNullOrEmpty()) {
            val credentials = loadBip44Credentials(mnemonic)
            credentials.let {
                viewModel.sendTokens(
                    credentials, contractAddress, address, BigDecimal.valueOf(amount)
                )
                showPayDialog = false
            }
        }
    }

    if (!hashState.value.isNullOrEmpty() && hash != hashState.value) {
        showSuccessModal = true
        hash = hashState.value!!
    }

    LaunchedEffect(viewModel.selectedNetwork.value) {
        if(initialSelectedNetwork != viewModel.selectedNetwork.value) {
            viewModel.getTokens(application)
        }
        viewModel.currentNetworkBalances.value = getUserBalances(application, selectedNetwork = viewModel.selectedNetwork.value.displayName)
        viewModel.selectedToken.value = viewModel.currentNetworkBalances.value.firstOrNull()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxHeight(),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .fillMaxHeight()
            ) {
                Text(
                    text = "Wallet Address",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = walletAddress ?: "Address not found",
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
                WalletAddressModal(walletAddress = walletAddress ?: "Address not found",
                    onDismiss = { showWalletModal = false })
            }

            if (showSuccessModal) {
                SuccessDialogModal(value = sentAmount.toString(),
                    address = toAddress,
                    sentCurrency = sentCurrency,
                    onDismiss = { showSuccessModal = false; viewModel.hash = MutableLiveData("") })
            }

            // show the pay dialog if the state variable is true
            if (showPayDialog) {
                PayDialog(
                    onDismiss = { showPayDialog = false },
                    onPay = { address, amount, contractAddress ->
                        onPayConfirmed(
                            address,
                            amount.toDouble(),
                            contractAddress
                        )
                    },
                    selectedNetwork = viewModel.selectedNetwork.value,
                    tokens = tokens,
                    viewModel = viewModel
                )
            }

        }
        if (viewModel.loading.value) {
            item {
                Loading()
            }
        } else if (!viewModel.loading.value && tokens.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No tokens found for this wallet")
                }

            }
        } else {
            items(tokens.size) { token ->
                val t = tokens[token]
                val balanceInEth = t.balance.toBigDecimal().divide(BigDecimal.TEN.pow(18))
                val formatBalance = balanceInEth?.let { String.format("%.4f", it) } ?: "N/A"

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