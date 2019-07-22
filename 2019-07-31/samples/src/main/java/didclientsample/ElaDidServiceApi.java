package didclientsample;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.elastos.conf.RetCodeConfiguration;
import org.elastos.entity.ReturnMsgEntity;
import org.elastos.service.ElaDidService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ElaDidServiceApi {
    private static Logger logger = LoggerFactory.getLogger(ElaDidServiceApi.class);

    String didNodeUrl = "http://localhost:30111";
    String payPrivateKey = "1d5fdc0ad6b0b90e212042f850c0ab1e7d9fafcbd7a89e6da8ff64e8e5c490d2";
    String payPublicKey = "03848390f4a687c247f4f662364c142a060ad10a03749178268decf9461b3c0fa5";
    String payPublicAddr = "EKsSQae7goc5oGGxwvgbUxkMsiQhC9ZfJ3";
    String didPrivateKey = "";
    String didPublicKey = "";
    String did = "";
    ElaDidService didService = new ElaDidService();
    String didPropertyKey;
    String didPropertyValue;

    public ElaDidServiceApi() {
        didPropertyKey = "PrivateNet";
        List<String> didProperty = new ArrayList<>();
        didProperty.add("Elastos Developer Workshop #3: Decentralized Identifier Sidechain");
        didProperty.add("July is all about DID Sidechain");
        didProperty.add("August is all about Ethereum Sidechain");
        didPropertyValue = JSON.toJSONString(didProperty);
    }

    public void createDid() throws Exception {
        String memo = didService.createMnemonic();
        String ret = didService.createDid(memo, 0);
        Map data = JSON.parseObject(ret, Map.class);
        didPrivateKey = (String) data.get("DidPrivateKey");
        did = (String) data.get("Did");
        didPublicKey = (String) data.get("DidPublicKey");
        System.out.println("DidService.createDid() - Successfully created a new DID");
    }

    public void signAndVerifyDidMessage() throws Exception {
        String sig = didService.signMessage(didPrivateKey, didPropertyKey);
        if (null == sig) {
            System.out.println("Err DidService.signAndVerifyDidMessage() - Signing failed.");
            return;
        } else {
            System.out.println("DidService.signAndVerifyDidMessage() - Successfully signed\nSig: " + sig);
        }

        boolean isVerify = didService.verifyMessage(didPublicKey, sig, didPropertyKey);
        if (!isVerify) {
            System.out.println("Err DidService.signAndVerifyDidMessage() - Verification failed");
            return;
        } else {
            System.out.println("DidService.signAndVerifyDidMessage() - Successfully verified Did Message");
        }
    }

    public String getPublicKey() throws Exception {
        String pubKey = didService.getDidPublicKey(didPrivateKey);
        if (null == pubKey) {
            System.out.println("Err didService.getDidPublicKey failed. result:");
            return "";
        }

        return pubKey;
    }

    public String getDid() throws Exception {
        String did1 = didService.getDidFromPrivateKey(didPrivateKey);
        if (null == did1) {
            System.out.println("Err didService.getDidFromPrivateKey failed. result:");
            return "";
        }
        String did2 = didService.getDidFromPublicKey(didPublicKey);
        if (null == did2) {
            System.out.println("Err didService.getDidFromPublicKey failed. result:");
            return "";
        }
        if (!did1.equals(did2)) {
            System.out.println("Err didService.getDid failed. not equal!");
        }
        return did1;
    }

    public String setDidProperty() throws Exception {
        String rawData = didService.packDidProperty(didPrivateKey, didPropertyKey, didPropertyValue);
        if (null == rawData) {
            System.out.println("Err didService.packDidProperty failed.");
            return "";
        }
        ReturnMsgEntity ret = didService.upChainByWallet(didNodeUrl, payPrivateKey, rawData);
        long status = ret.getStatus();
        if (status != RetCodeConfiguration.SUCC) {
            System.out.println("Err didService.upChainByWallet failed. result:" + JSON.toJSONString(ret.getResult()));
            return "";
        }

        String txId = (String) ret.getResult();
        return txId;
    }

    public String deleteDidProperty() throws Exception {
        String rawData = didService.packDelDidProperty(didPrivateKey, didPropertyKey);
        if (null == rawData) {
            System.out.println("Err didService.packDelDidProperty failed.");
            return "";
        }
        ReturnMsgEntity ret = didService.upChainByWallet(didNodeUrl, payPrivateKey, rawData);
        long status = ret.getStatus();
        if (status != RetCodeConfiguration.SUCC) {
            System.out.println("Err didService.packDelDidProperty failed. result:" + JSON.toJSONString(ret.getResult()));
            return "";
        }

        String txId = (String) ret.getResult();
        return txId;
    }

    public String deleteDid() throws Exception {
        String rawData = didService.packDestroyDid(didPrivateKey);
        if (null == rawData) {
            System.out.println("Err didService.packDestroyDid failed.");
            return "";
        }
        ReturnMsgEntity ret = didService.upChainByWallet(didNodeUrl, payPrivateKey, rawData);
        long status = ret.getStatus();
        if (status != RetCodeConfiguration.SUCC) {
            System.out.println("Err didService.packDestroyDid failed. result:" + JSON.toJSONString(ret.getResult()));
            return "";
        }
        String txId = (String) ret.getResult();
        return txId;
    }

    public String getDidPropertyByTxId(String txId) throws Exception {
        ReturnMsgEntity ret = didService.getDidPropertyByTxid(didNodeUrl, did, didPropertyKey, txId);
        long status = ret.getStatus();
        while (status != RetCodeConfiguration.SUCC) {
            System.out.println("Waiting 30 seconds more seconds until txId: " + txId + " is put onto the chain...");
            TimeUnit.SECONDS.sleep(30);
            ret = didService.getDidPropertyByTxid(didNodeUrl, did, didPropertyKey, txId);
            status = ret.getStatus();
        }
        String propertyJson = (String) ret.getResult();
        String property = JSONObject.parseObject(propertyJson).getString(didPropertyKey);
        return property;
    }

    public void getDidPropertyStatus(String txId) throws Exception {
        ReturnMsgEntity ret = didService.getDidPropertyByTxid(didNodeUrl, did, didPropertyKey, txId);
        long status = ret.getStatus();
        System.out.println("DidService.getDidPropertyStatus - Status: " + status + " result: " + ret.getResult());
    }

    public String getDidProperty(String txId) throws Exception {
        ReturnMsgEntity ret = didService.getDidPropertyByTxid(didNodeUrl, did, didPropertyKey, txId);
        long status = ret.getStatus();
        while (status != RetCodeConfiguration.SUCC) {
            System.out.println("Waiting 30 seconds more seconds until txId: " + txId + " is put onto the chain...");
            TimeUnit.SECONDS.sleep(30);
            ret = didService.getDidPropertyByTxid(didNodeUrl, did, didPropertyKey, txId);
            status = ret.getStatus();
        }
        String propertyJson = (String) ret.getResult();
        String property = JSONObject.parseObject(propertyJson).getString(didPropertyKey);
        return property;
    }

    public static void main(String[] args) throws Exception {
        ElaDidServiceApi sample = new ElaDidServiceApi();
        System.out.println("Creating a DID...");
        sample.createDid();

        System.out.printf("\nGetting Public Key...\n");
        String pubKey = sample.getPublicKey();
        System.out.println("Public Key: " + pubKey);

        System.out.printf("\nGetting DID...\n");
        String newDid = sample.getDid();
        System.out.println("DID: " + newDid);

        System.out.printf("\nSigning and Verifying DID message...\n");
        sample.signAndVerifyDidMessage();

        System.out.printf("\nSetting DID Property...\n");
        String setDidTxId = sample.setDidProperty();
        System.out.println("TxId: " + setDidTxId);

        System.out.printf("\nGetting DID Property by TxId...\n");
        String property = sample.getDidPropertyByTxId(setDidTxId);
        System.out.println("Property: " + property);

        System.out.printf("\nDeleting DID Property...\n");
        String delDidPropertyTxId = sample.deleteDidProperty();
        System.out.println("TxId: " + delDidPropertyTxId);

        //System.out.printf("\nDeleting DID...\n");
        //String delDidTxId = sample.deleteDid();
        //System.out.println("TxId: " + delDidTxId);
    }
}