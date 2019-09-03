package com.tomochain.wallet.core.wallet

import android.util.Log
import com.tomochain.wallet.core.common.Config
import com.tomochain.wallet.core.common.LogTag
import com.tomochain.wallet.core.common.WalletSourceType
import com.tomochain.wallet.core.common.exception.InvalidAddressException
import com.tomochain.wallet.core.common.exception.InvalidMnemonicException
import com.tomochain.wallet.core.common.exception.InvalidPrivateKeyException
import com.tomochain.wallet.core.habak.cryptography.Habak
import com.tomochain.wallet.core.room.walletSecret.EntityWalletSecret
import io.reactivex.Single
import org.bitcoinj.crypto.ChildNumber
import org.bitcoinj.crypto.HDKeyDerivation
import org.bitcoinj.wallet.DeterministicSeed
import org.web3j.crypto.*
import org.web3j.utils.Numeric
import java.security.SecureRandom
import java.util.*

/**
 * Created by NienLe on 2019-05-09,May,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class WalletServiceImpl(var habak: Habak?) : WalletService {

    override fun getWordList(): List<String> {
        return try{
            MnemonicUtils.getWords()
        }catch(e: Exception){
            Log.e(LogTag.TAG_W3JL, "getWordList",e)
            arrayListOf()
        }
    }

    override fun generateMnemonics(): String {
        return try{
            val initialEntropy = ByteArray(16)
            val secureRandom =  SecureRandom()
            secureRandom.nextBytes(initialEntropy)
            MnemonicUtils.generateMnemonic(initialEntropy)
        }catch(e: Exception){
            Log.e(LogTag.TAG_W3JL, "generateMnemonics",e)
            ""
        }
    }

    override fun createWalletFromMnemonics(mnemonic: String,
                                           hdPath: String,
                                           walletName: String??): Single<EntityWalletSecret?> {
        return Single.create {
            try{
                val pathArray = hdPath.split("/".toRegex()).dropLastWhile {path -> path.isEmpty() }.toTypedArray()
                val passphrase = ""
                val list = mnemonic.split(" ")
                if (list.size != 12){
                    it.onError(InvalidMnemonicException())
                    return@create
                }
                val creationTimeSeconds = System.currentTimeMillis() / 1000
                val ds = DeterministicSeed(list, null, passphrase, creationTimeSeconds)
                val seedBytes = ds.seedBytes
                var dkKey = HDKeyDerivation.createMasterPrivateKey(seedBytes)
                for (i in 1 until pathArray.size) {
                    val childNumber: ChildNumber
                    childNumber = if (pathArray[i].endsWith("'")) {
                        val number = Integer.parseInt(pathArray[i].substring(0,
                            pathArray[i].length - 1))
                        ChildNumber(number, true)
                    } else {
                        val number = Integer.parseInt(pathArray[i])
                        ChildNumber(number, false)
                    }
                    dkKey = HDKeyDerivation.deriveChildKey(dkKey, childNumber)
                }
                val keyPair = ECKeyPair.create(dkKey.privKeyBytes)
                val privateKey = Numeric.toHexStringNoPrefixZeroPadded(keyPair.privateKey, Keys.PRIVATE_KEY_LENGTH_IN_HEX)
                val c = Credentials.create(privateKey)
                val wallet = EntityWalletSecret(
                    c.address,
                    Calendar.getInstance().timeInMillis,
                    WalletSourceType.MNEMONICS,
                    habak?.encrypt(mnemonic)?.writeToString() ?: "",
                    habak?.encrypt(privateKey)?.writeToString() ?: "",
                    Config.Database.VERSION,
                    hdPath)
                it.onSuccess(wallet)
            }catch(e: Exception){
                Log.e(LogTag.TAG_W3JL, "createWalletFromMnemonics", e)
                it.onError(e)
            }
        }
    }

    override fun createWalletFromPrivateKey(privateKey: String,
                                            walletName: String?): Single<EntityWalletSecret?> {
        return Single.create{
            try{
                if (!WalletUtils.isValidPrivateKey(privateKey)){
                    it.onError(InvalidPrivateKeyException())
                    return@create
                }
                val c = Credentials.create(privateKey)

                val wallet = EntityWalletSecret(
                    c.address,
                    Calendar.getInstance().timeInMillis,
                    WalletSourceType.PRIVATEKEY,
                    "",
                    habak?.encrypt(privateKey)?.writeToString() ?: "",
                    Config.Database.VERSION,
                    "")
                it.onSuccess(wallet)
            }catch(e: Exception){
                Log.e(LogTag.TAG_W3JL, "createWalletFromPrivateKey",e)
                it.onError(e)
            }
        }
    }

    override fun createWalletFromAddress(address: String,
                                         walletName: String?): Single<EntityWalletSecret?> {
        return Single.create{
            try{
                if (!WalletUtils.isValidAddress(address)){
                    it.tryOnError(InvalidAddressException())
                    return@create
                }
                val wallet = EntityWalletSecret(
                    address,
                    Calendar.getInstance().timeInMillis,
                    WalletSourceType.ADDRESS,
                    "",
                    "",
                    Config.Database.VERSION,
                    "")
                it.onSuccess(wallet)
            }catch(e: Exception){
                Log.e(LogTag.TAG_W3JL, "createWalletFromPrivateKey",e)
                it.onError(e)
            }
        }
    }


}