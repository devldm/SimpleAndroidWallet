package com.example.ethktprototype

import android.app.Application
import android.content.Context
import androidx.compose.runtime.MutableState
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


    private val _selectedNetwork = mutableStateOf(walletRepository.getLastSelectedNetwork())
    val selectedNetwork: MutableState<Network> = _selectedNetwork

    private val _currentNetworkBalances = mutableStateOf(getUserBalances(application, selectedNetwork = selectedNetwork.value.displayName ))
    val currentNetworkBalances: MutableState<List<TokenBalance>> = _currentNetworkBalances

    private val _selectedToken = mutableStateOf(currentNetworkBalances.value.firstOrNull())
    val selectedToken: MutableState<TokenBalance?> = _selectedToken

    var hash = MutableLiveData<String>()

    fun updateSelectedToken(token: TokenBalance?) {
        _selectedToken.value = token
    }


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

    fun removeAllWalletData(){
        walletRepository.removeAllWalletData()
        mnemonicLoaded.value = false
    }

    fun updateSelectedNetwork(network: Network) {
        val network = walletRepository.updateSelectedNetwork(network)
        _selectedNetwork.value = network
    }

    fun getTokens(application: Application): LiveData<List<TokenBalance>> {
        val walletRepo = WalletRepository(application)
        val tokens = MutableLiveData<List<TokenBalance>>()
        loading.value = true
        val walletAddress = walletAddress.value

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val balances = walletRepo.getTokens(walletAddress!!, getTokenContractAddresses(selectedNetwork.value), selectedNetwork.value)
                tokens.postValue(balances)
                currentNetworkBalances.value = balances
                selectedToken.value = currentNetworkBalances.value.firstOrNull()
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
                hash.postValue(returnedHash)
            }
        }
    }
}