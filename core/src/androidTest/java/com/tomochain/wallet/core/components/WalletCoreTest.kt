package com.tomochain.wallet.core.components

import androidx.test.InstrumentationRegistry

import androidx.test.runner.AndroidJUnit4
import com.tomochain.wallet.core.room.walletSecret.EntityWalletSecret
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.ref.WeakReference
import org.junit.Assert.*
import java.util.function.Predicate

/**
 * Created by cityme on 03,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
@RunWith(AndroidJUnit4::class)
internal class WalletCoreTest {

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        WalletCore.setup(WeakReference(context))
    }

    @After
    fun tearDown() {
        WalletCore.destroyInstance()
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



    }



}