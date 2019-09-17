package com.tomochain.wallet.core.w3jl.components.tomochain.token

import android.annotation.SuppressLint
import com.tomochain.wallet.core.common.BaseService
import com.tomochain.wallet.core.common.Config
import com.tomochain.wallet.core.w3jl.listeners.TransactionListener
import io.reactivex.Flowable
import io.reactivex.Single
import java.math.BigDecimal
import java.math.BigInteger

/**
 * Created by NienLe on 2019-09-15,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
interface TokenManagerService : BaseService{
    fun withTokenAddress(tokenAddress: String?) : TokenManagerImpl
    fun getToken() : TokenInfo
    fun getTokenBalance() : Single<BigInteger>?
    fun getTokenFormattedBalance() : Single<BigDecimal>?
    fun getTokenInfo() : Single<TokenInfo?>?
    fun getTokenTransferFee(
            recipient: String = "",
            amount: BigInteger = BigInteger.ZERO,
            gasPrice: BigInteger = BigInteger(Config.Transaction.DEFAULT_GAS_PRICE)
    ) : Single<Pair<BigInteger, TokenManagerImpl.UnitType>>?

    fun getFormattedTokenTransferFee(
            recipient: String = "",
            amount: BigInteger = BigInteger.ZERO,
            gasPrice: BigInteger = BigInteger(Config.Transaction.DEFAULT_GAS_PRICE)
    ) : Single<Pair<BigDecimal, TokenManagerImpl.UnitType>>?

    @SuppressLint("CheckResult")
    fun transferToken(recipient: String,
                      amount: BigInteger,
                      callback: TransactionListener?,
                      gasPrice: BigInteger? = BigInteger(Config.Transaction.DEFAULT_GAS_PRICE),
                      gasLimit: BigInteger? = null)

    @SuppressLint("CheckResult")
    fun transferFormattedToken(recipient: String,
                               amount: BigDecimal,
                               callback: TransactionListener?,
                               gasPrice: BigInteger? = BigInteger(Config.Transaction.DEFAULT_GAS_PRICE),
                               gasLimit: BigInteger? = null)

    fun getTRC20Services() : TRC20Service?
    fun getTRC21Services() : TRC21Service?
}