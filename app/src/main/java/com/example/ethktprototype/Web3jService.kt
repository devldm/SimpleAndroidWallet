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

enum class Network(val displayName: String, val url: String, val chainId: Long) {
    POLYGON_MAINNET("Polygon", "https://polygon-rpc.com/", 137),
    MUMBAI_TESTNET("Mumbai Testnet", "https://rpc-mumbai.maticvigil.com/",80001)
}


