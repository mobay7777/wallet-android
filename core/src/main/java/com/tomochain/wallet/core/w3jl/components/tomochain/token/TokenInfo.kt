package com.tomochain.wallet.core.w3jl.components.tomochain.token

import java.math.BigInteger

/**
 * Created by cityme on 06,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
data class TokenInfo(
    var address: String,
    var name: String,
    var symbol: String,
    var decimal: Int,
    var totalSupply: BigInteger
){
    fun withAddress(address: String?) : TokenInfo{
        this.address = address ?: ""
        return this
    }

    fun withName(name: String?) : TokenInfo{
        this.name = name ?: ""
        return this
    }

    fun withSymbol(symbol: String?) : TokenInfo{
        this.symbol = symbol ?: ""
        return this
    }

    fun withDecimal(decimal: Int?) : TokenInfo{
        this.decimal = decimal ?: 0
        return this
    }

    fun withTotalSupply(totalSupply: BigInteger?) : TokenInfo{
        this.totalSupply = totalSupply ?: BigInteger.ZERO
        return this
    }
}