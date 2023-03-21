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
import org.web3j.tx.gas.DefaultGasProvider
import java.io.IOException
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

class WalletViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferences =
        application.getSharedPreferences("WalletPrefs", Context.MODE_PRIVATE)
    private val walletAddressKey = "wallet_address"
    var mnemonicLoaded = mutableStateOf(false)


    private val _walletAddress = MutableLiveData<String>()
    val walletAddress: LiveData<String>
        get() = _walletAddress


    private val _selectedNetwork = mutableStateOf(Network.MUMBAI_TESTNET)
    val selectedNetwork: MutableState<Network> = _selectedNetwork

    val selectedNetworkPreference = sharedPreferences.edit().putString("SELECTED_NETWORK_NAME", _selectedNetwork.value.displayName).apply()



    val loading = mutableStateOf(true)

    init {
        // Load the wallet address from SharedPreferences when the ViewModel is created
        _walletAddress.value = sharedPreferences.getString(walletAddressKey, "")
    }

    fun updateSelectedNetwork(network: Network) {
        Log.d("network", "updating _selectedNetwork to: $network")
        _selectedNetwork.value = network
        sharedPreferences.edit().putString("SELECTED_NETWORK_NAME", network.displayName).apply()

    }

    // Function to load the mnemonic from SharedPreferences on startup
    fun loadMnemonicFromPrefs(context: Context) {
        val prefs = context.getSharedPreferences("WalletPrefs", Context.MODE_PRIVATE)
        val storedMnemonic = prefs.getString("encrypted_mnemonic", null)
        mnemonicLoaded.value = storedMnemonic != null
    }



    fun getTokens(
        walletAddress: String,
        contractAddresses: List<String>,
        context: Context,
        application: Application,
    ): LiveData<List<TokenBalance>> = liveData {

        // Use flag to indicate whether network calls have already been made or not
        var networkCallsMade = false

        val currentTime = System.currentTimeMillis() / 1000
        val web3j = Web3jService.build(_selectedNetwork.value)

        // Get the decrypted mnemonic from the Keystore
        val mnemonic = getMnemonic(context)
        val credentials = if (!mnemonic.isNullOrEmpty()) {
            Log.d("viewModel", "loading credentials with mnemonic $mnemonic")
            WalletUtils.loadBip39Credentials(null, mnemonic)
        } else {
            null
        }

        val balances = withContext(Dispatchers.IO) {
//            getUserBalances(application, _selectedNetwork.value.displayName, viewModel).ifEmpty
            //TODO: reimplement the above line, Cacheing caused lost tokens somehow. I.E only last token in list would show in ui
                Log.d("fetch", "making network calls")
                networkCallsMade = true
                    val tokenBalances =
                        getTokenBalances(walletAddress, contractAddresses, web3j, credentials)

                tokenBalances.forEach { tokenBalance ->
                    cacheUserBalance(
                        tokenBalance,
                        application,
                        selectedNetwork = _selectedNetwork.value.displayName,
                    )
                }
                Log.d("TBalances", "$tokenBalances")
                tokenBalances
            }


        emit(balances)
        loading.value = false
        Log.d("Tokens", "Emitted tokens: $balances")
        // Update the cache expiration time
        val sharedPreferences = getBalancesSharedPreferences(application)
        sharedPreferences.edit().putLong("CACHE_EXPIRATION_TIME", currentTime).apply()

        // Set networkCallsMade to false after a certain delay
        Handler(Looper.getMainLooper()).postDelayed({
            networkCallsMade = false
        }, getCacheExpirationTime(sharedPreferences))
    }






    private fun getTokenBalances(
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


    fun getTokenContractAddresses(): List<String> {
        return listOf(
            "0x7ceB23fD6bC0adD59E62ac25578270cFf1b9f619", // WETH
            "0xc2132D05D31c914a87C6611C10748AEb04B58e8F", // USDT
            "0x3BA4c387f786bFEE076A58914F5Bd38d668B42c3", // BNB
            "0x2791Bca1f2de4661ED88A30C99A7a9449Aa84174", // USDC
            "0xdAb529f40E671A1D4bF91361c21bf9f0C9712ab7", // BUSD
            "0x8f3Cf7ad23Cd3CaDbD9735AFf958023239c6A063", // DAI
            "0x1BFD67037B42Cf73acF2047067bd4F2C47D9BfD6",
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

//    fun getWalletBalance(walletAddress: String): LiveData<String> = liveData {
//        Log.d("WalletViewModel", "Getting balance for address: $walletAddress")
//        val balance = withContext(Dispatchers.IO) {
//            getBalance(walletAddress)
//        }
//        emit(balance ?: "Balance not Found")
//    }

    fun loadBip44Credentials(mnemonic: String): Credentials {
        val seed = MnemonicUtils.generateSeed(mnemonic, "")
        val masterKeypair = Bip32ECKeyPair.generateKeyPair(seed)
        val path = intArrayOf(
            44 or Bip32ECKeyPair.HARDENED_BIT,
            60 or Bip32ECKeyPair.HARDENED_BIT,
            0 or Bip32ECKeyPair.HARDENED_BIT,
            0,
            0
        )
        val childKeypair = Bip32ECKeyPair.deriveKeyPair(masterKeypair, path)
        return Credentials.create(childKeypair)
    }

    private fun getBalance(address: String): String? {
        val web3j = Web3jService.build(_selectedNetwork.value)

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
            Log.d("WalletViewModel", "getBalance failed $e")

            null
        }
    }
}


//    private val _watchlist = MutableLiveData<List<String>>(emptyList())
//    val watchlist: LiveData<List<String>>
//        get() = _watchlist

//    // Add a function to update the watchlist
//    fun addToWatchlist(address: String) {
//        _watchlist.value = _watchlist.value?.plus(address)
//    }
//
//    // Add a function to update the watchlist
//    fun removeFromWatchlist(address: String) {
//        _watchlist.value = _watchlist.value?.minus(address)
//    }