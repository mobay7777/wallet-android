package com.tomochain.wallet.core.w3jl.components.tomochain.token


import com.tomochain.wallet.core.common.Config
import com.tomochain.wallet.core.w3jl.entity.TransactionResult
import com.tomochain.wallet.core.w3jl.listeners.TransactionListener
import io.reactivex.Observable
import io.reactivex.Single
import java.math.BigInteger

/**
 * Created by cityme on 23,May,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
interface TRC20Service : TokenService{

    fun transferToken(
            tokenAddress: String,
            recipient: String,
            amount: BigInteger,
            gasPrice: BigInteger? = BigInteger(Config.Transaction.DEFAULT_GAS_PRICE),
            gasLimit: BigInteger? = null
    ) : Observable<TransactionResult>

    fun estimateTokenTransferGasLimit(tokenAddress: String,
                                 recipient: String,
                                 amount: BigInteger): Single<BigInteger>
}