package com.tomochain.wallet.core.w3jl.components.tomochain.token

import android.annotation.SuppressLint
import com.tomochain.wallet.core.common.Config
import com.tomochain.wallet.core.common.exception.InvalidAmountException
import com.tomochain.wallet.core.w3jl.listeners.TransactionListener
import com.tomochain.wallet.core.w3jl.utils.ConvertUtil
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.math.BigDecimal
import java.math.BigInteger

/**
 * Created by cityme on 10,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class TokenManagerImpl(private val tokenService: TokenService?,
                       private val tRC20Service: TRC20Service?,
                       private val tRC21Service: TRC21Service?) : TokenManagerService {

    enum class UnitType{
        TOMO_AT_WEI,
        TOMO,
        TOKEN_AT_UNIT,
        TOKEN
    }

    private var tokenInfo : TokenInfo? = null
    private var address: String? = null
    private var tokenAddress: String? = null

    override fun withTokenAddress(tokenAddress: String?) : TokenManagerImpl{
        this.tokenAddress = tokenAddress
        this.tokenInfo = tokenService?.getTokenInfo(tokenAddress)?.blockingGet()
        return this
    }

    override fun setWalletAddress(address: String?) {
        this.address = address
        this.tokenService?.setWalletAddress(address)
        this.tRC20Service?.setWalletAddress(address)
        this.tRC21Service?.setWalletAddress(address)
    }

    override fun getToken() : TokenInfo{
        if (tokenInfo != null){
            tokenInfo = tokenService?.getTokenInfo(tokenAddress)?.blockingGet()
        }
        return tokenInfo!!
    }




    override fun getTokenBalance() : Single<BigInteger>?{
        return tokenService?.getBalance(tokenAddress)
    }

    override fun getTokenFormattedBalance() : Single<BigDecimal>?{
        return tokenService?.getBalance(tokenAddress)
            ?.map {
                it.toBigDecimal().divide(BigDecimal.TEN.pow(getToken().decimal))
            }
    }


    override fun getTokenInfo() : Single<TokenInfo?>?{
        return if (tokenInfo != null){
            Single.create { it.onSuccess(tokenInfo!!) }
        }else{
            tokenService?.getTokenInfo(tokenAddress!!)
        }
    }

    override fun getTokenTransferFee(
            recipient: String,
            amount: BigInteger,
            gasPrice: BigInteger
    ) : Single<Pair<BigInteger, UnitType>>?{
        return tRC21Service?.isTRC21Token(tokenAddress!!)
            ?.zipWith(tRC21Service.isTOMOZApplied(tokenAddress!!),
                BiFunction<Boolean, Boolean, Boolean> { isTRC21Token, isTOMOZApplied ->
                    isTOMOZApplied || isTRC21Token
                })
            ?.flatMap{result ->
                if (result){
                    tRC21Service.getTokenTransferFee(tokenAddress!!)
                        .map {
                            Pair(it, UnitType.TOKEN_AT_UNIT)
                        }
                }else{
                    tRC20Service?.estimateTokenTransferGasLimit(tokenAddress!!, recipient, amount)
                        ?.map {
                            Pair(it.multiply(gasPrice), UnitType.TOMO_AT_WEI)
                        }
                }

            }

        /*return tRC21Service?.isTRC21Token(tokenAddress!!)

            ?.flatMap {isTRC21 ->
                if (isTRC21){
                    tRC21Service.isTOMOZApplied(tokenAddress!!)
                        .flatMap { isTOMOZApplied ->
                            if (isTOMOZApplied){
                                tRC21Service.getTokenTransferFee(tokenAddress!!)
                                    .map {
                                        Pair(it, UnitType.TOKEN_AT_UNIT)
                                    }
                            }else{
                                tRC20Service?.estimateTokenTransferGasLimit(tokenAddress!!, recipient, amount)
                                    ?.map {
                                        Pair(it.multiply(gasPrice), UnitType.TOMO_AT_WEI)
                                    }
                            }

                        }
                }else{
                    tRC20Service?.estimateTokenTransferGasLimit(tokenAddress!!, recipient, amount)
                        ?.map {
                            Pair(it.multiply(gasPrice), UnitType.TOMO_AT_WEI)
                        }
                }
            }*/
    }


    override fun getFormattedTokenTransferFee(
            recipient: String,
            amount: BigInteger,
            gasPrice: BigInteger
    ) : Single<Pair<BigDecimal, UnitType>>?{
        return tRC21Service?.isTRC21Token(tokenAddress!!)
            ?.flatMap {isTRC21 ->
                if (isTRC21){
                    tRC21Service.getTokenTransferFee(tokenAddress!!)
                        .map {
                            Pair(it.toBigDecimal().divide(BigDecimal.TEN.pow(getToken().decimal)), UnitType.TOKEN)
                        }
                }else{
                    tRC20Service?.estimateTokenTransferGasLimit(tokenAddress!!, recipient, amount)
                        ?.map {
                            Pair(ConvertUtil.fromWei(it.multiply(gasPrice).toBigDecimal(), ConvertUtil.Unit.ETHER), UnitType.TOMO)
                        }
                }
            }
    }


    @SuppressLint("CheckResult")
    override fun transferToken(recipient: String,
                               amount: BigInteger,
                               callback: TransactionListener?,
                               gasPrice: BigInteger?,
                               gasLimit: BigInteger?){
        tRC21Service?.isTRC21Token(tokenAddress!!)
            ?.subscribe { isTRC21 ->
                if (isTRC21){
                    tRC21Service?.transferToken(tokenAddress!!, recipient, amount, callback, gasPrice, gasLimit)
                }else{
                    tRC20Service?.transferToken(tokenAddress!!, recipient, amount, callback, gasPrice, gasLimit)
                }
            }
    }


    @SuppressLint("CheckResult")
    override fun transferFormattedToken(recipient: String,
                                        amount: BigDecimal,
                                        callback: TransactionListener?,
                                        gasPrice: BigInteger?,
                                        gasLimit: BigInteger?){
        tRC21Service?.isTRC21Token(tokenAddress!!)
            ?.subscribe { isTRC21 ->
                val amountToken = amount.multiply(BigDecimal.TEN.pow(getToken().decimal))
                if (amountToken.stripTrailingZeros().scale() > 0){
                    callback?.onTransactionError(InvalidAmountException(
                        "The token has ${getToken().decimal} decimals, which mean the amount must has no more than ${getToken().decimal} fractional numbers"
                    ))
                    return@subscribe
                }
                if (isTRC21){
                    tRC21Service?.transferToken(tokenAddress!!, recipient, amount.multiply(
                        BigDecimal.TEN.pow(getToken().decimal)).toBigInteger(), callback, gasPrice, gasLimit)
                }else{
                    tRC20Service?.transferToken(tokenAddress!!, recipient, amount.multiply(
                        BigDecimal.TEN.pow(getToken().decimal)).toBigInteger(), callback, gasPrice, gasLimit)
                }
            }
    }

    override fun getTRC20Services(): TRC20Service? {
        return tRC20Service
    }

    override fun getTRC21Services(): TRC21Service? {
        return tRC21Service
    }
}