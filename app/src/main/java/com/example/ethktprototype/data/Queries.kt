package com.example.ethktprototype.data

import org.json.JSONObject
import org.json.JSONArray

object GraphQLQueries {
    fun getTokenBalancesQuery(
        addresses: List<String>,
        first: Int = 10
    ): JSONObject {
        return JSONObject().apply {
            put("query", """
                query TokenBalances(${"$"}addresses: [Address!]!, ${"$"}first: Int) {
                    portfolioV2(addresses: ${"$"}addresses) {
                        tokenBalances {
                            totalBalanceUSD
                            byToken(first: ${"$"}first) {
                                totalCount
                                edges {
                                    node {
                                        symbol
                                        tokenAddress
                                        balance
                                        balanceUSD
                                        price
                                        imgUrlV2
                                        name
                                        network {
                                            name
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            """.trimIndent())

            put("variables", JSONObject().apply {
                put("addresses", JSONArray().apply {
                    addresses.forEach { put(it) }
                })
                put("first", first)
            })
        }
    }

    fun getNftUsersTokensQuery(
        owners: List<String>,
        network: String,
        first: Int = 10
    ): JSONObject {
        val graphQLQuery = """
            query(${"$"}owners: [Address!]!, ${"$"}network: Network, ${"$"}first: Int) {
                nftUsersTokens(owners: ${"$"}owners, network: ${"$"}network, first: ${"$"}first) {
                    edges {
                        node {
                            id
                            tokenId
                            name
                            collection {
                                name
                                address
                            }
                            mediasV3 {
                                images {
                                    edges {
                                        node {
                                            original
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        """.trimIndent()

        return JSONObject().apply {
            put("query", graphQLQuery)
            put("variables", JSONObject().apply {
                put("owners", JSONArray().apply {
                    owners.forEach { put(it) }
                })
                put("network", network)
                put("first", first)
            })
        }
    }



}