package com.tomochain.wallet.core.common.di

import com.tomochain.wallet.core.components.WalletCore
import dagger.Component
import javax.inject.Singleton

/**
 * Created by cityme on 03,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
@Singleton
@Component(modules = [CoreModule::class])
interface CoreComponent {

    fun inject(walletCore: WalletCore?)
}