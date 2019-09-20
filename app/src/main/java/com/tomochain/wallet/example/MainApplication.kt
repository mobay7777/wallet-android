package com.tomochain.wallet.example

import android.provider.Settings
import android.util.Base64
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.tomochain.wallet.core.components.CoreConfig
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
        WalletCore.setup(WeakReference(this), object : CoreConfig(){
            override fun chain(): Chain {
                return CommonChain.TOMO_CHAIN_TEST_NET
            }

            override fun habakAlias(): String {
                return "thisIsAlias"
            }

            override fun roomHelperSalt(): String {
                return "thisIsSalt"
            }

            override fun cryptographyManager(): Habak? {
                return null
            }
        })
    }


    override fun onTerminate() {
        super.onTerminate()
        WalletCore.destroyInstance()
    }

}