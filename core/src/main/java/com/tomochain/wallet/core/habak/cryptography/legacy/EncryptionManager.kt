package com.tomochain.wallet.core.habak.cryptography.legacy

import android.annotation.TargetApi
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.*
import java.math.BigInteger
import java.nio.charset.Charset
import java.security.*
import java.security.cert.CertificateException
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.*
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal
import kotlin.experimental.xor

/**
 * Created by NienLe on 11,October,2018
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class EncryptionManager
/**
 *
 * @param context application context
 * @param prefStore backing store for storing information
 * @param keyAliasPrefix prefix for key aliases
 * @param bitShiftingKey a key to use for randomization (seed) and bit shifting, this enhances
 * the security on older OS versions a bit
 * @param recoveryHandler callback/listener for recovery notification
 * @throws IOException
 * @throws NoSuchAlgorithmException
 * @throws InvalidAlgorithmParameterException
 * @throws NoSuchProviderException
 * @throws NoSuchPaddingException
 * @throws CertificateException
 * @throws KeyStoreException
 * @throws UnrecoverableEntryException
 * @throws InvalidKeyException
 * @throws IllegalStateException
 */
@Throws(IOException::class, NoSuchAlgorithmException::class, InvalidAlgorithmParameterException::class, NoSuchProviderException::class, NoSuchPaddingException::class, CertificateException::class, KeyStoreException::class, UnrecoverableEntryException::class, InvalidKeyException::class, IllegalStateException::class)
constructor(private val mContext: Context, internal var mPrefs: SharedPreferences,  keyAliasPrefix: String?,
            val SHIFTING_KEY: ByteArray?, internal var mRecoveryHandler: KeyStoreRecoveryNotifier?) {
    private val RSA_BIT_LENGTH = 2048
    private val AES_BIT_LENGTH = 256
    private val MAC_BIT_LENGTH = 256
    private val GCM_TAG_LENGTH = 128

    private val COMPAT_IV_LENGTH = 16
    private val IV_LENGTH = 12

    private val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private val SSL_PROVIDER = "AndroidOpenSSL"
    private val BOUNCY_CASTLE_PROVIDER = "BC"

    private val RSA_KEY_ALIAS: String
    protected val AES_KEY_ALIAS: String
    protected val MAC_KEY_ALIAS: String

    private val DELIMITER = "]"

    private val RSA_CIPHER = KEY_ALGORITHM_RSA + "/" +
            BLOCK_MODE_ECB + "/" +
            ENCRYPTION_PADDING_RSA_PKCS1
    private val AES_CIPHER = KEY_ALGORITHM_AES + "/" +
            BLOCK_MODE_GCM + "/" +
            ENCRYPTION_PADDING_NONE
    private val AES_CIPHER_COMPAT = KEY_ALGORITHM_AES + "/" +
            BLOCK_MODE_CBC + "/" +
            ENCRYPTION_PADDING_PKCS7
    private val MAC_CIPHER = MAC_ALGORITHM_HMAC_SHA256

    protected val IS_COMPAT_MODE_KEY_ALIAS: String

    private var mStore: KeyStore? = null
    private var aesKey: SecretKey? = null
    private var macKey: SecretKey? = null

    private var publicKey: RSAPublicKey? = null
    private var privateKey: RSAPrivateKey? = null

    private val mKeyAliasPrefix: String

    private var isCompatMode = false

    internal val iv: ByteArray
        @Throws(UnsupportedEncodingException::class)
        get() {
            val iv: ByteArray
            if (!isCompatMode) {
                iv = ByteArray(IV_LENGTH)
            } else {
                iv = ByteArray(COMPAT_IV_LENGTH)
            }
            val rng = SecureRandom()
            rng.nextBytes(iv)
            return iv
        }

    /**
     * @param context
     * @param prefStore
     * @param recoveryNotifier
     * @throws IOException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws UnrecoverableEntryException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws NoSuchProviderException
     */
    @Deprecated("Use the full constructor for better security on older versions of Android\n" +
            "      ")
    @Throws(IOException::class, CertificateException::class, NoSuchAlgorithmException::class, KeyStoreException::class, UnrecoverableEntryException::class, InvalidAlgorithmParameterException::class, NoSuchPaddingException::class, InvalidKeyException::class, NoSuchProviderException::class)
    constructor(context: Context, prefStore: SharedPreferences, recoveryNotifier: KeyStoreRecoveryNotifier) : this(context, prefStore, null, null, recoveryNotifier) {
    }

    init {
        var keyAliasPrefix = keyAliasPrefix

        keyAliasPrefix = mPrefs.getString(getHashed(OVERRIDING_KEY_ALIAS_PREFIX_NAME), keyAliasPrefix)
        mKeyAliasPrefix = keyAliasPrefix ?: DEFAULT_KEY_ALIAS_PREFIX
        IS_COMPAT_MODE_KEY_ALIAS = String.format("%s_%s", mKeyAliasPrefix, IS_COMPAT_MODE_KEY_ALIAS_NAME)
        RSA_KEY_ALIAS = String.format("%s_%s", mKeyAliasPrefix, RSA_KEY_ALIAS_NAME)
        AES_KEY_ALIAS = String.format("%s_%s", mKeyAliasPrefix, AES_KEY_ALIAS_NAME)
        MAC_KEY_ALIAS = String.format("%s_%s", mKeyAliasPrefix, MAC_KEY_ALIAS_NAME)

        val isCompatKey = getHashed(IS_COMPAT_MODE_KEY_ALIAS)
        isCompatMode = mPrefs.getBoolean(isCompatKey, Build.VERSION.SDK_INT < Build.VERSION_CODES.M)

        loadKeyStore()

        var tryAgain = false

        try {
            setup(mContext, mPrefs, SHIFTING_KEY)
        } catch (ex: Exception) {
            if (isRecoverableError(ex))
                tryAgain = tryRecovery(ex)
            else
                throw ex
        }

        if (tryAgain) {
            setup(mContext, mPrefs, SHIFTING_KEY)
        }
    }

    internal fun <T : Exception> isRecoverableError(error: T): Boolean {
        return (error is KeyStoreException
                || error is UnrecoverableEntryException
                || error is InvalidKeyException
                || error is IllegalStateException
                || error is IOException && error.cause != null && error.cause is BadPaddingException)
    }

    @Throws(NoSuchPaddingException::class, InvalidKeyException::class, NoSuchAlgorithmException::class, KeyStoreException::class, UnrecoverableEntryException::class, NoSuchProviderException::class, InvalidAlgorithmParameterException::class, IOException::class)
    internal fun setup(context: Context, prefStore: SharedPreferences,  seed: ByteArray?) {
        val keyGenerated = generateKey(context, seed, prefStore)
        if (keyGenerated) {
            //store the alias prefix
            mPrefs.edit().putString(getHashed(OVERRIDING_KEY_ALIAS_PREFIX_NAME), mKeyAliasPrefix).commit()
        }

        loadKey(prefStore)
    }

    internal fun <T : Exception> tryRecovery(e: T): Boolean {
        return mRecoveryHandler != null && mRecoveryHandler!!.onRecoveryRequired(e, mStore!!, keyAliases())
    }

    internal fun keyAliases(): List<String> {
        return Arrays.asList(AES_KEY_ALIAS, RSA_KEY_ALIAS)
    }

    /**
     * Tries to recover once if a Keystore error occurs
     * @param bytes
     * @return
     * @throws NoSuchPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws NoSuchProviderException
     * @throws InvalidKeyException
     */
    @Throws(NoSuchPaddingException::class, InvalidAlgorithmParameterException::class, NoSuchAlgorithmException::class, IOException::class, BadPaddingException::class, IllegalBlockSizeException::class, NoSuchProviderException::class, InvalidKeyException::class, KeyStoreException::class, UnrecoverableEntryException::class)
    fun tryEncrypt(bytes: ByteArray): EncryptedData? {
        var result: EncryptedData? = null
        var tryAgain = false

        try {
            result = encrypt(bytes)
        } catch (ex: Exception) {
            if (isRecoverableError(ex))
                tryAgain = tryRecovery(ex)
            else
                throw ex
        }

        if (tryAgain) {
            setup(mContext, mPrefs, null)
            result = encrypt(bytes)
        }

        return result
    }

    /**
     * Doesn't delete the original file.
     * @param fileIn file to encrypt
     * @param fileOut file to write encrypted data
     * @throws IOException
     * @throws NoSuchProviderException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws NoSuchPaddingException
     * @throws KeyStoreException
     * @throws UnrecoverableEntryException
     */
    @Throws(IOException::class, NoSuchProviderException::class, InvalidAlgorithmParameterException::class, NoSuchAlgorithmException::class, InvalidKeyException::class, NoSuchPaddingException::class, KeyStoreException::class, UnrecoverableEntryException::class)
    fun tryEncrypt(fileIn: BufferedInputStream, fileOut: BufferedOutputStream) {
        var tryAgain = false

        try {
            encrypt(fileIn, fileOut)
        } catch (ex: Exception) {
            if (isRecoverableError(ex))
                tryAgain = tryRecovery(ex)
            else
                throw ex
        }

        if (tryAgain) {
            setup(mContext, mPrefs, null)
            encrypt(fileIn, fileOut)
        }
    }

    /**
     * Doesn't delete the original file.
     * @param fileIn file to decrypt
     * @param fileOut file to write decrypted data
     * @throws IOException
     * @throws NoSuchProviderException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws NoSuchPaddingException
     * @throws KeyStoreException
     * @throws UnrecoverableEntryException
     */
    @Throws(IOException::class, NoSuchProviderException::class, InvalidAlgorithmParameterException::class, NoSuchAlgorithmException::class, InvalidKeyException::class, NoSuchPaddingException::class, KeyStoreException::class, UnrecoverableEntryException::class)
    fun tryDecrypt(fileIn: BufferedInputStream, fileOut: BufferedOutputStream) {
        var tryAgain = false

        try {
            decrypt(fileIn, fileOut)
        } catch (ex: Exception) {
            if (isRecoverableError(ex))
                tryAgain = tryRecovery(ex)
            else
                throw ex
        }

        if (tryAgain) {
            setup(mContext, mPrefs, null)
            decrypt(fileIn, fileOut)
        }
    }

    /**
     * tries recovery once if a Keystore error occurs
     * @param data
     * @return
     * @throws NoSuchPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws UnrecoverableEntryException
     * @throws NoSuchProviderException
     * @throws InvalidKeyException
     * @throws IOException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws InvalidMacException
     */
    @Throws(NoSuchPaddingException::class, InvalidAlgorithmParameterException::class, NoSuchAlgorithmException::class, KeyStoreException::class, UnrecoverableEntryException::class, NoSuchProviderException::class, InvalidKeyException::class, IOException::class, BadPaddingException::class, IllegalBlockSizeException::class, InvalidMacException::class)
    fun tryDecrypt(data: EncryptedData): ByteArray? {
        var result: ByteArray? = null

        var tryAgain = false

        try {
            result = decrypt(data)
        } catch (ex: Exception) {
            if (isRecoverableError(ex))
                tryAgain = tryRecovery(ex)
            else
                throw ex
        }

        if (tryAgain) {
            setup(mContext, mPrefs, null)
            result = decrypt(data)
        }

        return result
    }

    /**
     * @param bytes
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws IOException
     * @throws BadPaddingException
     * @throws NoSuchProviderException
     * @throws IllegalBlockSizeException
     * @throws InvalidAlgorithmParameterException
     */
    @Throws(NoSuchPaddingException::class, NoSuchAlgorithmException::class, InvalidKeyException::class, IOException::class, BadPaddingException::class, NoSuchProviderException::class, IllegalBlockSizeException::class, InvalidAlgorithmParameterException::class)
    fun encrypt(bytes: ByteArray?): EncryptedData? {
        if (bytes != null && bytes.size > 0) {
            val IV = iv
            return if (isCompatMode)
                encryptAESCompat(bytes, IV)
            else
                encryptAES(bytes, IV)
        }

        return null
    }

    /**
     *
     * @param data
     * @return
     * @throws IOException
     * @throws NoSuchPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidMacException
     * @throws NoSuchProviderException
     * @throws InvalidKeyException
     */
    @Throws(IOException::class, NoSuchPaddingException::class, InvalidAlgorithmParameterException::class, NoSuchAlgorithmException::class, IllegalBlockSizeException::class, BadPaddingException::class, InvalidMacException::class, NoSuchProviderException::class, InvalidKeyException::class)
    fun decrypt(data: EncryptedData?): ByteArray? {
        return if (data != null && data.encryptedData != null) {
            if (isCompatMode)
                decryptAESCompat(data)
            else
                decryptAES(data)
        } else null

    }

    /**
     *
     * @param text
     * @return base64 encoded encrypted data
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IOException
     * @throws IllegalBlockSizeException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchProviderException
     * @throws BadPaddingException
     */
    @Throws(InvalidKeyException::class, NoSuchAlgorithmException::class, NoSuchPaddingException::class, IOException::class, IllegalBlockSizeException::class, InvalidAlgorithmParameterException::class, NoSuchProviderException::class, BadPaddingException::class, KeyStoreException::class, UnrecoverableEntryException::class)
    internal fun encrypt(text: String?): String? {
        if (text != null && text.length > 0) {
            val encrypted = tryEncrypt(text.toByteArray(charset(DEFAULT_CHARSET)))
            return encodeEncryptedData(encrypted)
        }

        return null
    }

    /**
     *
     * @param text base64 encoded encrypted data
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IOException
     * @throws IllegalBlockSizeException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchProviderException
     * @throws BadPaddingException
     */
    @Throws(IOException::class, NoSuchPaddingException::class, InvalidKeyException::class, NoSuchAlgorithmException::class, IllegalBlockSizeException::class, BadPaddingException::class, InvalidMacException::class, NoSuchProviderException::class, InvalidAlgorithmParameterException::class, KeyStoreException::class, UnrecoverableEntryException::class)
    internal fun decrypt(text: String?): String? {
        if (text != null && text.length > 0) {
            val encryptedData = decodeEncryptedText(text)
            val decrypted = tryDecrypt(encryptedData)

            return String(decrypted!!, 0, decrypted!!.size, Charset.defaultCharset())
        }

        return null
    }

    /**
     *
     * @param fileIn file to encrypt
     * @param fileOut file to store encrypted data
     * @throws IOException
     * @throws NoSuchProviderException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws NoSuchPaddingException
     */
    @Throws(IOException::class, NoSuchProviderException::class, InvalidAlgorithmParameterException::class, NoSuchAlgorithmException::class, InvalidKeyException::class, NoSuchPaddingException::class)
    fun encrypt(fileIn: BufferedInputStream, fileOut: BufferedOutputStream) {
        val IV = iv
        val cipher = if (isCompatMode) getCipherAESCompat(IV, true) else getCipherAES(IV, true)
        val cipherOut = CipherOutputStream(fileOut, cipher)

        //store IV
        fileOut.write(IV)

        val buffer = ByteArray(4096)
        var read = fileIn.read(buffer)

        while (read != -1) {
            read = fileIn.read(buffer)
            cipherOut.write(buffer, 0, read)
        }

        //TODO: find a way to compute MAC iteratively without loading the whole file in memory

        cipherOut.flush()
        cipherOut.close()

        fileIn.close()
    }

    /**
     *
     * @param fileIn encrypted file
     * @param fileOut file to store decrypted data
     * @throws IOException
     * @throws NoSuchProviderException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws NoSuchPaddingException
     */
    @Throws(IOException::class, NoSuchProviderException::class, InvalidAlgorithmParameterException::class, NoSuchAlgorithmException::class, InvalidKeyException::class, NoSuchPaddingException::class)
    fun decrypt(fileIn: BufferedInputStream, fileOut: BufferedOutputStream) {
        val IVLength = if (isCompatMode) COMPAT_IV_LENGTH else IV_LENGTH
        val IV = ByteArray(IVLength)

        var read = fileIn.read(IV, 0, IVLength)

        if (read == -1 || read != IVLength) throw IllegalArgumentException("Unexpected encryption state")

        //TODO: find a way to validate MAC iteratively without loading the whole file in memory

        val cipher = if (isCompatMode) getCipherAESCompat(IV, false) else getCipherAES(IV, false)
        val cipherIn = CipherInputStream(fileIn, cipher)

        val buffer = ByteArray(4096)

        read = cipherIn.read(buffer)
        while (read != -1) {
            read = cipherIn.read(buffer)
            fileOut.write(buffer, 0, read)
        }
        fileOut.flush()
        fileOut.close()

        cipherIn.close()
    }

    internal fun encodeEncryptedData(data: EncryptedData?): String {
        return if (data!!.mac != null) {
            base64Encode(data.iv) + DELIMITER + base64Encode(data.encryptedData) + DELIMITER + base64Encode(data.mac)
        } else {
            base64Encode(data.iv) + DELIMITER + base64Encode(data.encryptedData)
        }
    }

    internal fun decodeEncryptedText(text: String): EncryptedData {
        val result = EncryptedData()
        val parts = text.split(DELIMITER.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        result.iv = base64Decode(parts[0])
        result.encryptedData = base64Decode(parts[1])

        if (parts.size > 2) {
            result.mac = base64Decode(parts[2])
        }

        return result
    }

    @Throws(KeyStoreException::class, CertificateException::class, NoSuchAlgorithmException::class, IOException::class)
    internal fun loadKeyStore() {
        mStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
        mStore!!.load(null)
    }

    /**
     *
     * @param IV Initialisation Vector
     * @param modeEncrypt if true then cipher is for encryption, decryption otherwise
     * @return a Cipher
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Throws(NoSuchPaddingException::class, NoSuchAlgorithmException::class, InvalidAlgorithmParameterException::class, InvalidKeyException::class)
    internal fun getCipherAES(IV: ByteArray?, modeEncrypt: Boolean): Cipher {
        val cipher = Cipher.getInstance(AES_CIPHER)
        cipher.init(if (modeEncrypt) Cipher.ENCRYPT_MODE else Cipher.DECRYPT_MODE, aesKey, GCMParameterSpec(GCM_TAG_LENGTH, IV))

        return cipher
    }

    /**
     *
     * @param bytes
     * @param IV
     * @return IV and Encrypted data
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws UnsupportedEncodingException
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Throws(NoSuchPaddingException::class, NoSuchAlgorithmException::class, InvalidAlgorithmParameterException::class, InvalidKeyException::class, BadPaddingException::class, IllegalBlockSizeException::class, UnsupportedEncodingException::class)
    internal fun encryptAES(bytes: ByteArray, IV: ByteArray): EncryptedData {
        val cipher = getCipherAES(IV, true)
        val result = EncryptedData()
        result.iv = cipher.iv
        result.encryptedData = cipher.doFinal(bytes)

        return result
    }

    /**
     *
     * @param encryptedData - IV and Encrypted data
     * @return decrypted data
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws UnsupportedEncodingException
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Throws(NoSuchPaddingException::class, NoSuchAlgorithmException::class, InvalidAlgorithmParameterException::class, InvalidKeyException::class, BadPaddingException::class, IllegalBlockSizeException::class, UnsupportedEncodingException::class)
    internal fun decryptAES(encryptedData: EncryptedData): ByteArray {
        val cipher = getCipherAES(encryptedData.iv, false)
        return cipher.doFinal(encryptedData.encryptedData)
    }

    @Throws(NoSuchPaddingException::class, NoSuchAlgorithmException::class, NoSuchProviderException::class, InvalidAlgorithmParameterException::class, InvalidKeyException::class)
    internal fun getCipherAESCompat(IV: ByteArray?, modeEncrypt: Boolean): Cipher {
        val c = Cipher.getInstance(AES_CIPHER_COMPAT, BOUNCY_CASTLE_PROVIDER)
        c.init(if (modeEncrypt) Cipher.ENCRYPT_MODE else Cipher.DECRYPT_MODE, aesKey, IvParameterSpec(IV))

        return c
    }

    /**
     *
     * @param bytes
     * @param IV
     * @return IV, Encrypted Data and Mac
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws UnsupportedEncodingException
     * @throws InvalidAlgorithmParameterException
     */
    @Throws(NoSuchPaddingException::class, NoSuchAlgorithmException::class, NoSuchProviderException::class, InvalidKeyException::class, BadPaddingException::class, IllegalBlockSizeException::class, UnsupportedEncodingException::class, InvalidAlgorithmParameterException::class)
    internal fun encryptAESCompat(bytes: ByteArray, IV: ByteArray): EncryptedData {
        val c = getCipherAESCompat(IV, true)
        val result = EncryptedData()
        result.iv = c.iv
        result.encryptedData = c.doFinal(bytes)
        result.mac = computeMac(result.dataForMacComputation)

        return result
    }

    @Throws(UnsupportedEncodingException::class, NoSuchAlgorithmException::class, InvalidKeyException::class, NoSuchProviderException::class, NoSuchPaddingException::class, InvalidAlgorithmParameterException::class, BadPaddingException::class, IllegalBlockSizeException::class, InvalidMacException::class)
    internal fun decryptAESCompat(encryptedData: EncryptedData): ByteArray {
        if (verifyMac(encryptedData.mac, encryptedData.dataForMacComputation)) {
            val c = getCipherAESCompat(encryptedData.iv, false)
            return c.doFinal(encryptedData.encryptedData)
        } else
            throw InvalidMacException()
    }

    @Throws(KeyStoreException::class, UnrecoverableEntryException::class, NoSuchAlgorithmException::class, NoSuchPaddingException::class, NoSuchProviderException::class, InvalidKeyException::class, IOException::class)
    internal fun loadKey(prefStore: SharedPreferences) {
        if (!isCompatMode) {
            if (mStore!!.containsAlias(AES_KEY_ALIAS) && mStore!!.entryInstanceOf(AES_KEY_ALIAS, KeyStore.SecretKeyEntry::class.java)) {
                val entry = mStore!!.getEntry(AES_KEY_ALIAS, null) as KeyStore.SecretKeyEntry
                aesKey = entry.secretKey
            }
        } else {
            aesKey = getFallbackAESKey(prefStore)
            macKey = getMacKey(prefStore)
        }
    }

    @Throws(KeyStoreException::class, NoSuchProviderException::class, NoSuchAlgorithmException::class, InvalidAlgorithmParameterException::class, UnrecoverableEntryException::class, NoSuchPaddingException::class, InvalidKeyException::class, IOException::class)
    internal fun generateKey(context: Context,  seed: ByteArray?, prefStore: SharedPreferences): Boolean {
        var keyGenerated = false

        if (!isCompatMode) {
            keyGenerated = generateAESKey(seed)
        } else {
            keyGenerated = generateRSAKeys(context, seed)
            loadRSAKeys()
            keyGenerated = generateFallbackAESKey(prefStore, seed) || keyGenerated
            keyGenerated = generateMacKey(prefStore, seed) || keyGenerated
        }

        return keyGenerated
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Throws(KeyStoreException::class, NoSuchProviderException::class, NoSuchAlgorithmException::class, InvalidAlgorithmParameterException::class)
    internal fun generateAESKey( seed: ByteArray?): Boolean {
        if (!mStore!!.containsAlias(AES_KEY_ALIAS)) {
            val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)

            val spec = KeyGenParameterSpec.Builder(AES_KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setCertificateSubject(X500Principal("CN = Secured Preference Store, O = Devliving Online"))
                    .setCertificateSerialNumber(BigInteger.ONE)
                    .setKeySize(AES_BIT_LENGTH)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(false) //TODO: set to true and let the Cipher generate a secured IV
                    .build()
            if (seed != null && seed.size > 0) {
                val random = SecureRandom(seed)
                keyGen.init(spec, random)
            } else {
                keyGen.init(spec)
            }

            keyGen.generateKey()

            return true
        }

        return false
    }

    @Throws(IOException::class, NoSuchAlgorithmException::class, NoSuchPaddingException::class, InvalidKeyException::class, KeyStoreException::class, NoSuchProviderException::class, UnrecoverableEntryException::class)
    internal fun generateFallbackAESKey(prefStore: SharedPreferences,  seed: ByteArray?): Boolean {
        val key = getHashed(AES_KEY_ALIAS)

        if (!prefStore.contains(key)) {
            val keyGen = KeyGenerator.getInstance(KEY_ALGORITHM_AES)

            if (seed != null && seed.size > 0) {
                val random = SecureRandom(seed)
                keyGen.init(AES_BIT_LENGTH, random)
            } else {
                keyGen.init(AES_BIT_LENGTH)
            }

            val sKey = keyGen.generateKey()

            val shiftedEncodedKey = xorWithKey(sKey.encoded, SHIFTING_KEY)
            val encryptedData = RSAEncrypt(shiftedEncodedKey)

            val AESKey = base64Encode(encryptedData)
            val result = prefStore.edit().putString(key, AESKey).commit()
            val isCompatKey = getHashed(IS_COMPAT_MODE_KEY_ALIAS)
            prefStore.edit().putBoolean(isCompatKey, true).apply()
            return result
        }

        return false
    }

    @Throws(NoSuchPaddingException::class, InvalidKeyException::class, NoSuchAlgorithmException::class, KeyStoreException::class, NoSuchProviderException::class, UnrecoverableEntryException::class, IOException::class)
    internal fun generateMacKey(prefStore: SharedPreferences,  seed: ByteArray?): Boolean {
        val key = getHashed(MAC_KEY_ALIAS)

        if (!prefStore.contains(key)) {
            val randomBytes = ByteArray(MAC_BIT_LENGTH / 8)
            val rng: SecureRandom
            if (seed != null && seed.size > 0) {
                rng = SecureRandom(seed)
            } else {
                rng = SecureRandom()
            }

            rng.nextBytes(randomBytes)

            val encryptedKey = RSAEncrypt(randomBytes)
            val macKey = base64Encode(encryptedKey)
            return prefStore.edit().putString(key, macKey).commit()
        }

        return false
    }

    private fun xorWithKey(a: ByteArray, key: ByteArray?): ByteArray {
        if (key == null || key.size == 0) return a

        val out = ByteArray(a.size)
        for (i in a.indices) {
            out[i] = (a[i] xor key[i % key.size]).toByte()
        }
        return out
    }

    @Throws(IOException::class, NoSuchAlgorithmException::class, InvalidKeyException::class, NoSuchProviderException::class, NoSuchPaddingException::class)
    internal fun getFallbackAESKey(prefStore: SharedPreferences): SecretKey? {
        val key = getHashed(AES_KEY_ALIAS)

        val base64Value = prefStore.getString(key, null)
        if (base64Value != null) {
            val encryptedData = base64Decode(base64Value)
            val shiftedEncodedKey = RSADecrypt(encryptedData)
            val keyData = xorWithKey(shiftedEncodedKey, SHIFTING_KEY)

            return SecretKeySpec(keyData, "AES")
        }

        return null
    }

    @Throws(IOException::class, NoSuchAlgorithmException::class, InvalidKeyException::class, NoSuchProviderException::class, NoSuchPaddingException::class)
    internal fun getMacKey(prefStore: SharedPreferences): SecretKey? {
        val key = getHashed(MAC_KEY_ALIAS)

        val base64 = prefStore.getString(key, null)
        if (base64 != null) {
            val encryptedKey = base64Decode(base64)
            val keyData = RSADecrypt(encryptedKey)

            return SecretKeySpec(keyData, MAC_CIPHER)
        }

        return null
    }

    @Throws(KeyStoreException::class, UnrecoverableEntryException::class, NoSuchAlgorithmException::class)
    internal fun loadRSAKeys() {
        if (mStore!!.containsAlias(RSA_KEY_ALIAS) && mStore!!.entryInstanceOf(RSA_KEY_ALIAS, KeyStore.PrivateKeyEntry::class.java)) {
            val entry = mStore!!.getEntry(RSA_KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
            publicKey = entry.certificate.publicKey as RSAPublicKey
            privateKey = entry.privateKey as RSAPrivateKey
        }
    }

    @Throws(NoSuchProviderException::class, NoSuchAlgorithmException::class, InvalidAlgorithmParameterException::class, KeyStoreException::class)
    internal fun generateRSAKeys(context: Context, seed: ByteArray?): Boolean {
        if (!mStore!!.containsAlias(RSA_KEY_ALIAS)) {
            val keyGen = KeyPairGenerator.getInstance(KEY_ALGORITHM_RSA, KEYSTORE_PROVIDER)

            val spec: KeyPairGeneratorSpec
            val start = Calendar.getInstance()
            //probable fix for the timezone issue
            start.add(Calendar.HOUR_OF_DAY, -26)

            val end = Calendar.getInstance()
            end.add(Calendar.YEAR, 100)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                spec = KeyPairGeneratorSpec.Builder(context)
                        .setAlias(RSA_KEY_ALIAS)
                        .setKeySize(RSA_BIT_LENGTH)
                        .setKeyType(KeyProperties.KEY_ALGORITHM_RSA)
                        .setSerialNumber(BigInteger.ONE)
                        .setSubject(X500Principal("CN = Secured Preference Store, O = Devliving Online"))
                        .setStartDate(start.time)
                        .setEndDate(end.time)
                        .build()
            } else {
                spec = KeyPairGeneratorSpec.Builder(context)
                        .setAlias(RSA_KEY_ALIAS)
                        .setSerialNumber(BigInteger.ONE)
                        .setSubject(X500Principal("CN = Secured Preference Store, O = Devliving Online"))
                        .setStartDate(start.time)
                        .setEndDate(end.time)
                        .build()
            }

            if (seed != null && seed.size > 0) {
                val random = SecureRandom(seed)
                keyGen.initialize(spec, random)
            } else {
                keyGen.initialize(spec)
            }
            keyGen.generateKeyPair()

            return true
        }

        return false
    }

    @Throws(NoSuchAlgorithmException::class, InvalidKeyException::class)
    internal fun computeMac(data: ByteArray): ByteArray {
        val HmacSha256 = Mac.getInstance(MAC_CIPHER)
        HmacSha256.init(macKey)
        return HmacSha256.doFinal(data)
    }

    @Throws(InvalidKeyException::class, NoSuchAlgorithmException::class)
    internal fun verifyMac(mac: ByteArray?, data: ByteArray?): Boolean {
        if (mac != null && data != null) {
            val actualMac = computeMac(data)

            if (actualMac.size != mac.size) {
                return false
            }
            var result = 0
            for (i in actualMac.indices) {
                result = result or ((actualMac[i] xor mac[i]).toInt())
            }
            return result == 0
        }

        return false
    }

    @Throws(KeyStoreException::class, UnrecoverableEntryException::class, NoSuchAlgorithmException::class, NoSuchProviderException::class, NoSuchPaddingException::class, InvalidKeyException::class, IOException::class)
    internal fun RSAEncrypt(bytes: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(RSA_CIPHER, SSL_PROVIDER)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)

        val outputStream = ByteArrayOutputStream()
        val cipherOutputStream = CipherOutputStream(outputStream, cipher)
        cipherOutputStream.write(bytes)
        cipherOutputStream.close()

        return outputStream.toByteArray()
    }

    @Throws(NoSuchPaddingException::class, NoSuchAlgorithmException::class, NoSuchProviderException::class, InvalidKeyException::class, IOException::class)
    internal fun RSADecrypt(bytes: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(RSA_CIPHER, SSL_PROVIDER)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)

        val cipherInputStream = CipherInputStream(ByteArrayInputStream(bytes), cipher)

        val values = ArrayList<Byte>()
        var nextByte = cipherInputStream.read()

        while (nextByte !== -1) {
            values.add(nextByte.toByte())
            nextByte = cipherInputStream.read()
        }

        val dbytes = ByteArray(values.size)
        for (i in dbytes.indices) {
            dbytes[i] = values[i]
        }

        cipherInputStream.close()
        return dbytes
    }

    class EncryptedData {
        var iv: ByteArray? = null
        var encryptedData: ByteArray? = null
        var mac: ByteArray? = null

        /**
         * @return IV + CIPHER
         */
        internal val dataForMacComputation: ByteArray
            get() {
                val combinedData = ByteArray(iv!!.size + encryptedData!!.size)
                System.arraycopy(iv, 0, combinedData, 0, iv!!.size)
                System.arraycopy(encryptedData, 0, combinedData, iv!!.size, encryptedData!!.size)

                return combinedData
            }

        constructor() {
            iv = null
            encryptedData = null
            mac = null
        }

        constructor(IV: ByteArray, encryptedData: ByteArray, mac: ByteArray) {
            this.iv = IV
            this.encryptedData = encryptedData
            this.mac = mac
        }
    }

    inner class InvalidMacException : GeneralSecurityException("Invalid Mac, failed to verify integrity.")

    companion object {

        private val DEFAULT_CHARSET = "UTF-8"

        private val RSA_KEY_ALIAS_NAME = "rsa_key"
        private val AES_KEY_ALIAS_NAME = "aes_key"
        private val MAC_KEY_ALIAS_NAME = "mac_key"

        protected val OVERRIDING_KEY_ALIAS_PREFIX_NAME = "OverridingAlias"
        protected val DEFAULT_KEY_ALIAS_PREFIX = "sps"

        private val KEY_ALGORITHM_AES = "AES"
        private val KEY_ALGORITHM_RSA = "RSA"

        private val BLOCK_MODE_ECB = "ECB"
        private val BLOCK_MODE_GCM = "GCM"
        private val BLOCK_MODE_CBC = "CBC"

        private val ENCRYPTION_PADDING_RSA_PKCS1 = "PKCS1Padding"
        private val ENCRYPTION_PADDING_PKCS7 = "PKCS7Padding"
        private val ENCRYPTION_PADDING_NONE = "NoPadding"
        private val MAC_ALGORITHM_HMAC_SHA256 = "HmacSHA256"
        private val IS_COMPAT_MODE_KEY_ALIAS_NAME = "data_in_compat"

        @Throws(NoSuchAlgorithmException::class, UnsupportedEncodingException::class)
        fun getHashed(text: String): String {
            val digest = MessageDigest.getInstance("SHA-256")

            val result = digest.digest(text.toByteArray(charset(DEFAULT_CHARSET)))

            return toHex(result)
        }

        internal fun toHex(data: ByteArray): String {
            val sb = StringBuilder()

            for (b in data) {
                sb.append(String.format("%02X", b))
            }

            return sb.toString()
        }

        fun base64Encode(data: ByteArray?): String {
            return Base64.encodeToString(data, Base64.NO_WRAP)
        }

        fun base64Decode(text: String): ByteArray {
            return Base64.decode(text, Base64.NO_WRAP)
        }
    }
}