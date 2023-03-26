package utils

import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.Credentials
import org.web3j.crypto.MnemonicUtils

fun getTokenContractAddresses(): List<String> {
    return listOf(
        "0x7ceB23fD6bC0adD59E62ac25578270cFf1b9f619", // WETH
        "0xc2132D05D31c914a87C6611C10748AEb04B58e8F", // USDT
        "0x3BA4c387f786bFEE076A58914F5Bd38d668B42c3", // BNB
        "0x2791Bca1f2de4661ED88A30C99A7a9449Aa84174", // USDC
        "0xdAb529f40E671A1D4bF91361c21bf9f0C9712ab7", // BUSD
        "0x8f3Cf7ad23Cd3CaDbD9735AFf958023239c6A063", // DAI
        "0x1BFD67037B42Cf73acF2047067bd4F2C47D9BfD6", // WBTC
        "0x0000000000000000000000000000000000001010", // MATIC
        "0xfe4F5145f6e09952a5ba9e956ED0C25e3Fa4c7F1", // Dummy ERC20
        "0x2d7882beDcbfDDce29Ba99965dd3cdF7fcB10A1e", // Test Token
        "0x326C977E6efc84E512bB9C30f76E30c160eD06FB" // LINK

        // Add more contract addresses here as needed
    )
}

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