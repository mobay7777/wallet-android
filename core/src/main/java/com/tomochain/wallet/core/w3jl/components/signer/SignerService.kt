package com.tomochain.wallet.core.w3jl.components.signer

import com.tomochain.wallet.core.common.BaseService
import com.tomochain.wallet.core.common.Config
import io.reactivex.Single
import java.math.BigInteger

/**
 * Created by cityme on 03,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
interface SignerService : BaseService {

    fun signRawMessage( message: String?) : Single<SignResult>?
    fun signMessage( message: String?) : Single<SignResult>?
    fun signPersonalMessage( message: String?) : Single<SignResult>?

    fun signTransaction(
        recipient: String,
        amount: BigInteger?,
        gasPrice: BigInteger? = BigInteger(Config.Transaction.DEFAULT_GAS_PRICE),
        gasLimit: BigInteger? = BigInteger(Config.Transaction.DEFAULT_GAS_LIMIT),
        payload: String? = null) : Single<SignResult>?
}