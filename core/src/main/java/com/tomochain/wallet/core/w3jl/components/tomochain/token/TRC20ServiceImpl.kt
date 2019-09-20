package com.tomochain.wallet.core.w3jl.components.tomochain.token

import android.annotation.SuppressLint
import android.util.Log
import com.tomochain.wallet.core.common.Config
import com.tomochain.wallet.core.common.LogTag
import com.tomochain.wallet.core.common.exception.InsufficientBalanceException
import com.tomochain.wallet.core.common.exception.InvalidAddressException
import com.tomochain.wallet.core.common.exception.InvalidPrivateKeyException
import com.tomochain.wallet.core.components.CoreFunctions
import com.tomochain.wallet.core.habak.EncryptedModel
import com.tomochain.wallet.core.habak.cryptography.Habak
import com.tomochain.wallet.core.room.walletSecret.WalletSecretDAO
import com.tomochain.wallet.core.w3jl.components.coreBlockchain.BlockChainService
import com.tomochain.wallet.core.w3jl.config.chain.Chain
import com.tomochain.wallet.core.w3jl.entity.TransactionResult
import com.tomochain.wallet.core.w3jl.entity.TransactionStatus

import com.tomochain.wallet.core.w3jl.utils.ConvertUtil
import com.tomochain.wallet.core.w3jl.utils.WalletUtil
import com.tomochain.wallet.core.wallet.WalletSecretDataService
import io.reactivex.Observable
import io.reactivex.Single
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Bool
import org.web3j.crypto.Credentials
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.utils.Numeric
import java.lang.IllegalStateException
import java.math.BigInteger

/**
 * Created by cityme on 23,May,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class TRC20ServiceImpl( override var address: String?,
                        override val web3j: Web3j?,
                        override val chain: Chain?,
                        private val walletSecretDataService: WalletSecretDataService?,
                        private val coreBlockChainService: BlockChainService?) : TokenServiceImpl(address, web3j, chain), TRC20Service {

    @SuppressLint("CheckResult")
    override fun transferToken(
        tokenAddress: String,
        recipient: String,
        amount: BigInteger,
        gasPrice: BigInteger?,
        gasLimit: BigInteger?
    ): Observable<TransactionResult> {
        return Observable.create {emitter ->
            try {
                emitter.onNext(TransactionResult(null, TransactionStatus.CREATING))
                if (!WalletUtil.isValidAddress(address) || !WalletUtil.isValidAddress(tokenAddress)){
                    emitter.onError(InvalidAddressException())
                    return@create
                }
                walletSecretDataService?.setWalletAddress(address)
                walletSecretDataService?.getPrivateKey()?.subscribe(
                    { pKey ->
                        if ( !WalletUtil.isValidPrivateKey(pKey.toString())){
                            emitter.onError(InvalidPrivateKeyException())
                            pKey?.clear()
                            return@subscribe
                        }
                        getBalance(tokenAddress)
                            .subscribe({tokenBalance->
                                if (tokenBalance < amount){
                                    emitter.onError(InsufficientBalanceException("Your Token balance is not enough to perform transaction"))
                                    return@subscribe
                                }
                                val ethGetTransactionCount = web3j?.ethGetTransactionCount(
                                    address, DefaultBlockParameterName.LATEST)?.sendAsync()?.get()
                                val nonce = ethGetTransactionCount?.transactionCount

                                val credentials = Credentials.create(pKey.toString())
                                pKey?.clear()
                                val function = Function(
                                    "transfer",
                                    listOf(Address(recipient), Uint256(amount)),
                                    listOf(object : TypeReference<Bool>() {}))
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


                                val transactionFee = calculatedGasLimit.multiply(gasPrice)
                                coreBlockChainService?.setWalletAddress(address)
                                val availableTOMO = coreBlockChainService
                                    ?.getAccountBalance()?.blockingGet() ?: BigInteger.ZERO

                                if (availableTOMO < transactionFee){
                                    emitter.onError(InsufficientBalanceException
                                        ("This transaction require $transactionFee wei as fee. Your balance is $availableTOMO wei"))
                                    return@subscribe
                                }

                                val rawTransaction = RawTransaction
                                    .createTransaction(nonce,gasPrice, calculatedGasLimit,
                                        tokenAddress, encodedFunction)

                                val signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials)
                                val signedMessageHex  = Numeric.toHexString(signedMessage)
                                val transaction =web3j?.ethSendRawTransaction(signedMessageHex)?.send()
                                if (transaction != null && transaction.transactionHash != null){
                                    emitter.onNext(TransactionResult(transaction.transactionHash, TransactionStatus.PENDING))
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
                                            emitter.onNext(TransactionResult(transaction.transactionHash,
                                                if (it == "0x1") TransactionStatus.SUCCESS else TransactionStatus.FAILED))
                                        }
                                }else{
                                    emitter.onError(IllegalStateException(transaction?.error?.message))
                                }
                            },{

                            })
                    },{
                        emitter.onError(it)
                    }
                )
            }catch (e: Exception){
                emitter.onError(e)
            }
        }

    }

    override fun estimateTokenTransferGasLimit(tokenAddress: String,
                                          recipient: String,
                                          amount: BigInteger): Single<BigInteger> {
        return Single.create { emitter ->
            try {

                if (!WalletUtil.isValidAddress(address) || !WalletUtil.isValidAddress(tokenAddress) ){
                    emitter.onError(InvalidAddressException())
                }
                val function = Function(
                    "transfer",
                    listOf(
                        Address(recipient),
                        Uint256(ConvertUtil.toWei("1", ConvertUtil.Unit.ETHER).toBigInteger())
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
                        emitter.onSuccess(BigInteger(Config.Transaction.DEFAULT_GAS_LIMIT))
                    }
                }else{
                    emitter.onSuccess(BigInteger(Config.Transaction.DEFAULT_GAS_LIMIT))
                }
            } catch (e: Exception) {
                emitter.tryOnError(e)
            }
        }
    }


}