package com.tomochain.wallet.core.components

import com.tomochain.wallet.core.w3jl.components.coreBlockchain.BlockChainService
import com.tomochain.wallet.core.w3jl.entity.SignResult
import com.tomochain.wallet.core.w3jl.components.signer.SignerService
import com.tomochain.wallet.core.w3jl.entity.TransactionResult

import io.reactivex.Observable
import io.reactivex.Single
import java.math.BigInteger



/**
 * Created by cityme on 16,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class WalletFunctionsImpl(private val blockChainService: BlockChainService?,
                          private val signerService: SignerService?) : WalletFunctions {

    private var walletAddress: String? = ""

    override fun setWalletAddress(address: String?) {
        this.walletAddress = address?.toLowerCase()
        blockChainService?.setWalletAddress(this.walletAddress)
        signerService?.setWalletAddress(this.walletAddress)
    }

    override fun signMessage(message: String?) : Single<SignResult>?{
        return signerService?.signMessage(message)
    }

    override fun signPersonalMessage(message: String?) : Single<SignResult>?{
        return signerService?.signPersonalMessage(message)
    }

    override fun signTransaction(
        recipient: String,
        amount: BigInteger?,
        gasPrice: BigInteger?,
        gasLimit: BigInteger?,
        payload: String?
    ) : Single<SignResult>?{
        return signerService?.signTransaction(recipient, amount, gasPrice, gasLimit, payload)
    }

    override fun sendSignedTransaction(signedTransaction: String?): Observable<TransactionResult>?{
        return blockChainService?.sendSignedTransaction(signedTransaction)
    }


    override fun transfer(
        recipient: String,
        amount: BigInteger?,
        payload: String?,
        gasPrice: BigInteger?,
        gasLimit: BigInteger?
    ) : Observable<TransactionResult>?{
        return blockChainService?.transfer(recipient, amount, payload, gasPrice, gasLimit)
    }

    override fun getBalance(): Single<BigInteger>? {
        return blockChainService?.getAccountBalance()
    }
}