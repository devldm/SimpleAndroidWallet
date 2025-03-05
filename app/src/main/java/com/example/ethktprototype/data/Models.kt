package com.example.ethktprototype.data

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class GraphQLData(
    val nftUsersTokens: NftUsersTokens?
)

@Serializable
data class NftUsersTokens(
    val edges: List<NftEdge>?
)

@Serializable
data class NftEdge(
    val node: NftNode
)

@Serializable
data class NftNode(
    val id: String,
    val tokenId: String,
    val name: String,
    val collection: NftCollection,
    val mediasV3: MediasV3?
)

@Serializable
data class NftCollection(
    val name: String?,
    val address: String
)

@Serializable
data class MediasV3(
    val images: Images
)

@Serializable
data class Images(
    val edges: List<ImageEdge>
)

@Serializable
data class ImageEdge(
    val node: ImageNode
)

@Serializable
data class ImageNode(
    val original: String
)

@Serializable
data class GraphQLResponse<T>(
    val data: T? = null,
    val errors: List<GraphQLError>? = null
)

@Serializable
data class GraphQLError(
    val message: String,
    val locations: List<GraphQLLocation>? = null
)

@Serializable
data class GraphQLLocation(val line: Int, val column: Int)


@Serializable
data class PortfolioData(
    val portfolioV2: TokenBalances
)

@Serializable
data class TokenBalances(
    val tokenBalances: TokenBalancesData
)

@Serializable
data class TokenBalancesData(
    val totalBalanceUSD: Double,
    val byToken: TokenByToken
)

@Serializable
data class TokenByToken(
    val totalCount: Int,
    val edges: List<TokenEdge>
)

@Serializable
data class TokenEdge(
    val node: TokenNode
)

@Serializable
data class TokenNode(
    val symbol: String,
    val tokenAddress: String,
    val balance: Double,
    val balanceUSD: Double,
    val price: Double,
    val imgUrlV2: String,
    val name: String,
    val network: NetworkData
)

@Serializable
data class NetworkData(
    val name: String
)

@Serializable
data class TokenBalance(
    val contractAddress: String,
    @Contextual val balance: String,
    val name: String,
    val symbol: String,
    val decimals: Int,
    val tokenIcon: String,
    val balanceUSD: Double,
    val networkName: String,
)

@Serializable
data class NftValue(
    val contractAddress: String,
    val contractName: String,
    val image: String
)