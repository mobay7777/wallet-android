package com.tomochain.wallet.core.room.wallet

import android.content.Context
import android.text.SpannableStringBuilder
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.commonsware.cwac.saferoom.SafeHelperFactory
import com.tomochain.wallet.core.common.Config

/**
 * Created by cityme on 30,August,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
@Database(entities = [EntityWallet::class], version = Config.Database.VERSION, exportSchema = false)
abstract class DatabaseWallet : RoomDatabase(){

    abstract fun walletDAO(): WalletDAO

    companion object {
        private var INSTANCE: DatabaseWallet? = null

        fun getInstance(context: Context, helperSalt: String = ""): DatabaseWallet? {
            if (INSTANCE == null) {
                synchronized(DatabaseWallet::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        DatabaseWallet::class.java, Config.Database.NAME)
                        .openHelperFactory(
                            SafeHelperFactory
                                .fromUser(SpannableStringBuilder(helperSalt)))
                        .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}