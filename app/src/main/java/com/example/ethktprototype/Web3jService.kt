package com.example.ethktprototype

import android.util.Log
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService

object Web3jService {

    fun build(selectedNetwork: Network): Web3j {
        Log.d("web3j", selectedNetwork.url)
        return Web3j.build(HttpService(selectedNetwork.url))
    }

}

enum class Network(val displayName: String, val url: String) {
    POLYGON_MAINNET("Polygon", "https://polygon-rpc.com/"),
    MUMBAI_TESTNET("Mumbai Testnet", "https://rpc-mumbai.maticvigil.com/")
}

val networks = listOf(
    Pair(Network.POLYGON_MAINNET.displayName, Network.POLYGON_MAINNET.url),
    Pair(Network.MUMBAI_TESTNET.displayName, Network.MUMBAI_TESTNET.url)
)


