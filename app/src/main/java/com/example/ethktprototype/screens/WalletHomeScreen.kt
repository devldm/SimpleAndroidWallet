package com.example.ethktprototype.screens

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ethktprototype.WalletViewModel
import com.example.ethktprototype.composables.MyRow
import com.example.ethktprototype.composables.NftList
import com.example.ethktprototype.composables.ReceiveBottomSheet
import com.example.ethktprototype.composables.SendBottomSheet
import com.example.ethktprototype.composables.SuccessDialogModal
import com.example.ethktprototype.composables.TokenList
import java.text.DecimalFormat

@Composable
fun TokenListScreen(
    navController: NavHostController,
    viewModel: WalletViewModel,
    application: Application,
) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val decimalFormatBalance = DecimalFormat("#.##")

    LaunchedEffect(uiState.transactionHash) {
        if (uiState.transactionHash.isNotEmpty()) {
            viewModel.setShowSuccessModal(true)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getBalances()
        viewModel.getNftBalances()
    }

    LaunchedEffect(uiState.selectedNetwork) {
        viewModel.getBalances()
        viewModel.getNftBalances()
    }


    Column(
        Modifier
            .fillMaxSize()
            .fillMaxHeight()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 0.dp)
                .wrapContentHeight()
        ) {
            Spacer(Modifier.weight(1f))
            IconButton(
                modifier = Modifier.size(30.dp),
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$${uiState.totalBalanceUSD.let { decimalFormatBalance.format(it) } ?: "0.00"}",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 50.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uiState.ens.ifEmpty {
                        uiState.walletAddress.take(5) + "..." + uiState.walletAddress.takeLast(
                            4
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                if (uiState.ens.isNotEmpty()) {
                    Text(
                        text = uiState.walletAddress.take(5) + "..." + uiState.walletAddress.takeLast(
                            4
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }
            IconButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(uiState.walletAddress))
                }
            ) {
                Icon(
                    Icons.Filled.ContentCopy,
                    contentDescription = "Copy wallet address",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

//        NetworkDropdown(
//            networks = networks,
//            selectedNetwork = viewModel.selectedNetwork,
//            updateSelectedNetwork = { viewModel.updateSelectedNetwork(it) }
//        )

        Spacer(modifier = Modifier.height(8.dp))

        MyRow(
            setShowPayDialog = { viewModel.setShowPayDialog(it) },
            setShowWalletModal = { viewModel.setShowWalletModal(it) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        val titles = listOf("Tokens", "NFTs")
        var state by remember { mutableIntStateOf(0) }

        TabRow(selectedTabIndex = state, containerColor = MaterialTheme.colorScheme.background) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = state == index,
                    onClick = { state = index },
                    text = { Text(text = title, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                )
            }
        }

        when (state) {
            0 -> TokenList(
                selectedNetwork = uiState.selectedNetwork,
                viewModel = viewModel,
                tokens = uiState.tokens,
                tokenBlocklist = uiState.tokenBlocklist,
                isTokensLoading = uiState.isTokensLoading,
                showTokenBottomSheet = uiState.showTokenBottomSheet,
                selectedToken = uiState.selectedToken,
                onTokenSelected = { token ->
                    viewModel.updateSelectedToken(token)
                }
            )

            1 -> NftList(nfts = uiState.nfts, viewModel = viewModel)
        }
    }


    if (uiState.showPayDialog) {
        SendBottomSheet(
            onDismiss = { viewModel.setShowPayDialog(false) },
            onPay = { address, amount, contractAddress ->
                viewModel.onPayConfirmed(
                    address,
                    amount.toDouble(),
                    contractAddress
                )
            },
            selectedNetwork = uiState.selectedNetwork,
            tokens = uiState.tokens,
            viewModel = viewModel
        )
    }

    if (uiState.showWalletModal) {
        ReceiveBottomSheet(
            walletAddress = uiState.walletAddress,
            onDismiss = { viewModel.setShowWalletModal(false) }
        )
    }

    if (uiState.showSuccessModal) {
        SuccessDialogModal(
            value = decimalFormatBalance.format(uiState.sentAmount).toString(),
            network = uiState.selectedNetwork,
            hash = uiState.hash,
            address = uiState.toAddress,
            sentCurrency = uiState.sentCurrency,
            onDismiss = { viewModel.setShowSuccessModal(false); viewModel.setHashValue("") }
        )
    }
}