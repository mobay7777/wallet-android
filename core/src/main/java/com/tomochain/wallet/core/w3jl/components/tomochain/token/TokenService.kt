package com.tomochain.wallet.core.w3jl.components.tomochain.token

import com.tomochain.wallet.core.common.BaseService
import com.tomochain.wallet.core.common.Config
import com.tomochain.wallet.core.w3jl.listeners.TransactionListener
import io.reactivex.Single
import java.math.BigDecimal
import java.math.BigInteger

/**
 * Created by cityme on 06,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
interface TokenService : BaseService {

    fun getBalance(tokenAddress: String?) : Single<BigInteger>
    fun getBalance(tokenInfo: TokenInfo?) : Single<BigInteger>

    fun getName(tokenAddress: String?) : Single<String>
    fun getSymbol(tokenAddress: String?) : Single<String>
    fun getDecimal(tokenAddress: String?) : Single<Int>
    fun getTotalSupply(tokenAddress: String?) : Single<BigInteger>
    fun getTokenInfo(tokenAddress: String?) : Single<TokenInfo?>

}