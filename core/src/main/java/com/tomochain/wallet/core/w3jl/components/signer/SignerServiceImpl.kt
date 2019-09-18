package com.tomochain.wallet.core.w3jl.components.signer

import android.util.Log
import com.tomochain.wallet.core.common.LogTag.TAG_W3JL
import com.tomochain.wallet.core.common.exception.InvalidPrivateKeyException
import com.tomochain.wallet.core.w3jl.utils.WalletUtil
import com.tomochain.wallet.core.wallet.WalletSecretDataService
import io.reactivex.Single
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.Sign
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.utils.Numeric
import java.math.BigInteger
import java.nio.charset.Charset

/**
 * Created by cityme on 03,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class SignerServiceImpl(var address: String?,
                        private val walletSecretDataService: WalletSecretDataService?,
                        private val web3j: Web3j?) : SignerService {
    override fun setWalletAddress(address: String?) {
        this.address = address?.toLowerCase()
        this.walletSecretDataService?.setWalletAddress(this.address)
    }

    override fun signRawMessage(message: String?): Single<SignResult>? {
        return walletSecretDataService?.getPrivateKey()
            ?.flatMap {pKey ->
                Single.create<SignResult> {
                    try{
                        if (!WalletUtil.isValidPrivateKey(pKey.toString())){
                            it.onSuccess(
                                SignResult(
                                    SignStatus.SIGN_INVALID_CREDENTIAL,
                                    null,
                                    null,
                                    address,
                                    InvalidPrivateKeyException()
                                )
                            )
                            pKey.clear()
                            return@create
                        }
                        if (message == null){
                            it.onSuccess(
                                SignResult(
                                    SignStatus.SIGN_INVALID_INPUT,
                                    null,
                                    null,
                                    address,
                                    Exception("Null or empty input")
                                )
                            )
                            pKey.clear()
                            return@create
                        }

                        val credential = Credentials.create(pKey.toString())
                        pKey.clear()
                        val signature = Sign.signPrefixedMessage(message!!.toByteArray(Charset.defaultCharset()), credential.ecKeyPair)
                        val signed = Numeric.toHexString(signature.r) +
                                Numeric.cleanHexPrefix(Numeric.toHexString(signature.s)) +
                                Integer.toHexString(signature.v.toInt())
                        it.onSuccess(SignResult(
                            SignStatus.SIGN_SUCCESS,
                            message,
                            signed,
                            credential.address,
                            null
                        ))


                    }catch(t: Throwable){
                        Log.e(TAG_W3JL,"SignerServiceImpl > signRawMessage: ${t.localizedMessage}")
                        it.onSuccess(
                            SignResult(
                                SignStatus.SIGN_FAIL,
                                message,
                                null,
                                null,
                                t
                            )
                        )
                    }

                }
            }
    }

    override fun signMessage(message: String?): Single<SignResult>? {
        return signRawMessage( message)
    }

    override fun signPersonalMessage(message: String?): Single<SignResult>? {
        return walletSecretDataService?.getPrivateKey()
            ?.flatMap {pKey ->
                Single.create<SignResult> {
                    try{

                        if (!WalletUtil.isValidPrivateKey(pKey.toString())){
                            it.onSuccess(
                                SignResult(
                                    SignStatus.SIGN_INVALID_CREDENTIAL,
                                    null,
                                    null,
                                    address,
                                    InvalidPrivateKeyException()
                                )
                            )
                            pKey.clear()
                            return@create
                        }
                        if (message == null){
                            it.onSuccess(
                                SignResult(
                                    SignStatus.SIGN_INVALID_INPUT,
                                    null,
                                    null,
                                    address,
                                    Exception("Null or empty input")
                                )
                            )
                            pKey.clear()
                            return@create
                        }

                        val credential = Credentials.create(pKey.toString())
                        pKey.clear()
                        val signature = Sign.signPrefixedMessage(Numeric.hexStringToByteArray(message), credential.ecKeyPair)
                        val signed = Numeric.toHexString(signature.r) +
                                Numeric.cleanHexPrefix(Numeric.toHexString(signature.s)) +
                                Integer.toHexString(signature.v.toInt())
                        it.onSuccess(SignResult(
                            SignStatus.SIGN_SUCCESS,
                            message,
                            signed,
                            credential.address,
                            null
                        ))


                    }catch(t: Throwable){
                        Log.e(TAG_W3JL,"SignerServiceImpl > signPersonalMessage: ${t.localizedMessage}")
                        it.onSuccess(
                            SignResult(
                                SignStatus.SIGN_FAIL,
                                message,
                                null,
                                null,
                                t
                            )
                        )
                    }

                }
            }
    }

    override fun signTransaction(
        recipient: String,
        amount: BigInteger?,
        gasPrice: BigInteger?,
        gasLimit: BigInteger?,
        payload: String?
    ): Single<SignResult>? {
        return walletSecretDataService?.getPrivateKey()
            ?.flatMap {pKey ->
                Single.create<SignResult> {
                    try{
                        if (!WalletUtil.isValidPrivateKey(pKey.toString())){
                            it.onSuccess(
                                SignResult(
                                    SignStatus.SIGN_INVALID_CREDENTIAL,
                                    null,
                                    null,
                                    address,
                                    InvalidPrivateKeyException()
                                )
                            )
                            pKey.clear()
                            return@create
                        }

                        val credential = Credentials.create(pKey.toString())
                        pKey.clear()

                        val realAmount = amount ?: BigInteger.ZERO
                        val from = credential.address
                        val ethGetTransactionCount = web3j?.ethGetTransactionCount(
                            from, DefaultBlockParameterName.LATEST)?.sendAsync()?.get()
                        val nonce = ethGetTransactionCount?.transactionCount
                        val rawTransaction = if (payload == null || payload.isEmpty()){
                            RawTransaction.createEtherTransaction(
                                nonce, gasPrice, gasLimit, recipient, realAmount)
                        }
                        else{
                            RawTransaction.createTransaction(nonce, gasPrice, gasLimit, recipient, realAmount, payload)
                        }
                        val signedMessage = TransactionEncoder.signMessage(rawTransaction, credential)
                        val signedMessageHex  = Numeric.toHexString(signedMessage)

                        it.onSuccess(
                            SignResult(
                                SignStatus.SIGN_SUCCESS,
                                "",
                                signedMessageHex,
                                credential.address,
                                null
                            )
                        )
                        return@create
                    }catch(t: Throwable){
                        Log.e(TAG_W3JL,"SignerServiceImpl > signTransaction: ${t.localizedMessage}")
                        it.onSuccess(
                            SignResult(
                                SignStatus.SIGN_FAIL,
                                null,
                                null,
                                null,
                                t
                            )
                        )
                        return@create
                    }
                }
            }
    }
}