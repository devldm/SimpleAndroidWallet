package utils

import android.util.Base64
import com.example.ethktprototype.Network
import com.example.ethktprototype.Web3jService
import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.Credentials
import org.web3j.crypto.MnemonicUtils
import org.web3j.ens.EnsResolver

fun ensResolver(ensName: String): String {
    return if (!ensName.contains(".eth")) {
        ensName
    } else {
        val web3jService = Web3jService.build(Network.ETH_MAINNET)
        val ensResolve = EnsResolver(web3jService)

        ensResolve.resolve(ensName)
    }
}


fun buildScanUrl(network: Network, hash: String): String {
    if (network.chainId == Network.MUMBAI_TESTNET.chainId) {
        return "https://mumbai.polygonscan.com/tx/${hash}"
    }
    return "https://polygonscan.com/tx/${hash}"
}

// Helper function to encode a byte array to a base64 string
fun ByteArray.encodeBase64(): String =
    Base64.encodeToString(this, Base64.NO_WRAP)

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

fun isValidMnemonic(mnemonic: String): Boolean {
    return try {
        MnemonicUtils.validateMnemonic(mnemonic)
    } catch (e: Exception) {
        false
    }
}