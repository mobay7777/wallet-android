package com.tomochain.wallet.core.w3jl.components.tomochain.token

import android.annotation.SuppressLint
import android.util.Log
import com.tomochain.wallet.core.common.Config
import com.tomochain.wallet.core.common.LogTag
import com.tomochain.wallet.core.common.exception.InvalidAddressException
import com.tomochain.wallet.core.common.exception.InvalidPrivateKeyException
import com.tomochain.wallet.core.common.exception.WalletNotFoundException
import com.tomochain.wallet.core.habak.EncryptedModel
import com.tomochain.wallet.core.habak.cryptography.Habak
import com.tomochain.wallet.core.room.walletSecret.WalletSecretDAO
import com.tomochain.wallet.core.w3jl.config.chain.Chain
import com.tomochain.wallet.core.w3jl.listeners.TransactionListener
import com.tomochain.wallet.core.w3jl.utils.WalletUtil
import io.reactivex.Single
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Bool
import org.web3j.abi.datatypes.Utf8String
import org.web3j.crypto.Credentials
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.tx.response.QueuingTransactionReceiptProcessor
import org.web3j.utils.Numeric
import java.math.BigInteger

/**
 * Created by cityme on 23,May,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class TRC20ServiceImpl(override var address: String?,
                       override var web3j: Web3j?,
                       override var chain: Chain?,
                       private var dao: WalletSecretDAO?,
                       private var habak: Habak?) : TokenServiceImpl(address, web3j, chain), TRC20Service {

    @SuppressLint("CheckResult")
    override fun transferToken(
        tokenAddress: String,
        recipient: String,
        amount: BigInteger,
        callback: TransactionListener?,
        gasPrice: BigInteger?,
        gasLimit: BigInteger?
    ) {
        try {
            if (!WalletUtil.isValidAddress(address)){
                callback?.onTransactionError(InvalidAddressException())
                return
            }
            dao?.getWallet(address!!)?.subscribe(
                { wallet ->
                    if (!WalletUtil.isValidAddress(address) || !WalletUtil.isValidAddress(tokenAddress) ){
                        callback?.onTransactionError(InvalidAddressException())
                    }
                    val pKey = habak?.decrypt(EncryptedModel.readFromString(wallet!!.encryptedPKey))
                    if ( !WalletUtil.isValidPrivateKey(pKey.toString())){
                        callback?.onTransactionError(InvalidPrivateKeyException())
                        pKey?.clear()
                        return@subscribe
                    }
                    val ethGetTransactionCount = web3j?.ethGetTransactionCount(
                        address, DefaultBlockParameterName.LATEST)?.sendAsync()?.get()
                    val nonce = ethGetTransactionCount?.transactionCount

                    val credentials = Credentials.create(pKey.toString())
                    pKey?.clear()

                    val function = Function(
                        "transfer",
                        listOf(
                            Address(recipient),
                            Uint256(amount)
                        ),
                        listOf(object : TypeReference<Bool>() {

                        }))
                    val encodedFunction = FunctionEncoder.encode(function)


                    val calculatedGasLimit = if (gasLimit == null){
                        val response =web3j?.ethEstimateGas(Transaction
                            .createFunctionCallTransaction(
                                address,
                                nonce,
                                BigInteger(Config.Transaction.DEFAULT_GAS_PRICE),
                                BigInteger(Config.Transaction.DEFAULT_GAS_LIMIT_CONTRACT_CALL),
                                tokenAddress, encodedFunction))?.sendAsync()?.get()
                        if (response != null && response.result != null)
                            BigInteger(Numeric.cleanHexPrefix(response.result),16)
                        else
                            BigInteger(Config.Transaction.DEFAULT_GAS_LIMIT_CONTRACT_CALL)

                    }else{
                        gasLimit
                    }

                    Log.d(LogTag.TAG_W3JL, "calculatedGasLimit: $calculatedGasLimit")
                    val rawTransaction = RawTransaction
                        .createTransaction(nonce,gasPrice, calculatedGasLimit,
                            tokenAddress, encodedFunction)

                    val signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials)
                    val signedMessageHex  = Numeric.toHexString(signedMessage)
                    web3j?.ethSendRawTransaction(signedMessageHex)?.flowable()?.subscribe({transaction ->
                        callback?.onTransactionCreated(transaction.transactionHash)
                        web3j?.ethGetTransactionReceipt(transaction.transactionHash)
                            ?.flowable()
                            ?.toObservable()
                            ?.repeat()
                            ?.map { r ->
                                r.result.status
                            }
                            ?.takeUntil {
                                it.contains("0x", true)
                            }
                            ?.retry()
                            ?.subscribe {
                                callback?.onTransactionComplete(transaction.transactionHash, it)
                            }
                    }, {
                        callback?.onTransactionError(it as Exception)
                    })

                    /*val tokenContract =
                        TRC20(
                            tokenAddress,
                            web3j,
                            credentials,
                            gasPrice,
                            gasLimit
                        )
                    tokenContract.transfer(recipient, amount).flowable()
                        .subscribe({ t ->
                            callback?.onTransactionCreated(t.transactionHash)
                            web3j?.ethGetTransactionReceipt(t.transactionHash)
                                ?.flowable()
                                ?.toObservable()
                                ?.repeat()
                                ?.map { r ->
                                    r.result.status
                                }
                                ?.takeUntil {
                                    it.contains("0x", true)
                                }
                                ?.retry()
                                ?.subscribe {
                                    callback?.onTransactionComplete(t.transactionHash, it)
                                }
                        },{e ->
                            callback?.onTransactionError(e as Exception)
                        })*/

                },{
                    callback?.onTransactionError(it as Exception)
                }
            )
        }catch (e: Exception){
            callback?.onTransactionError(e)
        }
    }

    override fun estimateTokenTransferGas(tokenAddress: String,
                                          recipient: String,
                                          amount: BigInteger): Single<BigInteger> {
        return Single.create { emitter ->
            try {
                val function = Function(
                    "transfer",
                    listOf(
                        Address(recipient),
                        Uint256(amount)
                    ),
                    listOf(object : TypeReference<Bool>() {

                    }))
                val encodedFunction = FunctionEncoder.encode(function)
                val ethGetTransactionCount = web3j?.ethGetTransactionCount(
                    address, DefaultBlockParameterName.LATEST)?.sendAsync()?.get()
                val nonce = ethGetTransactionCount?.transactionCount
                val transaction  = Transaction
                    .createFunctionCallTransaction(
                        address,
                        nonce,
                        BigInteger(Config.Transaction.DEFAULT_GAS_PRICE),
                        BigInteger(Config.Transaction.DEFAULT_GAS_LIMIT_CONTRACT_CALL),
                        tokenAddress, encodedFunction)

                val response = web3j?.ethEstimateGas(transaction)?.sendAsync()?.get()
                if (response != null && response.result != null){
                    try {
                        emitter.onSuccess(BigInteger(Numeric.cleanHexPrefix(response.result),16))
                    }catch (e: Exception){
                        emitter.onSuccess(BigInteger.valueOf(21000))
                    }
                }else{
                    emitter.onSuccess(BigInteger.valueOf(21000))
                }
            } catch (e: Exception) {
                emitter.tryOnError(e)
            }
        }
    }

    override fun test(): String {
        return "test"
    }
}