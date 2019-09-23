package com.tomochain.wallet.core.components

import com.tomochain.wallet.core.habak.cryptography.Habak
import com.tomochain.wallet.core.w3jl.config.chain.Chain
import com.tomochain.wallet.core.w3jl.config.chain.CommonChain

/**
 * Created by cityme on 03,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
abstract class CoreConfig(
    open var chain: CommonChain? = CommonChain.TOMO_CHAIN,
    open var cryptoAlias: String? = "cryptoAlias",
    open var roomAlias: String? = "roomAlias",
    open var cryptographyManager: Habak? = null
)