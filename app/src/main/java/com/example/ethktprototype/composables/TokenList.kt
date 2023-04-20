package com.example.ethktprototype.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ethktprototype.TokenBalance
import com.example.ethktprototype.WalletViewModel
import java.math.BigDecimal
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenList(tokens: List<TokenBalance>, viewModel: WalletViewModel) {
    val decimalFormatBalance = DecimalFormat("#.#######")
    var showTokenDialog by remember { mutableStateOf(false) }
    var selectedToken by remember { mutableStateOf<TokenBalance?>(null) }
    var tokensBlocked = rememberUpdatedState(newValue = viewModel.getTokenBlocklist())
    val rememberedTokens = rememberUpdatedState(viewModel.currentNetworkBalances.value)

    LaunchedEffect(viewModel.tokenBlocklist.value) {
        viewModel.tokenBlocklist.value = viewModel.getTokenBlocklist()
    }

    fun onHideClick() {
        showTokenDialog = false
    }

    if (!viewModel.loading.value && tokens.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("No tokens found for this wallet")
        }
    }

    if (viewModel.loading.value) {
        Loading()
    }


    LazyColumn(Modifier.fillMaxHeight(), userScrollEnabled = true) {
        val nonBlockedTokens: List<TokenBalance> =
            rememberedTokens.value.filter { token -> !tokensBlocked.value.contains(token) }
        items(nonBlockedTokens.size) { token ->
            val t = nonBlockedTokens[token]

            if (showTokenDialog && selectedToken == t) {
                TokenDialog(
                    token = t,
                    network = viewModel.selectedNetwork.value,
                    viewModel = viewModel,
                    onDismiss = { showTokenDialog = false; },
                    setShowTokenDialog = { onHideClick() }
                )
            }

            val balanceInEth = t.balance.toBigDecimal().divide(BigDecimal.TEN.pow(t.decimals))
            val formattedBalance = decimalFormatBalance.format(balanceInEth) ?: "N/A"
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(8.dp)
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clickable(onClick = { showTokenDialog = true; selectedToken = t })
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
                        ) {


                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    t.symbol,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1
                                )

                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = t.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1
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
