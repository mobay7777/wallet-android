package com.tomochain.wallet.core.habak

import android.os.Build
import android.util.Base64
import com.google.gson.Gson
import java.nio.charset.Charset

/**
 * Created by cityme on 30,August,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class EncryptedModel(private var data: ByteArray? = null,
                     private var iv: ByteArray? = null,
                     private var lastUpdate: Long? = null) {

    fun getEncryptedData() : Pair<ByteArray?, ByteArray?>{
        return Pair(data,iv)
    }

    override fun toString(): String {
        return "EncryptedModel(lastUpdate=$lastUpdate)"
    }

    fun toByteArrayString(): String {
        return "EncryptedModel(data='${data?.contentToString()}', iv='${iv?.contentToString()}', lastUpdate=$lastUpdate)"
    }

    fun writeToString() : String{
        val s = Gson().toJson(this)
        val data = s.toByteArray(Charset.defaultCharset())
        val b= Base64.encodeToString(data, Base64.DEFAULT)
        val r = b.substring(0,2)
        return r + b.substring(2).replace("a",r,true)
    }

    companion object {
        fun readFromString(src : String) : EncryptedModel {
            val r = src.substring(0,2)
            val r1 = src.substring(2).replace(r,"a", true)
            val data = Base64.decode(r + r1, Base64.DEFAULT)
            val s = String(data, Charset.defaultCharset())
            return Gson().fromJson(s, EncryptedModel::class.java)
        }
    }

}