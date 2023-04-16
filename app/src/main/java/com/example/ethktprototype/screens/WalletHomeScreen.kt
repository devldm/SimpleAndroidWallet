package com.example.ethktprototype.screens

import NetworkDropdown
import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.ethktprototype.Network
import com.example.ethktprototype.WalletViewModel
import com.example.ethktprototype.composables.*
import com.example.ethktprototype.getUserBalances
import java.math.BigDecimal
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenListScreen(
    navController: NavHostController,
    viewModel: WalletViewModel,
    application: Application
) {
    val walletAddress = viewModel.walletAddress.value
    val networks = remember { Network.values().toList() }
    val tokensState = viewModel.getBalances()
    val tokens by tokensState.observeAsState(emptyList())
    val hashState = viewModel.hash.observeAsState()
    var hash by remember { mutableStateOf("") }
    val initialSelectedNetwork = remember { viewModel.selectedNetwork.value }
    var showPayDialog = viewModel.showPayDialog.observeAsState()
    var showWalletModal by remember { mutableStateOf(false) }
    var showSuccessModal by remember { mutableStateOf(false) }
    val decimalFormatBalance = DecimalFormat("#.#######")


    if (!hashState.value.isNullOrEmpty() && hash != hashState.value) {
        showSuccessModal = true
        hash = hashState.value!!
    }

    LaunchedEffect(viewModel.selectedNetwork.value) {
        if (initialSelectedNetwork != viewModel.selectedNetwork.value) {
            viewModel.getBalances()
        }
        viewModel.currentNetworkBalances.value = getUserBalances(
            application,
            selectedNetwork = viewModel.selectedNetwork.value.displayName
        )
        viewModel.selectedToken.value = viewModel.currentNetworkBalances.value.firstOrNull()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxHeight(),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 0.dp)
            ) {
                Spacer(Modifier.weight(1f))
                IconButton(
                    modifier = Modifier.size(20.dp),
                    onClick = { navController.navigate("settingsScreen") }
                ) {
                    Icon(
                        Icons.Filled.Settings,
                        "contentDescription",
                    )
                }
            }
        }
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
            MyRow(viewModel.showPayDialog.value!!,
                setShowPayDialog = { viewModel.showPayDialog.value = it },
                showWalletModal,
                setShowWalletModal = { showWalletModal = it })
            Spacer(modifier = Modifier.height(16.dp))

            if (showWalletModal) {
                WalletAddressModal(walletAddress = walletAddress ?: "Address not found",
                    onDismiss = { showWalletModal = false })
            }

            if (showSuccessModal) {
                SuccessDialogModal(
                    value = decimalFormatBalance.format(viewModel.sentAmount.value).toString(),
                    network = viewModel.selectedNetwork.value,
                    hash = viewModel.hash.value ?: "",
                    address = viewModel.toAddress.value,
                    sentCurrency = viewModel.sentCurrency.value,
                    onDismiss = { showSuccessModal = false; viewModel.hash.value = "" })
            }

            // show the pay dialog if the state variable is true
            if (showPayDialog.value == true) {
                PayDialog(
                    onDismiss = { viewModel.showPayDialog.value = false },
                    onPay = { address, amount, contractAddress ->
                        viewModel.onPayConfirmed(
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
                val balanceInEth = t.balance.toBigDecimal().divide(BigDecimal.TEN.pow(t.decimals))
                val formattedBalance = decimalFormatBalance.format(balanceInEth) ?: "N/A"

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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier.size(50.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = t.tokenIcon,
                                    contentDescription = "Token Icon",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .size(45.dp)
                                        .padding(start = 10.dp)
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
                                    .clickable(onClick = {
                                        // TODO: Handle click on token
                                    })
                            ) {


                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
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
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Balance: $formattedBalance",
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
}