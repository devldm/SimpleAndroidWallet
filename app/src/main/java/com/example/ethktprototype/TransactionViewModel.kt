import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.example.ethktprototype.Network
import com.example.ethktprototype.TokenBalance
import com.example.ethktprototype.Web3jService
import com.example.ethktprototype.getUserBalances
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.math.BigDecimal
import java.math.BigInteger


class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val web3j = Web3jService.build(Network.MUMBAI_TESTNET)

    private val balanceSharePrefs = application.getSharedPreferences("Balances", Context.MODE_PRIVATE)
    private val walletSharePrefs = application.getSharedPreferences("WalletPrefs", Context.MODE_PRIVATE)
    private val currentNetwork = walletSharePrefs.getString("SELECTED_NETWORK_NAME", null)
    private val currentNetworkBalances = currentNetwork?.let { getUserBalances(application, it) }

    private val _selectedToken = mutableStateOf(currentNetworkBalances?.firstOrNull())
    val selectedToken: State<TokenBalance?> = _selectedToken

    fun updateSelectedToken(token: TokenBalance?) {
        Log.d("token", "updating _selectedToken to: $token")
        _selectedToken.value = token
    }

    suspend fun sendTokens(credentials: Credentials, contractAddress: String, toAddress: String, value: BigDecimal): String {
        Log.d("send", "sending $value tokens to $toAddress")

        return withContext(Dispatchers.IO) {
            try {
                // Get the nonce for the transaction
                val nonce: BigInteger = web3j.ethGetTransactionCount(credentials.address, DefaultBlockParameterName.LATEST)
                    .send().transactionCount
                val chainIdLong: Long = 80001


                // Get the gas price and gas limit
                val gasPrice = web3j.ethGasPrice().send().gasPrice
                val gasLimit = DefaultGasProvider().gasLimit

                // Create a function call to transfer tokens
                val function = Function(
                    "transfer",
                    listOf(Address(toAddress), Uint256(Convert.toWei(value, Convert.Unit.ETHER).toBigInteger())),
                    emptyList()
                )

                // Encode the function call to get the data that needs to be sent in the transaction
                val encodedFunction = FunctionEncoder.encode(function)

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
                val signedT = TransactionEncoder.signMessage(transaction, chainIdLong, credentials)

                // Convert the signed transaction to hex format
                val hexValue = Numeric.toHexString(signedT)

                // Send the signed transaction to the network
                val transactionResponse = web3j.ethSendRawTransaction(hexValue).sendAsync().get()

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
}



