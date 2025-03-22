package com.example.ethktprototype.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ethktprototype.data.TokenBalance
import java.math.BigDecimal
import java.text.DecimalFormat

@Composable
fun TokenDropdown(
    tokens: List<TokenBalance>,
    selectedToken: TokenBalance?,
    updateSelectedToken: (TokenBalance) -> Unit,
) {
    val expanded = remember { mutableStateOf(false) }
    val decimalFormatBalance = DecimalFormat("#.#######")
    val decimalUsdFormatBalance = DecimalFormat("##.##")

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Select Token",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = { expanded.value = true })
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                if (selectedToken != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = selectedToken.tokenIcon,
                                contentDescription = "Token Icon",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(35.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(
                                text = selectedToken.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )

                            val balanceInEth = selectedToken.balance.toBigDecimal()
                                .divide(BigDecimal.TEN.pow(selectedToken.decimals))
                            val formattedBalance = decimalFormatBalance.format(balanceInEth)

                            Text(
                                text = "$formattedBalance ${selectedToken.symbol}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$${decimalUsdFormatBalance.format(selectedToken.balanceUSD)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )

                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Select token",
                            modifier = Modifier.padding(start = 4.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                        )
                    }
                } else {
                    Text(
                        text = "Select a token",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Select token",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )
                }
            }
        }

        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false },
            modifier = Modifier
                .heightIn(max = 350.dp)
                .width(with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp - 32.dp })
        ) {
            tokens.forEachIndexed { index, token ->
                val balanceInEth =
                    token.balance.toBigDecimal().divide(BigDecimal.TEN.pow(token.decimals))
                val formattedBalance = decimalFormatBalance.format(balanceInEth)
                val formattedUsdBalance = decimalUsdFormatBalance.format(token.balanceUSD)

                DropdownMenuItem(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        updateSelectedToken(token)
                        expanded.value = false
                    },
                    text = {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()

                            ) {

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {

                                    Box(
                                        modifier = Modifier.size(40.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = token.tokenIcon,
                                            contentDescription = "Token Icon",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.size(35.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column {
                                        Text(
                                            text = token.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            overflow = TextOverflow.Ellipsis,
                                            maxLines = 1
                                        )


                                        Text(
                                            text = "$formattedBalance ${token.symbol}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = 0.8f
                                            )
                                        )
                                    }
                                }


                                Text(
                                    text = "$${formattedUsdBalance}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                )

                if (index < tokens.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                }
            }

        }
    }
}
