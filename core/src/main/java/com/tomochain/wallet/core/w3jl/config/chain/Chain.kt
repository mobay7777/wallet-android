package com.tomochain.wallet.core.w3jl.config.chain

/**
 * Created by cityme on 03,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
interface Chain {
    fun getEndpoint() : String
    fun getChainName() : String
    fun getChainId() : Int
    fun getExplorerURL() : String
    fun getCoinBaseUnit() : String
    fun getCoinBaseSymbol() : String
    fun getHDPath() : String
}