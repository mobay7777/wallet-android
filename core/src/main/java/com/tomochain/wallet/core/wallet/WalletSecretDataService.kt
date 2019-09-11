package com.tomochain.wallet.core.wallet

import io.reactivex.Single
import java.lang.StringBuilder

/**
 * Created by cityme on 11,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
interface WalletSecretDataService {

    fun getPrivateKey(walletAddress: String?) : Single<StringBuilder>
    fun getMnemonics(walletAddress: String?) : Single<StringBuilder>
}