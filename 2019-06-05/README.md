## Requirements

To go through this, you will need the following:

1. You are using a Mac/Linux
2. You have installed docker and docker-compose
3. You have installed Go
4. You have checked out https://github.com/cyber-republic/elastos-privnet/tree/v0.3 and are on tag v0.3
5. Catch up on First Developer Workshop on how to run an elastos private net on your local machine if you haven't. Visit [Elastos Developer Workshop #1: Running Private Net](https://www.youtube.com/watch?v=0Mn9pz2UORo) for more info

## Contents

0. Set up your Private Net
1. Setup ELA mainchain node
2. Setup DID sidechain node
3. Setup Token sidechain node
4. Setup Elastos Carrier bootstrap node
5. Register for your supernode
6. Vote for your supernode
7. Verify whether your supernode is working

## Set up your Private Net

1. Just run with docker-compose from the directory where elastos-privnet is located(github.com/cyber-republic/elastos-privnet/blockchain):

   ```
   cd $GOPATH/src/github.com/cyber-republic/elastos-privnet/blockchain;
   git checkout v0.3;
   tools/copy_freshdata_docker.sh;
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
   curl http://localhost:30111/api/v1/asset/balances/EKsSQae7goc5oGGxwvgbUxkMsiQhC9ZfJ3
   ```

   You should see 100,000 ELA in the DID Sidechain wallet pre-loaded:

   ```
   {"Result":"100000","Error":0,"Desc":"Success"}
   ```

4. Verify the Token Sidechain is running by checking the pre-loaded wallet:

   ```
   curl -H 'Content-Type: application/json' -H 'Accept:application/json' --data '{"method":"getreceivedbyaddress","params":{"address":"EUscMawPCr8uFxKDtVxaq93Wbjm1DdtzeW"}}' http://localhost:40113
   ```

   You should see at least 99999.99 ELA in the miner wallet:

   ```
   {"id":null,"jsonrpc":"2.0","result":{"a3d0eaa466df74983b5d7c543de6904f4c9418ead5ffd6d25814234a96db37b0":"99999.99990000"},"error":null}
   ```

## Setup ELA mainchain node

1. Create a directory to work off of:

  ```
  mkdir -p ~/node/ela;
  cd ~/node/ela
  ```

2. Download the files that are needed:

- [ela](https://github.com/elastos/Elastos.ELA/releases/download/v0.3.2/ela)
  ```
  wget https://github.com/elastos/Elastos.ELA/releases/download/v0.3.2/ela;
  chmod +x ela
  ```
- [ela-cli](https://github.com/elastos/Elastos.ELA/releases/download/v0.3.2/ela-cli)
  ```
  wget https://github.com/elastos/Elastos.ELA/releases/download/v0.3.2/ela-cli;
  chmod +x ela-cli
  ```
- [config.json](https://raw.githubusercontent.com/elastos/Elastos.ELA/release_v0.3.2/docs/dpos_config.json.sample)
  ```
  wget https://raw.githubusercontent.com/elastos/Elastos.ELA/release_v0.3.2/docs/dpos_config.json.sample;
  mv dpos_config.json.sample config.json
  ```

3. Create a wallet using ela-cli

- Creating a wallet creates a keystore.dat file. This file is used to store your node's public key and the DPoS supernode uses this file to complete node conmmunication
- The password encrypts keystore.dat so it needs to be added when running your ela node later on. It is recommended to set up a more complex password in production environment

  ```bash
  # This creates a keystore.dat file with password "elastos"
  ./ela-cli wallet create -p elastos
  # Let's first save our ELA Address in a variable so we can keep on using # it
  ELAADDRESS=$(./ela-cli wallet a -p elastos | tail -2 | head -1 | cut -d' ' -f1)
  PUBLICKEY=$(./ela-cli wallet a -p elastos | tail -2 | head -1 | cut -d' ' -f2)
  PRIVATEKEY=$(./ela-cli wallet export -p elastos | tail -2 | head -1 | cut -d' ' -f2)
  # Make sure your info is correct
  echo $ELAADDRESS $PUBLICKEY $PRIVATEKEY
  ```
  ```

4. Take a note of your public key associated to your wallet

This is the node public key you will want to update to from your elastos wallet by editing your supernode details

  ```
  echo $PUBLICKEY
  ```

5. Modify ela configuration file: `config.json`

- For connecting to mainnet:

  Refer to [https://github.com/cyber-republic/supernode-setup/tree/v0.1](https://github.com/cyber-republic/supernode-setup/tree/v0.1) for more detailed info on how to connect to mainnet
  ```json
  {
    "Configuration": {
      "DPoSConfiguration": {
        "EnableArbiter": true,
        "IPAddress": "192.168.0.1"
      },
      "EnableRPC": true,
      "RpcConfiguration": {
        "User": "User",
        "Pass": "Password",
        "WhiteIPList": [
          "127.0.0.1"
        ]
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
        "127.0.0.1:10016",
        "127.0.0.1:10116",
        "127.0.0.1:10216",
        "127.0.0.1:10316",
        "127.0.0.1:10416",
        "127.0.0.1:10516",
        "127.0.0.1:10616"
      ],
      "HttpRestStart": true,
      "EnableRPC": true,
      "PrintLevel": 1,
      "MaxLogsSize": 0,
      "MaxPerLogSize": 0,
      "MinCrossChainTxFee": 10000,
      "FoundationAddress": "ENqDYUYURsHpp1wQ8LBdTLba4JhEvSDXEw",
      "DPoSConfiguration": {
        "EnableArbiter": true,
        "Magic": 7630403,
        "PrintLevel": 1,
        "IPAddress": "127.0.0.1",
        "SignTolerance": 5,
        "MaxLogsSize": 0,
        "MaxPerLogSize": 0,
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
        "CandidatesCount": 24,
        "EmergencyInactivePenalty": 0,
        "MaxInactiveRounds": 20,
        "InactivePenalty": 0,
        "PreConnectOffset": 20
      },
      "CheckAddressHeight": 101,
      "VoteStartHeight": 100,
      "CRCOnlyDPOSHeight": 200,
      "PublicDPOSHeight": 500,
      "RpcConfiguration": {
        "User": "user",
        "Pass": "password",
        "WhiteIPList": [
          "0.0.0.0"
        ]
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
  curl -X POST http://user:password@localhost:20336 -H 'Content-Type: application/json'   -d '{"method": "getnodestate"}'
  ```

## Setup DID sidechain node

1. Create a directory to work off of:

  ```
  mkdir -p ~/node/did
  cd ~/node/did
  ```

2. Download the files that are needed:

- [did](https://github.com/elastos/Elastos.ELA.SideChain.ID/releases/download/v0.1.2/did)
  ```
  wget https://github.com/elastos/Elastos.ELA.SideChain.ID/releases/download/v0.1.2/did;
  chmod +x did
  ```
- [config.json](https://raw.githubusercontent.com/elastos/Elastos.ELA.SideChain.ID/master/docs/mainnet_config.json.sample)
  ```
  wget https://raw.githubusercontent.com/elastos/Elastos.ELA.SideChain.ID/master/docs/mainnet_config.json.sample;
  mv mainnet_config.json.sample config.json
  ```

3. Modify did configuration file: `config.json`

- For connecting to mainnet:

  Refer to [https://github.com/cyber-republic/supernode-setup/tree/v0.1](https://github.com/cyber-republic/supernode-setup/tree/v0.1) for more detailed info on how to connect to mainnet
  ```json
  {
    "SPVDisableDNS": false,
    "SPVPermanentPeers": [
      "localhost:20338"
    ],
    "EnableRPC": true,
    "RPCUser": "User",
    "RPCPass": "Password",
    "RPCWhiteList": [
      "127.0.0.1"
    ]
  }
  ```
- For connecting to privatenet:
  ```json
  {
    "Magic": 7630404,
    "SPVMagic": 7630401,
    "DisableDNS": true,
    "SPVDisableDNS": true,
    "PermanentPeers": [
      "127.0.0.1:30115",
      "127.0.0.1:30215",
      "127.0.0.1:30315",
      "127.0.0.1:30415"
    ],
    "SPVPermanentPeers": [
      "127.0.0.1:10016",
      "127.0.0.1:10116",
      "127.0.0.1:10216",
      "127.0.0.1:10316",
      "127.0.0.1:10416",
      "127.0.0.1:10516",
      "127.0.0.1:10616"
    ],
    "ExchangeRate": 1.0,
    "MinCrossChainTxFee": 10000,
    "EnableREST": true,
    "EnableRPC": true,
    "Loglevel": 1,
    "LogsFolderSize": 0,
    "PerLogFileSize": 0,
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
  curl --user user:password -X POST http://localhost:20606 -H 'Content-Type: application/json' -d '{"method": "getnodestate"}'
  ```

## Setup Token sidechain node

1. Create a directory to work off of:

  ```
  mkdir -p ~/node/token;
  cd ~/node/token
  ```

2. Download the files that are needed:

- [token](https://github.com/elastos/Elastos.ELA.SideChain.Token/releases/download/v0.1.2/token)
  ```
  wget https://github.com/elastos/Elastos.ELA.SideChain.Token/releases/download/v0.1.2/token;
  chmod +x token
  ```
- [config.json](https://raw.githubusercontent.com/elastos/Elastos.ELA.SideChain.Token/master/docs/mainnet_config.json.sample)
  ```
  wget https://raw.githubusercontent.com/elastos/Elastos.ELA.SideChain.Token/master/docs/mainnet_config.json.sample;
  mv mainnet_config.json.sample config.json
  ```

3. Modify token configuration file: `config.json`

- For connecting to mainnet:

  Refer to [https://github.com/cyber-republic/supernode-setup/tree/v0.1](https://github.com/cyber-republic/supernode-setup/tree/v0.1) for more detailed info on how to connect to mainnet
  ```json
  {
    "SPVDisableDNS": false,
    "SPVPermanentPeers": [
      "localhost:20338"
    ],
    "EnableRPC": true,
    "RPCUser": "User",
    "RPCPass": "Password",
    "RPCWhiteList": [
      "127.0.0.1"
    ]
  }
  ```
- For connecting to privatenet:
  ```json
  {
    "Magic": 7630405,
    "SPVMagic": 7630401,
    "DisableDNS": true,
    "SPVDisableDNS": true,
    "PermanentPeers": [
      "127.0.0.1:40115",
      "127.0.0.1:40215",
      "127.0.0.1:40315",
      "127.0.0.1:40415"
    ],
    "SPVPermanentPeers": [
      "127.0.0.1:10016",
      "127.0.0.1:10116",
      "127.0.0.1:10216",
      "127.0.0.1:10316",
      "127.0.0.1:10416",
      "127.0.0.1:10516",
      "127.0.0.1:10616"
    ],
    "ExchangeRate": 1.0,
    "MinCrossChainTxFee": 10000,
    "EnableREST": true,
    "EnableRPC": true,
    "Loglevel": 1,
    "LogsFolderSize": 0,
    "PerLogFileSize": 0,
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
  curl --user user:password -X POST http://localhost:20616 -H 'Content-Type: application/json' -d '{"method": "getnodestate"}'
  ```

## Setup Elastos Carrier bootstrap node

1. Create a directory to work off of:

  ```
  mkdir ~/node/carrier;
  cd ~/node/carrier
  ```

2. Download the files that are needed:

- [carrier](https://github.com/elastos/Elastos.NET.Carrier.Bootstrap/releases/download/release-v5.2.3/elastos-carrier-bootstrap-5.2.717741-linux-x86_64-Debug.deb)
  ```
  wget https://github.com/elastos/Elastos.NET.Carrier.Bootstrap/releases/download/release-v5.2.3/elastos-carrier-bootstrap-5.2.717741-linux-x86_64-Debug.deb
  ```

3. Run carrier bootstrap node

  ```
  sudo dpkg -i elastos-carrier-bootstrap-5.2.717741-linux-x86_64-Debug.deb
  ```

3. Make changes to `/etc/elastos/bootstrapd.conf`
- Set external IP to turn server explicitly: Some Linux VPS servers, for example, servers from AWS, can't fetch public IP address directly by itself, so you have manually update the public IP address of item external_ip under the section "turn"

4. Restart carrier bootstrap node

  ```
  systemctl restart ela-bootstrapd
  ```

5. Check the status of carrier bootstrap node

  ```
  systemctl status ela-bootstrapd
  ```

## Register for your supernode

1. If you want to register for your supernode that's connected to mainnet, do so using Elastos Wallet. 

2. Get the public key of your DPoS node

  Let's first save our ELA Address in a variable so we can keep on using it
  ```bash
  cd ~/node/ela;
  ELAADDRESS=$(./ela-cli wallet a -p elastos | tail -2 | head -1 | cut -d' ' -f1)
  PUBLICKEY=$(./ela-cli wallet a -p elastos | tail -2 | head -1 | cut -d' ' -f2)
  PRIVATEKEY=$(./ela-cli wallet export -p elastos | tail -2 | head -1 | cut -d' ' -f2)
  # Make sure your info is correct
  echo $ELAADDRESS $PUBLICKEY $PRIVATEKEY
  ```

  Should return something like:
  ```
  ADDRESS                            PUBLIC KEY                                                        
  ---------------------------------- ------------------------------------------------------------------
  Ec39reRzMTixsYt7yoXkQWDi7kb5dwbj66 036d49dfbb70932b8aea1218beee8dd7aa5e0aafa7a079cb15ba468d74c38a99cf
  ---------------------------------- ------------------------------------------------------------------
  ```

3. Edit your supernode using Elastos Wallet

Go to your Elastos Wallet, open up your Supernode Edit Page and then enter the above public key as your node key. This is what will link your wallet(owner public key) to your actual supernode(node public key).

4. If you want to register for a supernode on the private net, do the following:

  NOTE: PLEASE DO NOT USE THIS METHOD TO REGISTER FOR YOUR SUPERNODE ON MAINNET AS THIS IS ONLY FOR TESTING PURPOSES. WE RECOMMEND USING ELASTOS WALLET TO REGISTER FOR YOUR SUPERNODE ON MAINNET

  ```
  cp $GOPATH/src/github.com/cyber-republic/developer-workshop/2019-06-05/register_supernode.lua .;
  ./ela-cli wallet depositaddr $ELAADDRESS
  ```

  Should output your "deposit_address" that you enter on register_supernode.lua script

  ```
  ./ela-cli wallet account -p elastos
  ```

  Should output your public key that you can enter for both "own_publickey" and "node_publickey" for testing purposes

  Finally, also, make sure to change "nick_name", "url" and "location" to your own choosing.

5. Register your local supernode

  Let's send some ELA to this ELA address first because we need 5000 ELA to register for our supernode
  ```
  curl -X POST -H "Content-Type: application/json" -d '{"sender": [{"address": "EUSa4vK5BkKXpGE3NoiUt695Z9dWVJ495s","privateKey": "109a5fb2b7c7abd0f2fa90b0a295e27de7104e768ab0294a47a1dd25da1f68a8"}],"receiver": [{"address": '"$ELAADDRESS"',"amount": "6000"}]}' localhost:8091/api/1/transfer
  ```

  Wait for the transaction to confirm(around 6 blocks) and then check your new balance:
  ```
  ./ela-cli wallet b --rpcuser user --rpcpassword password
  ```

  Should output something like:
  ```
  INDEX                            ADDRESS BALANCE                           (LOCKED) 
  ----- ---------------------------------- ------------------------------------------
      0 Ec39reRzMTixsYt7yoXkQWDi7kb5dwbj66 6000                                   (0) 
  ----- ---------------------------------- ------------------------------------------
  ```

  Finally, let's run the script to register our supernode on our private net
  ```
  ./ela-cli script --file ./register_supernode.lua --rpcuser user --rpcpassword password
  ```

  If the transaction is successful, it should say "tx send success" at the end of the output

6. Verify your supernode got registered successfully

  After about 6 blocks, check that your supernode got registered successfully
  ```
  curl --user user:password -H 'Content-Type: application/json' -H 'Accept:application/json' --data '{"method":"listproducers", "params":{"start":"0"}}' http://localhost:20336 
  ```

  You should see your new supernode listed there

## Vote for your supernode

- Give 500 votes to Noderators supernode using the same wallet
  ```
  curl -X POST -H "Content-Type: application/json" -d '{
      "sender":[
          {
              "address":'"$ELAADDRESS"',
              "privateKey":'"$PRIVATEKEY"'
          }
      ],
      "memo":"Voting for Dev Workshop Supernode",
      "receiver":[
          {
              "address":'"$ELAADDRESS"',
              "amount":"500",
              "candidatePublicKeys":['"$PUBLICKEY"']
          }
      ]
  }' localhost:8091/api/1/dpos/vote
  ```

  After some blocks, your vote will be seen. Let's verify this:
  ```
  curl --user user:password -H 'Content-Type: application/json' -H 'Accept:application/json' --data '{"method":"listproducers", "params":{"start":"0"}}' http://localhost:20336
  ```

  Should output something like:
  ```
  {
    "error": null,
    "id": null,
    "jsonrpc": "2.0",
    "result": {
      "producers": [
        {
          "ownerpublickey": "03521eb1f20fcb7a792aeed2f747f278ae7d7b38474ee571375ebe1abb3fa2cbbb",
          "nodepublickey": "0295890a17feb7d5191da656089b5daad83f596edcc491f5c91d025b42955a9f25",
          "nickname": "KP Supernode",
          "url": "www.pachhai.com",
          "location": 112211,
          "active": true,
          "votes": "75000",
          "state": "Active",
          "registerheight": 418,
          "cancelheight": 0,
          "inactiveheight": 0,
          "illegalheight": 0,
          "index": 0
        },
        {
          "ownerpublickey": "03aa307d123cf3f181e5b9cc2839c4860a27caf5fb329ccde2877c556881451007",
          "nodepublickey": "021cfade3eddd057d8ca178057a88c4654b15c1ada7ee9ab65517f00beb6977556",
          "nickname": "Noderators",
          "url": "www.noderators.org",
          "location": 112211,
          "active": true,
          "votes": "50000",
          "state": "Active",
          "registerheight": 368,
          "cancelheight": 0,
          "inactiveheight": 0,
          "illegalheight": 0,
          "index": 1
        },
        {
          "ownerpublickey": "036d49dfbb70932b8aea1218beee8dd7aa5e0aafa7a079cb15ba468d74c38a99cf",
          "nodepublickey": "036d49dfbb70932b8aea1218beee8dd7aa5e0aafa7a079cb15ba468d74c38a99cf",
          "nickname": "Dev Workshop Supernode",
          "url": "https://github.com/cyber-republic/developer-workshop",
          "location": 112211,
          "active": true,
          "votes": "500",
          "state": "Active",
          "registerheight": 1514,
          "cancelheight": 0,
          "inactiveheight": 0,
          "illegalheight": 0,
          "index": 2
        }
      ],
      "totalvotes": "125500",
      "totalcounts": 3
    }
  }
  ```

  As you can see, our newly created supernode has 500 votes now

## Verify whether your supernode is working

  Keep checking your balance by doing:
  ```
  ./ela-cli wallet b --rpcuser user --rpcpassword password
  ```

  Eventually, you'll see that you have started to gain rewards. You will see something like:
  ```
  INDEX                            ADDRESS BALANCE                           (LOCKED) 
  ----- ---------------------------------- ------------------------------------------
      0 Ec39reRzMTixsYt7yoXkQWDi7kb5dwbj66 999.99895140                  (0.03151776) 
  ----- ---------------------------------- ------------------------------------------
  ```

  You can also check your balance using the mainchain node API

## Stop your supernode processes

  ```
  cd $GOPATH/src/github.com/cyber-republic/elastos-privnet/blockchain;
  docker-compose down;
  kill -9 $(ps aux | grep "./ela" | grep -v grep | cut -d' ' -f2);
  kill -9 $(ps aux | grep "./did" | grep -v grep | cut -d' ' -f2);
  kill -9 $(ps aux | grep "./token" | grep -v grep | cut -d' ' -f2);
  sudo systemctl stop ela-bootstrapd;
  rm -rf ~/node/ela ~/node/did ~/node/token ~/node/carrier;
  cd $GOPATH/src/github.com/cyber-republic/elastos-privnet/blockchain
  ```
