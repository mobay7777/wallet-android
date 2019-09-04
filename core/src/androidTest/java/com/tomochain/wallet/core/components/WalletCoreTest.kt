package com.tomochain.wallet.core.components

import android.provider.Settings
import android.util.Log
import androidx.test.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import com.tomochain.wallet.core.common.exception.InvalidMnemonicException
import com.tomochain.wallet.core.common.exception.InvalidPrivateKeyException
import com.tomochain.wallet.core.common.exception.WalletAlreadyExistedException
import com.tomochain.wallet.core.w3jl.config.chain.Chain
import com.tomochain.wallet.core.w3jl.config.chain.CommonChain
import com.tomochain.wallet.core.w3jl.listeners.TransactionListener
import com.tomochain.wallet.core.w3jl.utils.ConvertUtil
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.ref.WeakReference
import org.junit.Assert.*
import org.junit.Rule
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
        })
    }

    @After
    fun tearDown() {
        WalletCore.destroyInstance()
    }

    @Test
    fun testCoreFunctionService(){


        val address = "0x6e7312d1028b70771bb9cdd9837442230a9349ca"
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

        service?.createWalletFromPrivateKey("fe514e9fa6e6f96e63640e80ba413ba0994bac81357fd7bab18b1302bf347750")?.subscribe(
            {
                Log.d(LOG, "success: $it")
            },{
                Log.e(LOG, "fail: " + it.localizedMessage)
            }
        )



        val allAccount = service?.getAllWallet()?.blockingGet()

        Log.e(LOG, "allAccount: " + allAccount?.size)

        /*assertEquals(allAccount?.size, 1)
        assertEquals(allAccount?.get(0)?.address, address)

        service?.createWalletFromAddress(address)?.test()?.assertError(WalletAlreadyExistedException())*/


        val coreBlockChainService = WalletCore.getInstance("0x06605b28aab9835be75ca242a8ae58f2e15f2f45")
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
            ConvertUtil.toWei("1", ConvertUtil.Unit.ETHER).toBigInteger(),
            BigInteger("250000000"),
            BigInteger("100000"),
                    "Google Play: Google Play is a collection of services that allow users to discover, install, and purchase applications from their Android device or the web. Google Play makes it easy for developers to reach Android users and potential customers. Google Play also provides community review, application license verification, application security scanning, and other security services" +
                    "Android updates: The Android update service delivers new capabilities and security updates to selected Android devices, including updates through the web or over the air (OTA)" +
                    "Application services: Frameworks that allow Android applications to use cloud capabilities such as (backing up) application data and settings and cloud-to-device messaging (C2DM) for push messaging" +
                    "Verify Apps: Warn or automatically block the installation of harmful applications, and continually scan applications on the device, warning about or removing harmful apps" +
                    "SafetyNet: A privacy preserving intrusion detection system to assist Google tracking and mitigating known security threats in addition to identifying new security threats" +
                    "SafetyNet Attestation: Third-party API to determine whether the device is CTS compatible. Attestation can also assist identify the Android app communicating with the app server" +
                    "Android Device Manager: A web app and Android app to locate lost or stolen device."
        )?.test()?.assertValue {
            it.length == 66
        }

        coreBlockChainService?.estimateTransactionFee(
            "0x6e7312d1028b70771bb9cdd9837442230a9349ca",
            ConvertUtil.toWei("1", ConvertUtil.Unit.ETHER).toBigInteger(),"Google provides a set of cloud-based services that are available to compatible Android devices with Google Mobile Services. While these services are not part of the Android Open Source Project, they are included on many Android devices. For more information on some of these services, see Android Security’s 2017 Year in Review" +
                   
                    "The primary Google security services are:" +
                  
                    "Google Play: Google Play is a collection of services that allow users to discover, install, and purchase applications from their Android device or the web. Google Play makes it easy for developers to reach Android users and potential customers. Google Play also provides community review, application license verification, application security scanning, and other security services" +
                    "Android updates: The Android update service delivers new capabilities and security updates to selected Android devices, including updates through the web or over the air (OTA)" +
                    "Application services: Frameworks that allow Android applications to use cloud capabilities such as (backing up) application data and settings and cloud-to-device messaging (C2DM) for push messaging" +
                    "Verify Apps: Warn or automatically block the installation of harmful applications, and continually scan applications on the device, warning about or removing harmful apps" +
                    "SafetyNet: A privacy preserving intrusion detection system to assist Google tracking and mitigating known security threats in addition to identifying new security threats" +
                    "SafetyNet Attestation: Third-party API to determine whether the device is CTS compatible. Attestation can also assist identify the Android app communicating with the app server" +
                    "Android Device Manager: A web app and Android app to locate lost or stolen device."
        )?.subscribe(
            {
                Log.d(LOG, "estimateTransactionFee > success: $it")
            },{
                Log.d(LOG, "estimateTransactionFee > fail: $it")
            }
        )

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

        val service = WalletCore.getInstance()?.walletService
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
                }


    }



}