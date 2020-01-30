package com.tomochain.wallet.core.w3jl.config.chain

/**
 * Created by cityme on 03,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
enum class CommonChain : Chain {

    TOMO_CHAIN {
        override fun getEndpoint(): String {
            return "https://rpc.rupaya.io"
        }

        override fun getChainName(): String {
            return "Rupaya"
        }

        override fun getChainId(): Int {
            return 308
        }

        override fun getExplorerURL(): String {
            return "https://scan.rupaya.io"
        }

        override fun getCoinBaseUnit(): String {
            return "Rupaya"
        }

        override fun getCoinBaseSymbol(): String {
            return "RUPX"
        }

        override fun getHDPath(): String {
            return "M/44H/889H/0H/0/0"
        }
    },

    TOMO_CHAIN_TEST_NET {
        override fun getEndpoint(): String {
            return "https://testnet.rupaya.io"
        }

        override fun getChainName(): String {
            return "Rupaya Testnet"
        }

        override fun getChainId(): Int {
            return 89
        }

        override fun getExplorerURL(): String {
            return "https://scan.testnet.rupaya.io"
        }

        override fun getCoinBaseUnit(): String {
            return "Rupaya"
        }

        override fun getCoinBaseSymbol(): String {
            return "RUPX"
        }

        override fun getHDPath(): String {
            return "M/44H/889H/0H/0/0"
        }
    },

    TOMO_CHAIN_DEV_NET {
        override fun getEndpoint(): String {
            return "https://rpc.devnet.rupaya.io"
        }

        override fun getChainName(): String {
            return "Rupaya Devnet"
        }

        override fun getChainId(): Int {
            return 90
        }

        override fun getExplorerURL(): String {
            return "https://scan.devnet.rupaya.io"
        }

        override fun getCoinBaseUnit(): String {
            return "Rupaya"
        }

        override fun getCoinBaseSymbol(): String {
            return "RUPX"
        }

        override fun getHDPath(): String {
            return "M/44H/889H/0H/0/0"
        }
    }
}