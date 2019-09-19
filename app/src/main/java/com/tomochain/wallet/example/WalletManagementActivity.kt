package com.tomochain.wallet.example

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.tomochain.wallet.core.components.WalletCore
import com.tomochain.wallet.core.room.walletSecret.EntityWalletSecret
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_wallet_management.*
import kotlinx.android.synthetic.main.item_wallet_list.view.*
import java.lang.StringBuilder

class WalletManagementActivity : AppCompatActivity() {

    private val coreFunctions = WalletCore.getCoreFunctions()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet_management)
    }

    override fun onResume() {
        super.onResume()
        displayWalletList()
    }

    @SuppressLint("CheckResult")
    fun createWallet(view: View) {
        coreFunctions?.createWallet()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                displayWalletList()
            },{
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
            })
    }
    fun importFromMnemonic(view: View) {
        MaterialDialog(this).show {
            input { _, text ->
                coreFunctions?.importWalletFromMnemonics(text.toString())
                    ?.subscribeOn(Schedulers.io())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe({
                        displayWalletList()
                    },{
                        Toast.makeText(this@WalletManagementActivity, it.localizedMessage, Toast.LENGTH_SHORT).show()
                    })
            }
            title(text = "type 12 words")
        }
    }
    fun importFromPrivateKey(view: View) {
        MaterialDialog(this).show {
            input { _, text ->
                coreFunctions?.importWalletFromPrivateKey(text.toString())
                    ?.subscribeOn(Schedulers.io())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe({
                        displayWalletList()
                    },{
                        Toast.makeText(this@WalletManagementActivity, it.localizedMessage, Toast.LENGTH_SHORT).show()
                    })
            }
            title(text = "type private key")
        }
    }
    fun importFromAddress(view: View) {
        MaterialDialog(this).show {
            input { _, text ->
                coreFunctions?.importWalletFromAddress(text.toString())
                    ?.subscribeOn(Schedulers.io())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe({
                        displayWalletList()
                    },{
                        Toast.makeText(this@WalletManagementActivity, it.localizedMessage, Toast.LENGTH_SHORT).show()
                    })
            }
            title(text = "type address")
        }
    }



    @SuppressLint("CheckResult")
    private fun displayWalletList(){

        coreFunctions?.getAllWallet()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                populateViews(it)
            },{
                Toast.makeText(this@WalletManagementActivity, it.localizedMessage, Toast.LENGTH_SHORT).show()
            })

    }

    private fun populateViews(data: MutableList<EntityWalletSecret>){
        containerWalletList.removeAllViews()
        data.forEach {
            val view = LayoutInflater.from(this)
                .inflate(R.layout.item_wallet_list, null)
            view.txtContent.text = "${it.address} - ${it.createdAt} - ${it.createdFrom}"
            view.btnDelete.setOnClickListener {_ ->
                deleteWallet(it.address)
            }
            view.btnShowData.setOnClickListener {_ ->
                showData(it.address)
            }
            containerWalletList.addView(view)
        }
    }

    @SuppressLint("CheckResult")
    private fun showData(address: String) {
        /*coreFunctions?.removeWallet(address)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                displayWalletList()
            },{
                Toast.makeText(this@WalletManagementActivity, it.localizedMessage, Toast.LENGTH_SHORT).show()
            })*/
        WalletCore.getWalletSecretData(address)
            ?.getPrivateKey()
            ?.zipWith(
                WalletCore.getWalletSecretData(address)?.getMnemonics(),
                BiFunction<StringBuilder, StringBuilder, String> { t1, t2 -> "pkey:[$t1]\nmnemonics:[$t2]" })
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                MaterialDialog(this).show {
                    message(text = it)
                    negativeButton (text = "close")
                }
            },{
                Toast.makeText(this@WalletManagementActivity, it.localizedMessage, Toast.LENGTH_SHORT).show()
            })

    }


    @SuppressLint("CheckResult")
    private fun deleteWallet(address: String){
        coreFunctions?.removeWallet(address)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                displayWalletList()
            },{
                Toast.makeText(this@WalletManagementActivity, it.localizedMessage, Toast.LENGTH_SHORT).show()
            })
    }
}
