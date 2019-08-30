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

    var encryptedSeed: String,

    var encryptedPKey: String,

    var metadata: String
)