package com.tomochain.wallet.core.room.wallet

import androidx.room.*
import io.reactivex.Maybe
import io.reactivex.Single

/**
 * Created by cityme on 30,August,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
@Dao
interface WalletDAO {

    @Query("SELECT * FROM EntityWallet")
    fun getAllWallet() : Maybe<List<EntityWallet>>

    @Query("SELECT * FROM EntityWallet WHERE address = :address LIMIT 1")
    fun getWallet(address: String) : Maybe<EntityWallet>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addNewWallet(wallet: EntityWallet) : Single<Long>

    @Update
    fun updateWallet(vararg wallet: EntityWallet) : Single<Int>

    @Delete
    fun deleteWallet(vararg wallet: EntityWallet) : Single<Int>

    @Query("DELETE FROM EntityWallet")
    fun kaboom() : Single<Int>
}