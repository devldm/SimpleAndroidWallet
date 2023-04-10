package com.example.ethktprototype

import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import java.math.BigDecimal

interface IWalletRepository {
    suspend fun getTokens(
        walletAddress: String,
        contractAddresses: List<String>,
        selectedNetwork: Network
    ): List<TokenBalance>

    fun getTokenBalances(
        walletAddress: String,
        contractAddresses: List<String>,
        web3j: Web3j,
        credentials: Credentials?
    ): List<TokenBalance>

    fun storeMnemonic(mnemonic: String)

    fun loadMnemonicFromPrefs(): String?

    fun getLastSelectedNetwork(): Network

    fun removeAllWalletData()

    fun getMnemonic(): String?

    suspend fun sendTokens(
        credentials: Credentials,
        contractAddress: String,
        toAddress: String,
        value: BigDecimal
    ): String
}
