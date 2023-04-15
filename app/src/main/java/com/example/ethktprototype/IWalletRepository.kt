package com.example.ethktprototype

import org.web3j.crypto.Credentials
import java.math.BigDecimal

interface IWalletRepository {
    fun storeMnemonic(mnemonic: String)

    fun loadMnemonicFromPrefs(): String?

    fun getLastSelectedNetwork(): Network

    fun removeAllWalletData()

    fun fetchBalances(
        chainName: String,
        walletAddress: String,
        selectedNetwork: Network
    ): List<TokenBalance>

    fun getMnemonic(): String?

    suspend fun sendTokens(
        credentials: Credentials,
        contractAddress: String,
        toAddress: String,
        value: BigDecimal
    ): String
}
