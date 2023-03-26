package com.example.ethktprototype

import ERC20
import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.math.BigDecimal
import java.math.BigInteger

class WalletRepository(private val application: Application) : IWalletRepository {
    val context = application.applicationContext
    private val sharedPreferences = context.getSharedPreferences("WalletPrefs", Context.MODE_PRIVATE)
    private val walletAddressKey = "wallet_address"
    private val _selectedNetwork = mutableStateOf(Network.MUMBAI_TESTNET)
    val selectedNetwork: MutableState<Network> = _selectedNetwork
    val mnemonicLoaded = MutableLiveData<Boolean>()

    //private val web3jService = Web3jService.build(selectedNetwork.value)

    override fun storeMnemonic(mnemonic: String) {
        encryptMnemonic(context, mnemonic)

        val sharedPreferences = context.getSharedPreferences("WalletPrefs", Context.MODE_PRIVATE)
        val encryptedMnemonic = getEncryptedMnemonic(context)
        val encodedMnemonic = Base64.encodeToString(encryptedMnemonic, Base64.DEFAULT)
        sharedPreferences.edit().putString("encrypted_mnemonic", encodedMnemonic).apply()

        mnemonicLoaded.value = true
    }

    override fun loadMnemonicFromPrefs() {
        val prefs = context.getSharedPreferences("WalletPrefs", Context.MODE_PRIVATE)
        val storedMnemonic = prefs.getString("encrypted_mnemonic", null)
        mnemonicLoaded.value = storedMnemonic != null
        storedMnemonic
    }

    fun updateSelectedNetwork(network: Network): Network {
        Log.d("network", "updating _selectedNetwork to: $network")
        selectedNetwork.value = network
        sharedPreferences.edit().putString("SELECTED_NETWORK_NAME", network.displayName).apply()
        return network
    }

    fun storeWallet(walletAddress: String) {
        // Store the wallet address in SharedPreferences
        Log.d("storeWallet", "Storing: $walletAddress")
        sharedPreferences.edit {
            putString(walletAddressKey, walletAddress)
            apply()
        }
    }

    override fun getMnemonic(): String? {
        // Decrypt the mnemonic
        return getDecryptedMnemonic(context)
    }

    override suspend fun sendTokens(credentials: Credentials, contractAddress: String, toAddress: String, value: BigDecimal): String {
        Log.d("send", "sending $value tokens to $toAddress")
        Log.d("send", "network is: ${selectedNetwork.value}")
        val web3jService = Web3jService.build(selectedNetwork.value)

        return withContext(Dispatchers.IO) {
            try {
                // Get the nonce for the transaction
                val nonce: BigInteger = web3jService.ethGetTransactionCount(credentials.address, DefaultBlockParameterName.LATEST)
                    .send().transactionCount
                //val gasLimit = web3jService.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send().block.gasLimit

                val contract = ERC20.load(contractAddress, web3jService, credentials, DefaultGasProvider())
                val decimals = contract.decimals()
                Log.d("send", "${contract.name()} has $decimals decimals")
                // Get the gas price and gas limit
                val gasPrice = web3jService.ethGasPrice().send().gasPrice
                Log.d("send", "gasPrice is $gasPrice")

               // val gasLimit = DefaultGasProvider().gasLimit
               //TODO: try to add ethEstimateGasUsed for rudimentary simulation
                //Log.d("send", "gasLimit is $gasLimit")
                ///Log.d("send", "gasLimit should be $a")

                // Create a function call to transfer tokens
                val function = Function(
                    "transfer",
                    listOf(Address(toAddress), Uint256(Convert.toWei(value.multiply(BigDecimal.TEN.pow(decimals.toInt())), Convert.Unit.WEI).toBigInteger())),
                    emptyList()
                )

                // Encode the function call to get the data that needs to be sent in the transaction
                val encodedFunction = FunctionEncoder.encode(function)

                val ethEstimateGas = web3jService.ethEstimateGas(
                    Transaction.createFunctionCallTransaction(
                       credentials.address, nonce, gasPrice, null, toAddress, encodedFunction
                    )
                ).send()

                val gasLimit = ethEstimateGas.amountUsed.plus(BigInteger.valueOf(40000))

                //Log.d("send","is $gasLimit")

                //val gasLimit = DefaultGasProvider.GAS_LIMIT

                // Create a raw transaction object
                val transaction = RawTransaction.createTransaction(
                    nonce,
                    gasPrice,
                    gasLimit,
                    contractAddress,
                    encodedFunction
                )

                // Sign the transaction using the credentials
                // val signedTransaction = signTransaction(transaction, credentials)

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
            } catch (e: Exception) {
                Log.e("send", "transaction failed: ${e.message}")
                throw e
            }
        }
    }


     override suspend fun getTokens(walletAddress: String, contractAddresses: List<String>, selectedNetwork: Network): List<TokenBalance> {
        val currentTime = System.currentTimeMillis() / 1000
         Log.d("getTokens", "moment before crash selectedNetwork: ${selectedNetwork}")
         val web3jService = Web3jService.build(selectedNetwork)

         //Log.d("getTokens", "walletAddress: $walletAddress")
         //Log.d("getTokens", "contractAddresses: $contractAddresses")

        // Use flag to indicate whether network calls have already been made or not
        var networkCallsMade = false

        val mnemonic = getMnemonic()

         //Log.d("getTokens", "getMnemonic returns: $mnemonic")

        val credentials = if (!mnemonic.isNullOrEmpty()) {
            //Log.d("getTokens", "loading credentials with mnemonic $mnemonic")
            WalletUtils.loadBip39Credentials(null, mnemonic)
        } else {
            null
        }

        val balances = withContext(Dispatchers.IO) {
            Log.d("getTokens", "making network calls")
            Log.d("getTokens", "using Network: ${selectedNetwork.displayName}")
            networkCallsMade = true
            val tokenBalances = getTokenBalances(walletAddress, contractAddresses, web3jService, credentials)

            tokenBalances.forEach { tokenBalance ->
                cacheUserBalance(
                    tokenBalance,
                    application,
                    selectedNetwork = selectedNetwork.displayName,
                )
            }
            Log.d("getTokens", "$tokenBalances")
            tokenBalances
        }

        // Update the cache expiration time
        val sharedPreferences = getBalancesSharedPreferences(application)
        sharedPreferences.edit().putLong("CACHE_EXPIRATION_TIME", currentTime).apply()

        // Set networkCallsMade to false after a certain delay
        Handler(Looper.getMainLooper()).postDelayed({
            networkCallsMade = false
        }, getCacheExpirationTime(sharedPreferences))

        return balances
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