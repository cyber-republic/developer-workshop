## Requirements

To go through this, you will need the following:

1. You are using a Mac/Linux
2. You have installed docker and docker-compose
3. You have installed Go
4. You have checked out https://github.com/cyber-republic/elastos-privnet/ and are on tag v0.3
5. Catch up on First Developer Workshop on how to run an elastos private net on your local machine if you haven't. Visit [Elastos Developer Workshop #1: Running Private Net](https://www.youtube.com/watch?v=0Mn9pz2UORo) for more info

## Contents

0. Set up your Private Net
1. Setup ELA mainchain node
1. Setup DID sidechain node
1. Setup Token sidechain node
1. Setup Arbitrator node
1. Setup Elastos Carrier bootstrap node
1. Register for your supernode
1. Vote for your supernode
1. Verify whether your supernode is working

## Set up your Private Net

1. Just run with docker-compose from the directory where elastos-privnet is located(github.com/cyber-republic/elastos-privnet/blockchain):

   ```
   docker-compose up --remove-orphans --build --force-recreate -d
   ```

2. Verify the Mainchain is running by checking the miner reward wallet:

   ```
   curl http://localhost:10012/api/v1/asset/balances/EQ4QhsYRwuBbNBXc8BPW972xA9ANByKt6U
   ```

   You should see at least 915 ELA in the miner wallet:

   ```
   {"Desc":"Success","Error":0,"Result":"915.91409329"}
   ```

3. Verify the DID Sidechain is running by checking the pre-loaded wallet:

   ```
   curl http://localhost:30112/api/v1/asset/balances/EKsSQae7goc5oGGxwvgbUxkMsiQhC9ZfJ3
   ```

   You should see 100,000 ELA in the DID Sidechain wallet pre-loaded:

   ```
   {"Result":"100000","Error":0,"Desc":"Success"}
   ```

4. Verify the Token Sidechain is running by checking the pre-loaded wallet:

   ```
   curl -H 'Content-Type: application/json' -H 'Accept:application/json' --data '{"method":"getreceivedbyaddress","params":{"address":"EUscMawPCr8uFxKDtVxaq93Wbjm1DdtzeW"}}' http://localhost:40144
   ```

   You should see at least 100000 ELA in the miner wallet:

   ```
   {"result":"100000","status":200}
   ```

## Setup ELA mainchain node

1. Create a directory to work off of:

  ```
  mkdir ~/node/ela
  cd ~/node/ela
  ```

2. Download the files that are needed:

- [ela](https://github.com/elastos/Elastos.ELA/releases/download/v0.3.2/ela)
  ```
  wget https://github.com/elastos/Elastos.ELA/releases/download/v0.3.2/ela
  ```
- [ela-cli](https://github.com/elastos/Elastos.ELA/releases/download/v0.3.2/ela-cli)
  ```
  wget https://github.com/elastos/Elastos.ELA/releases/download/v0.3.2/ela-cli
  ```
- [config.json](https://raw.githubusercontent.com/elastos/Elastos.ELA/release_v0.3.2/docs/dpos_config.json.sample)
  ```
  wget https://raw.githubusercontent.com/elastos/Elastos.ELA/release_v0.3.2/docs/dpos_config.json.sample
  mv dpos_config.json.sample config.json
  ```

3. Create a wallet using ela-cli

- Creating a wallet creates a keystore.dat file. This file is used to store your node's public key and the DPoS supernode uses this file to complete node conmmunication
- The password encrypts keystore.dat so it needs to be added when running your ela node later on. It is recommended to set up a more complex password in production environment

  ```bash
  # This creates a keystore.dat file with password "elastos"
  ./ela-cli wallet create -p elastos
  ```

4. Take a note of your public key associated to your wallet

This is the node public key you will want to update to from your elastos wallet by editing your supernode details

  ```
  ./ela-cli wallet account -p elastos
  ```

5. Modify ela configuration file: `config.json`

- For connecting to mainnet:
  ```json
  {
    "Configuration": {
      "DPoSConfiguration": {
        "EnableArbiter": true,
        "IPAddress": "127.0.0.1"
      },
      "RpcConfiguration": {
        "User": "user",
        "Pass": "password",
        "WhiteIPList": ["0.0.0.0"]
      }
    }
  }
  ```
- For connecting to privatenet:
  ```json
  {
    "Configuration": {
      "Magic": 7630401,
      "DisableDNS": true,
      "PermanentPeers": [
        "127.0.0.1:10015",
        "127.0.0.1:10115",
        "127.0.0.1:10215",
        "127.0.0.1:10315",
        "127.0.0.1:10415",
        "127.0.0.1:10515",
        "127.0.0.1:10615"
      ],
      "HttpRestStart": true,
      "EnableRPC": true,
      "FoundationAddress": "ENqDYUYURsHpp1wQ8LBdTLba4JhEvSDXEw",
      "DPoSConfiguration": {
        "EnableArbiter": true,
        "Magic": 7630403,
        "IPAddress": "127.0.0.1",
        "OriginArbiters": [
          "02677bd3dc8ea4a9ab22f8ba5c5348fc1ce4ba5f1810e8ec8603d5bd927b630b3e",
          "0232d3172b7fc139b7605b83cd27e3c6f64fde1e71da2489764723639a6d40b5b9"
        ],
        "CRCArbiters": [
          "0386206d1d442f5c8ddcc9ae45ab85d921b6ade3a184f43b7ccf6de02f3ca0b450",
          "0353197d11802fe0cd5409f064822b896ceaa675ea596287f1e5ce009be7684f08",
          "032e74c386af5d672cb196334f2b6ee6451d61f2257f0837ea7af340ef4dea4e1a",
          "02eafcd36390b064431b82a4b2934f6d93fddfcfa4a86602b2ae32d858b8d3bcd7"
        ],
        "NormalArbitratorsCount": 2,
        "CandidatesCount": 24
      },
      "CheckAddressHeight": 101,
      "VoteStartHeight": 100,
      "CRCOnlyDPOSHeight": 200,
      "PublicDPOSHeight": 500,
      "RpcConfiguration": {
        "User": "user",
        "Pass": "password",
        "WhiteIPList": ["0.0.0.0"]
      }
    }
  }
  ```

6. Run ela node

- The ela node startup command needs to enter the password of the keystore.dat file
  ```bash
  # This will run the ./ela program in the background and will pass in
  # the password "elastos" and it sends all the outputted logs to
  # /dev/null and only captures error logs to a file called "output"
  echo elastos | nohup ./ela > /dev/null 2>output &
  ```
- After the node is started, you can check the node information such as node height, version, etc with ela-cli
  ```bash
  # This assumes that RPC username and password are not set and default
  # port is used for RPC
  ./ela-cli info getnodestate
  # This assumes that RPC username and password are set with the following
  # and the HttpJsonPort is configured to be 20336
  ./ela-cli info getnodestate --rpcport 20336 --rpcuser user --rpcpassword password
  # You can also interact with RPC port directly without using ela-cli
  curl -X POST http://localhost:20336 -H 'Content-Type: application/json' \
  -d '{"method": "getnodestate"}'
  ```

## Setup DID sidechain node

1. Create a directory to work off of:

  ```
  mkdir ~/node/did
  cd ~/node/did
  ```

2. Download the files that are needed:

- [did](https://github.com/elastos/Elastos.ELA.SideChain.ID/releases/download/v0.1.2/did)
  ```
  wget https://github.com/elastos/Elastos.ELA.SideChain.ID/releases/download/v0.1.2/did
  ```
- [config.json](https://raw.githubusercontent.com/elastos/Elastos.ELA.SideChain.ID/master/docs/mainnet_config.json.sample)
  ```
  wget https://raw.githubusercontent.com/elastos/Elastos.ELA.SideChain.ID/master/docs/mainnet_config.json.sample
  mv mainnet_config.json.sample config.json
  ```

3. Modify did configuration file: `config.json`

- For connecting to mainnet:
  ```json
  {
    "SPVDisableDNS": false,
    "SPVPermanentPeers": ["127.0.0.1:20338"],
    "EnableRPC": true,
    "RPCUser": "user",
    "RPCPass": "password",
    "RPCWhiteList": ["0.0.0.0"]
  }
  ```
- For connecting to privatenet:
  ```json
  {
    "Magic": 7630404,
    "SPVMagic": 7630401,
    "SPVDisableDNS": true,
    "PermanentPeers": [
      "127.0.0.1:30115",
      "127.0.0.1:30215",
      "127.0.0.1:30315",
      "127.0.0.1:30415"
    ],
    "SPVPermanentPeers": [
      "127.0.0.1:10015",
      "127.0.0.1:10115",
      "127.0.0.1:10215",
      "127.0.0.1:10315",
      "127.0.0.1:10415",
      "127.0.0.1:10515",
      "127.0.0.1:10615"
    ],
    "EnableREST": true,
    "EnableRPC": true,
    "FoundationAddress": "ENqDYUYURsHpp1wQ8LBdTLba4JhEvSDXEw",
    "RPCUser": "user",
    "RPCPass": "password",
    "RPCWhiteList": ["0.0.0.0"]
  }
  ```

4. Run did node

- The did node startup command
  ```bash
  # This will run the ./did program in the background and it sends all the
  # outputted logs to /dev/null and only captures error logs to a file
  # called "output"
  nohup ./did > /dev/null 2>output &
  ```
- After the node is started, you can check the node information such as node height, version, etc with RPC interface
  ```bash
  # This assumes that RPC username and password are not set and default
  # port is used for RPC
  curl -X POST http://localhost:20606 -H 'Content-Type: application/json' \
  -d '{"method": "getnodestate"}'
  # This assumes that RPC username and password are set with the following
  # and the HttpJsonPort is configured to be 20606
  curl --user user:pass -X POST http://localhost:20606 -H 'Content-Type: application/json' -d '{"method": "getnodestate"}'
  ```

## Setup Token sidechain node

1. Create a directory to work off of:

  ```
  mkdir ~/node/token
  cd ~/node/token
  ```

2. Download the files that are needed:

- [token](https://github.com/elastos/Elastos.ELA.SideChain.Token/releases/download/v0.1.2/token)
  ```
  wget https://github.com/elastos/Elastos.ELA.SideChain.Token/releases/download/v0.1.2/token
  ```
- [config.json](https://raw.githubusercontent.com/elastos/Elastos.ELA.SideChain.Token/master/docs/mainnet_config.json.sample)
  ```
  wget https://raw.githubusercontent.com/elastos/Elastos.ELA.SideChain.Token/master/docs/mainnet_config.json.sample
  mv mainnet_config.json.sample config.json
  ```

3. Modify did configuration file: `config.json`

- For connecting to mainnet:
  ```json
  {
    "SPVDisableDNS": false,
    "SPVPermanentPeers": ["127.0.0.1:20338"],
    "EnableRPC": true,
    "RPCUser": "user",
    "RPCPass": "password",
    "RPCWhiteList": ["0.0.0.0"]
  }
  ```
- For connecting to privatenet:
  ```json
  {
    "Magic": 7630405,
    "SPVMagic": 7630401,
    "SPVDisableDNS": true,
    "PermanentPeers": [
      "127.0.0.1:40115",
      "127.0.0.1:40215",
      "127.0.0.1:40315",
      "127.0.0.1:40415"
    ],
    "SPVPermanentPeers": [
      "127.0.0.1:10015",
      "127.0.0.1:10115",
      "127.0.0.1:10215",
      "127.0.0.1:10315",
      "127.0.0.1:10415",
      "127.0.0.1:10515",
      "127.0.0.1:10615"
    ],
    "EnableREST": true,
    "EnableRPC": true,
    "FoundationAddress": "ENqDYUYURsHpp1wQ8LBdTLba4JhEvSDXEw",
    "RPCUser": "user",
    "RPCPass": "password",
    "RPCWhiteList": ["0.0.0.0"]
  }
  ```

4. Run token node

- The token node startup command
  ```bash
  # This will run the ./token program in the background and it sends all the
  # outputted logs to /dev/null and only captures error logs to a file
  # called "output"
  nohup ./token > /dev/null 2>output &
  ```
- After the node is started, you can check the node information such as node height, version, etc with RPC interface
  ```bash
  # This assumes that RPC username and password are not set and default
  # port is used for RPC
  curl -X POST http://localhost:20616 -H 'Content-Type: application/json' \
  -d '{"method": "getnodestate"}' 
  # This assumes that RPC username and password are set with the following
  # and the HttpJsonPort is configured to be 20616
  curl --user user:pass -X POST http://localhost:20616 -H 'Content-Type: application/json' -d '{"method": "getnodestate"}'
  ```

## Setup Arbitrator node

## Setup Elastos Carrier bootstrap node

1. Create a directory to work off of:

  ```
  mkdir ~/node/carrier
  cd ~/node/carrier
  ```

2. Download the files that are needed:

- [carrier](https://github.com/elastos/Elastos.NET.Carrier.Bootstrap/releases/download/release-v5.1.1/elastos-carrier-bootstrap-5.2.2da37f-linux-x86_64-Debug.deb)
  ```
  wget https://github.com/elastos/Elastos.NET.Carrier.Bootstrap/releases/download/release-v5.1.1/elastos-carrier-bootstrap-5.2.2da37f-linux-x86_64-Debug.deb
  ```

3. Run carrier bootstrap node

  ```
  dpkg -i elastos-carrier-bootstrap-5.2.2da37f-linux-x86_64-Debug.deb
  sudo systemctl start ela-bootstrapd
  ```

4. Check the status of carrier bootstrap node

  ```
  sudo systemctl status ela-bootstrapd
  ```

## Register for your supernode

## Vote for your supernode

## Verify whether your supernode is working

## Exercises

1. Transfer 10 ELA from main chain to DID sidechain using elastos/Elastos.ELA.Client/ela-cli
2. Check the result of the transaction hash you got from #1 via Wallet.Service API
3. Check the entire transaction history of an ELA address via Wallet.Service API
4. Transfer 5 DID ELA from DID sidechain to main chain using DID.Service API
