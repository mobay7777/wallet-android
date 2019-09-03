package com.tomochain.wallet.core.habak.cryptography.legacy

import java.security.KeyStore

/**
 * Created by NienLe on 11,October,2018
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */

interface KeyStoreRecoveryNotifier{
    /**
     *
     * @param e
     * @param keyStore
     * @param keyAliases
     * @return true if the error could be resolved
     */
    abstract fun onRecoveryRequired(e: Exception, keyStore: KeyStore, keyAliases: List<String>): Boolean
}