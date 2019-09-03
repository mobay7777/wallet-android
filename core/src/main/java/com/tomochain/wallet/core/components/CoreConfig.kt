package com.tomochain.wallet.core.components

import com.tomochain.wallet.core.w3jl.config.chain.Chain

/**
 * Created by cityme on 03,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
abstract class CoreConfig {

    abstract fun chain() : Chain
    abstract fun habakAlias() : String
    abstract fun roomHelperSalt() : String
}