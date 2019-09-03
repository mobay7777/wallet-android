package com.tomochain.wallet.core.w3jl.components.signer

import io.reactivex.Single
import java.math.BigInteger

/**
 * Created by cityme on 03,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
interface SignerService {

    fun signRawMessage( message: String?) : Single<SignResult>?
    fun signMessage( message: String?) : Single<SignResult>?
    fun signPersonalMessage( message: String?) : Single<SignResult>?

    fun signTransaction(
        recipient: String,
        amount: BigInteger?,
        gasPrice: BigInteger?,
        gasLimit: BigInteger?,
        payload: String?) : Single<SignResult>?
}