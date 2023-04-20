package com.example.ethktprototype.screens

import NetworkDropdown
import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ethktprototype.Network
import com.example.ethktprototype.WalletViewModel
import com.example.ethktprototype.composables.MyRow
import com.example.ethktprototype.composables.NftList
import com.example.ethktprototype.composables.PayDialog
import com.example.ethktprototype.composables.SuccessDialogModal
import com.example.ethktprototype.composables.TokenList
import com.example.ethktprototype.composables.WalletAddressModal
import com.example.ethktprototype.getUserBalances
import java.text.DecimalFormat

@Composable
fun TokenListScreen(
    navController: NavHostController,
    viewModel: WalletViewModel,
    application: Application
) {
    val walletAddress = viewModel.walletAddress.value
    val networks = remember { Network.values().toList() }
    val tokensState = rememberUpdatedState(newValue = viewModel.getBalances())
    val tokens = rememberUpdatedState(newValue = viewModel.currentNetworkBalances.value)
    val hashState = viewModel.hash.observeAsState()
    var hash by remember { mutableStateOf("") }
    val initialSelectedNetwork = remember { viewModel.selectedNetwork.value }
    var showPayDialog = viewModel.showPayDialog.observeAsState()
    var showWalletModal by remember { mutableStateOf(false) }
    var showSuccessModal by remember { mutableStateOf(false) }
    val decimalFormatBalance = DecimalFormat("#.#######")
    val ensState = viewModel.checkForEnsName(walletAddress)
    val ens = viewModel.userEnsName.observeAsState()
    val nftTest = remember { viewModel.getNftBalances() }
    val nfts by nftTest.observeAsState(emptyList())

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
    Column(
        Modifier
            .fillMaxSize()
            .fillMaxHeight()
            .padding(vertical = 8.dp, horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 0.dp)
                .wrapContentHeight()
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
                text = walletAddress ?: "Address not found",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            if (!ens.value.isNullOrEmpty()) {
                Text(text = "ens: ${ens.value}")
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        NetworkDropdown(networks = networks,
            selectedNetwork = viewModel.selectedNetwork,
            updateSelectedNetwork = { viewModel.updateSelectedNetwork(it) })
        Spacer(modifier = Modifier.height(8.dp))
        MyRow(
            viewModel.showPayDialog.value!!,
            setShowPayDialog = { viewModel.showPayDialog.value = it },
            showWalletModal,
            setShowWalletModal = { showWalletModal = it })
        Spacer(modifier = Modifier.height(8.dp))

        val titles = listOf("Tokens", "NFTs")
        var state by remember { mutableStateOf(0) }

        TabRow(selectedTabIndex = state, containerColor = MaterialTheme.colorScheme.background) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = state == index,
                    onClick = { state = index },
                    text = { Text(text = title, maxLines = 2, overflow = TextOverflow.Ellipsis) })
            }
        }
        when (state) {
            0 -> TokenList(tokens = tokens.value, viewModel = viewModel)
            1 -> NftList(nfts = nfts, viewModel = viewModel)
        }
    }

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
            tokens = tokens.value,
            viewModel = viewModel
        )
    }
}