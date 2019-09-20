package com.tomochain.wallet.core.w3jl.components.tomochain.token

import android.annotation.SuppressLint
import com.tomochain.wallet.core.common.BaseService
import com.tomochain.wallet.core.common.Config
import com.tomochain.wallet.core.w3jl.entity.TransactionResult

import io.reactivex.Flowable
import io.reactivex.Observable
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
            recipient: String = Config.Address.DEFAULT,
            amount: BigInteger = BigInteger.TEN,
            gasPrice: BigInteger = BigInteger(Config.Transaction.DEFAULT_GAS_PRICE)
    ) : Single<Pair<BigInteger, TokenManagerImpl.UnitType>>?

    fun getFormattedTokenTransferFee(
            recipient: String = Config.Address.DEFAULT,
            amount: BigInteger = BigInteger.ONE,
            gasPrice: BigInteger = BigInteger(Config.Transaction.DEFAULT_GAS_PRICE)
    ) : Single<Pair<BigDecimal, TokenManagerImpl.UnitType>>?

    @SuppressLint("CheckResult")
    fun transferToken(recipient: String,
                      amount: BigInteger,
                      gasPrice: BigInteger? = BigInteger(Config.Transaction.DEFAULT_GAS_PRICE),
                      gasLimit: BigInteger? = null) : Observable<TransactionResult>?

    @SuppressLint("CheckResult")
    fun transferFormattedToken(recipient: String,
                               amount: BigDecimal,
                               gasPrice: BigInteger? = BigInteger(Config.Transaction.DEFAULT_GAS_PRICE),
                               gasLimit: BigInteger? = null) : Observable<TransactionResult>?

    fun getTRC20Services() : TRC20Service?
    fun getTRC21Services() : TRC21Service?
}