package com.tomochain.wallet.core.w3jl.components.coreBlockchain

import io.reactivex.Single
import java.math.BigInteger

/**
 * Created by cityme on 03,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
interface BlockChainService {

    fun getAccountBalance(): Single<BigInteger>
    fun getTransactionCount(): Single<BigInteger>
    fun transfer(
        recipient: String,
        amount: BigInteger?,
        gasPrice: BigInteger?,
        gasLimit: BigInteger?,
        payload: String?
    ): Single<String>
}