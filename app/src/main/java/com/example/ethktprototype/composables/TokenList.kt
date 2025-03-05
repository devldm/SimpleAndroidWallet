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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ethktprototype.Network
import com.example.ethktprototype.data.TokenBalance
import com.example.ethktprototype.WalletViewModel
import java.math.BigDecimal
import java.text.DecimalFormat

@Composable
fun TokenList(
    selectedNetwork: Network,
    viewModel: WalletViewModel,
    tokens: List<TokenBalance>,
    tokenBlocklist: List<TokenBalance>,
    isTokensLoading: Boolean,
    showTokenBottomSheet: Boolean,
    selectedToken: TokenBalance?,
    onTokenSelected: (TokenBalance) -> Unit,
) {
    val decimalFormatBalance = DecimalFormat("#.#######")
    val decimalUsdFormatBalance = DecimalFormat("##.##")

    if (!isTokensLoading && tokens.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("No tokens found for this wallet")
        }
    }

    if (isTokensLoading) {
        Loading()
    }


    LazyColumn(Modifier.fillMaxHeight(), userScrollEnabled = true) {
        val nonBlockedTokens = tokens.filter { token -> !tokenBlocklist.contains(token) }

        items(nonBlockedTokens.size) { token ->
            val t = nonBlockedTokens[token]

            if (showTokenBottomSheet && selectedToken == t) {
                TokenBottomSheet(
                    token = t,
                    network = selectedNetwork,
                    viewModel = viewModel,
                    onDismiss = { viewModel.setShowTokenBottomSheet(false) },
                )
            }

            val balanceInEth = t.balance.toBigDecimal().divide(BigDecimal.TEN.pow(t.decimals))
            val formattedBalance = decimalFormatBalance.format(balanceInEth) ?: "N/A"
            val formattedUsdBalance = decimalUsdFormatBalance.format(t.balanceUSD) ?: "N/A"

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
                        .clickable(onClick = {
                            viewModel.setShowTokenBottomSheet(true); onTokenSelected(
                            t
                        )
                        })
                ) {
                    Row(

                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
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

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Text(
                                    text = t.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "$formattedBalance ${t.symbol}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Text(
                            text = "$${formattedUsdBalance}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            modifier = Modifier.padding(end = 10.dp)
                        )
                    }
                }

            }
        }

    }
}
