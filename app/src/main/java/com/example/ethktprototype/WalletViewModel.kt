package com.example.ethktprototype

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ethktprototype.data.TokenBalance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import utils.addressToEnsResolver
import utils.ensResolver
import utils.loadBip44Credentials
import java.math.BigDecimal


class WalletViewModel(application: Application) : AndroidViewModel(application) {
    private val walletRepository = WalletRepository(application)
    private val sharedPreferences =
        application.getSharedPreferences("WalletPrefs", Context.MODE_PRIVATE)
    private val walletAddressKey = "wallet_address"

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        val savedWalletAddress = sharedPreferences.getString(walletAddressKey, "") ?: ""
        updateUiState { it.copy(walletAddress = savedWalletAddress) }

        val lastNetwork = walletRepository.getLastSelectedNetwork()
        updateUiState { it.copy(selectedNetwork = lastNetwork) }

        if (savedWalletAddress.isNotEmpty()) {
            getBalances()
            checkForEnsName(savedWalletAddress)
        }

        loadMnemonicFromPrefs()
        getTokenBlocklist()
    }

    private fun updateUiState(update: (WalletUiState) -> WalletUiState) {
        _uiState.update(update)
    }

    private fun getMnemonic(): String? {
        return walletRepository.getMnemonic()
    }

    fun storeWallet(walletAddress: String) {
        walletRepository.storeWallet(walletAddress)
        updateUiState { it.copy(walletAddress = walletAddress) }
    }

    fun storeMnemonic(mnemonic: String) {
        walletRepository.storeMnemonic(mnemonic)
        updateUiState { it.copy(mnemonicLoaded = true) }
    }

    private fun loadMnemonicFromPrefs() {
        val storedMnemonic = walletRepository.loadMnemonicFromPrefs()
        updateUiState { it.copy(mnemonicLoaded = storedMnemonic != null) }
    }

    fun updateTokenBlockList(token:TokenBalance) {
        val updatedBlocklist = uiState.value.tokenBlocklist + token
        walletRepository.updateTokenBlockList(tokenBlocklist = updatedBlocklist)
        updateUiState { it.copy(tokenBlocklist = updatedBlocklist) }
    }

    private fun getTokenBlocklist() {
        val blockList = walletRepository.getTokenBlocklist()
        updateUiState { it.copy(tokenBlocklist = blockList) }
    }

    fun getBalances() {
        updateUiState { it.copy(isTokensLoading = true) }
        val walletAddress = uiState.value.walletAddress

        if (walletAddress.isEmpty()) return

        viewModelScope.launch {
            try {
                val (totalBalance, tokenBalances) = withContext(Dispatchers.IO) {
                    walletRepository.fetchBalances(walletAddress, 101)
                }

                updateUiState {
                    it.copy(
                        totalBalanceUSD = totalBalance,
                        tokens = tokenBalances,
                        selectedToken = tokenBalances.firstOrNull(),
                        isTokensLoading = false
                    )
                }
            } catch (e: Exception) {
                // Handle errors
                updateUiState { it.copy(isTokensLoading = false) }
            }
        }
    }

    fun getNftBalances() {
        updateUiState { it.copy(isNftsLoading = true) }
        val walletAddress = uiState.value.walletAddress

        if (walletAddress.isEmpty()) return

        viewModelScope.launch {
            try {
                val nftBalances = withContext(Dispatchers.IO) {
                    walletRepository.fetchNfts(walletAddress, uiState.value.selectedNetwork)
                }

                updateUiState { it.copy(nfts = nftBalances, isNftsLoading = false) }
            } catch (e: Exception) {
                // Handle errors
                updateUiState { it.copy(isNftsLoading = false) }
            }
        }
    }

    fun removeAllWalletData() {
        walletRepository.removeAllWalletData()
        updateUiState {
            WalletUiState() // Reset to default state
        }
    }

    fun clearTokenBlocklist() {
        val emptyBlocklist = walletRepository.clearTokenBlocklist()
        updateUiState { it.copy(tokenBlocklist = emptyBlocklist) }
    }

    fun updateSelectedNetwork(network: Network) {
        val updatedNetwork = walletRepository.updateSelectedNetwork(network)
        updateUiState { it.copy(selectedNetwork = updatedNetwork) }
        // Refresh balances when network changes
        getBalances()
        getNftBalances()
    }

    fun updateSelectedToken(token: TokenBalance?) {
        updateUiState { it.copy(selectedToken = token) }
    }

    fun setShowPayDialog(show: Boolean) {
        updateUiState { it.copy(showPayDialog = show) }
    }

    fun setShowTokenBottomSheet(show: Boolean) {
        updateUiState { it.copy(showTokenBottomSheet = show) }
    }

    fun setShowWalletModal(show: Boolean) {
        updateUiState { it.copy(showWalletModal = show) }
    }

    fun setHashValue(value: String) {
        updateUiState { it.copy(hash = value) }
    }

    fun setShowSuccessModal(show: Boolean) {
        updateUiState { it.copy(showSuccessModal = show) }
    }

    fun onPayConfirmed(address: String, amount: Double, contractAddress: String) {
        val mnemonic = getMnemonic()

        updateUiState {
            it.copy(
                sentAmount = amount,
                sentCurrency = it.selectedToken?.symbol ?: ""
            )
        }

        viewModelScope.launch {
            try {
                val resolvedAddress = withContext(Dispatchers.IO) {
                    ensResolver(address)
                }

                updateUiState { it.copy(toAddress = resolvedAddress) }

                if (!mnemonic.isNullOrEmpty()) {
                    val credentials = loadBip44Credentials(mnemonic)
                    credentials.let {
                        val hash = withContext(Dispatchers.IO) {
                            walletRepository.sendTokens(
                                credentials,
                                contractAddress,
                                resolvedAddress,
                                BigDecimal.valueOf(amount)
                            )
                        }

                        updateUiState { state ->
                            state.copy(
                                transactionHash = hash,
                                showPayDialog = false,
                                showSuccessModal = true
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle errors
                updateUiState { it.copy(showPayDialog = false) }
            }
        }
    }

    private fun checkForEnsName(walletAddress: String?) {
        if (walletAddress.isNullOrEmpty()) return

        viewModelScope.launch {
            try {
                val ens = withContext(Dispatchers.IO) {
                    addressToEnsResolver(walletAddress)
                }

                updateUiState { it.copy(userEnsName = ens) }
            } catch (e: Exception) {
                // Handle errors
            }
        }
    }
}

