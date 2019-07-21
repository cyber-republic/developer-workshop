## Requirements

To go through this, you will need the following:

1. You are using a Mac/Linux
2. You have installed docker and docker-compose
3. You have installed Go
4. You have checked out https://github.com/cyber-republic/elastos-privnet/tree/v0.4 and are on tag v0.4
5. Catch up on First Developer Workshop on how to run an elastos private net on your local machine if you haven't. Visit [Elastos Developer Workshop #1: Running Private Net](https://www.youtube.com/watch?v=0Mn9pz2UORo) for more info

## Contents

0. Set up your Private Net
1. Create a DID
2. Retrieve Metadata stored in DID
3. Sign message using DID Private Key
4. Verify contents of the message using DID Public key
5. 

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

   You should see at least 915 ELA in the miner wallet:

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

## Create a DID

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

## Retrieve Metadata stored in DID

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

## Sign message using DID Private Key


## Verify contents of the message using DID Public key