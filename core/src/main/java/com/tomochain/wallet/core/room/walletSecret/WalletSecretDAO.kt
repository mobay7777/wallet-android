package com.tomochain.wallet.core.room.walletSecret

import androidx.room.*
import com.tomochain.wallet.core.room.wallet.EntityWallet
import io.reactivex.Maybe
import io.reactivex.Single

/**
 * Created by cityme on 30,August,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
@Dao
interface WalletSecretDAO {

    @Query("SELECT * FROM EntityWalletSecret")
    fun getAllWallet() : Maybe<List<EntityWalletSecret>>

    @Query("SELECT * FROM EntityWalletSecret WHERE address = :address LIMIT 1")
    fun getWallet(address: String) : Maybe<EntityWalletSecret>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addNewWallet(wallet: EntityWalletSecret) : Single<Long>

    @Update
    fun updateWallet(vararg wallet: EntityWalletSecret) : Single<Int>

    @Delete
    fun deleteWallet(vararg wallet: EntityWalletSecret) : Single<Int>

    @Query("DELETE FROM EntityWalletSecret")
    fun kaboom() : Single<Int>
}