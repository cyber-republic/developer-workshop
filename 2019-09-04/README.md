# Workshop #4: Ethereum Sidechain Part 1

## Requirements

To go through this, you will need the following:

1. You are using a Mac/Linux
2. You have installed docker and docker-compose
3. You have checked out https://github.com/cyber-republic/elastos-privnet/tree/v0.5 and are on tag v0.5
7. Catch up on First Developer Workshop on how to run an elastos private net on your local machine if you haven't. Visit [Elastos Developer Workshop #1: Running Private Net](https://www.youtube.com/watch?v=0Mn9pz2UORo) for more info

## Resources
- Eth Sidechain Repo: [https://github.com/elastos/Elastos.ELA.SideChain.ETH](https://github.com/elastos/Elastos.ELA.SideChain.ETH)
- Eth Sidechain Browser Repo: [https://github.com/elastos/Elastos.ORG.Browser.ETH](https://github.com/elastos/Elastos.ORG.Browser.ETH)
- Eth RPC Methods: [https://github.com/ethereum/wiki/wiki/JSON-RPC#json-rpc-methods](https://github.com/ethereum/wiki/wiki/JSON-RPC#json-rpc-methods)
- Management APIs: [https://github.com/elastos/Elastos.ELA.SideChain.ETH/wiki/Management-APIs](https://github.com/elastos/Elastos.ELA.SideChain.ETH/wiki/Management-APIs)
- Managing Accounts: [https://github.com/elastos/Elastos.ELA.SideChain.ETH/wiki/Managing-your-accounts](https://github.com/elastos/Elastos.ELA.SideChain.ETH/wiki/Managing-your-accounts)
- What is Ethereum Sidechain: [https://developer.elastos.org/discover_elastos/core_modules/ethereum_sidechain/](https://developer.elastos.org/discover_elastos/core_modules/ethereum_sidechain/)
- Ethereum Sidechain RPC Reference: [https://developer.elastos.org/elastos_blockchain/reference/rpc/ethereum_sidechain/](https://developer.elastos.org/elastos_blockchain/reference/rpc/ethereum_sidechain/)
- Ethereum Sidechain Smart Contract Guide: [https://developer.elastos.org/elastos_core_services/guides/ethereum_smart_contracts/](https://developer.elastos.org/elastos_core_services/guides/ethereum_smart_contracts/)

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
  curl -H 'Content-Type: application/json' -H 'Accept:application/json' --data '{"jsonrpc":"2.0","method":"eth_getBalance","params":["0x4505b967d56f84647eb3a40f7c365f7d87a88bc3", "latest"],"id":1}' localhost:60011
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
7. Clean up your environment:
  
  ```
  cd $GOPATH/src/github.com/cyber-republic/elastos-privnet/blockchain/ela-mainchain;
  rm -f cli-config.json keystore.dat ready_to_send.txn to_be_signed.txn wallet.db;
  cd $GOPATH/src/github.com/cyber-republic/elastos-privnet/blockchain;
  tools/copy_dockerdata_host.sh;
  cd $GOPATH/src/github.com/cyber-republic/developer-workshop/2019-07-31;
  git checkout samples/src/main/java/didclientsample/ElaDidServiceApi.java
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
    "result": "0x152cf383e51ef1920000"
  }
  ```
  0x152cf383e51ef1920000 is 99998900000000000000000 in decimal format which is the unit in Wei. This equals to 99998.9 ETH ELA
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

## Connect to Ethereum Sidechain testnet

## Deploy a simple Ethereum Smart Contract