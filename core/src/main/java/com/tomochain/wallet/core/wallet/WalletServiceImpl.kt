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
import com.tomochain.wallet.core.w3jl.utils.WalletUtil
import io.reactivex.Single
import org.bitcoinj.crypto.ChildNumber
import org.bitcoinj.crypto.HDKeyDerivation
import org.bitcoinj.crypto.HDUtils
import org.bitcoinj.wallet.DeterministicKeyChain
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
                                           hdPath: String): Single<EntityWalletSecret?> {
        return Single.create {
            try{


                if (!WalletUtil.isValidMnemonics(mnemonic, true)){
                    it.onError(InvalidMnemonicException())
                    return@create
                }
                val seed = DeterministicSeed(mnemonic,null,"",System.currentTimeMillis())
                val chain = DeterministicKeyChain.builder().seed(seed).build()
                val keyPath = HDUtils.parsePath(hdPath.toUpperCase().replace("'","H"))
                val key = chain.getKeyByPath(keyPath, true)
                val c = Credentials.create(key.privateKeyAsHex)
                val wallet = EntityWalletSecret(
                    c.address,
                    Calendar.getInstance().timeInMillis,
                    WalletSourceType.MNEMONICS,
                    habak?.encrypt(mnemonic)?.writeToString() ?: "",
                    habak?.encrypt(key.privateKeyAsHex)?.writeToString() ?: "",
                    Config.Database.VERSION,
                    hdPath)
                it.onSuccess(wallet)
            }catch(e: Exception){
                Log.e(LogTag.TAG_W3JL, "createWalletFromMnemonics", e)
                it.onError(e)
            }
        }
    }

    override fun createWalletFromPrivateKey(privateKey: String): Single<EntityWalletSecret?> {
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

    override fun createWalletFromAddress(address: String): Single<EntityWalletSecret?> {
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