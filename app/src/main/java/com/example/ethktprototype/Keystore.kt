package com.example.ethktprototype

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.nio.charset.Charset
import java.security.KeyStore
import java.security.Security
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

// Define a key name for the mnemonic in the Android Keystore
private const val MNEMONIC_KEY_NAME = "encrypted_mnemonic"
private const val MNEMONIC_IV_KEY_NAME = "mnemonic_iv_key"

// Generate a secret key for the mnemonic
private fun generateSecretKey(): SecretKey {
    Security.addProvider(BouncyCastleProvider())
    val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
    val keyGenParameterSpec = KeyGenParameterSpec.Builder(
        "encrypted_mnemonic",
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setUserAuthenticationRequired(false)
        .build()
    keyGenerator.init(keyGenParameterSpec)
    return keyGenerator.generateKey()
}

fun encryptMnemonic(context: Context, mnemonic: String): ByteArray {
    Security.addProvider(BouncyCastleProvider())

    val secretKey = generateSecretKey()
    val cipher = Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
    val encrypted = cipher.doFinal(mnemonic.toByteArray(Charsets.UTF_8))
    val encryptionIv: ByteArray = cipher.iv
    storeEncryptedMnemonicIV(context, encryptionIv)
    storeEncryptedMnemonic(context, encrypted)
    return encrypted
}

private fun storeEncryptedMnemonic(context: Context, encrypted: ByteArray) {
    val sharedPreferences = context.getSharedPreferences("WalletPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString("encrypted_mnemonic", Base64.encodeToString(encrypted, Base64.DEFAULT))
    editor.apply()
}


// Get the encrypted mnemonic bytes from SharedPreferences
fun getEncryptedMnemonic(context: Context): ByteArray? {
    Security.addProvider(BouncyCastleProvider())

    val sharedPreferences = context.getSharedPreferences("WalletPrefs", Context.MODE_PRIVATE)
    val encryptedMnemonicString =
        sharedPreferences.getString("encrypted_mnemonic", "") ?: return null
    return Base64.decode(encryptedMnemonicString, Base64.DEFAULT)
}


fun getDecryptedMnemonic(context: Context): String? {
    Security.addProvider(BouncyCastleProvider())
    Log.d("Keystore", "get decrypted has been called ")
    val sharedPreferences = context.getSharedPreferences("WalletPrefs", Context.MODE_PRIVATE)

    try {
        Log.d("Keystore", "Trying to decrypt")
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val entry = keyStore.getEntry("encrypted_mnemonic", null) as KeyStore.SecretKeyEntry?
        val secretKey = entry?.secretKey ?: return null

        // Get the encrypted mnemonic bytes from SharedPreferences
        val encryptedMnemonic = getEncryptedMnemonic(context) ?: return null
        val base64EncryptionIv: String? = sharedPreferences.getString(MNEMONIC_IV_KEY_NAME, null)
        val encyptionIV: ByteArray = Base64.decode(base64EncryptionIv, Base64.DEFAULT)

        Log.d("Keystore", "encrypted mnemonic $encryptedMnemonic")

        // Decrypt the mnemonic
        val cipher = Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, encyptionIV))
        val decryptedBytes = cipher.doFinal(encryptedMnemonic)

        val mnemonic = String(decryptedBytes, Charset.forName("UTF-8"))

        Log.d("Keystore", "Decrypted mnemonic: $mnemonic")
        return mnemonic
    } catch (e: Exception) {
        Log.e("Keystore", "Error getting decrypted mnemonic", e)
        return null
    }
}


private fun storeEncryptedMnemonicIV(context: Context, iv: ByteArray) {
    val sharedPreferences = context.getSharedPreferences("WalletPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString(MNEMONIC_IV_KEY_NAME, Base64.encodeToString(iv, Base64.DEFAULT))
    editor.apply()
}


