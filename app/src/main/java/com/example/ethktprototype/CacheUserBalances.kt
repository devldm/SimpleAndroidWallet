package com.example.ethktprototype

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
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
    val symbol: String
)

fun getBalancesSharedPreferences(application: Application): SharedPreferences {
    return application.getSharedPreferences("Balances", Context.MODE_PRIVATE)
}

fun getCacheExpirationTime(sharedPreferences: SharedPreferences): Long {
    val CACHE_EXPIRATION_INTERVAL = 300 // 5 minutes in seconds
    val CACHE_EXPIRATION_TIME_KEY = "CACHE_EXPIRATION_TIME"
    return sharedPreferences.getLong(CACHE_EXPIRATION_TIME_KEY, 0L) + CACHE_EXPIRATION_INTERVAL
}

fun getTokenBalancesSharedPreferencesKey(selectedNetwork: String): String {
    val TOKEN_BALANCES_KEY = "TOKEN_BALANCES"
    return "${selectedNetwork}_$TOKEN_BALANCES_KEY"
}

fun cacheUserBalance(tokenBalance: TokenBalance, application: Application, selectedNetwork: String) {
    val sharedPreferences = getBalancesSharedPreferences(application)
    Log.d("cache", "cached User balance $tokenBalance with key ${getTokenBalancesSharedPreferencesKey(selectedNetwork)}")
    val existingBalances = getUserBalances(application, selectedNetwork)

    Log.d("cache", "exisitingBalances: $existingBalances")
    val newBalances = existingBalances.toMutableList()
    newBalances.removeAll { it.contractAddress == tokenBalance.contractAddress }
    newBalances.add(tokenBalance)

    val json = Json.encodeToString(newBalances)
    Log.d("cache", "newBalances: $newBalances")

    sharedPreferences.edit().putString(getTokenBalancesSharedPreferencesKey(selectedNetwork), json).apply()
}

fun getUserBalances(application: Application, selectedNetwork: String): List<TokenBalance> {
    val sharedPreferences = getBalancesSharedPreferences(application)
    val cacheExpirationTime = getCacheExpirationTime(sharedPreferences)
    val currentTime = System.currentTimeMillis() / 1000

    val json = sharedPreferences.getString(getTokenBalancesSharedPreferencesKey(selectedNetwork), null)
    Log.d("ggub", "$json")

    return if (json != null && cacheExpirationTime > currentTime) {
        try {
            val type = object : TypeToken<List<TokenBalance>>() {}.type
            val jsonReturn = Json.decodeFromString<List<TokenBalance>>(json)
            Log.d("getUserBalances", "$jsonReturn")

            jsonReturn
        } catch (e: Exception) {
            emptyList()
        }
    } else {
        emptyList()
    }
}



