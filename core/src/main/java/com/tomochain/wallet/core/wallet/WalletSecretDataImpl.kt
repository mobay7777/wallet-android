package com.tomochain.wallet.core.wallet

import com.tomochain.wallet.core.common.exception.InvalidAddressException
import com.tomochain.wallet.core.common.exception.ServiceNotImplementException
import com.tomochain.wallet.core.habak.EncryptedModel
import com.tomochain.wallet.core.habak.cryptography.Habak
import com.tomochain.wallet.core.room.walletSecret.DatabaseWalletSecret
import com.tomochain.wallet.core.w3jl.utils.WalletUtil
import io.reactivex.Single
import java.lang.NullPointerException

/**
 * Created by cityme on 11,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class WalletSecretDataImpl(private val  habak: Habak?,
                           private val  dao: DatabaseWalletSecret?) : WalletSecretDataService{

    private var walletAddress: String? = ""

    override fun setWalletAddress(address: String?) {
        this.walletAddress = address?.toLowerCase()
    }

    override fun getPrivateKey(): Single<StringBuilder> {
        return Single.create {emitter ->
            try{
                if (habak == null || dao == null) {
                    emitter.onError(ServiceNotImplementException())
                    return@create
                }
                if (!WalletUtil.isValidAddress(walletAddress)){
                    emitter.onError(InvalidAddressException())
                    return@create
                }
                dao.walletDAO().getWallet(walletAddress!!).subscribe(
                    { wallet ->
                        if (wallet == null){
                            emitter.onSuccess(StringBuilder(""))
                        }else{
                            emitter.onSuccess(habak.decrypt(wallet.encryptedPKey))
                        }

                    },{
                        emitter.onSuccess(StringBuilder(""))
                    }
                )
            }catch(_: Throwable){
                emitter.onSuccess(StringBuilder(""))
            }
        }
    }

    override fun getMnemonics(): Single<StringBuilder> {
        return Single.create {emitter ->
            try{
                if (habak == null || dao == null) {
                    emitter.onError(ServiceNotImplementException())
                    return@create
                }
                if (!WalletUtil.isValidAddress(walletAddress)){
                    emitter.onError(InvalidAddressException())
                    return@create
                }
                dao.walletDAO().getWallet(walletAddress!!).subscribe(
                    { wallet ->
                        if (wallet == null){
                            emitter.onSuccess(StringBuilder(""))
                        }else{
                            emitter.onSuccess(habak.decrypt(wallet.encryptedSeed))
                        }

                    },{
                        emitter.onSuccess(StringBuilder(""))
                    }
                )
            }catch(t: Throwable){
                emitter.onSuccess(StringBuilder(""))
            }
        }
    }
}