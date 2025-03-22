import android.util.Log
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint8
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.tx.Contract
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger

class ERC20(
    contractAddress: String, web3j: Web3j, credentials: Credentials, gasProvider: DefaultGasProvider
) : Contract(ERC20_ABI, contractAddress, web3j, credentials, gasProvider) {
    init {
        try {
            Log.d("ERC20", "Initializing contract at address $contractAddress")
        } catch (e: Exception) {
            Log.e("ERC20", "Error initializing contract at address $contractAddress: ${e.message}")
        }
    }

    fun transfer(toAddress: String, value: BigDecimal): String {
        val function = Function(
            "transfer", listOf(
                Address(toAddress), Uint256(
                    Convert.toWei(
                        value.multiply(BigDecimal.TEN.pow(decimals().toInt())), Convert.Unit.WEI
                    ).toBigInteger()
                )
            ), emptyList()
        )

        val encodedFunction = FunctionEncoder.encode(function)
        return encodedFunction
    }

    fun balanceOf(address: String): BigInteger {
        val function = Function(
            "balanceOf", listOf(Address(address)), listOf(object : TypeReference<Type<*>>() {})
        )
        val encodedFunction = FunctionEncoder.encode(function)

        val response = executeCallSingleValueReturn<Uint256>(
            Function(
                "balanceOf", listOf(Address(address)), listOf(object : TypeReference<Uint256>() {})
            )
        )

        return response.value as BigInteger
    }

    fun name(): String {
        val function = Function(
            "name", emptyList(), listOf(object : TypeReference<Utf8String>() {})
        )

        val response = executeCallSingleValueReturn<Utf8String>(function)

        return response.value
    }

    fun symbol(): String {
        val function = Function(
            "symbol", emptyList(), listOf(object : TypeReference<Utf8String>() {})
        )

        val response = executeCallSingleValueReturn<Utf8String>(function)

        return response.value
    }

    fun decimals(): BigInteger {
        val function = Function(
            "decimals", emptyList(), listOf(object : TypeReference<Uint8>() {})
        )

        val response = executeCallSingleValueReturn<Uint8>(function)

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
                },
                 {
            "constant": true,
            "inputs": [],
            "name": "decimals",
            "outputs": [
                {
                    "name": "",
                    "type": "uint8"
                }
            ],
            "payable": false,
            "stateMutability": "view",
            "type": "function"
        },
        "name": "transfer",
        "outputs": [
            {
                "name": "",
                "type": "bool"
            }
        ],
        "payable": false,
        "stateMutability": "nonpayable",
        "type": "function"
    }
    ]
""".trimIndent()


