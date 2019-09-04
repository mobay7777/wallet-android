package com.tomochain.wallet.core.w3jl.components.coreBlockchain

import com.tomochain.wallet.core.common.BaseService
import com.tomochain.wallet.core.common.Config
import io.reactivex.Single
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
    fun estimateTransactionFee(
        recipient: String,
        amount: BigInteger?,
        payload: String?
    ): Single<BigInteger>
    fun transfer(
        recipient: String,
        amount: BigInteger?,
        gasPrice: BigInteger? = BigInteger(Config.Transaction.DEFAULT_GAS_PRICE),
        gasLimit: BigInteger? = BigInteger(Config.Transaction.DEFAULT_GAS_LIMIT),
        payload: String?
    ): Single<String>
}