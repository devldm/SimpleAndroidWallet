package com.example.ethktprototype.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ethktprototype.data.NftValue
import com.example.ethktprototype.WalletViewModel

@Composable
fun NftList(nfts: List<NftValue>, viewModel: WalletViewModel) {
    val nftsLoading = viewModel.uiState.collectAsState().value.nftsLoading

    if (!nftsLoading && nfts.isEmpty()) {
        Column(
            Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("No NFTs found for this wallet")

        }
    }

    LazyVerticalGrid(columns = GridCells.Fixed(2)) {
        if (nftsLoading) {
            item {
                Loading()
            }
        } else {
            items(nfts.size) { nft ->
                val n = nfts[nft]
                Column(
                    modifier = Modifier
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
                                .padding(
                                    start = 16.dp,
                                    top = 16.dp,
                                    end = 16.dp,
                                    bottom = 16.dp
                                )
                                .clickable(onClick = {
                                    // TODO: Handle click on token
                                })
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = n.image,
                                    contentDescription = "NFT image",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                n.contractName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            )


                        }
                    }
                }

            }

        }

    }
}