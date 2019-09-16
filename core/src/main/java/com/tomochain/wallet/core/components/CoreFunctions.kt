package com.tomochain.wallet.core.components

import com.tomochain.wallet.core.common.Config
import com.tomochain.wallet.core.room.walletSecret.EntityWalletSecret
import io.reactivex.Single

/**
 * Created by cityme on 03,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
interface CoreFunctions {

    fun createWallet(hdPath: String = Config.HDPath.TOMO) : Single<String>

    fun importWalletFromMnemonics(mnemonics: String?, hdPath: String = Config.HDPath.TOMO) : Single<String>

    fun importWalletFromPrivateKey(privateKey: String?) : Single<String>

    fun importWalletFromAddress(address: String?) : Single<String>

    fun getAllWallet() : Single<MutableList<EntityWalletSecret>>

    fun getWalletByAddress(address: String?) : Single<EntityWalletSecret?>

    fun removeWallet(address: String?) : Single<EntityWalletSecret?>
}