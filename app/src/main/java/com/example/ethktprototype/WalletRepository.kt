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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
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

class WalletRepository(private val application: Application) : IWalletRepository {
    val context = application.applicationContext
    private val sharedPreferences =
        context.getSharedPreferences("WalletPrefs", Context.MODE_PRIVATE)
    private val walletAddressKey = "wallet_address"
    private val _selectedNetwork = mutableStateOf(Network.MUMBAI_TESTNET)
    val selectedNetwork: MutableState<Network> = _selectedNetwork
    val mnemonicLoaded = MutableLiveData<Boolean>()

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
        Log.d("network", "updating _selectedNetwork to: $network")
        selectedNetwork.value = network
        sharedPreferences.edit().putString("SELECTED_NETWORK_NAME", network.displayName).apply()
        return network
    }

    fun storeWallet(walletAddress: String) {
        // Store the wallet address in SharedPreferences
        sharedPreferences.edit {
            putString(walletAddressKey, walletAddress)
            apply()
        }
    }

    override fun getMnemonic(): String? {
        // Decrypt the mnemonic
        return getDecryptedMnemonic(context)
    }

    fun getGasPrices(): Pair<BigInteger, BigInteger> {
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

    override suspend fun sendTokens(credentials: Credentials, contractAddress: String, toAddress: String, value: BigDecimal): String {
        val web3jService = Web3jService.build(selectedNetwork.value)
        val contract = ERC20.load(contractAddress, web3jService, credentials, DefaultGasProvider())
        val decimals = contract.decimals()
        val gasPrice = web3jService.ethGasPrice().send().gasPrice

        val function = Function(
            "transfer",
            listOf(Address(toAddress), Uint256(Convert.toWei(value.multiply(BigDecimal.TEN.pow(decimals.toInt())), Convert.Unit.WEI).toBigInteger())),
            emptyList()
        )
        val nonce: BigInteger = web3jService.ethGetTransactionCount(credentials.address, DefaultBlockParameterName.LATEST)
            .send().transactionCount

        // Encode the function call to get the data that needs to be sent in the transaction
        val encodedFunction = FunctionEncoder.encode(function)

        val ethEstimateGas = web3jService.ethEstimateGas(
            Transaction.createFunctionCallTransaction(
                credentials.address, nonce, gasPrice, null, toAddress, encodedFunction
            )
        ).send()

        val gasLimit = ethEstimateGas.amountUsed.plus(BigInteger.valueOf(40000))

        val (maxPriorityFeeWei, maxFeeWei) = getGasPrices()


        return withContext(Dispatchers.IO) {
            try {

                if(contractAddress == "0x0000000000000000000000000000000000001010") {
                    val transfer = Transfer.sendFundsEIP1559(
                        web3jService,
                        credentials,
                        toAddress,
                        Convert.toWei(value.multiply(BigDecimal.TEN.pow(decimals.toInt())), Convert.Unit.WEI),
                        Convert.Unit.WEI,
                        gasLimit,
                        maxPriorityFeeWei,
                        maxFeeWei
                    ).sendAsync().get()

                    if(transfer.isStatusOK) {
                        Log.d("send", "EIP1559 transaction successful, hash: ${transfer.transactionHash}")
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
                    val signedT = TransactionEncoder.signMessage(transaction, selectedNetwork.value.chainId, credentials)

                    // Convert the signed transaction to hex format
                    val hexValue = Numeric.toHexString(signedT)

                    // Send the signed transaction to the network
                    val transactionResponse = web3jService.ethSendRawTransaction(hexValue).sendAsync().get()

                    // Check if the transaction was successful or not
                    if (transactionResponse.hasError()) {
                        Log.d("send", "transaction failed: ${transactionResponse.error.message}")
                        Log.d("send", "full transaction: ${transactionResponse.error.data}")
                        throw RuntimeException("Transaction failed: ${transactionResponse.error.message}")
                    } else {
                        Log.d("send", "transaction successful, hash: ${transactionResponse.transactionHash}")
                        transactionResponse.transactionHash
                    }
                }

            } catch (e: Exception) {
                Log.e("send", "transaction failed: ${e.message}")
                throw e
            }
        }
    }


    override suspend fun getTokens(
        walletAddress: String,
        contractAddresses: List<String>,
        selectedNetwork: Network
    ): List<TokenBalance> {
        val currentTime = System.currentTimeMillis() / 1000
        val sharedPreferences = getBalancesSharedPreferences(application)
        val cacheExpirationTime = getCacheExpirationTime(sharedPreferences)
        val cachedBalances = getUserBalances(application, selectedNetwork.displayName)

        return if (cachedBalances.isNotEmpty() && cacheExpirationTime > currentTime) {
            // Return the cached balances if they are still valid
            Log.d("newGetTokens", "using cached Balances $cachedBalances")
            cachedBalances
        } else {
            // Fetch the balances from the network and update the cache
            val web3jService = Web3jService.build(selectedNetwork)
            Log.d("newGetTokens", "Making network calls")

            val mnemonic = getMnemonic()
            val credentials = if (!mnemonic.isNullOrEmpty()) {
                WalletUtils.loadBip39Credentials(null, mnemonic)
            } else {
                null
            }

            val balances = withContext(Dispatchers.IO) {
                val tokenBalances =
                    getTokenBalances(walletAddress, contractAddresses, web3jService, credentials)
                cacheUserBalance(tokenBalances, application, selectedNetwork.displayName)
                tokenBalances
            }

            // Update the cache expiration time
            sharedPreferences.edit().putLong("CACHE_EXPIRATION_TIME", currentTime).apply()

            balances
        }
    }

    override fun getTokenBalances(
        walletAddress: String,
        contractAddresses: List<String>,
        web3j: Web3j,
        credentials: Credentials?
    ): List<TokenBalance> {
        return contractAddresses.mapNotNull { address ->
            try {
                val contract = ERC20.load(address, web3j, credentials!!, DefaultGasProvider())
                val balance = contract.balanceOf(walletAddress)
                if (balance == BigInteger.valueOf(0)) {
                    null // Skip this token if balance is 0
                } else {
                    TokenBalance(
                        address,
                        balance.toString(),
                        contract.name(),
                        contract.symbol()
                    )
                }
            } catch (e: Exception) {
                if (e.message?.contains("Invalid BigInteger") == true) {
                    // Handle "Invalid BigInteger" error
                    Log.e("Tokens", "Invalid BigInteger for $address: ${e.message}")
                    null
                } else {
                    // Log other errors and continue processing
                    Log.e(
                        "Tokens",
                        "Error fetching token balance for $address: ${e.message}"
                    )
                    null
                }
            }
        }
    }
}