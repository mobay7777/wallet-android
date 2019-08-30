package com.tomochain.wallet.core.room.wallet

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by cityme on 30,August,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
@Entity(tableName = "EntityWallet")
data class EntityWallet (

    @PrimaryKey val address: String,

    var name: String,

    var createdFrom: Int,

    var createdAt: Long,

    var updateAt: Long,

    var metadata: String
)