package com.tomochain.wallet.example

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.tomochain.wallet.core.components.WalletCore
import com.tomochain.wallet.core.w3jl.listeners.TransactionListener
import com.tomochain.wallet.core.w3jl.utils.ConvertUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_core_function.*
import java.math.BigInteger
import java.util.*

class CoreFunctionActivity : AppCompatActivity() {

    private val walletFunctions = WalletCore.getWalletFunctions("0x06605B28aab9835be75ca242a8aE58f2e15F2F45")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_core_function)
    }

    @SuppressLint("CheckResult")
    fun checkBalance(view: View) {
        walletFunctions?.getBalance()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                val formattedBalance = ConvertUtil.fromWei(it.toBigDecimal(), ConvertUtil.Unit.ETHER)
                addLog("checkBalance success: $formattedBalance TOMO")
            },{
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                addLog("checkBalance fail: $it")
            })
    }

    @SuppressLint("CheckResult")
    fun signMessage(view: View) {
        val msg = "At the heart of TomoChain, the Proof-of-Stake Voting consensus enables it as an\n" +
                "EVM-compatible and scalable public blockchain, on which every Ethereum smart contract can be effectively run with almost instant transaction confirmation."
        walletFunctions?.signMessage(msg)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                addLog("signMessage success: $it")
            },{
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                addLog("signMessage fail: $it")
            })
    }

    @SuppressLint("CheckResult")
    fun signTransaction(view: View) {
        walletFunctions
            ?.signTransaction(
                "0x6e7312d1028b70771bb9cdd9837442230a9349ca",
                BigInteger.ONE)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                addLog("signTransaction success: $it")
            },{
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                addLog("signTransaction fail: $it")
            })
    }

    @SuppressLint("CheckResult")
    fun sendSignedTx(view: View) {

    }

    @SuppressLint("CheckResult")
    fun transfer(view: View) {

        walletFunctions
            ?.transfer(
                "0x6e7312d1028b70771bb9cdd9837442230a9349ca",
                BigInteger.ONE
            )
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                addLog("transfer success: $it")
            },{
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                addLog("transfer fail: $it")
            })
    }

    @SuppressLint("CheckResult")
    fun clearOutput(view: View) {
        txtOutput.text = ""
    }

    private fun addLog(log: String){
        txtOutput.text = "[${Calendar.getInstance().timeInMillis}]: $log\n${txtOutput.text}"
        Log.d("addLog","[${Calendar.getInstance().timeInMillis}]: $log\n${txtOutput.text}")
    }
}
