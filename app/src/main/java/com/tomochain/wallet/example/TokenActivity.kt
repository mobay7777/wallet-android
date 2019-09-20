package com.tomochain.wallet.example

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.tomochain.wallet.core.components.WalletCore
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_token.*
import java.math.BigInteger
import java.util.*

class TokenActivity : AppCompatActivity() {
    private val tokenManager = WalletCore.getTokenManager("0x06605B28aab9835be75ca242a8aE58f2e15F2F45")
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_token)

    }

    fun checkBalance(view: View) {
        val s = tokenManager
            ?.withTokenAddress("0x095d85e62cb6ad354ff900c1d530a7c4b8e247b5")
            ?.getTokenBalance()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                addLog("checkBalance succcess: $it")
            },{
                addLog("checkBalance fail: $it")
            })
        addToDisposable(s)
    }
    fun checkTRC20Fee(view: View) {
        val s = tokenManager
            ?.withTokenAddress("0x095d85e62cb6ad354ff900c1d530a7c4b8e247b5")
            ?.getTokenTransferFee()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                addLog("checkTRC20Fee succcess: $it")
            },{
                addLog("checkTRC20Fee fail: $it")
            })
        addToDisposable(s)
    }
    fun checkTRC21Fee(view: View) {
        val s = tokenManager
            ?.withTokenAddress("0x9afff1e2657e3b87b9ccc9cc9a3fc1ed2f177b8a")
            ?.getTokenTransferFee()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                addLog("checkTRC21Fee succcess: $it")
            },{
                addLog("checkTRC21Fee fail: $it")
            })
        addToDisposable(s)
    }


    fun transfer(view: View) {
        val s = tokenManager
            ?.withTokenAddress("0x9afff1e2657e3b87b9ccc9cc9a3fc1ed2f177b8a")
            ?.transferToken(
                "0x6e7312d1028b70771bb9cdd9837442230a9349ca",
                BigInteger.ONE
            )
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                addLog("transfer succcess: $it")
            },{
                addLog("transfer fail: $it")
            })
        addToDisposable(s)



    }
    fun transferTRC20(view: View) {
        val s = tokenManager
            ?.getTRC20Services()
            ?.transferToken(
                "0x095d85e62cb6ad354ff900c1d530a7c4b8e247b5",
                "0x6e7312d1028b70771bb9cdd9837442230a9349ca",
                BigInteger.ONE
            )
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                addLog("transferTRC20 succcess: $it")
            },{
                addLog("transferTRC20 fail: $it")
            })
        addToDisposable(s)
    }
    fun transferTRC21(view: View) {
        val s = tokenManager
            ?.getTRC21Services()
            ?.transferToken(
                "0x9afff1e2657e3b87b9ccc9cc9a3fc1ed2f177b8a",
                "0x6e7312d1028b70771bb9cdd9837442230a9349ca",
                BigInteger.ONE
            )
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                addLog("transferTRC21 succcess: $it")
            },{
                addLog("transferTRC21 fail: $it")
            })
        addToDisposable(s)
    }
    fun getTokenInfo(view: View) {
        val s = tokenManager
            ?.withTokenAddress("0x095d85e62cb6ad354ff900c1d530a7c4b8e247b5")
            ?.getTokenInfo()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                addLog("getTokenInfo succcess: $it")
            },{
                addLog("getTokenInfo fail: $it")
            })
        addToDisposable(s)
    }

    @SuppressLint("CheckResult")
    fun clearOutput(view: View) {
        txtOutput.text = ""
    }

    private fun addLog(log: String){
        txtOutput.text = "[${Calendar.getInstance().timeInMillis}]: $log\n${txtOutput.text}"
        Log.d("addLog","[${Calendar.getInstance().timeInMillis}]: $log\n${txtOutput.text}")
    }

    private fun addToDisposable(disposable: Disposable?){
        disposable?.let { compositeDisposable.add(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }


}
