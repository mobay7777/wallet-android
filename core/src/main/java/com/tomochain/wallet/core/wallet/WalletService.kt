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
    fun createWalletFromMnemonics(mnemonic: String,
                                  hdPath: String,
                                  walletName: String? = "") : Single<EntityWalletSecret?>

    fun createWalletFromPrivateKey(privateKey: String,
                                   walletName: String? = "") : Single<EntityWalletSecret?>

    fun createWalletFromAddress(address: String,
                                walletName: String? = "") : Single<EntityWalletSecret?>



}