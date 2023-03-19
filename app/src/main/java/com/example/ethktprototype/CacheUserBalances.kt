package com.example.ethktprototype

import android.app.Application
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

fun cacheUserBalance(tokenBalance: TokenBalance, application: Application, selectedNetwork: String, viewModel: WalletViewModel) {
    val sharedPreferences = viewModel.getBalancesSharedPreferences(application)
    Log.d("cache", "cached User balance $tokenBalance with key ${viewModel.getTokenBalancesSharedPreferencesKey(selectedNetwork)}")
    val existingBalances = getUserBalances(application, selectedNetwork, viewModel)

    Log.d("cache", "exisitingBalances: $existingBalances")
    val newBalances = existingBalances.toMutableList()
    newBalances.removeAll { it.contractAddress == tokenBalance.contractAddress }
    newBalances.add(tokenBalance)

    val json = Json.encodeToString(newBalances)
    Log.d("cache", "newBalances: $newBalances")

    sharedPreferences.edit().putString(viewModel.getTokenBalancesSharedPreferencesKey(selectedNetwork), json).apply()
}

fun getUserBalances(application: Application, selectedNetwork: String, viewModel: WalletViewModel): List<TokenBalance> {
    val sharedPreferences = viewModel.getBalancesSharedPreferences(application)
    val cacheExpirationTime = viewModel.getCacheExpirationTime(sharedPreferences)
    val currentTime = System.currentTimeMillis() / 1000

    val json = sharedPreferences.getString(viewModel.getTokenBalancesSharedPreferencesKey(selectedNetwork), null)
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



