package com.tomochain.wallet.core.wallet

import com.tomochain.wallet.core.room.walletSecret.EntityWalletSecret
import com.tomochain.wallet.core.w3jl.config.chain.CommonChain
import io.reactivex.Single

/**
 * Created by NienLe on 2019-05-03,May,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
interface WalletService {

    fun getWordList() : List<String>
    fun generateMnemonics() : String
    fun createWallet(hdPath: String = CommonChain.TOMO_CHAIN.getHDPath()) : Single<EntityWalletSecret?>
    fun importWalletFromMnemonics(mnemonic: String,
                                  hdPath: String = CommonChain.TOMO_CHAIN.getHDPath()) : Single<EntityWalletSecret?>

    fun importWalletFromPrivateKey(privateKey: String) : Single<EntityWalletSecret?>

    fun importWalletFromAddress(address: String) : Single<EntityWalletSecret?>



}