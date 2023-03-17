import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.ethktprototype.Network
import com.example.ethktprototype.Web3jService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.math.BigDecimal
import java.math.BigInteger


class TransactionViewModel : ViewModel() {
    private val web3j = Web3jService.build(Network.MUMBAI_TESTNET)


    suspend fun getGasPrice(): BigInteger {
        val url = "https://gasstation-mainnet.matic.network/v2"
        val request = Request.Builder()
            .url(url)
            .build()

        val response = withContext(Dispatchers.IO) {
            OkHttpClient().newCall(request).execute()
        }

        val json = JSONObject(response.body?.string())
        val gasPriceInGwei = json.getJSONObject("fast").getString("maxFee").toDouble()

        Log.d("gas", "$gasPriceInGwei")

        return BigInteger.valueOf((gasPriceInGwei).toLong())
    }


    suspend fun sendMatic(credentials: Credentials, toAddress: String, value: BigDecimal): String {
        Log.d("send", "sending $value to ${toAddress}")
        return withContext(Dispatchers.IO) {
            try {
                val nonce: BigInteger = web3j.ethGetTransactionCount(credentials.address, DefaultBlockParameterName.LATEST)
                    .send().transactionCount

                val a = web3j.ethGasPrice().send().gasPrice
                val gasMax = DefaultGasProvider().gasLimit


                val chainIdLong: Long = 80001

                val rawTransaction = RawTransaction.createEtherTransaction(
                    nonce,
                    a,
                    gasMax,
                    toAddress,
                    Convert.toWei(value, Convert.Unit.ETHER).toBigInteger()
                )

                // Sign the transaction using the credentials and chain ID.
                val signedMessage = TransactionEncoder.signMessage(rawTransaction, chainIdLong, credentials)
                val hexValue = Numeric.toHexString(signedMessage)
                // Send the signed transaction to the network.
                val transactionResponse = web3j.ethSendRawTransaction(hexValue).sendAsync().get()

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


//    suspend fun sendMatic(credentials: Credentials, toAddress: String, value: BigDecimal): String {
//        Log.d("send", "sending $value to ${toAddress}")
//        return withContext(Dispatchers.IO) {
//            try {
//                val nonce: BigInteger = web3j.ethGetTransactionCount(credentials.address, DefaultBlockParameterName.LATEST)
//                    .send().transactionCount // subtract a smaller value from the current nonce
//
//                val gasPrice = BigInteger.valueOf(80)
//                val gasLimit = BigInteger.valueOf(21000)
//
//                val chainIdLong: Long = 80001
//
//                // Create a new instance of `org.web3j.crypto.RawTransaction` with the constructor that takes the
//                // following arguments: nonce, gasPrice, gasLimit, toAddress, value, data. In this case, we don't
//                // need to include any data, so we can pass null.
//                val rawTransaction = RawTransaction.createEtherTransaction(
//                    nonce,
//                    gasLimit,
//                    toAddress,
//                    Convert.toWei(value, Convert.Unit.WEI).toBigInteger(),
//                    gasPrice,
//                    null
//                )
//
//                // Create an EIP-155 signature data object using the `org.web3j.crypto.TransactionEncoder` class.
////               val chainId = BigInteger.valueOf(80001)
////               val signatureData = Sign.signMessage(rawTransaction, credentials.ecKeyPair, false)
////                val eip155 = TransactionEncoder.createEip155SignatureData(signatureData, chainId.toLong())
//
//                // Use `org.web3j.crypto.TransactionEncoder` to encode the transaction using the `createEip155Transaction`
//                // method. This method takes the same arguments as the `RawTransaction` constructor, as well as the
//                // `v`, `r`, and `s` values from the `EIP155SignatureData` object.
//                val transactionReceipt = Transfer.sendFundsEIP1559(
//                    web3j,
//                    credentials,
//                    toAddress,
//                    value,
//                    Convert.Unit.WEI,
//                gasLimit,
//                gasPrice,
//                null).send()
//
//
////                val transactionResponse = web3j.ethSendRawTransaction(signedMessage).send()
//
//                if (!transactionReceipt.isStatusOK) {
//                    Log.d("send", "transaction failed: ${transactionReceipt.transactionHash}")
//
//                    throw RuntimeException("Transaction failed: ${transactionReceipt}")
//                } else {
//                    Log.d("send", "transaction successful, hash: ${transactionReceipt.transactionHash}")
//                    transactionReceipt.transactionHash
//                }
//            } catch (e: Exception) {
//                Log.e("send", "transaction failed: ${e.message}")
//                throw e
//            }
//        }
//    }
}



