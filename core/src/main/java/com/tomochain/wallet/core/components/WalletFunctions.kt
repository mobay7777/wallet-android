package com.tomochain.wallet.core.components

import com.tomochain.wallet.core.common.BaseService
import com.tomochain.wallet.core.common.Config
import com.tomochain.wallet.core.w3jl.entity.SignResult
import com.tomochain.wallet.core.w3jl.entity.TransactionResult

import io.reactivex.Observable

import io.reactivex.Single
import java.math.BigInteger

/**
 * Created by cityme on 16,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
interface WalletFunctions : BaseService {
    fun getBalance(): Single<BigInteger>?
    fun signMessage(message: String?) : Single<SignResult>?
    fun signPersonalMessage(message: String?) : Single<SignResult>?
    fun signTransaction(
        recipient: String,
        amount: BigInteger?,
        gasPrice: BigInteger? = BigInteger(Config.Transaction.DEFAULT_GAS_PRICE),
        gasLimit: BigInteger? = BigInteger(Config.Transaction.DEFAULT_GAS_LIMIT),
        payload: String? = null) : Single<SignResult>?

    fun sendSignedTransaction(signedTransaction: String?): Observable<TransactionResult>?
    fun transfer(
        recipient: String,
        amount: BigInteger?,
        payload: String? = null,
        gasPrice: BigInteger? = BigInteger(Config.Transaction.DEFAULT_GAS_PRICE),
        gasLimit: BigInteger? = BigInteger(Config.Transaction.DEFAULT_GAS_LIMIT)
    ) : Observable<TransactionResult>?
}