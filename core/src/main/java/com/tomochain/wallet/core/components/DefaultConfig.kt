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
class DefaultConfig(
    override var chain: Chain? = CommonChain.TOMO_CHAIN,
    override var cryptoAlias: String? = "cryptoAlias",
    override var roomAlias: String? = "roomHelperSalt",
    override var cryptographyManager: Habak? = null) : CoreConfig(chain, cryptoAlias, roomAlias, cryptographyManager)