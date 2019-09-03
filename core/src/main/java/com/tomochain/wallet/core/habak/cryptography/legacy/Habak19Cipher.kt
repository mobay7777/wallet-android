package com.tomochain.wallet.core.habak.cryptography.legacy

import android.annotation.TargetApi
import android.content.Context
import android.os.Build

import android.security.KeyPairGeneratorSpec
import java.math.BigInteger
import java.util.*
import javax.security.auth.x500.X500Principal
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
import com.tomochain.wallet.core.habak.EncryptedModel
import com.tomochain.wallet.core.habak.cryptography.Habak
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.*
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.GCMParameterSpec
import kotlin.collections.ArrayList


/**
 * Created by NienLe on 8/11/18,August,2018
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class Habak19Cipher(private var alias : String, var context: Context) : Habak {
    private lateinit var encryptionManager: EncryptionManager
    override fun initialize() {
        val preference = context.getSharedPreferences(alias, Context.MODE_PRIVATE)
        val keyAliasPrefix = "pr"
        val bitShiftKey = "wallet-core".toByteArray()
        encryptionManager = EncryptionManager(context, preference,
                keyAliasPrefix, bitShiftKey, object : KeyStoreRecoveryNotifier {
            override fun onRecoveryRequired(e: Exception, keyStore: KeyStore, keyAliases: List<String>): Boolean {
                return false
            }
        })
    }

    override fun encrypt(plainText: String): EncryptedModel {
        val encryptedData = encryptionManager.encrypt(plainText)!!.toByteArray()
        return EncryptedModel(encryptedData!!, byteArrayOf(0), Calendar.getInstance().timeInMillis)
    }

    override fun decrypt(data: EncryptedModel): StringBuilder {
        return try {
            StringBuilder(encryptionManager.decrypt(String(data.data!!))!!)
        } catch (e: Exception) {
            StringBuilder()
        }
    }
}