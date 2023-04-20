package com.example.ethktprototype

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.common.reflect.TypeToken
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class TokenBalance(
    val contractAddress: String,
    @Contextual val balance: String,
    val name: String,
    val symbol: String,
    val decimals: Int,
    val tokenIcon: String
)

@Serializable
data class NftValue(
    val contractAddress: String,
    val contractName: String,
    val image: String
)

fun getBalancesSharedPreferences(application: Application): SharedPreferences {
    return application.getSharedPreferences("WalletPrefs", Context.MODE_PRIVATE)
}

fun getCacheExpirationTime(sharedPreferences: SharedPreferences): Long {
    val CACHE_EXPIRATION_INTERVAL = 300 // 5 minutes in seconds
    val CACHE_EXPIRATION_TIME_KEY = "CACHE_EXPIRATION_TIME"
    return sharedPreferences.getLong(CACHE_EXPIRATION_TIME_KEY, 0L) + CACHE_EXPIRATION_INTERVAL
}

fun getNftCacheExpirationTime(sharedPreferences: SharedPreferences): Long {
    val CACHE_EXPIRATION_INTERVAL = 30000 // 5 minutes in seconds
    val NFT_CACHE_EXPIRATION_TIME_KEY = "CACHE_EXPIRATION_TIME_NFT"
    return sharedPreferences.getLong(NFT_CACHE_EXPIRATION_TIME_KEY, 0L) + CACHE_EXPIRATION_INTERVAL
}

fun getTokenBalancesSharedPreferencesKey(selectedNetwork: String): String {
    val TOKEN_BALANCES_KEY = "TOKEN_BALANCES"
    return "${selectedNetwork}_$TOKEN_BALANCES_KEY"
}

fun getNftBalancesSharedPreferencesKey(selectedNetwork: String): String {
    val NFT_BALANCES_KEY = "NFT_BALANCES"
    return "${selectedNetwork}_$NFT_BALANCES_KEY"
}

fun cacheUserBalance(tokenBalance: List<TokenBalance>, application: Application, selectedNetwork: String) {
    val sharedPreferences = getBalancesSharedPreferences(application)
    val existingBalances = getUserBalances(application, selectedNetwork)
    existingBalances.toMutableList().clear()

    val json = Json.encodeToString(tokenBalance)

    sharedPreferences.edit().putString(getTokenBalancesSharedPreferencesKey(selectedNetwork), json).apply()
}

fun cacheUserNftBalance(nftBalance: List<NftValue>, application: Application, selectedNetwork: String) {
    val sharedPreferences = getBalancesSharedPreferences(application)
    val existingBalances = getUserNftBalances(application, selectedNetwork)
    existingBalances.toMutableList().clear()

    val json = Json.encodeToString(nftBalance)

    sharedPreferences.edit().putString(getNftBalancesSharedPreferencesKey(Network.POLYGON_MAINNET.displayName), json).apply()
}

fun getUserNftBalances(application: Application, selectedNetwork: String): List<NftValue> {
    val sharedPreferences = getBalancesSharedPreferences(application)
    val cacheExpirationTime = getCacheExpirationTime(sharedPreferences)
    val currentTime = System.currentTimeMillis() / 1000

    val json = sharedPreferences.getString(getNftBalancesSharedPreferencesKey(Network.POLYGON_MAINNET.displayName), null)

    return if (!json.isNullOrEmpty() && cacheExpirationTime > currentTime) {
        try {
            val type = object : TypeToken<List<NftValue>>() {}.type
            val jsonReturn = Json.decodeFromString<List<NftValue>>(json)
            jsonReturn
        } catch (e: Exception) {
            emptyList()
        }
    } else {
        emptyList()
    }
}

fun getUserBalances(application: Application, selectedNetwork: String): List<TokenBalance> {
    val sharedPreferences = getBalancesSharedPreferences(application)
    val cacheExpirationTime = getCacheExpirationTime(sharedPreferences)
    val currentTime = System.currentTimeMillis() / 1000

    val json = sharedPreferences.getString(getTokenBalancesSharedPreferencesKey(selectedNetwork), null)

    return if (!json.isNullOrEmpty() && cacheExpirationTime > currentTime) {
        try {
            val type = object : TypeToken<List<TokenBalance>>() {}.type
            val jsonReturn = Json.decodeFromString<List<TokenBalance>>(json)
            jsonReturn
        } catch (e: Exception) {
            emptyList()
        }
    } else {
        emptyList()
    }
}



