# Workshop #4: Ethereum Sidechain Part 1

## Requirements

To go through this, you will need the following:

1. You are using a Mac/Linux
2. You have installed docker and docker-compose
3. You have checked out https://github.com/cyber-republic/elastos-privnet/tree/v0.5 and are on tag v0.5
4. Catch up on First Developer Workshop on how to run an elastos private net on your local machine if you haven't. Visit [Elastos Developer Workshop #1: Running Private Net](https://www.youtube.com/watch?v=0Mn9pz2UORo) for more info
5. You have installed NodeJS, NPM, Truffle Box and Solidity compiler
  ```
  sudo apt-get install nodejs npm;
  npm install -g truffle solc;
  ```

## Resources
- Eth Sidechain Repo: [https://github.com/elastos/Elastos.ELA.SideChain.ETH](https://github.com/elastos/Elastos.ELA.SideChain.ETH)
- Eth Sidechain Browser Repo: [https://github.com/elastos/Elastos.ORG.Browser.ETH](https://github.com/elastos/Elastos.ORG.Browser.ETH)
- Eth RPC Methods: [https://github.com/ethereum/wiki/wiki/JSON-RPC#json-rpc-methods](https://github.com/ethereum/wiki/wiki/JSON-RPC#json-rpc-methods)
- Management APIs: [https://github.com/elastos/Elastos.ELA.SideChain.ETH/wiki/Management-APIs](https://github.com/elastos/Elastos.ELA.SideChain.ETH/wiki/Management-APIs)
- Managing Accounts: [https://github.com/elastos/Elastos.ELA.SideChain.ETH/wiki/Managing-your-accounts](https://github.com/elastos/Elastos.ELA.SideChain.ETH/wiki/Managing-your-accounts)
- What is Ethereum Sidechain: [https://developer.elastos.org/discover_elastos/core_modules/ethereum_sidechain/](https://developer.elastos.org/discover_elastos/core_modules/ethereum_sidechain/)
- Ethereum Sidechain RPC Reference: [https://developer.elastos.org/elastos_blockchain/reference/rpc/ethereum_sidechain/](https://developer.elastos.org/elastos_blockchain/reference/rpc/ethereum_sidechain/)
- Ethereum Sidechain Smart Contract Guide: [https://developer.elastos.org/elastos_core_services/guides/ethereum_smart_contracts/](https://developer.elastos.org/elastos_core_services/guides/ethereum_smart_contracts/)
- Eth Sidechain Testnet Wallet: [https://wallet.elaeth.io](https://wallet.elaeth.io)
- Eth Sidechain Testnet Faucet: [https://faucet.elaeth.io/](https://faucet.elaeth.io/)
- Eth Sidechain Testnet Explorer: [https://explorer.elaeth.io/](https://explorer.elaeth.io/)
- Eth Task Force Github Repo: [https://github.com/elaeth](https://github.com/elaeth)
- Truffle Box: [https://truffle-box.github.io](https://truffle-box.github.io)

## Contents

0. Set up your Private Net
1. Intro to Ethereum Sidechain
2. Transfer ELA from main chain to ETH Sidechain
3. Interact with Ethereum Sidechain using RPC methods
4. Interacting with Management APIs
5. Managing Accounts: Create accounts, import wallets, list accounts and check balances
6. Connect to Ethereum Sidechain testnet
7. Deploy a simple Ethereum Smart Contract

## Set up your Private Net

1. Just run with docker-compose from the directory where elastos-privnet is located(github.com/cyber-republic/elastos-privnet/blockchain):

   ```
   cd $GOPATH/src/github.com/cyber-republic/elastos-privnet/blockchain;
   git checkout v0.5;
   tools/copy_freshdata_docker.sh;
   docker-compose up --remove-orphans --build --force-recreate -d
   ```

2. Verify the Mainchain is running by checking the miner reward wallet:

   ```
   curl http://localhost:10012/api/v1/asset/balances/EQ4QhsYRwuBbNBXc8BPW972xA9ANByKt6U
   ```

   You should see at least 1000 ELA in the miner wallet:

   ```
   {"Desc":"Success","Error":0,"Result":"1005.60664465"}
   ```

3. Verify the ETH sidechain is running by checking the preloaded wallet:

  ```
  curl -H 'Content-Type: application/json' -H 'Accept:application/json' --data '{"jsonrpc":"2.0","method":"eth_getBalance","params":["0x4505b967d56f84647eb3a40f7c365f7d87a88bc3", "latest"],"id":1}' localhost:60111
  ```
  
  Should return something like:
  ```
  {
    "jsonrpc": "2.0",
    "id": 1,
    "result": "0x152cf383e51ef1920000"
  }
  ```
  0x152cf383e51ef1920000 is 99998900000000000000000 in decimal format which is the unit in wei. This equals to 99998.9 ETH ELA

## Intro to Ethereum Sidechain
- [Ethereum Sidechain on Elastos Developer Portal](https://developer.elastos.org/discover_elastos/core_modules/ethereum_sidechain/)

## Transfer ELA from main chain to ETH Sidechain

1. Change directory
  ```
  cd $GOPATH/src/github.com/cyber-republic/elastos-privnet/blockchain/ela-mainchain
  ```

2. Configure ela-cli config file

    Create a file called "cli-config.json" and put the following content in that file:

    ```
    {
      "Host": "127.0.0.1:10014",
      "DepositAddress":"XZyAtNipJ7fdgBRhdzCoyS7A3PDSzR7u98"
    }
3. Create a new wallet using ela-cli-crosschain client for testing purposes

    ```
    ./ela-cli-crosschain wallet --create -p elastos
    ```

    Save ELA address, Public key and Private key to a variable so it can be used later
    ```bash
    ELAADDRESS=$(./ela-cli-crosschain wallet -a -p elastos | tail -2 | head -1 | cut -d' ' -f1)
    PUBLICKEY=$(./ela-cli-crosschain wallet -a -p elastos | tail -2 | head -1 | cut -d' ' -f2)
    PRIVATEKEY=$(./ela-cli-crosschain wallet --export -p elastos | tail -2 | head -1 | cut -d' ' -f2)
    # Make sure your info is correct
    echo $ELAADDRESS $PUBLICKEY $PRIVATEKEY
    ```

4. Transfer ELA from the resources wallet to this newly created wallet

    ```
    curl -X POST -H "Content-Type: application/json" -d '{"sender": [{"address": "EUSa4vK5BkKXpGE3NoiUt695Z9dWVJ495s","privateKey": "109a5fb2b7c7abd0f2fa90b0a295e27de7104e768ab0294a47a1dd25da1f68a8"}],"receiver": [{"address": '"$ELAADDRESS"',"amount": "100000"}]}' localhost:8091/api/1/transfer
    ```

    Check whether the ELA got transferred successfully

    ```
    ./ela-cli-crosschain wallet -l
    ```
5. Transfer ELA from main chain to eth sidechain

    ```
    ./ela-cli-crosschain wallet -t create --from $ELAADDRESS --deposit 0x4505b967d56f84647eb3a40f7c365f7d87a88bc3 --amount 99999 --fee 0.1;
    ./ela-cli-crosschain wallet -t sign -p elastos --file to_be_signed.txn;
    ./ela-cli-crosschain wallet -t send --file ready_to_send.txn;
    ```
6. Check eth balance:

  ```
  curl -H 'Content-Type: application/json' -H 'Accept:application/json' --data '{"jsonrpc":"2.0","method":"eth_getBalance","params":["0x4505b967d56f84647eb3a40f7c365f7d87a88bc3", "latest"],"id":1}' localhost:60111
  ```

  Should return something like:
  ```
  {
    "jsonrpc": "2.0",
    "id": 1,
    "result": "0x2a59e707ca3de3240000"
  }
  ```
  0x2a59e707ca3de3240000 is 199997800000000000000000 in decimal format which is the unit in wei. This equals to 199997.8 ETH ELA

7. Clean up your environment:
  
  ```
  cd $GOPATH/src/github.com/cyber-republic/elastos-privnet/blockchain/ela-mainchain;
  rm -f cli-config.json keystore.dat ready_to_send.txn to_be_signed.txn wallet.db;
  cd $GOPATH/src/github.com/cyber-republic/developer-workshop/2019-09-04;
  ```

## Interact with Ethereum Sidechain using RPC methods
1. **eth_gasPrice**: Returns the current price per gas in wei
  ```
  curl -X POST -H 'Content-Type: application/json' -H 'Accept:application/json' --data '{"jsonrpc":"2.0","method":"eth_gasPrice","params":[],"id":73}' http://localhost:60111
  ```
  Should return 
  ```
  {
    "jsonrpc": "2.0",
    "id": 73,
    "result": "0x77359400"
  }
  ```
  0x77359400 is 2000000000 in decimal format which is the unit in Wei. This equals to 0.000000002 ETH ELA
2. **eth_getBalance**: Returns the ETH ELA balance for the given address
   ```
   curl -X POST -H 'Content-Type: application/json' -H 'Accept:application/json' --data '{"jsonrpc":"2.0","method":"eth_getBalance","params":["0x4505b967d56f84647eb3a40f7c365f7d87a88bc3", "latest"],"id":1}' localhost:60111
   ```
  Should return:
  ```
  {
    "jsonrpc": "2.0",
    "id": 1,
    "result": "0x2a59e707ca3de3240000"
  }
  ```
  0x2a59e707ca3de3240000 is 199997800000000000000000 in decimal format which is the unit in wei. This equals to 199997.8 ETH ELA
3. **eth_getBlockByHash**: Returns information about a block from its hash
  ```
  curl -X POST -H 'Content-Type: application/json' -H 'Accept:application/json' --data '{"jsonrpc":"2.0","method":"eth_getBlockByHash","params":["0xb0cd29490c792dbcbe75adadee415270b9e5c8ae89dfed835440f2ac606eebfc", true],"id":1}' http://localhost:60111
  ```
  Should return
  ```
  {
    "jsonrpc": "2.0",
    "id": 1,
    "result": {
      "difficulty": "0x1",
      "extraData": "0x0000000000000000000000000000000000000000000000000000000000000000961386e437294f9171040e2d56d4522c4f55187d0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
      "gasLimit": "0x2068f7700",
      "gasUsed": "0x0",
      "hash": "0xb0cd29490c792dbcbe75adadee415270b9e5c8ae89dfed835440f2ac606eebfc",
      "logsBloom": "0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
      "miner": "0x0000000000000000000000000000000000000000",
      "mixHash": "0x0000000000000000000000000000000000000000000000000000000000000000",
      "nonce": "0x0000000000000000",
      "number": "0x0",
      "parentHash": "0x0000000000000000000000000000000000000000000000000000000000000000",
      "receiptsRoot": "0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421",
      "sha3Uncles": "0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347",
      "size": "0x274",
      "stateRoot": "0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421",
      "timestamp": "0x5bda9da6",
      "totalDifficulty": "0x1",
      "transactions": [],
      "transactionsRoot": "0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421",
      "uncles": []
    }
  }
  ```

  Note: The block hash we used is that of privnet Ethereum sidechain genesis block
4. For interacting with more RPC methods, visit [https://github.com/ethereum/wiki/wiki/JSON-RPC#json-rpc-methods](https://github.com/ethereum/wiki/wiki/JSON-RPC#json-rpc-methods)

## Interacting with Management APIs
1. These management APIs are also provided using RPC methods and follow exactly the same conventions. The purpose of this section is only to briefly introduce what these management APIs are.
2. You have access to the management APIs when you run `geth console --testnet`. For more info on what the management APIs are, please refer to [https://github.com/elastos/Elastos.ELA.SideChain.ETH/wiki/Management-APIs](https://github.com/elastos/Elastos.ELA.SideChain.ETH/wiki/Management-APIs)

## Managing Accounts: Create accounts, import wallets, list accounts and check balances
NOTE: Currently, there is no way to run a second node and connect that node to your locally running privnet Ethereum sidechain network. The following just utilizes geth program to create accounts, list accounts, etc connected to the testnet.  
1. Create a new account
  ```
  cd bin;
  ./geth --testnet --datadir elastos_eth account new
  ```
  Should return something like 
  ```
  INFO [08-28|15:59:43.314] Maximum peer count                       ETH=25 LES=0 total=25
  Your new account is locked with a password. Please give a password. Do not forget this password.
  Passphrase: 
  Repeat passphrase: 
  lAddress: {285f996244aa936e1c54bcf77d5e253790614af5}
  ```
2. Check the newly created account
  ```
  ./geth --testnet --datadir elastos_eth account list;
  ```
  Should return
  ```
  INFO [09-03|08:51:04.632] Maximum peer count                       ETH=25 LES=0 total=25
  Account #0: {285f996244aa936e1c54bcf77d5e253790614af5} keystore:///home/kpachhai/dev/src/github.com/cyber-republic/developer-workshop/2019-09-04/bin/elastos_eth/keystore/UTC--2019-09-03T12-50-16.360697479Z--285f996244aa936e1c54bcf77d5e253790614af5
  ```
  You can check the keystore file at the above location:
  ```
  cat elastos_eth/keystore/UTC--2019-09-03T12-50-16.360697479Z--285f996244aa936e1c54bcf77d5e253790614af5
  ```
  Should return:
  ```
  {
    "address": "285f996244aa936e1c54bcf77d5e253790614af5",
    "crypto": {
      "cipher": "aes-128-ctr",
      "ciphertext": "05e96f05af56e9e6d1ad6a22deca514ad72f4b5ef41f8dfce8a8e9fbbf4097bc",
      "cipherparams": {
        "iv": "62a6c36ee0a3746f46d6d67dd2fda7b7"
      },
      "kdf": "scrypt",
      "kdfparams": {
        "dklen": 32,
        "n": 262144,
        "p": 1,
        "r": 8,
        "salt": "e419edef08127c39122ecca7d996b86389acc672832d2721ede9c9bf410ae927"
      },
      "mac": "e7c9d7152a51070bfce02b92558996bef6eb3d3d56c1878e39f8ccc533c3c3ce"
    },
    "id": "a89aea30-1c6f-4c98-9136-34e1324aa369",
    "version": 3
  }
  ```
3. Update your account
  ```
  ./geth --testnet --datadir elastos_eth account update 285f996244aa936e1c54bcf77d5e253790614af5
  ```
  Should return
  ```
  Unlocking account 285f996244aa936e1c54bcf77d5e253790614af5 | Attempt 1/3
  Passphrase: 
  INFO [08-28|16:16:28.857] Unlocked account                         address=285f996244aa936e1c54bcf77d5e253790614af5
  Please give a new password. Do not forget this password.
  Passphrase: 
  Repeat passphrase: 
  ```

## Connect to Ethereum Sidechain testnet
1. Let's run a local Ethereum Sidechain node that's connected to the testnet
  ```
  ./geth --testnet --datadir elastos_eth --ethash.dagdir elastos_ethash --rpc --rpcaddr 0.0.0.0 --rpccorsdomain '*' --rpcport 21636 --rpcapi 'personal,db,eth,net,web3,txpool,miner'
  ```
  Note: It may take a while for the blocks to sync to latest height

2. Request some test Eth ELA
  We're going to use the same Eth ELA address we created earlier for this: 0x285f996244aa936e1c54bcf77d5e253790614af5. Or, you can also create a new Eth ELA wallet for testnet at [https://wallet.elaeth.io](https://wallet.elaeth.io). As this website is in testing phase, it may not work as expected so for this developer workshop, we recommend creating the wallet using the above method instead of the website.
  Let's check its current balance:
  ```
  curl -X POST -H 'Content-Type: application/json' -H 'Accept:application/json' --data '{"jsonrpc":"2.0","method":"eth_getBalance","params":["0x285f996244aa936e1c54bcf77d5e253790614af5", "latest"],"id":1}' localhost:21636
  ```

  Should return
  ```
  {
    "jsonrpc": "2.0",
    "id": 1,
    "result": "0x0"
  }
  ```

  Now, we're going to request some test ETH ELA. Visit [https://faucet.elaeth.io/](https://faucet.elaeth.io/) and put in your test Eth ELA address and request some test Eth ELA to your newly created address.
  After a while, check to make sure your address has some Eth ELA in it.
  ```
  curl -X POST -H 'Content-Type: application/json' -H 'Accept:application/json' --data '{"jsonrpc":"2.0","method":"eth_getBalance","params":["0x285f996244aa936e1c54bcf77d5e253790614af5", "latest"],"id":1}' localhost:21636
  ```

  Should return
  ```
  {
    "jsonrpc": "2.0",
    "id": 1,
    "result": "0x16345785d8a0000"
  }
  ```
  0x16345785d8a0000 is 100000000000000000 in decimal format which is the unit in wei. This equals to 0.1 ETH ELA.
  Note: You can also check the balance for your Eth ELA address for testnet by visiting [https://explorer.elaeth.io/](https://explorer.elaeth.io/)

## Deploy a simple Ethereum Smart Contract
1. Install Truffle Box
  We're going to be using Truffle Box that will help us in deploying our smart contract to our private ethereum sidechain and testnet ethereum 
  sidechain.
  ```
  cd $GOPATH/src/github.com/cyber-republic/developer-workshop/2019-09-04/smart_contracts;
  npm install -g truffle;
  ```

2. Install Solidity Compiler
  ```
  npm install -g solc;
  ```

3. Compile Eth Smart Contracts
  ```
  truffle compile;
  ```
  Should return something like:
  ```
  Compiling your contracts...
  ===========================
  > Compiling ./contracts/HelloWorld.sol
  > Compiling ./contracts/Migrations.sol
  > Compiling ./contracts/StoreNumber.sol
  > Artifacts written to /home/kpachhai/dev/src/github.com/cyber-republic/developer-workshop/2019-09-04/smart_contracts/build/contracts
  > Compiled successfully using:
    - solc: 0.5.1+commit.c8a2cb62.Emscripten.clang
  ```

4. Unlock our account 
  Stop your currently running geth process and rerun with the following command. Note that a new flag "console" has been added
  ```
  ./geth --testnet --datadir elastos_eth --ethash.dagdir elastos_ethash --rpc --rpcaddr 0.0.0.0 --rpccorsdomain '*' --rpcport 21636 --rpcapi 'personal,db,eth,net,web3,txpool,miner' console
  ```

  Then, unlock your account that we created in testnet. I'm going to unlock 0x285f996244aa936e1c54bcf77d5e253790614af5
  ```
  > personal.unlockAccount(web3.eth.coinbase)
  ```
  Should return
  ```
  Unlock account 0x285f996244aa936e1c54bcf77d5e253790614af5
  Passphrase: 
  true
  ```

5. Migrate Eth Smart Contracts
  ```
  truffle migrate --network develop;
  ```
  Should return something like
  ```
  Compiling your contracts...
  ===========================
  > Everything is up to date, there is nothing to compile.


  Migrations dry-run (simulation)
  ===============================
  > Network name:    'develop-fork'
  > Network id:      3
  > Block gas limit: 0x7a1200


  1_initial_migration.js
  ======================

    Deploying 'Migrations'
    ----------------------
    > block number:        75741
    > block timestamp:     1567521542
    > account:             0x285f996244AA936E1c54Bcf77d5e253790614Af5
    > balance:             0.10900768
    > gas used:            268300
    > gas price:           2 gwei
    > value sent:          0 ETH
    > total cost:          0.0005366 ETH

    -------------------------------------
    > Total cost:           0.0005366 ETH


  2_deploy_HelloWorld.js
  ======================

    Deploying 'HelloWorld'
    ----------------------
    > block number:        75743
    > block timestamp:     1567521542
    > account:             0x285f996244AA936E1c54Bcf77d5e253790614Af5
    > balance:             0.108485426
    > gas used:            234099
    > gas price:           2 gwei
    > value sent:          0 ETH
    > total cost:          0.000468198 ETH

    -------------------------------------
    > Total cost:         0.000468198 ETH


  3_deploy_StoreNumber.js
  =======================

    Deploying 'StoreNumber'
    -----------------------
    > block number:        75745
    > block timestamp:     1567521543
    > account:             0x285f996244AA936E1c54Bcf77d5e253790614Af5
    > balance:             0.108203052
    > gas used:            114159
    > gas price:           2 gwei
    > value sent:          0 ETH
    > total cost:          0.000228318 ETH

    -------------------------------------
    > Total cost:         0.000228318 ETH


  Summary
  =======
  > Total deployments:   3
  > Final cost:          0.001233116 ETH


  Starting migrations...
  ======================
  > Network name:    'develop'
  > Network id:      3
  > Block gas limit: 0x7a1200


  1_initial_migration.js
  ======================

    Deploying 'Migrations'
    ----------------------
    > transaction hash:    0x9428fca370b41ff5d6468057753675f2a14aa1aed6671194fba38a50cf559b59
    > Blocks: 0            Seconds: 12
    > contract address:    0x44bd7606C53a53088fDeDE4Bab294d3eD9AcB43d
    > block number:        75741
    > block timestamp:     1567521557
    > account:             0x285f996244AA936E1c54Bcf77d5e253790614Af5
    > balance:             0.10387828
    > gas used:            283300
    > gas price:           20 gwei
    > value sent:          0 ETH
    > total cost:          0.005666 ETH


    > Saving migration to chain.
    > Saving artifacts
    -------------------------------------
    > Total cost:            0.005666 ETH


  2_deploy_HelloWorld.js
  ======================

    Deploying 'HelloWorld'
    ----------------------
    > transaction hash:    0x9f51eef8f8d635b1bb1847122ff9f60bb035a1a9e603f16dcd497ba5ea725550
    > Blocks: 0            Seconds: 12
    > contract address:    0x0CF5E37FB86A19E14Ddf305a6B65754dB8dB2F22
    > block number:        75743
    > block timestamp:     1567521587
    > account:             0x285f996244AA936E1c54Bcf77d5e253790614Af5
    > balance:             0.09835574
    > gas used:            234099
    > gas price:           20 gwei
    > value sent:          0 ETH
    > total cost:          0.00468198 ETH


    > Saving migration to chain.
    > Saving artifacts
    -------------------------------------
    > Total cost:          0.00468198 ETH


  3_deploy_StoreNumber.js
  =======================

    Deploying 'StoreNumber'
    -----------------------
    > transaction hash:    0xfe7f7fede1aba3e42f9b5d878f5e7a7f63ed94280f745d397bdf28d243e704c4
    > Blocks: 0            Seconds: 12
    > contract address:    0x77715e313730a64Ff6a7C8430a4dBe1C90c6463e
    > block number:        75745
    > block timestamp:     1567521617
    > account:             0x285f996244AA936E1c54Bcf77d5e253790614Af5
    > balance:             0.095532
    > gas used:            114159
    > gas price:           20 gwei
    > value sent:          0 ETH
    > total cost:          0.00228318 ETH


    > Saving migration to chain.
    > Saving artifacts
    -------------------------------------
    > Total cost:          0.00228318 ETH


  Summary
  =======
  > Total deployments:   3
  > Final cost:          0.01263116 ETH
  ```

6. Enter the truffle console
  ```
  truffle console --network develop;
  ```

7. Execute HelloWorld.sol smart contract on testnet Ethereum sidechain
  First, let's see what message is currently set 
  ```
  truffle(develop)>var first_contract
  truffle(develop)>HelloWorld.deployed().then(function(instance) { first_contract = instance; })
  truffle(develop)> first_contract.message.call()
  ```
  Should return
  ```
  ''
  ```
  This is because the function Hello() hasn't been called yet.

  Let's now call the function to set its value
  ```
  truffle(develop)> first_contract.Hello()
  truffle(develop)> first_contract.message.call()
  ```
  Should return
  ```
  'Hello World!'
  ```
  Success!

8. Execute StoreNumber.sol smart contract on testnet Ethereum sidechain
  First, let's see what number is currently stored
  ```
  truffle(develop)>var second_contract
  truffle(develop)>StoreNumber.deployed().then(function(instance) { second_contract = instance; })
  truffle(develop)> second_contract.get()
  ```
  Should return
  ```
  BN {
    negative: 0,
    words: [ 0, <1 empty item> ],
    length: 1,
    red: null }
  ```

  Let's now change the value of this item
  ```
  truffle(develop)> second_contract.set(5)
  truffle(develop)> second_contract.get()
  ```
  Now, this should return
  ```
  BN {
    negative: 0,
    words: [ 5, <1 empty item> ],
    length: 1,
    red: null }
  ```
  As you can see, we successfully set the value from 0 to 5
