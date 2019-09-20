package com.tomochain.wallet.core.components

import android.provider.Settings

import android.util.Log
import androidx.test.InstrumentationRegistry
import androidx.test.internal.util.LogUtil
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4

import com.tomochain.wallet.core.common.exception.InvalidMnemonicException
import com.tomochain.wallet.core.common.exception.InvalidPrivateKeyException
import com.tomochain.wallet.core.common.exception.WalletAlreadyExistedException
import com.tomochain.wallet.core.habak.EncryptedModel
import com.tomochain.wallet.core.habak.cryptography.Habak
import com.tomochain.wallet.core.w3jl.components.tomochain.token.TokenInfo
import com.tomochain.wallet.core.w3jl.config.chain.Chain
import com.tomochain.wallet.core.w3jl.config.chain.CommonChain
import com.tomochain.wallet.core.w3jl.utils.ConvertUtil
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.ref.WeakReference
import org.junit.Assert.*
import org.junit.Rule
import org.web3j.crypto.Wallet
import java.math.BigDecimal
import java.math.BigInteger

/**
 * Created by cityme on 03,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
@RunWith(AndroidJUnit4::class)
internal class WalletCoreTest {

    val LOG = "WalletCoreTest-TAG"

    @get:Rule var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.INTERNET)


    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        WalletCore.setup(WeakReference(context), object : CoreConfig(){
            override fun chain(): Chain {
                return CommonChain.TOMO_CHAIN_TEST_NET
            }

            override fun habakAlias(): String {
                return Settings.Secure.ANDROID_ID
            }

            override fun roomHelperSalt(): String {
                return Settings.Secure.ANDROID_ID
            }

            override fun cryptographyManager(): Habak? {
                return null
            }
        })
    }

    @After
    fun tearDown() {
        WalletCore.destroyInstance()
    }

    @Test
    fun testToken(){

        /*WalletCore.getCoreFunctions()?.createWallet()?.subscribe(
            {
                Log.d(LOG, "createWalletFromMnemonics > success: $it")
            },{
                Log.e(LOG, "createWalletFromMnemonics > fail: " + it.localizedMessage)
            }
        )*/
        WalletCore.getCoreFunctions()?.importWalletFromPrivateKey("fe514e9fa6e6f96e63640e80ba413ba0994bac81357fd7bab18b1302bf347750")?.subscribe(
            {
                Log.d(LOG, "createWalletFromPrivateKey > success: $it")
            },{
                Log.e(LOG, "createWalletFromPrivateKey > fail: " + it.localizedMessage)
            }
        )

        WalletCore.getCoreFunctions()
            ?.getAllWallet()
            ?.subscribe(
                {
                    it.forEach {wallet ->
                        Log.d(LOG, "getAllWallet > onSuccess: $wallet")
                    }
                },{
                    Log.e(LOG, "getAllWallet > error: $it")
                }
            )


        WalletCore.getWalletFunctions("0x06605b28aab9835be75ca242a8ae58f2e15f2f45")
            ?.getBalance()
            ?.subscribe(
                {
                    Log.d(LOG, "getBalance > success: $it")
                },{
                    Log.e(LOG, "getBalance > fail: " + it.localizedMessage)
                }
            )

        /*WalletCore.getWalletFunctions("0x06605b28aab9835be75ca242a8ae58f2e15f2f45")
            ?.sendSignedTransaction("0xf87081b3840ee6b2808252089406605b28aab9835be75ca242a8ae58f2e15f2f458579ecc8e6bc870aefeffceffcef1ca0a5730e68d6a6d64e5627f919f700de70944fc87a96d65bccc33d0d7aec855c08a023dfd687c1554e258bf3cc45cd6e6d02a49807969ab950d1840681f64e1315a8",
                callback =  object : TransactionListener{
                    override fun onTransactionCreated(txId: String) {
                        Log.d(LOG, "sendSignedTransaction > onTransactionCreated: $txId")
                    }

                    override fun onTransactionComplete(txId: String, status: String) {
                        Log.d(LOG, "sendSignedTransaction > onTransactionComplete: $txId $status")
                    }

                    override fun onTransactionError(e: Exception) {
                        Log.d(LOG, "sendSignedTransaction > onTransactionError: $e")
                    }
                })*/



        /*WalletCore.getWalletFunctions("0x06605b28aab9835be75ca242a8ae58f2e15f2f45")
            ?.signTransaction(
                "0x06605b28aab9835be75ca242a8ae58f2e15f2f45",
                BigInteger("523663632060"),
                payload="ahihi con cho"
            )
            ?.subscribe(
                {
                    Log.d(LOG, "getBalance > success: $it")
                },{
                    Log.e(LOG, "getBalance > fail: " + it.localizedMessage)
                }
            )*/


        WalletCore.getTokenManager("0x06605b28aab9835be75ca242a8ae58f2e15f2f45",
            "0x9afff1e2657e3b87b9ccc9cc9a3fc1ed2f177b8a")
            ?.getTokenTransferFee("0x06605b28aab9835be75ca242a8ae58f2e15f2f45")
            ?.subscribe(
                {
                    Log.d(LOG, "getTokenTransferFee TRC21 TOMOZ Fee 1 > success: $it")
                },{
                    Log.e(LOG, "getTokenTransferFee TRC21 TOMOZ Fee 1  > fail: " + it.localizedMessage)
                }
            )

        WalletCore.getTokenManager("0x06605b28aab9835be75ca242a8ae58f2e15f2f45",
            "0x93fcc8bf855aee8af15d9fd231b13907f9642599")
            ?.getTokenTransferFee("0x06605b28aab9835be75ca242a8ae58f2e15f2f45")
            ?.subscribe(
                {
                    Log.d(LOG, "getTokenTransferFee TRC21 TOMOZ Fee 0.05 > success: $it")
                },{
                    Log.e(LOG, "getTokenTransferFee TRC21 TOMOZ Fee 0.05 > fail: " + it.localizedMessage)
                }
            )

        WalletCore.getTokenManager("0x06605b28aab9835be75ca242a8ae58f2e15f2f45",
            "0xb38c1b54de4068d3c9c87ed447fdfa500954802b")
            ?.getTokenTransferFee("0x06605b28aab9835be75ca242a8ae58f2e15f2f45")
            ?.subscribe(
                {
                    Log.d(LOG, "getTokenTransferFee TRC21 Fee 0 > success: $it")
                },{
                    Log.e(LOG, "getTokenTransferFee TRC21 Fee 0 > fail: " + it.localizedMessage)
                }
            )

        WalletCore.getTokenManager("0x06605b28aab9835be75ca242a8ae58f2e15f2f45",
            "0x647115852d611312a363749a1892b307044838ca")
            ?.getTokenTransferFee("0x06605b28aab9835be75ca242a8ae58f2e15f2f45")
            ?.subscribe(
                {
                    Log.d(LOG, "getTokenTransferFee TRC20 Fee 0 > success: $it")
                },{
                    Log.e(LOG, "getTokenTransferFee TRC20 Fee 0 > fail: " + it.localizedMessage)
                }
            )


        WalletCore.getTokenManager("0x06605b28aab9835be75ca242a8ae58f2e15f2f45",
            "0x6e7312d1028b70771bb9cdd9837442230a9349ca")
            ?.getTokenTransferFee("0x06605b28aab9835be75ca242a8ae58f2e15f2f45")
            ?.subscribe(
                {
                    Log.d(LOG, "getTokenTransferFee Not token > success: $it")
                },{
                    Log.e(LOG, "getTokenTransferFee Not token > fail: " + it.localizedMessage)
                }
            )


        WalletCore.getTokenManager("0x06605b28aab9835be75ca242a8ae58f2e15f2f45")
            ?.getTRC20Services()
            ?.estimateTokenTransferGasLimit("0x27b34cd1615014e4ff8a2255f2969f65634f57fc",
                "0x6e7312d1028b70771bb9cdd9837442230a9349ca", BigInteger.ZERO)
            ?.subscribe(
                {
                    Log.d(LOG, "getTRC20Services > getTokenTransferFee > success: $it")
                },{
                    Log.e(LOG, "getTRC20Services > getTokenTransferFee > fail: " + it.localizedMessage)
                }
            )

        /*WalletCore.getTokenManager("0x06605b28aab9835be75ca242a8ae58f2e15f2f45",
            "0xb38c1b54de4068d3c9c87ed447fdfa500954802b")
            ?.getTRC21Services()
            ?.isTRC21Token("0xb38c1b54de4068d3c9c87ed447fdfa500954802b")
            ?.subscribe(
                {
                    Log.d(LOG, "isTRC21Token > success: $it")
                },{
                    Log.e(LOG, "isTRC21Token > fail: " + it.localizedMessage)
                }
            )*/



        /*WalletCore.getTokenManager("0x06605b28aab9835be75ca242a8ae58f2e15f2f45",
            "0x095d85e62cb6ad354ff900c1d530a7c4b8e247b5")
            ?.getTRC20Services()
            ?.transferToken("0x095d85e62cb6ad354ff900c1d530a7c4b8e247b5",
                "0x6e7312d1028b70771bb9cdd9837442230a9349ca",
                ConvertUtil.toWei("0.11111111111", ConvertUtil.Unit.ETHER).toBigInteger(),
                callback = object : TransactionListener{
                    override fun onTransactionCreated(txId: String) {
                        Log.d(LOG, "transferTRC20Token > onTransactionCreated: $txId")
                    }

                    override fun onTransactionComplete(txId: String, status: String) {
                        Log.d(LOG, "transferTRC20Token > onTransactionComplete: $txId $status")
                    }

                    override fun onTransactionError(e: Exception) {
                        Log.d(LOG, "transferTRC20Token > onTransactionError: $e")
                    }
                })

        WalletCore.getWalletFunctions("0x06605b28aab9835be75ca242a8ae58f2e15f2f45")
            ?.signTransaction("0x9ca26574122f2423c05ddee0d12083085dac0528",
                ConvertUtil.toWei("0.11111111111", ConvertUtil.Unit.ETHER).toBigInteger())
            ?.map {
                WalletCore.getWalletFunctions("0x06605b28aab9835be75ca242a8ae58f2e15f2f45")
                    ?.sendSignedTransaction(it.signedData,
                        callback = object : TransactionListener{
                            override fun onTransactionCreated(txId: String) {
                                Log.d(LOG, "sendSignedTransaction > onTransactionCreated: $txId")
                            }

                            override fun onTransactionComplete(txId: String, status: String) {
                                Log.d(LOG, "sendSignedTransaction > onTransactionComplete: $txId $status")
                            }

                            override fun onTransactionError(e: Exception) {
                                Log.d(LOG, "sendSignedTransaction > onTransactionError: $e")
                            }
                        }
                )
            }?.subscribe()


        WalletCore.getWalletSecretData("0x06605b28aab9835be75ca242a8ae58f2e15f2f45")
            ?.getPrivateKey()
            ?.subscribe({
                Log.d(LOG, "getPrivateKey > success: $it")
            },{
                Log.e(LOG, "getPrivateKey > fail: $it")
            })*/


        /*WalletCore.getWalletFunctions("0x06605b28aab9835be75ca242a8ae58f2e15f2f45")
            ?.sendSignedTransaction("0xf86c81a9840ee6b2808252089406605b28aab9835be75ca242a8ae58f2e15f2f45880de0b6b3a7640000801ca05b50465b09979286d97369f0d9dd19d2e9d99c54b05c8bd09be2fe7902c15a99a07265de3538497e73ab80660615c47a2321b34deb23ca366b1fa52906a5fec4b1",
                callback = object : TransactionListener{
                    override fun onTransactionCreated(txId: String) {
                        Log.d(LOG, "sendSignedTransaction > onTransactionCreated: $txId")
                    }

                    override fun onTransactionComplete(txId: String, status: String) {
                        Log.d(LOG, "sendSignedTransaction > onTransactionComplete: $txId $status")
                    }

                    override fun onTransactionError(e: Exception) {
                        Log.d(LOG, "sendSignedTransaction > onTransactionError: $e")
                    }
                })*/


        /*WalletCore.getWalletFunctions("0x9ca26574122f2423c05ddee0d12083085dac0528")
            ?.transfer("0x06605B28aab9835be75ca242a8aE58f2e15F2F45",
                ConvertUtil.toWei("1", ConvertUtil.Unit.ETHER).toBigInteger(),
                callback = object : TransactionListener{
                    override fun onTransactionCreated(txId: String) {
                        Log.d(LOG, "transfer > onTransactionCreated: $txId")
                    }

                    override fun onTransactionComplete(txId: String, status: String) {
                        Log.d(LOG, "transfer > onTransactionComplete: $txId $status")
                    }

                    override fun onTransactionError(e: Exception) {
                        Log.d(LOG, "transfer > onTransactionError: $e")
                    }
                })*/



        /*WalletCore.getInstance()?.coreFunctions?.createWalletFromMnemonics("renew tomato danger silly hybrid void fault inflict crazy sail develop exchange")?.subscribe(
            {
                Log.d(LOG, "createWalletFromMnemonics > success: $it")
            },{
                Log.e(LOG, "createWalletFromMnemonics > fail: " + it.localizedMessage)
            }
        )

        WalletCore.getInstance()?.coreFunctions?.createWalletFromPrivateKey("fe514e9fa6e6f96e63640e80ba413ba0994bac81357fd7bab18b1302bf347750")?.subscribe(
            {
                Log.d(LOG, "createWalletFromPrivateKey > success: $it")
            },{
                Log.e(LOG, "createWalletFromPrivateKey > fail: " + it.localizedMessage)
            }
        )*/

        /*WalletCore.getInstance()?.walletSecretDataService
            ?.getMnemonics("0x0743c519c2eb4364e022dc1e2e763ab4a532081c")
            ?.subscribe({
                Log.d(LOG, "getMnemonics > success: $it")
            },{
                Log.e(LOG, "getMnemonics > fail: " + it.localizedMessage)
            })

        WalletCore.getInstance()?.walletSecretDataService
            ?.getMnemonics("0x42228b53c9e93767d862f9a1b76307d317dc13a6")
            ?.subscribe({
                Log.d(LOG, "getMnemonics > success: $it")
            },{
                Log.e(LOG, "getMnemonics > fail: " + it.localizedMessage)
            })

        WalletCore.getInstance()?.coreFunctions?.importWalletFromMnemonics("0x42228b53c9e93767d862f9a1b76307d317dc13a6")?.subscribe(
            {
                Log.d(LOG, "createWalletFromMnemonics > success: $it")
            },{
                Log.e(LOG, "createWalletFromMnemonics > fail: " + it.localizedMessage)
            }
        )
        WalletCore.getInstance()
            ?.coreFunctions
            ?.getAllWallet()
            ?.subscribe(
                {
                    it.forEach {wallet ->
                        Log.d(LOG, "getAllWallet > onSuccess: $wallet")
                    }
                },{
                    Log.e(LOG, "getAllWallet > error: $it")
                }
            )*/
        /*WalletCore.getInstance()
            ?.tokenManager
            ?.withWalletAddress("0x06605b28aab9835be75ca242a8ae58f2e15f2f45")
            ?.withTokenAddress("0x42228b53c9e93767d862f9a1b76307d317dc13a6")
            ?.transferFormattedToken("0x6e7312d1028b70771bb9cdd9837442230a9349ca",
                BigDecimal("1.0"), callback = object : TransactionListener{
                    override fun onTransactionCreated(txId: String) {
                        Log.d(LOG,"transferToken > onTransactionCreated: $txId")
                    }

                    override fun onTransactionComplete(txId: String, status: String) {
                        Log.d(LOG,"transferToken > onTransactionComplete: $txId $status")
                    }

                    override fun onTransactionError(e: Exception) {
                        Log.d(LOG,"transferToken > onTransactionError: ${e.localizedMessage}")
                    }
                })*/
        /*WalletCore.getInstance("0x06605b28aab9835be75ca242a8ae58f2e15f2f45")?.signerService?.signTransaction(
            "0x6e7312d1028b70771bb9cdd9837442230a9349ca", BigInteger("100000"),payload = null
        )?.subscribe(
            {
                Log.d(LOG, "signTransaction > success: $it")
                WalletCore.getInstance()
                    ?.coreBlockChainService
                    ?.sendSignedTransaction(it.signedData,
                        callback = object: TransactionListener{
                            override fun onTransactionCreated(txId: String) {
                                Log.d(LOG,"transferToken > onTransactionCreated: $txId")
                            }

                            override fun onTransactionComplete(txId: String, status: String) {
                                Log.d(LOG,"transferToken > onTransactionComplete: $txId $status")
                            }

                            override fun onTransactionError(e: Exception) {
                                Log.d(LOG,"transferToken > onTransactionError: ${e.localizedMessage}")
                            }
                        })
            },{
                Log.e(LOG, "signTransaction > fail: " + it.localizedMessage)
            }
        )*/


        /*WalletCore.getInstance()?.coreFunctions?.createWalletFromMnemonics("renew tomato danger silly hybrid void fault inflict crazy sail develop exchange")?.subscribe(
            {
                Log.d(LOG, "createWalletFromPrivateKey > success: $it")
            },{
                Log.e(LOG, "createWalletFromPrivateKey > fail: " + it.localizedMessage)
            }
        )*/


        /*WalletCore.getInstance("0x06605b28aab9835be75ca242a8ae58f2e15f2f45")?.signerService?.signTransaction(
            "0x6e7312d1028b70771bb9cdd9837442230a9349ca", BigInteger("100000"),payload = null
        )?.subscribe(
            {
                Log.d(LOG, "signTransaction > success: $it")
            },{
                Log.e(LOG, "signTransaction > fail: " + it.localizedMessage)
            }
        )*/


        /*WalletCore.getInstance()
            ?.tokenManager
            ?.withWalletAddress("0x06605b28aab9835be75ca242a8ae58f2e15f2f45")
            ?.withTokenAddress("0x095d85e62cb6ad354ff900c1d530a7c4b8e247b5")
            ?.getTokenBalance()
            ?.subscribe(
                {
                    Log.d(LOG, "getTokenBalance > onSuccess: $it")
                },{
                    Log.e(LOG, "getTokenBalance > error: $it")
                }
            )*/


        /*WalletCore.getInstance()
            ?.tokenManager
            ?.withWalletAddress("0x06605b28aab9835be75ca242a8ae58f2e15f2f45")
            ?.withTokenAddress("0x095d85e62cb6ad354ff900c1d530a7c4b8e247b5")
            ?.getTokenFormattedBalance()
            ?.subscribe(
                {
                    Log.d(LOG, "getTokenBalance > onSuccess: $it")
                },{
                    Log.e(LOG, "getTokenBalance > error: $it")
                }
            )


        WalletCore.getInstance()
            ?.tokenManager
            ?.withWalletAddress("0x06605b28aab9835be75ca242a8ae58f2e15f2f45")
            ?.withTokenAddress("0x095d85e62cb6ad354ff900c1d530a7c4b8e247b5")
            ?.getFormattedTokenTransferFee("0x6e7312d1028b70771bb9cdd9837442230a9349ca", BigInteger("100000"))
            ?.subscribe(
                {
                    Log.d(LOG, "getTokenTransferFee > onSuccess: $it")
                },{
                    Log.e(LOG, "getTokenTransferFee > error: $it")
                }
            )*/
        /*WalletCore.getInstance()?.coreFunctions?.createWalletFromPrivateKey("fe514e9fa6e6f96e63640e80ba413ba0994bac81357fd7bab18b1302bf347750")?.subscribe(
            {
                Log.d(LOG, "createWalletFromPrivateKey > success: $it")
            },{
                Log.e(LOG, "createWalletFromPrivateKey > fail: " + it.localizedMessage)
            }
        )*/



        /*WalletCore.getInstance()?.coreFunctions
            ?.removeWallet("0x06605b28aab9835be75ca242a8ae58f2e15f2f45")
            ?.subscribe(
                {
                    Log.d(LOG,"removeWallet > success: $it")
                },{
                    Log.d(LOG,"removeWallet > error: $it")
                }
            )

        WalletCore.getInstance()
            ?.tokenManager
            ?.withWalletAddress("0x06605b28aab9835be75ca242a8ae58f2e15f2f45")
            ?.withTokenAddress("0x9afff1e2657e3b87b9ccc9cc9a3fc1ed2f177b8a")
            ?.transferFormattedToken("0x6e7312d1028b70771bb9cdd9837442230a9349ca",
                BigDecimal("1.0"), callback = object : TransactionListener{
                    override fun onTransactionCreated(txId: String) {
                        Log.d(LOG,"transferToken > onTransactionCreated: $txId")
                    }

                    override fun onTransactionComplete(txId: String, status: String) {
                        Log.d(LOG,"transferToken > onTransactionComplete: $txId $status")
                    }

                    override fun onTransactionError(e: Exception) {
                        Log.d(LOG,"transferToken > onTransactionError: ${e.localizedMessage}")
                    }
                })*/


        /*WalletCore.getInstance()
            ?.trc21TokenService
            ?.getTOMOZContractList()
            ?.subscribe(
                {
                    Log.d(LOG, "getTokensList > onSuccess: ${it.size}")
                    *//*it.forEach {address ->
                        Log.d(LOG, "getTokensList > onSuccess: $address")
                    }*//*
                },{
                    Log.e(LOG, "getTokensList > error: $it")
                }
            )


        WalletCore.getInstance()
            ?.tokenService
            ?.getTokenInfo("0x095d85e62cb6ad354ff900c1d530a7c4b8e247b5")
            ?.subscribe(
                {
                    Log.d(LOG, "getTokenInfo > onSuccess: $it")
                },{
                    Log.e(LOG, "getTokenInfo > error: $it")
                }
            )

        WalletCore.getInstance()
            ?.trc21TokenService
            ?.isTRC21Token("0xc9cbc5e28b6b634831a23eed9a6a0fe6caaacb4b")
            ?.subscribe(
                {
                    Log.d(LOG, "getTokenInfo > onSuccess: $it")
                },{
                    Log.e(LOG, "getTokenInfo > error: $it")
                }
            )*/


        /*WalletCore.getInstance()
            ?.coreFunctions
            ?.createWalletFromPrivateKey("d7dbceae0451d6fbe1e81e44d016cea7b27640aa75cf8cdf4f8a22fb1cf9f4a5")?.subscribe(
            {
                Log.d(LOG, "success: $it")
            },{
                Log.e(LOG, "fail: " + it.localizedMessage)
            }
            )*/
        /*WalletCore.getInstance("0xb085ab4dbbb3bcac94f871bc354ad32190e858dd")
            ?.trc21TokenService
            ?.getBalance("0xb38c1b54de4068d3c9c87ed447fdfa500954802b")
            ?.subscribe(
                {
                    Log.d(LOG, "getBalance > onSuccess: $it")
                },{
                    Log.e(LOG, "getBalance > error: $it")
                }
            )

        WalletCore.getInstance("0xb085ab4dbbb3bcac94f871bc354ad32190e858dd")
            ?.trc21TokenService
            ?.getTokenTransferFee("0xb38c1b54de4068d3c9c87ed447fdfa500954802b")
            ?.subscribe(
                {
                    Log.d(LOG, "getTokenTransferFee > onSuccess: $it")
                },{
                    Log.e(LOG, "getTokenTransferFee > error: $it")
                }
            )

        WalletCore.getInstance("0xb085ab4dbbb3bcac94f871bc354ad32190e858dd")
            ?.trc21TokenService
            ?.getTOMOZContractList()
            ?.subscribe(
                {
                    Log.d(LOG, "getTokensList > onSuccess: ${it.size}")
                    it.forEach {address ->
                        Log.d(LOG, "getTokensList > onSuccess: $address")
                    }
                },{
                    Log.e(LOG, "getTokensList > error: $it")
                }
            )


        val token = WalletCore.getInstance("0x06605b28aab9835be75ca242a8ae58f2e15f2f45")
            ?.trc20TokenService
        token?.getTokenInfo("0x30c83c01836efb367fad1c03247327988c35aeaf")
            ?.subscribe(
                {
                    Log.d(LOG, "getTokenInfo > onSuccess: $it")
                },{
                    Log.e(LOG, "getTokenInfo > error: $it")
                }
            )
*/
        /*WalletCore.getInstance("0xb085ab4dbbb3bcac94f871bc354ad32190e858dd")
            ?.trc21TokenService
            ?.transferToken("0xb38c1b54de4068d3c9c87ed447fdfa500954802b",
                "0x6e7312d1028b70771bb9cdd9837442230a9349ca",
                BigInteger("1").multiply(BigInteger.TEN.pow(18)), callback = object : TransactionListener{
                    override fun onTransactionCreated(txId: String) {
                        Log.d(LOG,"transferToken > onTransactionCreated: $txId")
                    }

                    override fun onTransactionComplete(txId: String, status: String) {
                        Log.d(LOG,"transferToken > onTransactionComplete: $txId $status")
                    }

                    override fun onTransactionError(e: Exception) {
                        Log.d(LOG,"transferToken > onTransactionError: ${e.localizedMessage}")
                    }
                })*/


        /*
        val token = WalletCore.getInstance("0x06605b28aab9835be75ca242a8ae58f2e15f2f45")
            ?.trc20TokenService
        token?.getTokenInfo("0x30c83c01836efb367fad1c03247327988c35aeaf")
            ?.subscribe(
                {
                    Log.d(LOG, "getTokenInfo > onSuccess: $it")
                },{
                    Log.e(LOG, "getTokenInfo > error: $it")
                }
            )

        token?.getBalance("0x3e86b7367d9b9669e09d6e9b8ec025baa372b241")
            ?.subscribe(
                {
                    Log.d(LOG, "getBalance > onSuccess: $it")
                },{
                    Log.e(LOG, "getBalance > error: $it")
                }
            )

        token?.getBalance(TokenInfo("0x3e86b7367d9b9669e09d6e9b8ec025baa372b241", "HayTraoChoAnh", "MTP", 18))
            ?.subscribe(
                {
                    Log.d(LOG, "getBalance > onSuccess: $it")
                },{
                    Log.e(LOG, "getBalance > error: $it")
                }
            )

        token?.transferToken("0x3d05de67538b3dafc757f970424eabce6b061bc2",
            "0x6e7312d1028b70771bb9cdd9837442230a9349ca",
            BigInteger("1").multiply(BigInteger.TEN.pow(18)), callback = object : TransactionListener{
                override fun onTransactionCreated(txId: String) {
                    Log.d(LOG,"transferToken > onTransactionCreated: $txId")
                }

                override fun onTransactionComplete(txId: String, status: String) {
                    Log.d(LOG,"transferToken > onTransactionComplete: $txId $status")
                }

                override fun onTransactionError(e: Exception) {
                    Log.d(LOG,"transferToken > onTransactionError: $e")
                }
            }, gasLimit = BigInteger("100000")
        )


        token?.estimateTokenTransferGasLimit("0x3d05de67538b3dafc757f970424eabce6b061bc2",
            "0x6e7312d1028b70771bb9cdd9837442230a9349ca",
            BigInteger("1").multiply(BigInteger.TEN.pow(18)))
            ?.subscribe(
                {
                    Log.d(LOG, "estimateTokenTransferGas > onSuccess: $it")
                },{
                    Log.e(LOG, "estimateTokenTransferGas > error: $it")
                }
            )*/
    }


    //@Test
    fun testCoreFunctionService(){


        /*val address = "0x6e7312d1028b70771bb9cdd9837442230a9349ca"
        Log.d(LOG, "testCoreFunctionService")
        val service = WalletCore
                .getInstance()?.coreFunctions




        service?.createWalletFromAddress(address)?.subscribe(
            {
                Log.d(LOG, "success: $it")
            },{
                Log.e(LOG, "fail: " + it.localizedMessage)
            }
        )


        service?.createWalletFromAddress("0x6e7312d1028b70771bb9cdd9837442230a9349cd")?.subscribe(
            {
                Log.d(LOG, "success: $it")
            },{
                Log.e(LOG, "fail: " + it.localizedMessage)
            }
        )

        service?.createWalletFromPrivateKey("d7dbceae0451d6fbe1e81e44d016cea7b27640aa75cf8cdf4f8a22fb1cf9f4a5")?.subscribe(
            {
                Log.d(LOG, "success: $it")
            },{
                Log.e(LOG, "fail: " + it.localizedMessage)
            }
        )



        val allAccount = service?.getAllWallet()?.blockingGet()

        Log.e(LOG, "allAccount: " + allAccount?.size)*/

        /*assertEquals(allAccount?.size, 1)
        assertEquals(allAccount?.get(0)?.address, address)

        service?.createWalletFromAddress(address)?.test()?.assertError(WalletAlreadyExistedException())*/


        /*val coreBlockChainService = WalletCore.getInstance("0x06605b28aab9835be75ca242a8ae58f2e15f2f45")
            ?.coreBlockChainService

        coreBlockChainService?.getAccountBalance()?.subscribe(
            {
                Log.d(LOG, "getAccountBalance > success: $it")
            },{
                Log.e(LOG, "getAccountBalance > fail: " + it.localizedMessage)
            }
        )

        coreBlockChainService?.transfer(
            "0x6e7312d1028b70771bb9cdd9837442230a9349ca",
            ConvertUtil.toWei("0.0002", ConvertUtil.Unit.ETHER).toBigInteger(),
            callback = object : TransactionListener{
                override fun onTransactionCreated(txId: String) {
                    Log.d(LOG,"transfer > onTransactionCreated: $txId")
                }

                override fun onTransactionComplete(txId: String, status: String) {
                    Log.d(LOG,"transfer > onTransactionComplete: $txId $status")
                }

                override fun onTransactionError(e: Exception) {
                    Log.d(LOG,"transfer > onTransactionError: $e")
                }
            }
        )*/

        /*coreBlockChainService?.getTransactionStatus(
            "0x1eabf1949a47f422fce15d2da98d57a66f5ae2c4edc224bb4424488c3c388515"
        )?.subscribe({
            Log.d(LOG,"getTransactionStatus > success: $it")
        },{
            Log.e(LOG,"getTransactionStatus > fail: $it")
        })*/


        /*WalletCore.getInstance("0x06605b28aab9835be75ca242a8ae58f2e15f2f45")
            ?.trC20Service
            ?.transferToken(
                "0x9afff1e2657e3b87b9ccc9cc9a3fc1ed2f177b8a",
                "0x6e7312d1028b70771bb9cdd9837442230a9349ca",
                BigInteger("5"),
                BigInteger("300000000"),
                BigInteger("500000"),
                object : TransactionListener{
                    override fun onTransactionCreated(txId: String) {
                        Log.d(LOG, "transferToken > onTransactionCreated: $txId")
                        assertEquals(txId.length, 66)
                    }

                    override fun onTransactionComplete(txId: String, status: String) {
                        Log.d(LOG, "transferToken > onTransactionComplete: $txId $status")
                    }

                    override fun onTransactionError(e: Exception) {
                        Log.d(LOG, "transferToken > onTransactionError: $e")
                    }
                })*/
    }

    @Test
    fun testWalletService(){

        /*val service = WalletCore.getInstance()?.walletService
        assertEquals("word list size is not 2048",service?.getWordList()?.size,2048)
        assertEquals("mnemonics size is not 12",service?.generateMnemonics()?.split(" ")?.size,12)

        val mockMnemonic = "embrace canyon orphan supreme cat theory hurt company purse strike pen state"
        val mockPKey = "fe514e9fa6e6f96e63640e80ba413ba0994bac81357fd7bab18b1302bf347750"
        val mockAddress = "0xb1da6b66311ae8d24fd3958e324157c01f3fdc75".toLowerCase()




        assertEquals("private key not match address",
            service?.createWalletFromPrivateKey(mockPKey)?.blockingGet()?.address, "0x06605b28aab9835be75ca242a8ae58f2e15f2f45")

        assertEquals("mnemonic not match address",
            service?.createWalletFromMnemonics(mockMnemonic)?.blockingGet()?.address, mockAddress)


        service?.createWalletFromPrivateKey(mockPKey.substring(1))
            ?.test()?.assertError {
                it is InvalidPrivateKeyException
            }

        service?.createWalletFromMnemonics("embrace canyon orphan")?.test()
                ?.assertError{
                    it is InvalidMnemonicException
                }*/


    }



}