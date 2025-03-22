package com.example.ethktprototype

import com.example.ethktprototype.data.TokenBalance
import com.example.ethktprototype.data.NftValue
import org.web3j.crypto.Credentials
import java.math.BigDecimal

interface IWalletRepository {
    fun storeMnemonic(mnemonic: String)

    fun loadMnemonicFromPrefs(): String?

    fun getLastSelectedNetwork(): Network

    fun removeAllWalletData()

    fun fetchBalances(addresses: String, first: Int): Pair<Double, List<TokenBalance>>

    fun getMnemonic(): String?

    suspend fun sendTokens(
        credentials: Credentials,
        contractAddress: String,
        toAddress: String,
        value: BigDecimal
    ): String

    fun clearTokenBlocklist(): List<TokenBalance>

    fun updateTokenBlockList(tokenBlocklist: List<TokenBalance>)

    fun getTokenBlocklist(): List<TokenBalance>

    fun fetchNfts(
        walletAddress: String,
        selectedNetwork: Network
    ): List<NftValue>
}
