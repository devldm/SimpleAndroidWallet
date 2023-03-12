package com.example.ethktprototype

import android.app.Application
import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.Credentials
import org.web3j.crypto.MnemonicUtils
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.DefaultGasProvider
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode

class WalletViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferences =
        application.getSharedPreferences("WalletPrefs", Context.MODE_PRIVATE)
    private val walletAddressKey = "wallet_address"
    var mnemonicLoaded = mutableStateOf(false)


    private val _walletAddress = MutableLiveData<String>()
    val walletAddress: LiveData<String>
        get() = _walletAddress

    private val _watchlist = MutableLiveData<List<String>>(emptyList())
    val watchlist: LiveData<List<String>>
        get() = _watchlist

    init {
        // Load the wallet address from SharedPreferences when the ViewModel is created
        _walletAddress.value = sharedPreferences.getString(walletAddressKey, "")
    }

    fun getTokens(walletAddress: String, context: Context): LiveData<List<TokenBalance>> =
        liveData {
            val web3j = Web3j.build(HttpService("https://matic-mumbai.chainstacklabs.com/"))

            // Get the decrypted mnemonic from the Keystore
            val mnemonic = getMnemonic(context)
            var credentials: Credentials? = null
            if (mnemonic.isNullOrEmpty()) {
                null
                Log.d("viewModel", "its empty")
            } else {
                Log.d("viewModel", "loading credentials with mnemonic $mnemonic")
                credentials = WalletUtils.loadBip39Credentials(null, mnemonic)
            }
            // Create Credentials object using the mnemonic
            val tokens = withContext(Dispatchers.IO) {
                val contractAddresses = getTokenContractAddresses()
                Log.d("TokenListScreen", "Contract addresses: $contractAddresses")
                val balances = contractAddresses.mapNotNull { address ->
                    try {
                        Log.d("TokenListScreen", "Creating contract for address: $address")
                        val contract =
                            ERC20.load(address, web3j, credentials!!, DefaultGasProvider())
                        Log.d(
                            "TokenListScreen",
                            "Contract created successfully for address: $address"
                        )

                        Log.d(
                            "TokenListScreen",
                            "Contract created successfully for address: $address"
                        )
                        val balance = contract.balanceOf(walletAddress)
                        Log.d(
                            "TokenListScreen",
                            "Retrieved balance successfully for address: $address"
                        )
                        val name = contract.name()
                        val symbol = contract.symbol()
                        TokenBalance(address, balance, name, symbol)
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
                balances
            }

            emit(tokens)
            Log.d("Tokens", "Emitted tokens: $tokens")
        }

    private fun getTokenContractAddresses(): List<String> {
        return listOf(
//            "0x7ceB23fD6bC0adD59E62ac25578270cFf1b9f619", // WETH
//            "0x0D500B1d8E8eF31E21C99d1Db9A6444d3ADf1270", // WMATIC
            "0x0000000000000000000000000000000000001010", // MATIC
            "0xfe4F5145f6e09952a5ba9e956ED0C25e3Fa4c7F1", // Dummy ERC20
            "0x2d7882beDcbfDDce29Ba99965dd3cdF7fcB10A1e", // Test Token
            "0x326C977E6efc84E512bB9C30f76E30c160eD06FB" // LINK

            // Add more contract addresses here as needed
        )
    }

    fun storeMnemonic(mnemonic: String, context: Context) {
        encryptMnemonic(context, mnemonic)

        val sharedPreferences = context.getSharedPreferences("WalletPrefs", Context.MODE_PRIVATE)
        val encryptedMnemonic = getEncryptedMnemonic(context)
        val encodedMnemonic = Base64.encodeToString(encryptedMnemonic, Base64.DEFAULT)
        sharedPreferences.edit().putString("encrypted_mnemonic", encodedMnemonic).apply()

        mnemonicLoaded.value = true
    }

    fun getMnemonic(context: Context): String? {
        // Decrypt the mnemonic
        val mnemonic = getDecryptedMnemonic(context)
        mnemonicLoaded.value = true
        return mnemonic
    }

    fun storeWallet(walletAddress: String) {
        // Store the wallet address in SharedPreferences
        sharedPreferences.edit {
            putString(walletAddressKey, walletAddress)
            apply()
        }
        // Update the LiveData with the new wallet address
        _walletAddress.value = walletAddress
    }

    // Add a function to update the watchlist
    fun addToWatchlist(address: String) {
        _watchlist.value = _watchlist.value?.plus(address)
    }

    // Add a function to update the watchlist
    fun removeFromWatchlist(address: String) {
        _watchlist.value = _watchlist.value?.minus(address)
    }


    fun getWalletBalance(walletAddress: String): LiveData<String> = liveData {
        Log.d("WalletViewModel", "Getting balance for address: $walletAddress")
        val balance = withContext(Dispatchers.IO) {
            getBalance(walletAddress)
        }
        emit(balance ?: "Balance not Found")
    }

    fun loadBip44Credentials(mnemonic: String): Credentials {
        val seed = MnemonicUtils.generateSeed(mnemonic, "")
        val masterKeypair = Bip32ECKeyPair.generateKeyPair(seed)
        val path = intArrayOf(44 or Bip32ECKeyPair.HARDENED_BIT, 60 or Bip32ECKeyPair.HARDENED_BIT, 0 or Bip32ECKeyPair.HARDENED_BIT, 0, 0)
        val childKeypair = Bip32ECKeyPair.deriveKeyPair(masterKeypair, path)
        return Credentials.create(childKeypair)
    }

    private fun getBalance(address: String): String? {
        println(address)
        val web3j =
            Web3j.build(HttpService("https://matic-mumbai.chainstacklabs.com/"))

        return try {
            Log.d("WalletViewModel", "the address is $address")
            val balance =
                web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send().balance

            Log.d("WalletViewModel", "balance for is: $balance")
            val balanceInEth = BigDecimal(balance)
                .divide(BigDecimal.TEN.pow(18))
                .setScale(3, RoundingMode.HALF_UP)
                .toPlainString()
            balanceInEth
        } catch (e: IOException) {
            Log.d("WalletViewModel", "getBalance faile $e")

            null
        }
    }
}