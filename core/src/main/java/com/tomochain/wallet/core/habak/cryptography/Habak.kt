package com.tomochain.wallet.core.habak.cryptography


import com.tomochain.wallet.core.habak.EncryptedModel

/**
 * Created by NienLe on 8/11/18,August,2018
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
interface Habak{

    fun initialize()
    fun encrypt(plainText: String) : EncryptedModel
    fun decrypt(data: EncryptedModel) : StringBuilder
}