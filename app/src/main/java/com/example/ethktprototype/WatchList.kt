//package com.example.ethktprototype
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.livedata.observeAsState
//import androidx.compose.runtime.rememberUpdatedState
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.LiveData
//import java.math.BigDecimal
//import java.math.RoundingMode
//
//@Composable
//fun WatchlistScreen(
//    viewModel: WalletViewModel,
//    onAddAddress: () -> Unit,
//    onRemoveAddress: (String) -> Unit
//) {
//    //val watchlist by viewModel.watchlist.observeAsState(initial = emptyList())
//
//
//    Surface(color = MaterialTheme.colorScheme.background) {
//        Column(
//            modifier = Modifier
//                .padding(16.dp)
//                .fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text(
//                text = "Watchlist",
//                style = MaterialTheme.typography.titleMedium,
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.padding(bottom = 16.dp)
//            )
//
//            // Button to add a new address to the watchlist
//            Button(
//                onClick = { onAddAddress() },
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("Add Address")
//            }
//
//            // Spacing between the "Add Address" button and the watchlist
//            Box(modifier = Modifier.height(16.dp))
//
//            // Display a list of addresses in the watchlist
//            if (watchlist.isNullOrEmpty()) {
//                Text(
//                    text = "No addresses added to the watchlist",
//                    style = MaterialTheme.typography.bodyMedium,
//                    modifier = Modifier.padding(top = 16.dp)
//                )
//            } else {
//                LazyColumn(
//                    modifier = Modifier.fillMaxWidth(),
//                    contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
//                ) {
//                    items(watchlist.size) { index ->
//                        var address = watchlist[index]
//                        WatchlistItem(
//                            address = address,
//                            balance = viewModel.getWalletBalance(address),
//                            onRemoveAddress = { onRemoveAddress(address) },
//                            modifier = Modifier.fillMaxWidth()
//                        )
//                        Divider()
//                    }
//                }
//            }
//        }
//    }
//}
//
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun WatchlistItem(
//    address: String,
//    balance: LiveData<String>,
//    onRemoveAddress: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Card(
//        modifier = modifier.padding(16.dp),
//        elevation = CardDefaults.cardElevation(),
//        shape = RoundedCornerShape(16.dp)
//    ) {
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text(
//                text = address,
//                style = MaterialTheme.typography.titleSmall,
//                fontWeight = FontWeight.Bold,
//                color = Color.Magenta
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            val balanceValue by balance.observeAsState(initial = null)
//            balanceValue?.let { balanceValue ->
//                Text(
//                    text = "Balance: $balanceValue MATIC",
//                    style = MaterialTheme.typography.titleMedium
//                )
//            } ?: Text(
//                text = "Balance: N/A",
//                style = MaterialTheme.typography.titleMedium
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            IconButton(
//                onClick = { onRemoveAddress() },
//                modifier = Modifier.align(Alignment.End)
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Delete,
//                    contentDescription = "Remove",
//                    tint = MaterialTheme.colorScheme.onBackground
//                )
//            }
//        }
//    }
//}
