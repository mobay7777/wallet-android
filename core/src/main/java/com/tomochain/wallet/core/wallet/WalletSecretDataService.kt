package com.tomochain.wallet.core.wallet

import com.tomochain.wallet.core.common.BaseService
import io.reactivex.Single
import java.lang.StringBuilder

/**
 * Created by cityme on 11,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
interface WalletSecretDataService : BaseService{

    fun getPrivateKey() : Single<StringBuilder>
    fun getMnemonics() : Single<StringBuilder>
}