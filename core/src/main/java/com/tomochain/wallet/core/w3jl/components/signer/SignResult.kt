package com.tomochain.wallet.core.w3jl.components.signer

/**
 * Created by cityme on 03,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
data class SignResult(var status: Int, var source: String?, var signedData: String?, var signer: String?, var error: Throwable?)