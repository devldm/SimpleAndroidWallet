import android.util.Log
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.tx.Contract
import org.web3j.tx.gas.DefaultGasProvider
import java.math.BigInteger

class ERC20(
    contractAddress: String,
    web3j: Web3j,
    credentials: Credentials,
    gasProvider: DefaultGasProvider
) : Contract(ERC20_ABI, contractAddress, web3j, credentials, gasProvider) {
    init {
        try {
            Log.d("ERC20", "Initializing contract at address $contractAddress")
        } catch (e: Exception) {
            Log.e("ERC20", "Error initializing contract at address $contractAddress: ${e.message}")
        }
    }

    fun balanceOf(address: String): BigInteger {
        Log.d("TokenListScreen", "Calling balanceOf function for address $address")
        val function = Function(
            "balanceOf",
            listOf(Address(address)),
            listOf(object : TypeReference<Type<*>>() {})
        )
        val encodedFunction = FunctionEncoder.encode(function)

        Log.d("ERC20", "Encoded function: $encodedFunction")

        val response = executeCallSingleValueReturn<Uint256>(
            Function(
                "balanceOf",
                listOf(Address(address)),
                listOf(object : TypeReference<Uint256>() {})
            )
        )

        Log.d("ERC20", "Response: $response")
        val balance = response.value as BigInteger
        Log.d("ERC20", "Balance of $address is $balance")
        return balance
    }

    fun name(): String {
        val function = Function(
            "name",
            emptyList(),
            listOf(object : TypeReference<Utf8String>() {})
        )

        val response = executeCallSingleValueReturn<Utf8String>(function)

        return response.value
    }

    fun symbol(): String {
        val function = Function(
            "symbol",
            emptyList(),
            listOf(object : TypeReference<Utf8String>() {})
        )

        val response = executeCallSingleValueReturn<Utf8String>(function)

        return response.value
    }
    companion object {
        fun load(
            contractAddress: String,
            web3j: Web3j,
            credentials: Credentials,
            gasProvider: DefaultGasProvider
        ): ERC20 {
            return ERC20(contractAddress, web3j, credentials, gasProvider)
        }
    }
}



private val ERC20_ABI = """
    [
        {
            "constant": true,
            "inputs": [
                {
                    "name": "_owner",
                    "type": "address"
                }
            ],
            "name": "balanceOf",
            "outputs": [
                {
                    "name": "balance",
                    "type": "uint256"
                }
            ],
            "payable": false,
            "stateMutability": "view",
            "type": "function"
        },
        {
            "constant": true,
            "inputs": [],
            "name": "name",
            "outputs": [
                {
                    "name": "",
                    "type": "string"
                }
            ],
            "payable": false,
            "stateMutability": "view",
            "type": "function"
        },
        {
            "constant": true,
            "inputs": [],
            "name": "iconUrl",
            "outputs": [
                {
                    "name": "",
                    "type": "string"
                }
            ],
            "payable": false,
            "stateMutability": "view",
            "type": "function"
        },
                {
                    "constant": true,
                    "inputs": [],
                    "name": "symbol",
                    "outputs": [
                        {
                            "name": "",
                            "type": "string"
                        }
                    ],
                    "payable": false,
                    "stateMutability": "view",
                    "type": "function"
                }
    ]
""".trimIndent()


