## Requirements

To go through this, you will need the following:

1. You are using a Mac/Linux
2. You have installed docker and docker-compose
3. You have installed Go
4. You have installed java
5. You have installed Maven
    - On Ubuntu, just do "sudo apt install maven"
6. You have checked out https://github.com/cyber-republic/elastos-privnet/tree/v0.4 and are on tag v0.4
7. Catch up on First Developer Workshop on how to run an elastos private net on your local machine if you haven't. Visit [Elastos Developer Workshop #1: Running Private Net](https://www.youtube.com/watch?v=0Mn9pz2UORo) for more info

## Contents

0. Set up your Private Net
1. Sidechain Service: Create a DID
2. Sidechain Service: Retrieve Metadata stored in DID
3. Sidechain Service: Sign message using DID Private Key
4. Sidechain Service: Verify contents of the message using DID Public key
5. DIDClient Java SDK: Sample App interacting with DID Sidechain
6. Elephant Wallet API: Sample app showing off webapp authentication with Elastos DID Sidechain and Elephant Wallet
7. Sneak Preview of Ethereum Sidechain

## Set up your Private Net

1. Just run with docker-compose from the directory where elastos-privnet is located(github.com/cyber-republic/elastos-privnet/blockchain):

   ```
   cd $GOPATH/src/github.com/cyber-republic/elastos-privnet/blockchain;
   git checkout v0.4;
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

3. Verify the DID Sidechain is running by checking the pre-loaded wallet:

   ```
   curl http://localhost:30111/api/v1/asset/balances/EKsSQae7goc5oGGxwvgbUxkMsiQhC9ZfJ3
   ```

   You should see 100,000 ELA in the DID Sidechain wallet pre-loaded:

   ```
   {"Result":"100000","Error":0,"Desc":"Success"}
   ```

## Sidechain Service: Create a DID

You can check out all the different functions exposed via DID Service at [https://didservice.readthedocs.io](https://didservice.readthedocs.io)

After creating a DID, you will receive both a did and a private key.

```
curl http://localhost:8092/api/1/gen/did
```

Should return something like
```
{
    "result": {
        "privateKey": "78F3F61DE57C2058FAB709641EAB8880F2312702896F5599FB4A714EBCF3CFFC",
        "publicKey": "02BDA7DBA5E4E1E24245566AF75E34CC9933FAA99FFFC61081156CC05AE65422E2",
        "publicAddr": "EJrijXpAJmFmn6Xbjdh8TZgAYKS1KsK26N",
        "did": "iXxFsEtpt8krhcNbVL7gzRfNqrJdRT4bSw"
    },
    "status": 200
}
```

NOTE: For your use, a DID has already been created and you can find about it at basics/did_example.json

Then you can call `POST /api/1/setDidInfo` to store data to this DID. There are two private keys, the outer private key is the private key of the wallet address that is paying for the store data transaction. We are going to use the preloaded ELA stored on the DID sidechain in adddress "EKsSQae7goc5oGGxwvgbUxkMsiQhC9ZfJ3" for this. You can check out the details about this DID ELA address at basics/preloaded-did-ela.json.

We will use the pre-configured DID to store info. Refer to basics/storeinfoon_didchain.json for what to pass in the body of the request to this API endpoint.

```
cat basics/storeinfoon_didchain.json
```

Should return
```
{
    "privateKey": "1d5fdc0ad6b0b90e212042f850c0ab1e7d9fafcbd7a89e6da8ff64e8e5c490d2",
    "settings": {
        "privateKey": "78F3F61DE57C2058FAB709641EAB8880F2312702896F5599FB4A714EBCF3CFFC",
        "info": {
            "Tag": "DID Property",
            "Ver": "1.0",
            "Status": 1,
            "Properties": [
                {
                "Key": "PrivateNet",
                "Value": "July is all about DID Sidechain. August will be about Ethereum Sidechain",
                "Status": 1
                }
            ]
        }
    }
}
```

The inner settings struct is the actual DID to modify, so you will use the private key from `/api/1/gen/did` here to specify that DID. For this example, you can use the file as is.

There is a cost of 10,000 SELA per 1kb on this privatenet, actual cost for the mainnet may vary.   

And when you want to send a request to post the above file to the DID sidechain with the key "PrivateNet" and value "July is all about DID Sidechain. August will be about Ethereum Sidechain", do the following(Save the above excerpt into a file named "storeinfoon_didchain.json):
```
curl -H "Content-type: application/json" -d @basics/storeinfoon_didchain.json http://localhost:8092/api/1/setDidInfo
```

Should return something like
```
{"result":"d1855ef2aea97c47cb52d2862a187e0302edda7f8f84d95420a204f121ccc741","status":200}
```

If you try to set the DID info before letting it be propagated to the block, you might get an error with something like 
```
{
    "result": "double spent UTXO inputs detected, transaction hash: 37393136626431306661653962363231663763396539626530636433653264653535306365313766393632656462663430633839383232383061373761376562, input: cf00f4c5600a5d7ec4f89197a555ba1334e506d3ce4f03b7f98b283765693696, index: 0",
    "status": 10001
}
```

Don't be alarmed. Just wait for this transaction to be added to the block and look at the results again to verify whether it did go through.

## Sidechain Service: Retrieve Metadata stored in DID

Retrieving the DID info must be on the Misc.API DID Sidechain - port 9092

Even if you use DID Sidechain Service to store DID property, you need to use Misc.API for DID sidechain to retrieve the DID property which should be running on port `9092`.

The API call should be `http://localhost:9092/api/1/did/{did}/{key}`

For example if you stored the property key "clark" above, and assuming the did was `iXxFsEtpt8krhcNbVL7gzRfNqrJdRT4bSw`, then calling

```
curl http://localhost:9092/api/1/did/iXxFsEtpt8krhcNbVL7gzRfNqrJdRT4bSw/PrivateNet
```

Would return something like
```
{
    "result": {
        "Did": "iXxFsEtpt8krhcNbVL7gzRfNqrJdRT4bSw",
        "Did_status": 1,
        "Public_key": "02BDA7DBA5E4E1E24245566AF75E34CC9933FAA99FFFC61081156CC05AE65422E2",
        "Property_key": "PrivateNet",
        "Property_value": "July is all about DID Sidechain. August will be about Ethereum Sidechain",
        "Txid": "c5be2e9e727883bf90f1e113ce0657bdd219d7372bd2c8e807f7e8fed16d88ed",
        "Block_time": 1563722975,
        "Height": 480
    },
    "status": 200
}
```

## Sidechain Service: Sign message using DID Private Key

You can use the API `POST /api/1/sign` to sign any message using your private key.
```
curl -X POST -H "Content-Type: application/json" -d '{"privateKey": "78F3F61DE57C2058FAB709641EAB8880F2312702896F5599FB4A714EBCF3CFFC", "msg": "This is our third developer workshop and it is all about DID. Our fourth developer workshop will be about introduction to ethereum sidechain. 😁"}' localhost:8092/api/1/sign
```

Should return something like:
```
{
  "result": {
    "msg": "54686973206973206F757220746869726420646576656C6F70657220776F726B73686F7020616E6420697420697320616C6C2061626F7574204449442E204F757220666F7572746820646576656C6F70657220776F726B73686F702077696C6C2062652061626F757420696E74726F64756374696F6E20746F20657468657265756D2073696465636861696E2E20F09F9881",
    "pub": "02BDA7DBA5E4E1E24245566AF75E34CC9933FAA99FFFC61081156CC05AE65422E2",
    "sig": "DDFFC3BA14534C101500B324F0D1DA96A02FD5FEF160C6931C12C71DCE2D569397BF90E26BDC35277446A5806D97A9068D07CBB40E689D491E7DD0187D75F179"
  },
  "status": 200
}
```

## Sidechain Service: Verify contents of the message using DID Public key

You can use the API `POST /api/1/verify` to verify the message that was signed using your private key. This is how you can know whether the message indeed was signed by a public key.

```
curl -X POST -H "Content-Type: application/json" -d '{"msg":"54686973206973206F757220746869726420646576656C6F70657220776F726B73686F7020616E6420697420697320616C6C2061626F7574204449442E204F757220666F7572746820646576656C6F70657220776F726B73686F702077696C6C2062652061626F757420696E74726F64756374696F6E20746F20657468657265756D2073696465636861696E2E20F09F9881","pub":"02BDA7DBA5E4E1E24245566AF75E34CC9933FAA99FFFC61081156CC05AE65422E2","sig":"DDFFC3BA14534C101500B324F0D1DA96A02FD5FEF160C6931C12C71DCE2D569397BF90E26BDC35277446A5806D97A9068D07CBB40E689D491E7DD0187D75F179"}' localhost:8092/api/1/verify
```

Should return something like:
```
{
  "result": true,
  "status": 200
}
```

## DIDClient Java SDK: Sample App

- Refer to [https://did-client-java-api.readthedocs.io/en/latest/](https://did-client-java-api.readthedocs.io/en/latest/) to learn more about the DIDClient Java SDK and the APIs that are available
- API References and sample codes for SDK: [https://did-client-java-api.readthedocs.io/en/latest/did_client_api_guide/#elastossdkdidclientapi](https://did-client-java-api.readthedocs.io/en/latest/did_client_api_guide/#elastossdkdidclientapi)
- Github repo: [https://github.com/elastos/Elastos.SDK.DIDClient.Java](https://github.com/elastos/Elastos.SDK.DIDClient.Java)
- Build the jar file
```
    cd samples;
    mvn clean;
    mvn compile;
    mvn package;
```
- Run the jar file
This java app does the following:
1. Creates a new DID on private net using DIDClient java SDK
2. Gets Public Key of the newly created DID
3. Signs a message and verifies it using DID private key
4. Sets a DID Property and puts it on the DID sidechain running on private net
5. Gets a DID Property by Transaction ID
6. Deletes DID Property attached to the DID
NOTE: There's also code to delete DID from the DID sidechain so you can uncomment the code for deleting DID and comment the code for deleting DID Property and then try building the jar file and executing again.
```
    java -jar target/samples-0.1.0.jar;
```

## Elephant Wallet: Sample app
- Courtesy of Jimmy from the community, [https://github.com/Compy](https://github.com/Compy), there is now a very simple web app that uses Elephant Wallet's DID as an authentication mechanism to register and log in to the website
- Clone the github repo of the did auth sample
    `git clone https://github.com/Compy/elastos-did-auth-sample`
- Follow the instructions(until Step #7) to set up the environment to run the web app in at [https://github.com/Compy/elastos-did-auth-sample/blob/master/README.md](https://github.com/Compy/elastos-did-auth-sample/blob/master/README.md)
- For the mysql database, we can just use the existing one that's already running as part of the Elastos Private net. You can check out its info at [https://github.com/cyber-republic/elastos-privnet/blob/master/blockchain/docker-compose.yml](https://github.com/cyber-republic/elastos-privnet/blob/master/blockchain/docker-compose.yml) but basically, below is the info you will be using in your .env file:
    `
    DB_CONNECTION=mysql
    DB_HOST=127.0.0.1
    DB_PORT=3307
    DB_DATABASE=ws
    DB_USERNAME=elastos
    DB_PASSWORD=12345678
    `
- For the env variable "APP_URL", you need to set it to your own IP address. For me, it's 192.168.1.23 and since the web app will be running on port 8000 on this IP address, I can set mine to the following:
    `APP_URL=http://192.168.1.23:8000`
- Last but not least, we also need to modify the DID section in the .env file. For now, you can just use the following but if you wanted to generate your own App ID and put it on the DID sidechain, have a look at [https://zuohuahua.github.io/Elastos.Tools.Creator.Capsule/](https://zuohuahua.github.io/Elastos.Tools.Creator.Capsule/). This website lets you generate DIDs on the fly and also to register your app on the DID sidechain. This is needed because this is what Elephant Wallet API will be requesting later on.
    `
    ELA_MNEMONIC="found case balcony law corn degree useless toddler install october elite goat"
    ELA_PRIVATE_KEY=0fc33eb184823fb80b9ac5ce34665ffba04598bc744fb6b8c41f8ec881d07274
    ELA_PUBLIC_KEY=02b8d47891ea51dd78ed97d49697fbb426bcaa6cb94f25e29714b873fb624c80d1
    ELA_ADDRESS=Eb1XdvCyxpTkMphK58s5S7kaK8QiLA5unc
    ELA_DID=iTHeDCTWrTFiV9HwrFMHy8ede59Aq62TrD
    ELA_APP_NAME=LaravelKPTest
    ELA_APP_ID=67b054ec2392ef0723fb5a1bee0b730eacb1d371fb0349ed95023d6a8dbdb9aa27c3eff7f28fb3f0a5b62fb48c16e0cac87d62f6f2585066db57e56272b943b7
    ELA_CALLBACK="${APP_URL}/api/did/callback"
    `
- Now, follow the instructions from Step #8-#10 from [https://github.com/Compy/elastos-did-auth-sample/blob/master/README.md](https://github.com/Compy/elastos-did-auth-sample/blob/master/README.md)
- Once you execute `php artisan serve --host 0.0.0.0`, your web app should be running
- You can now go to `localhost:8000` from your web browser and you will see the web app that will give you an option to register and login and also "Register with Elastos"
- Click on "Register with Elastos". This gets you a QR code on the screen
- Now, go to your Elephant Wallet and scan the QR code from there
- Once successful, this will automatically take you to the Registration page on the web app on your browser which will have your information pre-filled. All of this info was populated from whatever you have set your profile to contain on Elephant Wallet
- Great. Now you are logged in. You can log out and try to login using the same procedure
- This demo shows how a web app is able to utilize DID login using Elephant Wallet to authenticate to a web app. This sort of button utilizing DID login can be added to any existing web app. 
- Elastos DX team will be coming up with videos, tutorials and libraries that any developer can use in their own app. Be sure to sign up to Developer Email List to get notified.

## Sneak Preview of Ethereum Sidechain

### Transfer some ELA from main chain to ETH Sidechain
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