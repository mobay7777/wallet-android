package com.tomochain.wallet.core.w3jl.components.tomochain.token

import com.tomochain.wallet.core.common.Config
import com.tomochain.wallet.core.w3jl.listeners.TransactionListener
import io.reactivex.Single
import java.math.BigInteger

/**
 * Created by NienLe on 2019-09-08,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
interface TRC21Service : TokenService {

    fun transferToken(
            tokenAddress: String,
            recipient: String,
            amount: BigInteger,
            callback: TransactionListener?,
            gasPrice: BigInteger? = BigInteger(Config.Transaction.DEFAULT_GAS_PRICE),
            gasLimit: BigInteger? = null
    )
    fun getTokenTransferFee(tokenAddress: String): Single<BigInteger>
    fun isTomoZApplied(tokenAddress: String) : Single<Boolean>
}