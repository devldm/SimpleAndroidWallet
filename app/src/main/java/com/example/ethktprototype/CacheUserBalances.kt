package com.example.ethktprototype

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.example.ethktprototype.data.NftValue
import com.example.ethktprototype.data.TokenBalance
import com.google.common.reflect.TypeToken
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun getBalancesSharedPreferences(application: Application): SharedPreferences {
    return application.getSharedPreferences("WalletPrefs", Context.MODE_PRIVATE)
}

fun getCacheExpirationTime(sharedPreferences: SharedPreferences): Long {
    val CACHE_EXPIRATION_INTERVAL = 300 // 5 minutes in seconds
    val CACHE_EXPIRATION_TIME_KEY = "CACHE_EXPIRATION_TIME"
    return sharedPreferences.getLong(CACHE_EXPIRATION_TIME_KEY, 0L) + CACHE_EXPIRATION_INTERVAL
}

fun getNftCacheExpirationTime(sharedPreferences: SharedPreferences): Long {
    val CACHE_EXPIRATION_INTERVAL = 3000 // 50 minutes in seconds
    val NFT_CACHE_EXPIRATION_TIME_KEY = "CACHE_EXPIRATION_TIME_NFT"
    return sharedPreferences.getLong(NFT_CACHE_EXPIRATION_TIME_KEY, 0L) + CACHE_EXPIRATION_INTERVAL
}

fun getNftBalancesSharedPreferencesKey(selectedNetwork: String): String {
    val NFT_BALANCES_KEY = "NFT_BALANCES"
    return "${selectedNetwork}_$NFT_BALANCES_KEY"
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

fun getTokenBalancesSharedPreferencesKey(): String {
    return "TOKEN_BALANCES"
}

fun cacheUserBalance(tokenBalance: List<TokenBalance>, application: Application) {
    val sharedPreferences = getBalancesSharedPreferences(application)

    val json = Json.encodeToString(tokenBalance)
    sharedPreferences.edit().putString(getTokenBalancesSharedPreferencesKey(), json).apply()
}
fun cacheTotalBalanceUSD(totalBalanceUSD: Double, application: Application) {
    val sharedPreferences = getBalancesSharedPreferences(application)
    sharedPreferences.edit().putFloat("TOTAL_BALANCE_USD", totalBalanceUSD.toFloat()).apply()
}

fun getTotalBalanceUSD(application: Application): Double {
    val sharedPreferences = getBalancesSharedPreferences(application)
    return sharedPreferences.getFloat("TOTAL_BALANCE_USD", 0f).toDouble()
}

fun getUserBalances(application: Application): List<TokenBalance> {
    val sharedPreferences = getBalancesSharedPreferences(application)
    val cacheExpirationTime = getCacheExpirationTime(sharedPreferences)
    val currentTime = System.currentTimeMillis() / 1000

    val json = sharedPreferences.getString(getTokenBalancesSharedPreferencesKey(), null)

    return if (!json.isNullOrEmpty() && cacheExpirationTime > currentTime) {
        try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            emptyList()
        }
    } else {
        emptyList()
    }
}





