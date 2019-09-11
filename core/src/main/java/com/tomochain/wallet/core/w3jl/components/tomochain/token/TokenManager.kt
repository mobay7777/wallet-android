package com.tomochain.wallet.core.w3jl.components.tomochain.token

import android.annotation.SuppressLint
import android.location.Address
import com.tomochain.wallet.core.common.BaseService
import com.tomochain.wallet.core.common.Config
import com.tomochain.wallet.core.common.exception.InvalidAmountException
import com.tomochain.wallet.core.components.CoreFunctions
import com.tomochain.wallet.core.habak.cryptography.Habak
import com.tomochain.wallet.core.w3jl.components.coreBlockchain.BlockChainService
import com.tomochain.wallet.core.w3jl.config.chain.Chain
import com.tomochain.wallet.core.w3jl.listeners.TransactionListener
import com.tomochain.wallet.core.w3jl.utils.ConvertUtil
import io.reactivex.Single
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.EthGasPrice
import java.math.BigDecimal
import java.math.BigInteger
import java.time.temporal.TemporalAmount

/**
 * Created by cityme on 10,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class TokenManager(private val tokenService: TokenService?,
                   private val tRC20Service: TRC20Service?,
                   private val tRC21Service: TRC21Service?){

    enum class UnitType{
        TOMO_AT_WEI,
        TOMO,
        TOKEN_AT_UNIT,
        TOKEN
    }

    private var tokenInfo : TokenInfo? = null
    private var walletAddress: String? = null
    private var tokenAddress: String? = null

    fun withTokenAddress(tokenAddress: String?) : TokenManager{
        this.tokenAddress = tokenAddress
        this.tokenInfo = tokenService?.getTokenInfo(tokenAddress)?.blockingGet()
        return this
    }

    fun withWalletAddress(walletAddress: String?) : TokenManager {
        this.walletAddress = walletAddress
        this.tokenService?.setWalletAddress(walletAddress)
        this.tRC20Service?.setWalletAddress(walletAddress)
        this.tRC21Service?.setWalletAddress(walletAddress)
        return this
    }

    private fun getToken() : TokenInfo{
        if (tokenInfo != null){
            tokenInfo = tokenService?.getTokenInfo(tokenAddress)?.blockingGet()
        }
        return tokenInfo!!
    }




    fun getTokenBalance() : Single<BigInteger>?{
        return tokenService?.getBalance(tokenAddress)
    }

    fun getTokenFormattedBalance() : Single<BigDecimal>?{
        return tokenService?.getBalance(tokenAddress)
            ?.map {
                it.toBigDecimal().divide(BigDecimal.TEN.pow(getToken().decimal))
            }
    }


    fun getTokenInfo() : Single<TokenInfo?>?{
        return if (tokenInfo != null){
            Single.create { it.onSuccess(tokenInfo!!) }
        }else{
            tokenService?.getTokenInfo(tokenAddress!!)
        }
    }

    fun getTokenTransferFee(
        recipient: String = "",
        amount: BigInteger = BigInteger.ZERO,
        gasPrice: BigInteger = BigInteger(Config.Transaction.DEFAULT_GAS_PRICE)
    ) : Single<Pair<BigInteger, UnitType>>?{
        return tRC21Service?.isTRC21Token(tokenAddress!!)
            ?.flatMap {isTRC21 ->
                if (isTRC21){
                    tRC21Service?.getTokenTransferFee(tokenAddress!!)
                        ?.map {
                            Pair(it, UnitType.TOKEN_AT_UNIT)
                        }
                }else{
                    tRC20Service?.estimateTokenTransferGasLimit(tokenAddress!!, recipient, amount)
                        ?.map {
                            Pair(it.multiply(gasPrice), UnitType.TOMO_AT_WEI)
                        }
                }
            }
    }


    fun getFormattedTokenTransferFee(
        recipient: String = "",
        amount: BigInteger = BigInteger.ZERO,
        gasPrice: BigInteger = BigInteger(Config.Transaction.DEFAULT_GAS_PRICE)
    ) : Single<Pair<BigDecimal, UnitType>>?{
        return tRC21Service?.isTRC21Token(tokenAddress!!)
            ?.flatMap {isTRC21 ->
                if (isTRC21){
                    tRC21Service?.getTokenTransferFee(tokenAddress!!)
                        ?.map {
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
    fun transferToken(recipient: String,
                      amount: BigInteger,
                      callback: TransactionListener?,
                      gasPrice: BigInteger? = BigInteger(Config.Transaction.DEFAULT_GAS_PRICE),
                      gasLimit: BigInteger? = null){
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
    fun transferFormattedToken(recipient: String,
                      amount: BigDecimal,
                      callback: TransactionListener?,
                      gasPrice: BigInteger? = BigInteger(Config.Transaction.DEFAULT_GAS_PRICE),
                      gasLimit: BigInteger? = null){
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

}