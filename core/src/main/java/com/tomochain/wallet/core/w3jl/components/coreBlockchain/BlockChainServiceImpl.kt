package com.tomochain.wallet.core.w3jl.components.coreBlockchain

import android.annotation.SuppressLint
import android.util.Log
import com.tomochain.wallet.core.common.Config
import com.tomochain.wallet.core.common.LogTag
import com.tomochain.wallet.core.common.exception.InsufficientBalanceException
import com.tomochain.wallet.core.common.exception.InvalidAddressException
import com.tomochain.wallet.core.common.exception.InvalidPrivateKeyException
import com.tomochain.wallet.core.common.exception.WalletNotFoundException
import com.tomochain.wallet.core.components.CoreFunctions
import com.tomochain.wallet.core.habak.EncryptedModel
import com.tomochain.wallet.core.habak.cryptography.Habak
import com.tomochain.wallet.core.room.walletSecret.WalletSecretDAO
import com.tomochain.wallet.core.w3jl.entity.TransactionResult
import com.tomochain.wallet.core.w3jl.entity.TransactionStatus
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
import java.lang.IllegalStateException
import java.math.BigInteger


/**
 * Created by cityme on 03,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class BlockChainServiceImpl(var address: String?,
                            private val coreFunctions: CoreFunctions?,
                            private val walletSecretDataService: WalletSecretDataService?,
                            private val web3j: Web3j?) : BlockChainService {

    override fun setWalletAddress(address: String?) {
        this.address = address?.toLowerCase()
        this.walletSecretDataService?.setWalletAddress(this.address)
    }


    override fun getAccountBalance(): Single<BigInteger> {
        return Single.create{ emitter ->
            try {
                if (!WalletUtil.isValidAddress(address)){
                    emitter.onError(InvalidAddressException())
                    return@create
                }
                coreFunctions?.getWalletByAddress(address!!)
                    ?.subscribe({wallet ->
                        web3j?.ethGetBalance(wallet?.address, DefaultBlockParameterName.LATEST)
                            ?.flowable()
                            ?.doOnError {
                                emitter.onError(it)
                            }
                            ?.subscribe (
                                {e ->
                                    emitter.onSuccess(e.balance)
                                },
                                {t ->
                                    emitter.tryOnError(t)
                                }
                            )
                    },{
                        emitter.onError(WalletNotFoundException())
                    })

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
                coreFunctions?.getWalletByAddress(address!!)
                    ?.subscribe({wallet ->
                        web3j?.ethGetTransactionCount(wallet?.address, DefaultBlockParameterName.LATEST)
                            ?.flowable()
                            ?.doOnError {
                                emitter.onError(it)
                            }
                            ?.subscribe (
                                {e -> emitter.onSuccess(e.transactionCount)} ,
                                { t -> emitter.onError(t)}
                            )
                    },{
                        emitter.onError(WalletNotFoundException())
                    })
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
    ): Observable<TransactionResult> {
        return Observable.create {emitter ->
            if (!WalletUtil.isValidAddress(address)){
                emitter.onError(InvalidAddressException())
                return@create
            }
            val pKey = walletSecretDataService?.getPrivateKey()?.blockingGet()
            if(pKey == null || pKey.isEmpty()){
                emitter.onError(WalletNotFoundException())
                return@create
            }

            getAccountBalance()
                .subscribe({balance ->
                    if (balance <= amount){
                        emitter.onError(InsufficientBalanceException(""))
                        return@subscribe
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
                        RawTransaction.createEtherTransaction(nonce, gasPrice, exactGasLimit, recipient, realAmount)
                    }
                    else{
                        RawTransaction.createTransaction(nonce, gasPrice, exactGasLimit, recipient, realAmount, payload)
                    }
                    val signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials)
                    val signedMessageHex  = Numeric.toHexString(signedMessage)

                    val transaction =web3j?.ethSendRawTransaction(signedMessageHex)?.send()
                    if (transaction != null && transaction.transactionHash != null){
                        emitter.onNext(TransactionResult(transaction.transactionHash, TransactionStatus.PENDING))
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
                                Log.d(LogTag.TAG_W3JL,"tx status: $it")
                                emitter.onNext(TransactionResult(transaction.transactionHash, if (it == "0x1") TransactionStatus.SUCCESS
                                else TransactionStatus.FAILED))
                            }
                    }else{
                        emitter.onError(IllegalStateException(transaction?.error?.message))
                    }
                },{
                    emitter.onError(it)
                })
        }
    }


    @SuppressLint("CheckResult")
    override fun sendSignedTransaction(signedTransaction: String?) : Observable<TransactionResult>{
        return Observable.create { emitter ->
            val transaction =web3j?.ethSendRawTransaction(signedTransaction)?.send()
            if (transaction != null && transaction.transactionHash != null){
                emitter.onNext(TransactionResult(transaction.transactionHash, TransactionStatus.PENDING))
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
                        Log.d(LogTag.TAG_W3JL,"tx status: $it")
                        emitter.onNext(TransactionResult(transaction.transactionHash, if (it == "0x1") TransactionStatus.SUCCESS
                        else TransactionStatus.FAILED))
                    }
            }else{
                emitter.onError(IllegalStateException(transaction?.error?.message))
            }
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