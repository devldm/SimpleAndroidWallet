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
import utils.addressToEnsResolver
import utils.ensResolver
import utils.loadBip44Credentials
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

    private val _currentNetworkBalances = mutableStateOf(
        getUserBalances(
            application, selectedNetwork = selectedNetwork.value.displayName
        )
    )
    val currentNetworkBalances: MutableState<List<TokenBalance>> = _currentNetworkBalances

    private val _selectedToken = mutableStateOf(currentNetworkBalances.value.firstOrNull())
    val selectedToken: MutableState<TokenBalance?> = _selectedToken

    var hash = MutableLiveData<String>()

    fun updateSelectedToken(token: TokenBalance?) {
        _selectedToken.value = token
    }


    private var _mnemonicLoaded = mutableStateOf(false)
    var mnemonicLoaded: MutableState<Boolean> = _mnemonicLoaded

    val tokensLoading = mutableStateOf(false)

    val nftsLoading = mutableStateOf(false)

    var toAddress = mutableStateOf("")

    var sentAmount = mutableStateOf(0.0)

    var sentCurrency = mutableStateOf("")

    var showPayDialog = MutableLiveData(false)

    var showTokenDialog = MutableLiveData(false)

    var tokenBlocklist = mutableStateOf<List<TokenBalance>>(emptyList())

    var userEnsName = MutableLiveData("")


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

    fun updateTokenBlockList(token: TokenBalance) {
        tokenBlocklist.value = tokenBlocklist.value.plus(token)
        walletRepository.updateTokenBlockList(tokenBlocklist = tokenBlocklist.value)
        getTokenBlocklist()
    }

    fun getTokenBlocklist(): List<TokenBalance> {
        tokenBlocklist.value = walletRepository.getTokenBlocklist()
        return walletRepository.getTokenBlocklist()
    }

    fun getBalances(): MutableLiveData<List<TokenBalance>> {
        val tokens = MutableLiveData<List<TokenBalance>>()
        tokensLoading.value = true
        val walletAddress = walletAddress.value

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val tokenBalances = walletRepository.fetchBalances(
                    selectedNetwork.value.covalentChainName,
                    walletAddress!!,
                    selectedNetwork.value
                )
                tokens.postValue(tokenBalances)
                currentNetworkBalances.value = tokenBalances
                selectedToken.value = currentNetworkBalances.value.firstOrNull()
            }
            tokensLoading.value = false
        }
        return tokens
    }

    fun getNftBalances(): MutableLiveData<List<NftValue>> {
        val nfts = MutableLiveData<List<NftValue>>()
        nftsLoading.value = true
        val walletAddress = walletAddress.value

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val nftBalances = walletRepository.fetchNfts(
                    walletAddress!!,
                    selectedNetwork.value
                )
                nfts.postValue(nftBalances)
            }
            nftsLoading.value = false
        }
        return nfts
    }

    fun removeAllWalletData() {
        walletRepository.removeAllWalletData()
        mnemonicLoaded.value = false
    }

    fun clearTokenBlocklist() {
        tokenBlocklist.value = walletRepository.clearTokenBlocklist()
    }

    fun updateSelectedNetwork(network: Network) {
        val network = walletRepository.updateSelectedNetwork(network)
        _selectedNetwork.value = network
    }

    fun onPayConfirmed(address: String, amount: Double, contractAddress: String) {
        val mnemonic = getMnemonic()
        sentAmount.value = amount
        sentCurrency.value = selectedToken.value?.symbol ?: ""

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                toAddress.value = ensResolver(address)

                if (!mnemonic.isNullOrEmpty()) {
                    val credentials = loadBip44Credentials(mnemonic)
                    credentials.let {
                        sendTokens(
                            credentials,
                            contractAddress,
                            toAddress.value,
                            BigDecimal.valueOf(amount)
                        )
                        showPayDialog.postValue(false)
                    }
                }
            }
        }
    }

    fun checkForEnsName(walletAddress: String?) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                var ens = walletAddress?.let { addressToEnsResolver(it) }
                userEnsName.postValue(ens)
            }
        }
    }

    fun sendTokens(
        credentials: Credentials, contractAddress: String, toAddress: String, value: BigDecimal
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