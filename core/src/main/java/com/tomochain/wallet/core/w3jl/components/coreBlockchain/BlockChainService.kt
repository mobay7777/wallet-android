package com.tomochain.wallet.core.w3jl.components.coreBlockchain

import com.tomochain.wallet.core.common.BaseService
import com.tomochain.wallet.core.common.Config
import com.tomochain.wallet.core.w3jl.entity.TransactionResult
import com.tomochain.wallet.core.w3jl.listeners.TransactionListener
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt
import java.math.BigInteger

/**
 * Created by cityme on 03,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
interface BlockChainService : BaseService {

    fun getAccountBalance(): Single<BigInteger>
    fun getTransactionCount(): Single<BigInteger>
    fun getTransactionStatus(txId: String?): Observable<String>?

    fun transfer(
        recipient: String,
        amount: BigInteger?,
        payload: String? = null,
        gasPrice: BigInteger? = BigInteger(Config.Transaction.DEFAULT_GAS_PRICE),
        gasLimit: BigInteger? = BigInteger(Config.Transaction.DEFAULT_GAS_LIMIT)
    ) : Observable<TransactionResult>

    fun sendSignedTransaction(signedTransaction: String?): Observable<TransactionResult>


}