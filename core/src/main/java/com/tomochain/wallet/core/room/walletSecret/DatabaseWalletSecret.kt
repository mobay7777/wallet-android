package com.tomochain.wallet.core.room.walletSecret

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
@Database(entities = [EntityWalletSecret::class], version = Config.Database.VERSION, exportSchema = false)
abstract class DatabaseWalletSecret : RoomDatabase(){

    abstract fun walletDAO(): WalletSecretDAO

    companion object {
        private var INSTANCE: DatabaseWalletSecret? = null

        fun getInstance(context: Context, helperSalt: String = ""): DatabaseWalletSecret? {
            if (INSTANCE == null) {
                synchronized(DatabaseWalletSecret::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        DatabaseWalletSecret::class.java, Config.Database.NAME)
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