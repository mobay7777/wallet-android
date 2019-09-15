# tomowallet-android-core


## Table of contents

- [Installation](#installation)
- [Setup](#setup)
- [Manage wallet](#manage_wallet)
- [Sign data](#sign_data)
- [Blockchain methods](#blockchain_methods)
- [Wallet Secret](#wallet_secret)


## <a name=installation>Installation</a>

Include the `core` library to your app:

```kotlin
implementation project(':core')
```

## <a name=setup>Setup</a>

In your `Application` class, init `WalletCore`

```kotlin
WalletCore.setup(WeakReference(this))
```

## <a name=manage_wallet>Manage wallet</a>

The `CoreFunctions` interface include functions to create, fetch and remove wallets. When you perform create or import, the WalletCore will save all data into database (Room), but when you fetch wallets, the sensitive data will be transparent. 

```kotlin
WalletCore.getInstance()
	?.coreFunctions
	?.createWalletFromMnemonics("your-12-words")
	?.subscribe(
  		{
			//on success, return wallet address
		},{
			//on error
		})
		
WalletCore.getInstance()
	?.coreFunctions
	?.getWalletByAddress("your-wallet-address")
	?.subscribe(
  		{
			//on success, return wallet information
		},{
			//on error
		})
```

**Note** 

- Most functions will access the database, therefore, you must call them from thread, using Rx `subscribe` method, or use your own asyncTask.



## <a name=sign_data>Sign data</a>

The `SignerService` is used to sign Transaction, or personal message. To sign, call `signerService` from WalletCore

```kotlin
WalletCore.getInstance("your-wallet-address")
	?.signerService
	?.signPersonalMessage("your-personal-message")
	?.subscribe(
  		{
			//on success, return sign result
		},{
			//on error
		})
		
		
WalletCore.getInstance("your-wallet-address")
	?.signerService
	?.signTransaction("repicient-address",amount = BigInteger("100000"))
	?.subscribe(
  		{
			//on success, return sign result
		},{
			//on error
		})
```

**Note** 

- Before call `signerService`, you must specify which wallet is used by include the wallet address on `getInstance` method. WalletCore will look into database to find corresponding wallet and sign the message using credentials. It will throw `InvalidPrivateKeyException` if the wallet is not found, or wallet does not contain private key.
- `signTransaction` method contains `gasPrice`, `gasLimit` and `payload` params. If you don't specify, the gasPrice and gasLimit will use the default value.
- The `amount` value must be in `wei` unit.


## <a name=blockchain_methods>Blockchain methods</a>

The `BlockChainService` contains most functions you need to obtain account balance and perform transfer.

```
WalletCore.getInstance("your-wallet-address")
	?.coreBlockChainService
	?.getAccountBalance()?.subscribe(
            {
                //balance at wei unit
            },{
                //on error
            })

WalletCore.getInstance("your-wallet-address")
	?.coreBlockChainService
	?.transfer(
            "recipient-address",
            ConvertUtil.toWei("0.0002", ConvertUtil.Unit.ETHER).toBigInteger(),
            callback = object : TransactionListener{
                override fun onTransactionCreated(txId: String) {}
                override fun onTransactionComplete(txId: String, status: String) {}
                override fun onTransactionError(e: Exception) {}
            }
        )
```
**Note** 

- The function will calculate the amount of gas needed to transfer if `gasLimit` is not specified.
- `onTransactionComplete` is triggered when the transaction is done, with `0x1` as success, otherwise is fail.
- The `onTransactionError` is called when the transaction failed to create (not pushed to chain yet).

## <a name=tokens>Tokens</a>

WalletCore currently support `TRC20` and `TRC21` standard. To use the Token service, you can call the `Trc20TokenService`, `Trc21TokenService`, or simply call `TokenManager` without any doubt of knowing which type of current token.

```kotlin
interface TokenService : BaseService {
    fun getBalance(tokenAddress: String?) : Single<BigInteger>
    fun getBalance(tokenInfo: TokenInfo?) : Single<BigInteger>
    fun getName(tokenAddress: String?) : Single<String>
    fun getSymbol(tokenAddress: String?) : Single<String>
    fun getDecimal(tokenAddress: String?) : Single<Int>
    fun getTotalSupply(tokenAddress: String?) : Single<BigInteger>
    fun getTokenInfo(tokenAddress: String?) : Single<TokenInfo?>
}

interface TRC20Service : TokenService{
    fun transferToken(
            tokenAddress: String,
            recipient: String,
            amount: BigInteger,
            callback: TransactionListener?,
            gasPrice: BigInteger? =
            BigInteger(Config.Transaction.DEFAULT_GAS_PRICE),
            gasLimit: BigInteger? = null
    )
    fun estimateTokenTransferGasLimit(
    	tokenAddress: String,
    	recipient: String,
    	amount: BigInteger): Single<BigInteger>
}

interface TRC21Service : TokenService {
    fun transferToken(
            tokenAddress: String,
            recipient: String,
            amount: BigInteger,
            callback: TransactionListener?,
            gasPrice: BigInteger? = 
            BigInteger(Config.Transaction.DEFAULT_GAS_PRICE),
            gasLimit: BigInteger? = null
    )

    fun isTRC21Token(tokenAddress: String) : Single<Boolean>
    fun getTokenTransferFee(tokenAddress: String): Single<BigInteger>
    fun isTOMOZApplied(tokenAddress: String) : Single<Boolean>
    fun getTOMOZContractList() : Single<List<String>>
}

```

**Note** 

- The `amount` parameter of `transferToken` function must be in the smallest unit of token. For example, if you want to transfer 100 TRC20 Token with `decimal`of 12, then the function should look like (use `BigDecimal` for amount with decimal value):

```kotlin
WalletCore.getInstance("your-wallet-address")
	?.trc20TokenService
	?.transferToken(
		"token-contract-address",
		"recipient-address",
		BigInteger("100")
			.multiply(BigInteger.TEN.pow(12)), 
		callback = object : TransactionListener{})
```
- TRC21 transferToken method will check if the token is already applied to TomoZ before making transaction
- TRC21 `isTRC21Token`method will call `issuer` function from contract, so any contract with this function provided will be recognized as TRC21 contract.
- TRC21 `getTokenTransferFee` will call coressponding function from contract, and return `wei` value of TOMO or token unit due to each contract.

Beside individual token service, WalletCore also provide `TokenManagerService` to manage token easier. This service will auto recognize the Token information, and choose the right method to execute. This utility only require contract address when called.

```kotlin
WalletCore.getInstance("your-wallet-address")
	?.tokenManager
	?.withTokenAddress("contract-address")
	?.getTokenFormattedBalance()
	?.subscribe({
		//eg: 10.25
	},{
		
	})
``` 

## <a name=wallet_secret>Wallet Secret</a>

The only way to get sensitive information of wallet (recovery phrase, private key) is through `WalletSecretDataService`. This service obtain wallet address and return sensitive information if existed.

```kotlin
WalletCore.getInstance()
	?.walletSecretDataService
	?.getPrivateKey(""your-wallet-address"")
	?.subscribe({	},{})
```

**Note** 

- These methods always return a `StringBuilder` which contain **plain** sensitve information. You should call it only when you need, and clear the stringBuilder when you don't need it anymore.

