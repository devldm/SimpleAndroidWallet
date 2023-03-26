package com.example.ethktprototype

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.web3j.crypto.Credentials
import utils.getTokenContractAddresses
import java.math.BigDecimal

class WalletViewModel(application: Application) : AndroidViewModel(application) {
    private val walletRepository = WalletRepository(application)
    private val sharedPreferences =
        application.getSharedPreferences("WalletPrefs", Context.MODE_PRIVATE)
    private val walletAddressKey = "wallet_address"



    private val _walletAddress = MutableLiveData<String>()
    val walletAddress: LiveData<String>
        get() = _walletAddress


    private val _selectedNetwork = mutableStateOf(Network.MUMBAI_TESTNET)
    val selectedNetwork: MutableState<Network> = _selectedNetwork

    private val currentNetworkBalances = getUserBalances(application, selectedNetwork = selectedNetwork.value.displayName )

    private val _selectedToken = mutableStateOf(currentNetworkBalances?.firstOrNull())
    val selectedToken: State<TokenBalance?> = _selectedToken

    var hash = MutableLiveData<String>()

    fun updateSelectedToken(token: TokenBalance?) {
        Log.d("token", "updating _selectedToken to: $token")
        _selectedToken.value = token
    }

    val selectedNetworkPreference = sharedPreferences.edit().putString("SELECTED_NETWORK_NAME", _selectedNetwork.value.displayName).apply()

    private var _mnemonicLoaded = mutableStateOf(false)
    var mnemonicLoaded: MutableState<Boolean> = _mnemonicLoaded

    val loading = mutableStateOf(true)

    init {
        // Load the wallet address from SharedPreferences when the ViewModel is created
        _walletAddress.value = sharedPreferences.getString(walletAddressKey, "")
    }

    fun getMnemonic(): String? {
        val mnemonic = walletRepository.getMnemonic()
        //mnemonicLoaded.value = true
        return mnemonic
    }

    fun storeWallet(walletAddress: String) {
        walletRepository.storeWallet(walletAddress)
        _walletAddress.value = walletAddress
    }

    fun storeMnemonic(mnemonic: String) {
        walletRepository.storeMnemonic(mnemonic)
        mnemonicLoaded.value = true
    }

    fun loadMnemonicFromPrefs() {
        val storedMnemonic = walletRepository.loadMnemonicFromPrefs()
        mnemonicLoaded.value = storedMnemonic != null
    }

    fun updateSelectedNetwork(network: Network) {
        val network = walletRepository.updateSelectedNetwork(network)
        Log.d("updateSelectedNetwork","VM network: $network")
        _selectedNetwork.value = network
    }

    fun getTokens(application: Application): LiveData<List<TokenBalance>> {
        val walletRepo = WalletRepository(application)
        val tokens = MutableLiveData<List<TokenBalance>>()
        loading.value = true
        val walletAddress = walletAddress.value

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val balances = walletRepo.getTokens(walletAddress!!, getTokenContractAddresses(), selectedNetwork.value)
                tokens.postValue(balances)
            }
            loading.value = false
        }
        return tokens
    }

    fun sendTokens(credentials: Credentials, contractAddress: String, toAddress: String,
                   value: BigDecimal
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
               val returnedHash = walletRepository.sendTokens(
                    credentials, contractAddress, toAddress, value
                )
                Log.d("send", "returnedHash: $returnedHash")
                hash.postValue(returnedHash)
            }
        }
        Log.d("send", "hash returned from viewModel: ${hash.value}")
    }
}