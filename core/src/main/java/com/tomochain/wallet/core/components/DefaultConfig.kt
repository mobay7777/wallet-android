package com.tomochain.wallet.core.components

import android.provider.Settings
import com.tomochain.wallet.core.habak.cryptography.Habak
import com.tomochain.wallet.core.w3jl.config.chain.Chain
import com.tomochain.wallet.core.w3jl.config.chain.CommonChain

/**
 * Created by cityme on 03,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class DefaultConfig : CoreConfig() {

    override fun chain(): Chain {
        return CommonChain.TOMO_CHAIN
    }

    override fun habakAlias(): String {
        return "habakAlias"
    }

    override fun roomHelperSalt(): String {
        return "roomHelperSalt"
    }

    override fun cryptographyManager(): Habak? {
        return null
    }
}