package com.example.ethktprototype

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class CovalentResponse(
    val data: CovalentData,
    val error: Boolean,
    val error_message: String?,
    val error_code: Int?
)

@Serializable
data class CovalentData(
    val address: String,
    val updated_at: String,
    val next_update_at: String,
    val quote_currency: String,
    val chain_id: Int,
    val chain_name: String,
    val items: List<CovalentItem>,
    @Contextual val pagination: Any?
)

@Serializable
data class CovalentItem(
    val contract_decimals: Int,
    val contract_name: String,
    val contract_ticker_symbol: String,
    val contract_address: String,
    val supports_erc: List<String>,
    val logo_url: String?,
    val last_transferred_at: String?,
    val native_token: Boolean,
    val type: String,
    val balance: String,
    val balance_24h: String?,
    val quote_rate: Double?,
    val quote_rate_24h: Double?,
    val quote: Double,
    val quote_24h: Double?,
    @Contextual val nft_data: Any?
)

