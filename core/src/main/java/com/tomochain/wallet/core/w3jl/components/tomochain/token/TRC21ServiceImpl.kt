package com.tomochain.wallet.core.w3jl.components.tomochain.token

import android.annotation.SuppressLint
import com.tomochain.wallet.core.common.Config
import com.tomochain.wallet.core.common.exception.InsufficientBalanceException
import com.tomochain.wallet.core.common.exception.InvalidAddressException
import com.tomochain.wallet.core.common.exception.InvalidPrivateKeyException
import com.tomochain.wallet.core.w3jl.components.coreBlockchain.BlockChainService
import com.tomochain.wallet.core.w3jl.config.chain.Chain
import com.tomochain.wallet.core.w3jl.config.chain.CommonChain
import com.tomochain.wallet.core.w3jl.entity.TransactionResult
import com.tomochain.wallet.core.w3jl.entity.TransactionStatus
import com.tomochain.wallet.core.w3jl.utils.WalletUtil
import com.tomochain.wallet.core.wallet.WalletSecretDataService
import io.reactivex.Observable
import io.reactivex.Single
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Bool
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.utils.Numeric
import java.lang.IllegalStateException
import java.math.BigInteger

/**
 * Created by NienLe on 2019-09-08,September,2019
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class TRC21ServiceImpl(override var address: String?,
                       override val web3j: Web3j?,
                       override val chain: Chain?,
                       private val walletSecretDataService: WalletSecretDataService?,
                       private val coreBlockChainService: BlockChainService?) : TokenServiceImpl(address, web3j, chain), TRC21Service {

    private val tRC21IssuerContractAddress : String by lazy {
        when(chain){
            CommonChain.TOMO_CHAIN -> TrustedContract.TRC21Issuer.MainNet
            CommonChain.TOMO_CHAIN_TEST_NET -> TrustedContract.TRC21Issuer.TestNet
            else -> ""
        }
    }

    @SuppressLint("CheckResult")
    override fun transferToken(tokenAddress: String,
                               recipient: String,
                               amount: BigInteger,
                               gasPrice: BigInteger?,
                               gasLimit: BigInteger?) : Observable<TransactionResult> {
        return Observable.create { emitter ->
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
                            .subscribe({tokenBalance ->
                                if (tokenBalance < amount){
                                    emitter.onError(InsufficientBalanceException())
                                    return@subscribe
                                }

                                //fetch necessary params
                                val ethGetTransactionCount = web3j?.ethGetTransactionCount(
                                    address, DefaultBlockParameterName.LATEST)?.sendAsync()?.get()
                                val nonce = ethGetTransactionCount?.transactionCount

                                val credentials = Credentials.create(pKey.toString())
                                pKey?.clear()
                                val function = Function(
                                    "transfer",
                                    listOf(Address(recipient),Uint256(amount)),
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

                                //check TOMOZ fee
                                isTOMOZApplied(tokenAddress)
                                    .subscribe({isTOMOZApplied ->
                                        if (isTOMOZApplied){
                                            getTokenTransferFee(tokenAddress)
                                                .subscribe({tokenFee->
                                                    if (tokenBalance < (amount + tokenFee)){
                                                        if (tokenBalance < amount){
                                                            emitter.onError(
                                                                InsufficientBalanceException("Your Token Balance is not enough to transfer and pay the fee"))
                                                            return@subscribe
                                                        }
                                                    }
                                                },{
                                                    emitter.onError(it)
                                                })
                                        }else{
                                            val transactionFee = calculatedGasLimit.multiply(gasPrice)
                                            coreBlockChainService?.setWalletAddress(address)
                                            coreBlockChainService
                                                ?.getAccountBalance()
                                                ?.subscribe({availableTOMO->
                                                    if (availableTOMO < transactionFee){
                                                        emitter.onError(
                                                            InsufficientBalanceException("This transaction require $transactionFee wei as fee. Your balance is $availableTOMO wei"))
                                                        return@subscribe
                                                    }
                                                },{
                                                    emitter.onError(it)
                                                })
                                        }
                                    },{
                                        emitter.onError(it)
                                    })
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
                                emitter.onError(it)
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

    override fun getTokenTransferFee(tokenAddress: String): Single<BigInteger> {

        val function = Function(
            "estimateFee",
            listOf(Uint256(0)),
            listOf(object : TypeReference<Uint256>() {

            }))

        return Single.create{ emitter ->
            val responseValue = callSmartContractFunction(function, tokenAddress, address ?: Config.Address.DEFAULT)
            val response = FunctionReturnDecoder.decode(
                responseValue, function.outputParameters)
            if (response.size == 1) {
                emitter.onSuccess((response[0] as Uint256).value)
            } else {
                emitter.onError(NullPointerException())
            }
        }


    }

    override fun isTOMOZApplied(tokenAddress: String): Single<Boolean> {
        val function = Function(
                "getTokenCapacity",
                listOf(Address(tokenAddress)),
                listOf(object : TypeReference<Uint256>() {

                }))
        return Single.create{ emitter ->
            val responseValue =
                    callSmartContractFunction(function, tRC21IssuerContractAddress, address ?: Config.Address.DEFAULT)
            val response = FunctionReturnDecoder.decode(
                    responseValue, function.outputParameters)
            if (response.size == 1) {
                emitter.onSuccess((response[0] as Uint256).value != BigInteger.ZERO)
            } else {
                emitter.onError(NullPointerException())
            }
        }
    }


    override fun isTRC21Token(tokenAddress: String): Single<Boolean> {
        val function = Function(
            "issuer",
            listOf(),
            listOf(object : TypeReference<Address>() {

            }))
        return Single.create{ emitter ->
            try{
                val responseValue =
                    callSmartContractFunction(function, tokenAddress, address ?: Config.Address.DEFAULT)
                val response = FunctionReturnDecoder.decode(
                    responseValue, function.outputParameters)
                if (response.size == 1) {
                    emitter.onSuccess(WalletUtil.isValidAddress((response[0] as Address).value))
                } else {
                    emitter.onSuccess(false)
                }
            }catch(t: Throwable){
                emitter.onSuccess(false)
            }
        }
    }

    override fun getTOMOZContractList(): Single<List<String>> {
        val function = Function(
            "tokens",
            arrayListOf(),
            listOf(object : TypeReference<DynamicArray<Address>>() {

            }))
        return Single.create{ emitter ->
            val responseValue =
                callSmartContractFunction(function, tRC21IssuerContractAddress, address ?: Config.Address.DEFAULT)
            val response = FunctionReturnDecoder.decode(
                responseValue, function.outputParameters)
            if (response.size == 1) {
                val list : MutableList<String> = arrayListOf()

                val r = (response[0] as DynamicArray<Address>).value
                r.forEach { address ->
                    list.add(address.value)
                }
                emitter.onSuccess( list)
            } else {
                emitter.onError(NullPointerException())
            }
        }
    }
}