package com.example.ethktprototype

import com.example.ethktprototype.data.NftValue
import com.example.ethktprototype.data.TokenBalance

data class WalletUiState(
    val walletAddress: String = "",
    val userEnsName: String = "",
    val tokens: List<TokenBalance> = emptyList(),
    val nfts: List<NftValue> = emptyList(),
    val nftsLoading: Boolean = false,
    val totalBalanceUSD: Double = 0.0,
    val transactionHash: String = "",
    val selectedToken: TokenBalance? = null,
    val selectedNetwork: Network = Network.ETH_MAINNET,
    val isTokensLoading: Boolean = false,
    val isNftsLoading: Boolean = false,
    val showPayDialog: Boolean = false,
    val showTokenBottomSheet: Boolean = false,
    val showWalletModal: Boolean = false,
    val showSuccessModal: Boolean = false,
    val toAddress: String = "",
    val sentAmount: Double = 0.0,
    val sentCurrency: String = "",
    val tokenBlocklist: List<TokenBalance> = emptyList(),
    val hash: String = "",
    val ens: String = "",
    val mnemonicLoaded: Boolean = false,
    val tokensBlocked: List<TokenBalance> = emptyList()
)
