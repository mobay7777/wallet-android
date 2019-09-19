package com.tomochain.wallet.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class TokenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_token)
    }

    fun checkBalance(view: View) {}
    fun checkTRC20Fee(view: View) {}
    fun checkTRC21Fee(view: View) {}
    fun transfer(view: View) {}
    fun transferTRC20(view: View) {}
    fun transferTRC21(view: View) {}
    fun clearOutput(view: View) {}
}
