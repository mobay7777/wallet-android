package com.tomochain.wallet.core.w3jl.components.coreBlockchain

import android.util.Log
import com.tomochain.wallet.core.common.LogTag
import com.tomochain.wallet.core.common.exception.InvalidAddressException
import com.tomochain.wallet.core.common.exception.WalletNotFoundException
import com.tomochain.wallet.core.habak.EncryptedModel
import com.tomochain.wallet.core.habak.cryptography.Habak
import com.tomochain.wallet.core.room.walletSecret.WalletSecretDAO
import com.tomochain.wallet.core.w3jl.utils.WalletUtil
import io.reactivex.Single
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.utils.Numeric
import java.math.BigInteger

/**
 * Created by cityme on 03,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class BlockChainServiceImpl(var address: String?,
                            var dao: WalletSecretDAO?,
                            var habak: Habak?,
                            var web3j: Web3j?) : BlockChainService {


    override fun getAccountBalance(): Single<BigInteger> {
        return Single.create{ emitter ->
            try {
                if (!WalletUtil.isValidAddress(address)){
                    emitter.onError(InvalidAddressException())
                    return@create
                }
                val wallet = dao?.getWallet(address!!)?.blockingGet()
                if(wallet == null){
                    emitter.onError(WalletNotFoundException())
                    return@create
                }else{
                    web3j?.ethGetBalance(wallet.address, DefaultBlockParameterName.LATEST)
                        ?.flowable()
                        ?.doOnError {
                            emitter.onError(it)
                        }
                        ?.subscribe (
                            {e -> emitter.onSuccess(e.balance)} ,
                            { t -> emitter.onError(t)}
                        )
                }
            }catch (e : Exception){
                //emitter.tryOnError(e)
                Log.e(LogTag.TAG_W3JL,"CoreBlockChainServiceImpl > getAccountBalance: ${e.localizedMessage}")
                emitter.onError(e)
            }
        }
    }

    override fun getTransactionCount(): Single<BigInteger> {
        return Single.create{ emitter ->
            try {
                if (!WalletUtil.isValidAddress(address)){
                    emitter.onError(InvalidAddressException())
                    return@create
                }
                val wallet = dao?.getWallet(address!!)?.blockingGet()
                if(wallet == null){
                    emitter.onError(WalletNotFoundException())
                    return@create
                }else{
                    web3j?.ethGetTransactionCount(wallet.address, DefaultBlockParameterName.LATEST)
                        ?.flowable()
                        ?.doOnError {
                            emitter.onError(it)
                        }
                        ?.subscribe (
                            {e -> emitter.onSuccess(e.transactionCount)} ,
                            { t -> emitter.onError(t)}
                        )
                }
            }catch (e : Exception){
                //emitter.tryOnError(e)
                Log.e(LogTag.TAG_W3JL,"CoreBlockChainServiceImpl > getTransactionCount: ${e.localizedMessage}")
                emitter.onSuccess(BigInteger.ZERO)
            }
        }
    }

    override fun transfer(
        recipient: String,
        amount: BigInteger?,
        gasPrice: BigInteger?,
        gasLimit: BigInteger?,
        payload: String?
    ): Single<String> {
        return Single.create { emitter ->
            try{
                if (!WalletUtil.isValidAddress(address)){
                    emitter.onError(InvalidAddressException())
                    return@create
                }
                val wallet = dao?.getWallet(address!!)?.blockingGet()
                if(wallet == null){
                    emitter.onError(WalletNotFoundException())
                    return@create
                }
                val pKey = habak?.decrypt(EncryptedModel.readFromString(wallet.encryptedPKey))
                val credentials = Credentials.create(pKey?.toString())
                pKey?.clear()
                val realAmount = amount ?: BigInteger.ZERO
                val from = credentials.address
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
                val signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials)
                val signedMessageHex  = Numeric.toHexString(signedMessage)
                web3j?.ethSendRawTransaction(signedMessageHex)?.flowable()
                    ?.doOnError {
                        emitter.onError(it)
                    }
                    ?.subscribe ({e -> emitter.onSuccess(e.transactionHash)} , { t -> emitter.onError(t)})

            }catch(t: Throwable){
                Log.e(LogTag.TAG_W3JL,"CoreBlockChainServiceImpl > transfer: ${t.localizedMessage}")
                emitter.onError(t)
            }
        }
    }
}