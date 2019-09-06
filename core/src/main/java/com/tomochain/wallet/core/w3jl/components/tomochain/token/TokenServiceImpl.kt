package com.tomochain.wallet.core.w3jl.components.tomochain.token

import com.tomochain.wallet.core.common.exception.InvalidAddressException
import com.tomochain.wallet.core.w3jl.config.chain.Chain
import com.tomochain.wallet.core.w3jl.utils.WalletUtil
import io.reactivex.Single
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint8
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import java.lang.NullPointerException
import java.math.BigDecimal
import java.math.BigInteger

/**
 * Created by cityme on 06,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
open class TokenServiceImpl(open var address: String?,
                            open var web3j: Web3j?,
                            open var chain: Chain?) : TokenService{


    override fun getBalance(tokenAddress: String?): Single<BigInteger> {
        return Single.create{ emitter ->
            try {
                if (!WalletUtil.isValidAddress(address) || !WalletUtil.isValidAddress(tokenAddress)){
                    emitter.onError(InvalidAddressException())
                    return@create
                }
                val function = Function(
                    "balanceOf",
                    listOf(Address(address)),
                    listOf(object : TypeReference<Uint256>() {

                    }))
                val responseValue = callSmartContractFunction(function, tokenAddress!!, address!!)
                val response = FunctionReturnDecoder.decode(
                    responseValue, function.outputParameters)
                if (response.size == 1) {
                    emitter.onSuccess((response[0] as Uint256).value)
                } else {
                    emitter.onSuccess(BigInteger.ZERO)
                }
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }


    override fun getBalance(tokenInfo: TokenInfo?): Single<BigInteger> {
        return getBalance(tokenInfo?.address)
            .map {
                it.divide(BigInteger.TEN.pow(tokenInfo?.decimal ?: 0))
            }
    }

    override fun getName(tokenAddress: String?): Single<String> {
        return Single.create{ emitter ->
            try{
                if (!WalletUtil.isValidAddress(address) || !WalletUtil.isValidAddress(tokenAddress)){
                    emitter.onError(InvalidAddressException())
                    return@create
                }
                val function = Function(
                    "name",
                    emptyList(),
                    listOf(object : TypeReference<Utf8String>() {

                    }))
                val responseValue = callSmartContractFunction(function, tokenAddress!!, address!!)
                val response = FunctionReturnDecoder.decode(
                    responseValue, function.outputParameters)
                if (response.size == 1) {
                    emitter.onSuccess((response[0] as Utf8String).value)
                } else {
                    emitter.onSuccess("Unknown name!")
                }
            }catch(t: Throwable){
                emitter.onError(t)
            }
        }
    }

    override fun getSymbol(tokenAddress: String?): Single<String> {
        return Single.create{ emitter ->
            try{
                if (!WalletUtil.isValidAddress(address) || !WalletUtil.isValidAddress(tokenAddress)){
                    emitter.onError(InvalidAddressException())
                    return@create
                }
                val function = Function(
                    "symbol",
                    emptyList(),
                    listOf(object : TypeReference<Utf8String>() {

                    }))
                val responseValue = callSmartContractFunction(function, tokenAddress!!, address!!)
                val response = FunctionReturnDecoder.decode(
                    responseValue, function.outputParameters)
                if (response.size == 1) {
                    emitter.onSuccess(String((response[0] as Utf8String).value.toByteArray()))
                } else {
                    emitter.onSuccess("Unknown name!")
                }
            }catch(t: Throwable){
                emitter.onError(t)
            }
        }
    }

    override fun getDecimal(tokenAddress: String?): Single<Int> {
        return Single.create{ emitter ->
            try{
                if (!WalletUtil.isValidAddress(address) || !WalletUtil.isValidAddress(tokenAddress)){
                    emitter.onError(InvalidAddressException())
                    return@create
                }
                val function = Function(
                    "decimals",
                    emptyList(),
                    listOf(object : TypeReference<Uint8>() {

                    }))
                val responseValue = callSmartContractFunction(function, tokenAddress!!, address!!)
                val response = FunctionReturnDecoder.decode(
                    responseValue, function.outputParameters)
                if (response.size == 1) {
                    emitter.onSuccess(BigDecimal((response[0] as Uint8).value).intValueExact())
                } else {
                    emitter.onError(NullPointerException())
                }
            }catch(t: Throwable){
                emitter.onError(t)
            }
        }
    }


    override fun getTokenInfo(tokenAddress: String?): Single<TokenInfo?> {
        return getNameWithTokenInfo(tokenAddress, TokenInfo(tokenAddress!!,"","",0))
            .flatMap { t -> getSymbolWithTokenInfo(tokenAddress,t) }
            .flatMap { t -> getDecimalWithTokenInfo(tokenAddress,t) }
    }

    override fun setWalletAddress(address: String?) {
        this.address = address
    }


    private fun getNameWithTokenInfo(tokenAddress: String?, tokenInfo: TokenInfo) : Single<TokenInfo>{
        return getName(tokenAddress)
            .map { tokenInfo.withName(it) }
    }

    private fun getSymbolWithTokenInfo(tokenAddress: String?, tokenInfo: TokenInfo) : Single<TokenInfo>{
        return getSymbol(tokenAddress)
            .map { tokenInfo.withSymbol(it) }
    }

    private fun getDecimalWithTokenInfo(tokenAddress: String?, tokenInfo: TokenInfo) : Single<TokenInfo>{
        return getDecimal(tokenAddress)
            .map { tokenInfo.withDecimal(it) }
    }



    protected fun callSmartContractFunction(function: Function, contractAddress: String, address: String): String? {
        val encodedFunction = FunctionEncoder.encode(function)
        val response = web3j?.ethCall(
            Transaction.createEthCallTransaction(address, contractAddress, encodedFunction),
            DefaultBlockParameterName.LATEST)
            ?.sendAsync()?.get()
        return response?.value
    }
}