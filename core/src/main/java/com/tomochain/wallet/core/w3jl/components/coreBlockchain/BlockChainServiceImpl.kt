package com.tomochain.wallet.core.w3jl.components.coreBlockchain

import android.annotation.SuppressLint
import android.util.Log
import com.tomochain.wallet.core.common.Config
import com.tomochain.wallet.core.common.LogTag
import com.tomochain.wallet.core.common.exception.InsufficientBalanceException
import com.tomochain.wallet.core.common.exception.InvalidAddressException
import com.tomochain.wallet.core.common.exception.WalletNotFoundException
import com.tomochain.wallet.core.components.CoreFunctions
import com.tomochain.wallet.core.habak.EncryptedModel
import com.tomochain.wallet.core.habak.cryptography.Habak
import com.tomochain.wallet.core.room.walletSecret.WalletSecretDAO
import com.tomochain.wallet.core.w3jl.listeners.TransactionListener
import com.tomochain.wallet.core.w3jl.utils.WalletUtil
import com.tomochain.wallet.core.wallet.WalletSecretDataService
import io.reactivex.*
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
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
                            var coreFunctions: CoreFunctions?,
                            var walletSecretDataService: WalletSecretDataService?,
                            var web3j: Web3j?) : BlockChainService {

    override fun setWalletAddress(address: String?) {
        this.address = address
    }


    override fun getAccountBalance(): Single<BigInteger> {
        return Single.create{ emitter ->
            try {
                if (!WalletUtil.isValidAddress(address)){
                    emitter.onError(InvalidAddressException())
                    return@create
                }
                val wallet = coreFunctions?.getWalletByAddress(address!!)?.blockingGet()
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
                val wallet =  coreFunctions?.getWalletByAddress(address!!)?.blockingGet()
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
        payload: String?,
        gasPrice: BigInteger?,
        gasLimit: BigInteger?
    ): Single<String> {
        return Single.create { emitter ->
            try{
                if (!WalletUtil.isValidAddress(address)){
                    emitter.onError(InvalidAddressException())
                    return@create
                }
                val pKey =  walletSecretDataService?.getPrivateKey(address!!)?.blockingGet()
                if(pKey == null || pKey.isEmpty()){
                    emitter.onError(WalletNotFoundException())
                    return@create
                }

                if (getAccountBalance().blockingGet() <= amount){
                    emitter.onError(InsufficientBalanceException(""))
                    return@create
                }
                val credentials = Credentials.create(pKey?.toString())
                pKey.clear()
                val realAmount = amount ?: BigInteger.ZERO
                val from = credentials.address
                val ethGetTransactionCount = web3j?.ethGetTransactionCount(
                    from, DefaultBlockParameterName.LATEST)?.sendAsync()?.get()
                val nonce = ethGetTransactionCount?.transactionCount
                val exactGasLimit = if (gasLimit == null){
                    if (payload.isNullOrEmpty()) BigInteger(Config.Transaction.DEFAULT_GAS_LIMIT) else
                        BigInteger(Config.Transaction.DEFAULT_GAS_LIMIT_WITH_PAYLOAD)
                }else{
                    if (payload.isNullOrEmpty()) gasLimit else
                        BigInteger(Config.Transaction.DEFAULT_GAS_LIMIT_WITH_PAYLOAD)
                }

                val rawTransaction = if (payload == null || payload.isEmpty()){
                    RawTransaction.createEtherTransaction(
                        nonce, gasPrice, exactGasLimit, recipient, realAmount)
                }
                else{
                    RawTransaction.createTransaction(nonce, gasPrice, exactGasLimit, recipient, realAmount, payload)
                }
                val signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials)
                val signedMessageHex  = Numeric.toHexString(signedMessage)
                web3j?.ethSendRawTransaction(signedMessageHex)?.flowable()
                    ?.doOnError {
                        emitter.onError(it)
                    }
                    ?.subscribe ({
                            e -> emitter.onSuccess(e.transactionHash)
                    } , {
                            t -> emitter.onError(t)
                    })

            }catch(t: Throwable){
                Log.e(LogTag.TAG_W3JL,"CoreBlockChainServiceImpl > transfer: ${t.localizedMessage}")
                emitter.onError(t)
            }
        }
    }


    @SuppressLint("CheckResult")
    override fun transfer(
        recipient: String,
        amount: BigInteger?,
        payload: String?,
        gasPrice: BigInteger?,
        gasLimit: BigInteger?,
        callback: TransactionListener?
    ) {
        try{
            if (!WalletUtil.isValidAddress(address)){
                callback?.onTransactionError(InvalidAddressException())
                return
            }
            val pKey = walletSecretDataService?.getPrivateKey(address!!)?.blockingGet()
            if(pKey == null || pKey.isEmpty()){
                callback?.onTransactionError(WalletNotFoundException())
                return
            }
            if (getAccountBalance().blockingGet() <= amount){
                callback?.onTransactionError(InsufficientBalanceException(""))
                return
            }

            val credentials = Credentials.create(pKey.toString())
            pKey.clear()
            val realAmount = amount ?: BigInteger.ZERO
            val from = credentials.address
            val ethGetTransactionCount = web3j?.ethGetTransactionCount(
                from, DefaultBlockParameterName.LATEST)?.sendAsync()?.get()
            val nonce = ethGetTransactionCount?.transactionCount

            Log.e(LogTag.TAG_W3JL,"CoreBlockChainServiceImpl > transfer: $nonce")
            val exactGasLimit = if (gasLimit == null){
                if (payload.isNullOrEmpty()) BigInteger(Config.Transaction.DEFAULT_GAS_LIMIT) else
                    BigInteger(Config.Transaction.DEFAULT_GAS_LIMIT_WITH_PAYLOAD)
            }else{
                if (payload.isNullOrEmpty()) gasLimit else
                    BigInteger(Config.Transaction.DEFAULT_GAS_LIMIT_WITH_PAYLOAD)
            }

            val rawTransaction = if (payload == null || payload.isEmpty()){
                RawTransaction.createEtherTransaction(
                    nonce, gasPrice, exactGasLimit, recipient, realAmount)
            }
            else{
                RawTransaction.createTransaction(nonce, gasPrice, exactGasLimit, recipient, realAmount, payload)
            }
            val signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials)
            val signedMessageHex  = Numeric.toHexString(signedMessage)

            web3j?.ethSendRawTransaction(signedMessageHex)?.flowable()?.subscribe({transaction ->
                callback?.onTransactionCreated(transaction.transactionHash)
                web3j?.ethGetTransactionReceipt(transaction.transactionHash)
                    ?.flowable()
                    ?.toObservable()
                    ?.repeat()
                    ?.map { r ->
                        r.result.status
                    }
                    ?.takeUntil {
                        it.contains("0x", true)
                    }
                    ?.retry()
                    ?.subscribe {
                        callback?.onTransactionComplete(transaction.transactionHash, it)
                    }
            }, {
                callback?.onTransactionError(it as Exception)
            })

        }catch(t: Throwable){
            Log.e(LogTag.TAG_W3JL,"CoreBlockChainServiceImpl > transfer: ${t.localizedMessage}")
            callback?.onTransactionError(t as Exception)
        }
    }


    @SuppressLint("CheckResult")
    override fun sendSignedTransaction(signedTransaction: String?, callback: TransactionListener?) {
        try{
            web3j?.ethSendRawTransaction(signedTransaction)?.flowable()?.subscribe({transaction ->
                callback?.onTransactionCreated(transaction.transactionHash)
                web3j?.ethGetTransactionReceipt(transaction.transactionHash)
                    ?.flowable()
                    ?.toObservable()
                    ?.repeat()
                    ?.map { r ->
                        r.result.status
                    }
                    ?.takeUntil {
                        it.contains("0x", true)
                    }
                    ?.retry()
                    ?.subscribe {
                        callback?.onTransactionComplete(transaction.transactionHash, it)
                    }
            }, {
                callback?.onTransactionError(it as Exception)
            })

        }catch(t: Throwable){
            callback?.onTransactionError(t as Exception)
        }
    }

    override fun getTransactionStatus(txId: String?): Observable<String>? {
        return web3j?.ethGetTransactionReceipt(txId)
            ?.flowable()
            ?.toObservable()
            ?.repeat()
            ?.map { r ->
                r.result.status
            }
            ?.takeUntil {
                it.contains("0x", true)
            }
    }
}