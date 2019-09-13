# tomowallet-android-core


## Table of contents

- [Installation](#installation)
- [Setup](#setup)
- [Manage wallet](#manage_wallet)
- [Sign data](#sign_data)
- [Blockchain methods](#blockchain_methods)


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