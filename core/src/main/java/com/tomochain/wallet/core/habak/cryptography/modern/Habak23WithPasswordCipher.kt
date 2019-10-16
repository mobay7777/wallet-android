package com.tomochain.wallet.core.habak.cryptography.modern

import android.annotation.TargetApi
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import com.tomochain.wallet.core.habak.Constant
import com.tomochain.wallet.core.habak.EncryptedModel
import com.tomochain.wallet.core.habak.cryptography.Habak
import java.lang.StringBuilder
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.security.Key
import java.security.KeyStore
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Created by NienLe on 8/11/18,August,2018
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class Habak23WithPasswordCipher(private val alias : String, private val password : String) : Habak {

    private lateinit var keyStore: KeyStore



    /**
     * generate a keystore secret key that use to encrypt/decrypt data
     * This key is store in hardware layer and only can access within app at runtime
     * Specify the key by alias
     */
    @TargetApi(Build.VERSION_CODES.M)
    private fun generateSecretKey() {
        val keyGenerator = KeyGenerator
                .getInstance(KeyProperties.KEY_ALGORITHM_AES, Constant.ANDROID_KEY_STORE)

        keyGenerator.init(KeyGenParameterSpec.Builder(alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(false)
                .build())
        keyGenerator.generateKey()
    }

    /**
     * Get secret key from hardware along with 'alias' params
     * return the secret key
     */
    @TargetApi(Build.VERSION_CODES.M)
    private fun getSecretKey() : Key {
        val keyStore = KeyStore.getInstance(Constant.ANDROID_KEY_STORE)
        keyStore.load(null)
        return keyStore.getKey(alias, null)
    }


    /**
     * Init the Keystore system with default params.
     * Check if the key with alias 'alias' is already existed, generate if need
     */
    override fun initialize() {
        keyStore = KeyStore.getInstance(Constant.ANDROID_KEY_STORE)
        keyStore.load(null)
        if (!keyStore.containsAlias(alias))
            generateSecretKey()

    }

    /**
     * Encrypt the plain text
     * @param plainText
     * @return EncryptedModel object contain the encrypted data, zero-length IV
     * and the current timeStamp
     */
    override fun encrypt(plainText: String): String {
        return try {
            val cipher = Cipher.getInstance(Constant.AES_MODE_FROM_M)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(),
                    GCMParameterSpec(128, formatPasswordLength().toByteArray()))
            val encrypted = cipher.doFinal(plainText.toByteArray(charset(Constant.UTF8)))
            val now = Calendar.getInstance().timeInMillis
            return EncryptedModel(encrypted, ByteArray(0), now).writeToString()
        } catch (e: Exception) {
            EncryptedModel(ByteArray(0), ByteArray(0), 0).writeToString()
        }
    }

    /**
     * Decrypt data
     * @param data the EncryptedModel object to decrypt
     * @return decrypted plain string
     * and the current timeStamp
     */
    override fun decrypt(data: String): StringBuilder {
        return try {
            val cipher = Cipher.getInstance(Constant.AES_MODE_FROM_M)
            val encryptedModel = EncryptedModel.readFromString(data)
            val spec = GCMParameterSpec(128, formatPasswordLength().toByteArray())
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
            /*val r = String(cipher.doFinal(data.data), Charsets.UTF_8)
            return r*/

            val charset = Charset.forName("UTF-8")
            val decoder = charset.newDecoder()
            val srcBuffer = ByteBuffer.wrap(cipher.doFinal(encryptedModel.getEncryptedData().first))


            val sb = StringBuilder(decoder.decode(srcBuffer))
            /*cipher.doFinal(data.data).forEach {
                sb.append(it.toChar())
            }*/
            srcBuffer.clear()
            sb
        } catch (e: Exception) {
            Log.e("HABAK","decrypt: ", e)
            StringBuilder()
        }
    }

    /**
     * Since the accepted IV for cipher must has the length of 12 char, so the user password
     * must be cut-off if too long, or append some characters if too short
     * @return formatted password
     */
    private fun formatPasswordLength() : String{
        val ACCEPT_LENGTH = 12

        if (password.length > ACCEPT_LENGTH){
            return password.substring(0, 12)
        }
        if (password.length < ACCEPT_LENGTH){
            var tmp = password
            while(tmp.length < ACCEPT_LENGTH){
                tmp += '0'
            }
            return tmp
        }
        return password
    }


    /*private fun removeKeyStore(){
        if (keyStore.containsAlias(alias))
            keyStore.deleteEntry(alias)
    }*/
}