package com.tomochain.wallet.core.habak

import android.content.Context
import android.os.Build
import com.tomochain.wallet.core.habak.cryptography.Habak
import com.tomochain.wallet.core.habak.cryptography.legacy.Habak19Cipher
import com.tomochain.wallet.core.habak.cryptography.modern.Habak23Cipher
import com.tomochain.wallet.core.habak.cryptography.modern.Habak23WithPasswordCipher

/**
 * Created by NienLe on 2019-04-28,April,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class HabakFactory(var context: Context?) {

    var alias : String? = null
    var password : String = ""



    fun withAlias(alias : String?) : HabakFactory {
        this.alias = alias
        return this
    }


    fun withPassword(password : String?) : HabakFactory {
        this.password = password ?: ""
        return this
    }



    fun build() : Habak {
        requireNotNull(alias, lazyMessage = { "alias must not be null!" })
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            val h23 = if (password.isBlank()) {
                Habak23Cipher(alias!!)
            } else {
                Habak23WithPasswordCipher(alias!!, password)
            }
            h23.initialize()
            h23
        }
        else{
            val h19 = Habak19Cipher(alias!!, context!!)
            h19.initialize()
            h19
        }
    }
}