package com.tomochain.wallet.core.w3jl.components.tomochain.token

import com.tomochain.wallet.core.common.BaseService
import com.tomochain.wallet.core.w3jl.listeners.TransactionListener
import io.reactivex.Single
import java.math.BigDecimal
import java.math.BigInteger

/**
 * Created by cityme on 23,May,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
interface TRC20Service : BaseService{
    fun getBalance(tokenAddress: String) : Single<BigDecimal>
    fun getName(tokenAddress: String) : Single<String>
    fun getSymbol(tokenAddress: String) : Single<String>
    fun getDecimal(tokenAddress: String) : Single<Int>
    fun transferToken(
        tokenAddress: String,
        recipient: String,
        amount: BigInteger,
        gasPrice: BigInteger,
        gasLimit: BigInteger,
        callback: TransactionListener?
    )
    fun estimateTokenTransferGas(tokenAddress: String): Single<BigInteger>
    fun extractFunction(src: String) : TRC20FunctionType
    fun decodeHexData(src: String) : List<String>
}