package com.example.ethktprototype

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class CovalentNftResponse(
    val data: CovalentNftData,
    val error: Boolean,
    val error_message: String?,
    val error_code: Int?
)

@Serializable
data class CovalentNftData(
    val address: String,
    val updated_at: String,
    val items: List<CovalentNftItem>,
//    @Contextual val pagination: Any?
)

@Serializable
data class CovalentNftItem(
    val contract_name: String?,
    val contract_ticker_symbol: String?,
    val contract_address: String?,
    val supports_erc: List<String>,
    val last_transfered_at: String?,
    val balance: String,
    val balance_24h: String?,
    val type: String,
    @Contextual val nft_data: List<NftData>
)

@Serializable
data class NftData(
    val token_id: String,
    val token_url: String?,
    val original_owner: String?,
    val external_data: ExternalNftData?,
    val asset_cached: Boolean?,
    val image_cached: Boolean?
)

@Serializable
data class ExternalNftData(
    val name: String?,
    val description: String?,
    val asset_url: String?,
    val asset_file_extension: String?,
    val asset_mime_type: String?,
    val asset_size_bytes: String?,
    val image: String,
    val image_256: String?,
    val image_512: String?,
    val image_1024: String?,
    val animation_url: String?,
    val external_url: String?,
    val attributes: List<NftAttributes?>?,
)

@Serializable
data class NftAttributes(
    val trait_type: String,
    val value: String
)

