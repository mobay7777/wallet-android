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
class CoreFunctionsImpl(private val dao: DatabaseWalletSecret?,
                        private val walletService: WalletService?) : CoreFunctions{


    override fun createWallet(hdPath: String): Single<String> {
        return importWalletFromMnemonics(walletService?.generateMnemonics(), hdPath)
    }

    override fun importWalletFromMnemonics(mnemonics: String?, hdPath: String): Single<String> {
        return Single.create {emitter ->
            try{
                if ( dao == null || walletService == null) {
                    emitter.onError(ServiceNotImplementException())
                    return@create
                }

                if (!WalletUtil.isValidMnemonics(mnemonics, false)){
                    emitter.onError(InvalidMnemonicException())
                    return@create
                }
                val entityWalletKey = walletService.importWalletFromMnemonics(mnemonics!!, hdPath)?.blockingGet()

                if (entityWalletKey == null) {
                    emitter.onError(WalletNotFoundException())
                    return@create
                }

                dao.walletDAO().getWallet(entityWalletKey.address)
                    .subscribe(
                        {
                            emitter.onError(WalletAlreadyExistedException())
                            return@subscribe
                        },
                        {

                            dao.walletDAO().addNewWallet(entityWalletKey)
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

    override fun importWalletFromPrivateKey(privateKey: String?): Single<String> {
        return Single.create {emitter ->
            try{
                if ( dao == null || walletService == null) {
                    emitter.onError(ServiceNotImplementException())
                    return@create
                }

                if (!WalletUtil.isValidPrivateKey(privateKey)){
                    emitter.onError(InvalidMnemonicException())
                    return@create
                }
                val entityWalletKey = walletService.importWalletFromPrivateKey(privateKey!!)?.blockingGet()

                if (entityWalletKey == null) {
                    emitter.onError(WalletNotFoundException())
                    return@create
                }

                dao.walletDAO().getWallet(entityWalletKey.address)
                    .subscribe(
                        {
                            emitter.onError(WalletAlreadyExistedException())
                            return@subscribe
                        },
                        {

                            dao.walletDAO().addNewWallet(entityWalletKey)
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

    override fun importWalletFromAddress(address: String?): Single<String> {
        return Single.create {emitter ->
            try{
                if ( dao == null || walletService == null) {
                    emitter.onError(ServiceNotImplementException())
                    return@create
                }

                if (!WalletUtil.isValidAddress(address)){
                    emitter.onError(InvalidAddressException())
                    return@create
                }
                val entityWalletKey = walletService.importWalletFromAddress(address!!)?.blockingGet()

                if (entityWalletKey == null) {
                    emitter.onError(WalletNotFoundException())
                    return@create
                }

                dao.walletDAO().getWallet(entityWalletKey.address)
                    .subscribe(
                        {
                            emitter.onError(WalletAlreadyExistedException())
                        },
                        {
                            dao.walletDAO().addNewWallet(entityWalletKey)
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
                if (dao == null || walletService == null) {
                    emitter.onError(ServiceNotImplementException())
                    return@create
                }

                dao.walletDAO().getAllWallet().subscribe(
                    { list ->
                        val list1 : MutableList<EntityWalletSecret> = arrayListOf()
                        list.forEach {wallet ->
                            wallet.clearSensitiveContent()
                            list1.add(wallet)
                        }
                        emitter.onSuccess(list1)
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
                if (dao == null || walletService == null) {
                    emitter.onError(ServiceNotImplementException())
                    return@create
                }
                if (!WalletUtil.isValidAddress(address)){
                    emitter.onError(InvalidAddressException())
                    return@create
                }
                dao.walletDAO().getWallet(address!!).subscribe(
                    { wallet ->
                        wallet?.clearContent()
                        emitter.onSuccess(wallet!!)
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
                if (dao == null || walletService == null) {
                    emitter.onError(ServiceNotImplementException())
                    return@create
                }
                if (!WalletUtil.isValidAddress(address)){
                    emitter.onError(InvalidAddressException())
                    return@create
                }
                dao.walletDAO().getWallet(address!!)
                    .subscribe(
                        { wallet ->
                            dao.walletDAO().deleteWallet(wallet!!).subscribe()
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