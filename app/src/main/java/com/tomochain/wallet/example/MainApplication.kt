package com.tomochain.wallet.example

import android.provider.Settings
import android.util.Base64
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.tomochain.wallet.core.components.CoreConfig
import com.tomochain.wallet.core.components.DefaultConfig
import com.tomochain.wallet.core.components.WalletCore
import com.tomochain.wallet.core.habak.EncryptedModel
import com.tomochain.wallet.core.habak.cryptography.Habak
import com.tomochain.wallet.core.w3jl.config.chain.Chain
import com.tomochain.wallet.core.w3jl.config.chain.CommonChain
import java.lang.ref.WeakReference

/**
 * Created by cityme on 10,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class MainApplication : MultiDexApplication(){
    
    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)

        val config = object : CoreConfig(){}

        config.chain = CommonChain.TOMO_CHAIN
        config.cryptoAlias = "habakAlias"

        WalletCore.setup(WeakReference(this), config)
    }


    override fun onTerminate() {
        super.onTerminate()
        WalletCore.destroyInstance()
    }

}