package com.example.ethktprototype

import ERC20
import android.app.Application
import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import com.example.ethktprototype.data.GraphQLData
import com.example.ethktprototype.data.GraphQLQueries
import com.example.ethktprototype.data.GraphQLResponse
import com.example.ethktprototype.data.NftValue
import com.example.ethktprototype.data.PortfolioData
import com.example.ethktprototype.data.TokenBalance
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.tx.Transfer
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.math.BigDecimal
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.URL

object JsonUtils {
    val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
    }
}


class WalletRepository(private val application: Application) : IWalletRepository {
    private val context = application.applicationContext
    private val sharedPreferences =
        context.getSharedPreferences("WalletPrefs", Context.MODE_PRIVATE)
    private val walletAddressKey = "wallet_address"
    private val _selectedNetwork = mutableStateOf(Network.MUMBAI_TESTNET)
    private val selectedNetwork: MutableState<Network> = _selectedNetwork
    private val mnemonicLoaded = MutableLiveData<Boolean>()

    override fun storeMnemonic(mnemonic: String) {
        encryptMnemonic(context, mnemonic)

        val sharedPreferences = context.getSharedPreferences("WalletPrefs", Context.MODE_PRIVATE)
        val encryptedMnemonic = getEncryptedMnemonic(context)
        val encodedMnemonic = Base64.encodeToString(encryptedMnemonic, Base64.DEFAULT)
        sharedPreferences.edit().putString("encrypted_mnemonic", encodedMnemonic).apply()

        mnemonicLoaded.value = true
    }

    override fun loadMnemonicFromPrefs(): String? {
        val prefs = context.getSharedPreferences("WalletPrefs", Context.MODE_PRIVATE)
        val storedMnemonic = prefs.getString("encrypted_mnemonic", null)
        mnemonicLoaded.value = storedMnemonic != null
        return storedMnemonic
    }

    fun updateSelectedNetwork(network: Network): Network {
        selectedNetwork.value = network
        val jsonNetwork = Json.encodeToString(network)
        sharedPreferences.edit().putString("SELECTED_NETWORK_NAME", jsonNetwork).apply()
        return network
    }

    override fun getLastSelectedNetwork(): Network {
        val json = sharedPreferences.getString("SELECTED_NETWORK_NAME", null)
        val jsonReturn = json?.let { Json.decodeFromString<Network>(it) }
        return jsonReturn ?: Network.MUMBAI_TESTNET
    }

    fun storeWallet(walletAddress: String) {
        // Store the wallet address in SharedPreferences
        sharedPreferences.edit {
            putString(walletAddressKey, walletAddress)
            apply()
        }
    }

    override fun removeAllWalletData() {
        sharedPreferences.edit().clear().apply()
    }

    override fun getMnemonic(): String? {
        // Decrypt the mnemonic
        return getDecryptedMnemonic(context)
    }

    override fun clearTokenBlocklist(): List<TokenBalance> {
        sharedPreferences.edit().putString("TOKEN_BLOCKLIST", null).apply()
        return getTokenBlocklist()
    }

    override fun updateTokenBlockList(tokenBlocklist: List<TokenBalance>) {
        val jsonTokenBlocklist = Json.encodeToString(tokenBlocklist)
        sharedPreferences.edit { putString("TOKEN_BLOCKLIST", jsonTokenBlocklist).apply() }
    }

    override fun getTokenBlocklist(): List<TokenBalance> {
        val json = sharedPreferences.getString("TOKEN_BLOCKLIST", null)

        return if (!json.isNullOrEmpty()) {
            try {
                val jsonReturn = Json.decodeFromString<List<TokenBalance>>(json)
                jsonReturn
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

    }

    private fun getGasPrices(): Pair<BigInteger, BigInteger> {
        val endpoint = "https://gasstation-mainnet.matic.network/v2"
        val url = URL(endpoint)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val responseString = connection.inputStream.bufferedReader().use { it.readText() }
            val jsonResponse = JSONObject(responseString)
            val standard = jsonResponse.getJSONObject("standard")
            val maxPriorityFee = standard.getDouble("maxPriorityFee")
            val maxFee = standard.getDouble("maxFee")
            val maxFeeWei = Convert.toWei(BigDecimal(maxFee), Convert.Unit.GWEI).toBigInteger()
            val maxPriorityFeeWei =
                Convert.toWei(BigDecimal(maxPriorityFee), Convert.Unit.GWEI).toBigInteger()
            return Pair(maxPriorityFeeWei, maxFeeWei)
        } else {
            throw Exception("Failed to retrieve data from $endpoint. Response code: $responseCode")
        }
    }

    override fun fetchNfts(
        walletAddress: String,
        selectedNetwork: Network
    ): List<NftValue> {
        val envVars = EnvVars()
        val currentTime = System.currentTimeMillis() / 1000
        val sharedPreferences = getBalancesSharedPreferences(application)
        val cacheExpirationTime = getNftCacheExpirationTime(sharedPreferences)
        val cachedBalances = getUserNftBalances(application, selectedNetwork.displayName)

        return if (cachedBalances.isNotEmpty() && cacheExpirationTime > currentTime) {
            cachedBalances
        } else {
            val client = OkHttpClient()

            val requestBody = GraphQLQueries.getNftUsersTokensQuery(
                owners = listOf(walletAddress),
                network = selectedNetwork.name,
                first = 10
            )

            val request = Request.Builder()
                .url("https://public.zapper.xyz/graphql")
                .header("x-zapper-api-key", envVars.zapperApiKey)
                .post(requestBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            return try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                val jsonResponse = JsonUtils.json.decodeFromString<GraphQLResponse<GraphQLData>>(responseBody ?: "")
                val nftNodes = jsonResponse.data?.nftUsersTokens?.edges

                val nftList = nftNodes?.map { edge ->
                    val node = edge.node
                    val collection = node.collection
                    val image = node.mediasV3?.images?.edges?.firstOrNull()?.node?.original

                    NftValue(
                        contractAddress = collection.address,
                        contractName = collection.name ?: "Unknown",
                        image = image ?: ""
                    )
                } ?: emptyList()

                sharedPreferences.edit().putLong("CACHE_EXPIRATION_TIME_NFT", currentTime).apply()
                cacheUserNftBalance(nftList, application, selectedNetwork.displayName)

                nftList
            } catch (e: Exception) {
                Log.e("fetchNfts", "Error fetching NFT data", e)
                emptyList()  // Return empty list in case of error
            }
        }
    }


    override fun fetchBalances(
        addresses: String,
        first: Int,
    ): Pair<Double, List<TokenBalance>> {
        val currentTime = System.currentTimeMillis() / 1000
        val sharedPreferences = getBalancesSharedPreferences(application)
        val cacheExpirationTime = getCacheExpirationTime(sharedPreferences)
        val cachedBalances = getUserBalances(application)
        val cachedTotalBalance = getTotalBalanceUSD(application)
        val envVars = EnvVars()

        if (cachedBalances.isNotEmpty() && cacheExpirationTime > currentTime) {
            return Pair(cachedTotalBalance, cachedBalances)
        }

        val client = OkHttpClient()

        val json = GraphQLQueries.getTokenBalancesQuery(
            addresses = listOf(addresses),
            first = 10
        )
        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("https://public.zapper.xyz/graphql")
            .header("x-zapper-api-key", envVars.zapperApiKey)
            .post(requestBody)
            .build()

        return try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            val graphQLResponse = json.let {
                JsonUtils.json.decodeFromString<GraphQLResponse<PortfolioData>>(
                    responseBody!!
                )
            }

            if (graphQLResponse.data == null) {
                Log.e("fetchBalances", "Error fetching balances: ${graphQLResponse.errors}")
                return Pair(0.0, emptyList())
            }

            // Extract the totalBalanceUSD
            val totalBalanceUSD = graphQLResponse.data.portfolioV2.tokenBalances.totalBalanceUSD

            val balances = graphQLResponse.data.portfolioV2.tokenBalances.byToken.edges.map { edge ->
                TokenBalance(
                    contractAddress = edge.node.tokenAddress,
                    balance = edge.node.balance.toString(),
                    name = edge.node.name,
                    symbol = edge.node.symbol,
                    decimals = 18,
                    tokenIcon = edge.node.imgUrlV2,
                    balanceUSD = edge.node.balanceUSD,
                    networkName = edge.node.network.name
                )
            }

            cacheUserBalance(balances, application)
            cacheTotalBalanceUSD(totalBalanceUSD, application)

            sharedPreferences.edit().putLong("CACHE_EXPIRATION_TIME", currentTime + 3600).apply()

            Pair(totalBalanceUSD, balances)
        } catch (e: Exception) {
            Log.e("fetchTokenBalances", "Error fetching balances", e)
            Pair(cachedTotalBalance, cachedBalances)
        }
    }


    override suspend fun sendTokens(
        credentials: Credentials,
        contractAddress: String,
        toAddress: String,
        value: BigDecimal
    ): String {
        val web3jService = Web3jService.build(selectedNetwork.value)
        val contract = ERC20.load(contractAddress, web3jService, credentials, DefaultGasProvider())
        val decimals = contract.decimals()
        val gasPrice = web3jService.ethGasPrice().send().gasPrice

        val function = Function(
            "transfer",
            listOf(
                Address(toAddress),
                Uint256(
                    Convert.toWei(
                        value.multiply(BigDecimal.TEN.pow(decimals.toInt())),
                        Convert.Unit.WEI
                    ).toBigInteger()
                )
            ),
            emptyList()
        )
        val nonce: BigInteger = web3jService.ethGetTransactionCount(
            credentials.address,
            DefaultBlockParameterName.LATEST
        )
            .send().transactionCount

        // Encode the function call to get the data that needs to be sent in the transaction
        val encodedFunction = FunctionEncoder.encode(function)

        val ethEstimateGas = web3jService.ethEstimateGas(
            Transaction.createFunctionCallTransaction(
                //TODO: Fix this line causing errors - testing using burn address here for now
                "0x000000000000000000000000000000000000dEaD",
                nonce,
                gasPrice,
                null,
                "0x000000000000000000000000000000000000dEaD",
                encodedFunction
            )
        ).send()

        val gasLimit = ethEstimateGas.amountUsed.plus(BigInteger.valueOf(40000))

        val (maxPriorityFeeWei, maxFeeWei) = getGasPrices()


        return withContext(Dispatchers.IO) {
            try {

                if (contractAddress == "0x0000000000000000000000000000000000001010") {
                    val transfer = Transfer.sendFundsEIP1559(
                        web3jService,
                        credentials,
                        toAddress,
                        Convert.toWei(
                            value.multiply(BigDecimal.TEN.pow(decimals.toInt())),
                            Convert.Unit.WEI
                        ),
                        Convert.Unit.WEI,
                        gasLimit,
                        maxPriorityFeeWei,
                        maxFeeWei
                    ).sendAsync().get()

                    if (transfer.isStatusOK) {
                        transfer.transactionHash
                    } else {
                        throw RuntimeException("EIP1559 Transaction failed: ${transfer.logs}")
                    }
                } else {
                    // Create a raw transaction object
                    val transaction = RawTransaction.createTransaction(
                        nonce,
                        gasPrice,
                        gasLimit,
                        contractAddress,
                        encodedFunction
                    )

                    // my attempt to fix only EIP allowed over RPC
                    val signedT = TransactionEncoder.signMessage(
                        transaction,
                        selectedNetwork.value.chainId,
                        credentials
                    )

                    // Convert the signed transaction to hex format
                    val hexValue = Numeric.toHexString(signedT)

                    // Send the signed transaction to the network
                    val transactionResponse =
                        web3jService.ethSendRawTransaction(hexValue).sendAsync().get()

                    // Check if the transaction was successful or not
                    if (transactionResponse.hasError()) {
                        throw RuntimeException("Transaction failed: ${transactionResponse.error.message}")
                    } else {
                        transactionResponse.transactionHash
                    }
                }

            } catch (e: Exception) {
                Log.e("send", "transaction failed: ${e.message}")
                Sentry.captureException(e)
                throw e
            }
        }
    }
}