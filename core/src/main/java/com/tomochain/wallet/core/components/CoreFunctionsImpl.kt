package com.tomochain.wallet.core.components

import android.util.Log
import com.tomochain.wallet.core.common.exception.*
import com.tomochain.wallet.core.habak.cryptography.Habak
import com.tomochain.wallet.core.room.walletSecret.DatabaseWalletSecret
import com.tomochain.wallet.core.room.walletSecret.EntityWalletSecret
import com.tomochain.wallet.core.w3jl.utils.WalletUtil
import com.tomochain.wallet.core.wallet.WalletService
import io.reactivex.Single

/**
 * Created by cityme on 03,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class CoreFunctionsImpl(var habak: Habak?,
                        var dao: DatabaseWalletSecret?,
                        var walletService: WalletService?) : CoreFunctions{


    override fun createWalletFromMnemonics(mnemonics: String?, hdPath: String): Single<String> {
        return Single.create {emitter ->
            try{
                if (habak == null || dao == null || walletService == null) {
                    emitter.onError(ServiceNotImplementException())
                    return@create
                }

                if (!WalletUtil.isValidMnemonics(mnemonics, false)){
                    emitter.onError(InvalidMnemonicException())
                    return@create
                }
                val entityWalletKey = walletService?.createWalletFromMnemonics(mnemonics!!, hdPath)?.blockingGet()

                if (entityWalletKey == null) {
                    emitter.onError(WalletNotFoundException())
                    return@create
                }

                dao!!.walletDAO().getWallet(entityWalletKey.address)
                    .subscribe(
                        {
                            emitter.onError(WalletAlreadyExistedException())
                            return@subscribe
                        },
                        {

                            dao!!.walletDAO().addNewWallet(entityWalletKey)
                                .subscribe({
                                    emitter.onSuccess(entityWalletKey.address)
                                    entityWalletKey.clearContent()
                                },{ t ->
                                    entityWalletKey.clearContent()
                                    emitter.onError(t)
                                })

                        }
                    )
            }catch(t: Throwable){
                emitter.onError(t)
            }
        }
    }

    override fun createWalletFromPrivateKey(privateKey: String?): Single<String> {
        return Single.create {emitter ->
            try{
                if (habak == null || dao == null || walletService == null) {
                    emitter.onError(ServiceNotImplementException())
                    return@create
                }

                if (!WalletUtil.isValidPrivateKey(privateKey)){
                    emitter.onError(InvalidMnemonicException())
                    return@create
                }
                val entityWalletKey = walletService?.createWalletFromPrivateKey(privateKey!!)?.blockingGet()

                if (entityWalletKey == null) {
                    emitter.onError(WalletNotFoundException())
                    return@create
                }

                dao!!.walletDAO().getWallet(entityWalletKey.address)
                    .subscribe(
                        {
                            emitter.onError(WalletAlreadyExistedException())
                            return@subscribe
                        },
                        {

                            dao!!.walletDAO().addNewWallet(entityWalletKey)
                                .subscribe({
                                    emitter.onSuccess(entityWalletKey.address)
                                    entityWalletKey.clearContent()
                                },{ t ->
                                    entityWalletKey.clearContent()
                                    emitter.onError(t)
                                })

                        }
                    )
            }catch(t: Throwable){
                emitter.onError(t)
            }
        }
    }

    override fun createWalletFromAddress(address: String?): Single<String> {
        return Single.create {emitter ->
            try{
                if (habak == null || dao == null || walletService == null) {
                    emitter.onError(ServiceNotImplementException())
                    return@create
                }

                if (!WalletUtil.isValidAddress(address)){
                    emitter.onError(InvalidAddressException())
                    return@create
                }
                val entityWalletKey = walletService?.createWalletFromAddress(address!!)?.blockingGet()

                if (entityWalletKey == null) {
                    emitter.onError(WalletNotFoundException())
                    return@create
                }

                dao!!.walletDAO().getWallet(entityWalletKey.address)
                    .subscribe(
                        {
                            emitter.onError(WalletAlreadyExistedException())
                            return@subscribe
                        },
                        {

                            dao!!.walletDAO().addNewWallet(entityWalletKey)
                                .subscribe({
                                    emitter.onSuccess(entityWalletKey.address)
                                    entityWalletKey.clearContent()
                                },{ t ->
                                    entityWalletKey.clearContent()
                                    emitter.onError(t)
                                })

                        }
                    )
            }catch(t: Throwable){
                emitter.onError(t)
            }
        }
    }

    override fun getAllWallet(): Single<MutableList<EntityWalletSecret>> {
        return Single.create {emitter ->
            try{
                if (habak == null || dao == null || walletService == null) {
                    emitter.onError(ServiceNotImplementException())
                    return@create
                }

                dao!!.walletDAO().getAllWallet().subscribe(
                    { list ->
                        emitter.onSuccess(list as MutableList<EntityWalletSecret>)
                    },{ t ->
                        emitter.onError(t)
                    }
                )
            }catch(t: Throwable){
                emitter.onError(t)
            }
        }
    }

    override fun getWalletByAddress(address: String?): Single<EntityWalletSecret?> {
        return Single.create {emitter ->
            try{
                if (habak == null || dao == null || walletService == null) {
                    emitter.onError(ServiceNotImplementException())
                    return@create
                }
                if (!WalletUtil.isValidAddress(address)){
                    emitter.onError(InvalidAddressException())
                    return@create
                }
                dao!!.walletDAO().getWallet(address!!).subscribe(
                    { wallet ->
                        emitter.onSuccess(wallet)
                    },{ t ->
                        emitter.onError(t)
                    }
                )
            }catch(t: Throwable){
                emitter.onError(t)
            }
        }
    }

    override fun removeWallet(address: String?): Single<EntityWalletSecret?> {
        return Single.create {emitter ->
            try{
                if (habak == null || dao == null || walletService == null) {
                    emitter.onError(ServiceNotImplementException())
                    return@create
                }
                if (!WalletUtil.isValidAddress(address)){
                    emitter.onError(InvalidAddressException())
                    return@create
                }
                dao!!.walletDAO().getWallet(address!!)
                    .subscribe(
                        { wallet ->
                            dao!!.walletDAO().deleteWallet(wallet!!).subscribe()
                            emitter.onSuccess(wallet)
                        },
                        {
                            emitter.onError(WalletNotFoundException())
                        }
                    )
            }catch(t: Throwable){
                emitter.onError(t)
            }
        }
    }
}