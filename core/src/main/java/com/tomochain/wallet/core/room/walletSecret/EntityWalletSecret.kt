package com.tomochain.wallet.core.room.walletSecret

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by cityme on 30,August,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */

@Entity(tableName = "EntityWalletSecret")
data class EntityWalletSecret(

    @PrimaryKey val address: String,

    var createdAt: Long,

    var createdFrom: Int,

    var encryptedSeed: String,

    var encryptedPKey: String,

    var dbVersion: Int,

    var metadata: String
){
    fun clearContent(){
        createdAt = -1
        createdFrom = -1
        encryptedSeed = ""
        encryptedPKey = ""
        dbVersion = -1
        metadata = ""
    }

    override fun toString(): String {
        return "EntityWalletSecret(address='$address', " +
                "createdAt=$createdAt, " +
                "createdFrom=$createdFrom, " +
                "encryptedSeed='${encryptedSeed}', " +
                "encryptedPKey='${encryptedPKey}', " +
                "dbVersion=$dbVersion, metadata='$metadata')"
    }


}