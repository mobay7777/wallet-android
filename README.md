# tomowallet-android-core

## Table of contents

- [Installation](#installation)
- [Setup](#setup)
- [Core Functions](#core_function)
- [Wallet Functions](#wallet_functions)
- [Tokens](#tokens)
- [Wallet Secret](#wallet_secret)


## <a name=installation>Installation</a>

[![](https://jitpack.io/v/tomochain/tomowallet-core-android.svg)](https://jitpack.io/#tomochain/tomowallet-core-android)

include Jitpack to your root project `build.gradle`

```kotlin
allprojects{
    repositories{
        ...
	maven { url 'https://jitpack.io' }
    }
}
```

in your app `build.gradle`
```kotlin
dependencies{
    ...
    implementation 'com.github.tomochain:tomowallet-core-android:0.0.1'
}
```

## <a name=setup>Setup</a>

In your `Application` class, init `WalletCore`

```kotlin
WalletCore.setup(WeakReference(this))
```
This method use [DefaultConfig](core/src/main/java/com/tomochain/wallet/core/components/DefaultConfig.kt) to setup the environment. You can modify the parameter by extend `CoreConfig` object:

```kotlin
abstract class CoreConfig(
    open var chain: Chain? = CommonChain.TOMO_CHAIN,
    open var cryptoAlias: String? = "cryptoAlias",
    open var roomAlias: String? = "roomAlias",
    open var cryptographyManager: Habak? = null
)
```

- The `chain` param point to the blockchain network, you can select from the `CommonChain` or create your own chain call.
- WalletCore use Hardware-backed keystore as cryptography mechanism, and use `cryptoAlias` as the keystore alias. Then if you do not implement your own `cryptographyManager`, WalletCore will use default `Habak` (see [HabakFactory](core/src/main/java/com/tomochain/wallet/core/habak/HabakFactory.kt) for more detail) with default `cryptoAlias`. For more secure, you can use your own `cryptoAlias` and make sure the alias is remain the same when called.
- WalletCore also encrypt the Room database file using [Cwac-SafeRoom](https://github.com/commonsguy/cwac-saferoom) library. You can use the default value, or import your own key. Note that SafeRoom will not be used if you pass the empty string as `roomAlias`
- You can even implement your own `cryptographyManager` interface.


## <a name=core_function>Core Functions</a>

The [CoreFunctions](core/src/main/java/com/tomochain/wallet/core/components/CoreFunctions.kt) interface include functions to create, fetch and remove wallets. When you perform create or import, the WalletCore will save all data into database (Room), but when you fetch wallets, the sensitive data will be transparent. 

```kotlin
WalletCore.getCoreFunctions()
	?.createWallet()
	?.subscribeOn(Schedulers.io())
	?.observeOn(AndroidSchedulers.mainThread())
	?.subscribe({
		//success, return wallet address
	},{
		//error, return exception
	})
	
WalletCore.getCoreFunctions()
	?.createWalletFromMnemonics("your-12-words")
	?.subscribeOn(Schedulers.io())
	?.observeOn(AndroidSchedulers.mainThread())
	?.subscribe({
		//success, return wallet address
	},{
		//error, return exception
	})
		
WalletCore.getCoreFunctions()
	?.getWalletByAddress("your-wallet-address")
	?.subscribeOn(Schedulers.io())
	?.observeOn(AndroidSchedulers.mainThread())
	?.subscribe({
		//success, return wallet information
	},{
		//error, return exception
	})
```

**Note** 

- Most functions will access the database, therefore, you must call them from thread, using Rx `subscribe` method, or use your own asyncTask.



## <a name=wallet_functions>Wallet Functions</a>

The [WalletFunctions](core/src/main/java/com/tomochain/wallet/core/components/WalletFunctions.kt) covers most functions that the single wallet need.

```kotlin
WalletCore.getWalletFunctions("your-wallet-address")
	?.subscribeOn(Schedulers.io())
	?.observeOn(AndroidSchedulers.mainThread())
	?.subscribe({
		val formattedBalance = ConvertUtil.fromWei(it.toBigDecimal(), ConvertUtil.Unit.ETHER)
	},{
		//error, return exception
	})
		
		
WalletCore.getWalletFunctions("your-wallet-address")
	?.signTransaction(
		"0x6e7312d1028b70771bb9cdd9837442230a9349ca",
		BigInteger.ONE)
	?.subscribeOn(Schedulers.io())
	?.observeOn(AndroidSchedulers.mainThread())
	?.subscribe({
		//return encoded-signed transaction
	},{
		//error, return exception
	})
	
WalletCore.getWalletFunctions("your-wallet-address")
	?.transfer(
		"0x6e7312d1028b70771bb9cdd9837442230a9349ca",
		BigInteger.ONE)
	?.subscribeOn(Schedulers.io())
	?.observeOn(AndroidSchedulers.mainThread())
	?.subscribe({
		//emit the updated transaction status
	},{
		//error, return exception
	})
```

**Note** 

- Before call `WalletFunctions`, you must specify which wallet is used by include the wallet address on `getWalletFunctions` method. WalletCore will look into database to find corresponding wallet and sign the message using credentials. It will throw `InvalidPrivateKeyException` if the wallet is not found, or wallet does not contain private key.
- `signTransaction` and `transfer` method contains `gasPrice`, `gasLimit` and `payload` params. If you don't specify, the gasPrice and gasLimit will use the default value.
- The `amount` value must be in `wei` unit.


## <a name=tokens>Tokens</a>

WalletCore currently support `TRC20` and `TRC21` standard. To use the Token service, you can call the `TokenManager` without any doubt of knowing which type of current token.

```kotlin
WalletCore.getTokenManager("your-wallet-address")
	?.withTokenAddress("token-contract-address")
	?.getTokenBalance()
	?.subscribeOn(Schedulers.io())
	?.observeOn(AndroidSchedulers.mainThread())
	?.subscribe({
		//return token balancce
	},{
		//error, return exception
	})
	
	
WalletCore.getTokenManager("your-wallet-address")
	?.withTokenAddress("token-contract-address")
	?.transferFormattedToken(
		"0x6e7312d1028b70771bb9cdd9837442230a9349ca",
		BigDecimal("0.1")
	)
	?.subscribeOn(Schedulers.io())
	?.observeOn(AndroidSchedulers.mainThread())
	?.subscribe({
		//emit the updated transaction status
	},{
		//error, return exception
	})
```

**Note** 

- The `amount` parameter of `transferToken` function must be in the smallest unit of token. For example, if you want to transfer 100 TRC20 Token with `decimal`of 12, then the function should look like (use `BigDecimal` for amount with decimal value):

```kotlin
WalletCore.getTokenManager("your-wallet-address")
	?.withTokenAddress("token-contract-address")
	?.subscribeOn(Schedulers.io())
	?.observeOn(AndroidSchedulers.mainThread())
	?.transferToken(
		"recipient-address",
		BigInteger("100")
			.multiply(BigInteger.TEN.pow(12)))
```
- TRC21 transferToken method will check if the token is already applied to TomoZ before making transaction
- TRC21 `isTRC21Token`method will call `issuer` function from contract, so any contract with this function provided will be recognized as TRC21 contract.
- TRC21 `getTokenTransferFee` will call coressponding function from contract, and return `wei` value of TOMO or token unit due to each contract.

You can either use `TokenManager` or choose the specific Token-type-service (TRC20 or TRC21) service.

```kotlin
WalletCore.getInstance("your-wallet-address")
	?.tokenManager
	?.getTRC21Services()
	?.getTokenTransferFee("token-contract-address")
	?.subscribeOn(Schedulers.io())
	?.observeOn(AndroidSchedulers.mainThread())
	?.subscribe({
		//eg: 10.25
	},{
		
	})
``` 

## <a name=wallet_secret>Wallet Secret</a>

The only way to get sensitive information of wallet (recovery phrase, private key) is through `WalletSecretDataService`. This service obtain wallet address and return sensitive information if existed.

```kotlin
WalletCore.getWalletSecretData("your-wallet-address")
	?.walletSecretDataService
	?.subscribeOn(Schedulers.io())
	?.observeOn(AndroidSchedulers.mainThread())
	?.getPrivateKey()
	?.subscribe({	},{})
```

**Note** 

- These methods always return a `StringBuilder` which contain **plain** sensitve information. You should call it only when you need, and clear the stringBuilder when you don't need it anymore.



## LICENSE

MIT
