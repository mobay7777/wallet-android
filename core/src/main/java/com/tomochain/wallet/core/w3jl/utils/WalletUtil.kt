package com.tomochain.wallet.core.w3jl.utils

import android.util.Log
import com.tomochain.wallet.core.common.LogTag
import org.web3j.crypto.MnemonicUtils
import org.web3j.crypto.WalletUtils
import java.security.SecureRandom

/**
 * Created by NienLe on 2019-05-03,May,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
object WalletUtil {

    fun getWordList(): List<String> {
        return try{
            MnemonicUtils.getWords()
        }catch(e: Exception){
            Log.e(LogTag.TAG_W3JL, "getWordList",e)
            arrayListOf()
        }
    }


    fun isValidAddress(address: String?) : Boolean{
        return WalletUtils.isValidAddress(address)
    }

    fun isValidPrivateKey(privateKey: String?) : Boolean{
        return WalletUtils.isValidPrivateKey(privateKey)
    }

    fun isValidTransactionHash(txHash: String?) : Boolean{
        return txHash?.length == 66 && txHash.startsWith("0x", true)
    }

    fun isValidMnemonics(mnemonics: String?, matchCurrentWordList: Boolean) : Boolean{
        return try{
            mnemonics?.toLowerCase()?.split(" ")?.size == 12
        }catch(t: Throwable){
            Log.e(LogTag.TAG_W3JL, "isValidMnemonics",t)
            false
        }
    }

    fun generateMnemonics(): String {
        val initialEntropy = ByteArray(16)
        val secureRandom =  SecureRandom()
        secureRandom.nextBytes(initialEntropy)

        return MnemonicUtils.generateMnemonic(initialEntropy)
    }
}