package com.tomochain.wallet.core.common

/**
 * Created by cityme on 30,August,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class Config {

    class Database{
        companion object {
            const val VERSION = 1
            const val NAME = "tesseract.db"
        }
    }


    class Transaction{
        companion object{
            const val DEFAULT_GAS_PRICE = "300000000"
            const val DEFAULT_GAS_LIMIT = "50000"
            const val DEFAULT_GAS_LIMIT_CONTRACT_CALL = "500000"
        }
    }


    class HDPath {
        companion object {
            const val TOMO = "m/44'/889'/0'/0/0"
        }
    }
}