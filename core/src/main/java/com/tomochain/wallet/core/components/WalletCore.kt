package com.tomochain.wallet.core.components

import android.content.Context
import com.tomochain.wallet.core.common.di.CoreComponent
import com.tomochain.wallet.core.common.di.CoreModule
import com.tomochain.wallet.core.common.di.DaggerCoreComponent
import com.tomochain.wallet.core.w3jl.components.coreBlockchain.BlockChainService
import com.tomochain.wallet.core.w3jl.components.signer.SignerService
import com.tomochain.wallet.core.w3jl.components.tomochain.token.TRC20Service
import com.tomochain.wallet.core.w3jl.components.tomochain.token.TRC20ServiceImpl
import com.tomochain.wallet.core.w3jl.components.tomochain.token.TRC21Service
import com.tomochain.wallet.core.w3jl.components.tomochain.token.TokenService
import com.tomochain.wallet.core.wallet.WalletService
import java.lang.ref.WeakReference
import javax.inject.Inject

/**
 * Created by cityme on 03,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class WalletCore {


    private var context: WeakReference<Context>? = null
    private var config: CoreConfig = DefaultConfig()
    private var address = ""

    @Inject
    lateinit var coreBlockChainService: BlockChainService
    @Inject
    lateinit var signerService: SignerService
    @Inject
    lateinit var tokenService: TokenService
    @Inject
    lateinit var trc20TokenService: TRC20Service
    @Inject
    lateinit var trc21TokenService: TRC21Service
    @Inject
    lateinit var coreFunctions : CoreFunctions
    @Inject
    lateinit var walletService: WalletService

    companion object{

        private var instance: WalletCore? = null

        fun setup(context: WeakReference<Context>,config: CoreConfig = DefaultConfig()){
            instance = WalletCore()
            instance?.context = context
            instance?.config = config
            instance?.getCoreComponent()?.inject(instance)
        }

        fun getInstance(address: String) : WalletCore?{
            instance?.address = address
            instance?.coreBlockChainService?.setWalletAddress(address)
            instance?.signerService?.setWalletAddress(address)
            instance?.tokenService?.setWalletAddress(address)
            instance?.trc20TokenService?.setWalletAddress(address)
            instance?.trc21TokenService?.setWalletAddress(address)

            return instance
        }

        fun getInstance() : WalletCore?{
            return instance
        }

        fun destroyInstance() {
            instance = null
        }

    }

    fun getCurrentWalletAddress() : String?{
        return address
    }


    private fun getCoreComponent() : CoreComponent{
        return DaggerCoreComponent.builder()
            .coreModule(context?.let { CoreModule(it, config) }).build()
    }
}