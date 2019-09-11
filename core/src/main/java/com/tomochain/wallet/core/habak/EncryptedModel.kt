package com.tomochain.wallet.core.habak

import android.os.Build
import android.util.Base64
import android.util.Log
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
        val b= Base64.encodeToString(data, Base64.DEFAULT).trim()

        val b1 = b.substring(b.length - 2)
        val b2= b.substring(0, b.length - 2)
        var r = ""
        if (b2.length % 2 == 0){
            for (x in b2.indices step 2) {
                val c1 = b2[x]
                val c2 = b2[x + 1]
                r += "$c2$c1"
            }
        }else{
            for (x in 0 until r.length - 2 step 2) {
                val c1 = b2[x]
                val c2 = b2[x + 1]
                r += "$c2$c1"
            }
            r += b2.last()
        }
        return r + b1
    }

    companion object {
        fun readFromString(src : String) : EncryptedModel {
            if (src.isEmpty()) return EncryptedModel()
            val b1 = src.substring(src.length - 2)
            val b2= src.substring(0, src.length - 2)
            var r = ""
            if (b2.length % 2 == 0){
                for (x in b2.indices step 2) {
                    val c1 = b2[x]
                    val c2 = b2[x + 1]
                    r += "$c2$c1"
                }
            }else{
                for (x in 0 until r.length - 2 step 2) {
                    val c1 = b2[x]
                    val c2 = b2[x + 1]
                    r += "$c2$c1"
                }
                r += b2.last()
            }
            val data = Base64.decode(r + b1, Base64.DEFAULT)
            val s = String(data, Charset.defaultCharset())
            return Gson().fromJson(s, EncryptedModel::class.java)
        }
    }

}