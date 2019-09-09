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
            const val DEFAULT_GAS_PRICE = "250000000"
            const val DEFAULT_GAS_LIMIT = "21000"
            const val DEFAULT_GAS_LIMIT_WITH_PAYLOAD = "100000"
            const val DEFAULT_GAS_LIMIT_CONTRACT_CALL = "500000"
        }
    }


    class Address{
        companion object{
            const val DEFAULT = "0x0000000000000000000000000000000000000000"
            const val SAMPLE = "0x73507e8661778e4ea7df3dcb8a594A804411927E"
        }
    }


    class HDPath {
        companion object {
            const val TOMO = "m/44'/889'/0'/0/0"
        }
    }
}