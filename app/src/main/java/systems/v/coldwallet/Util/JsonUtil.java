package systems.v.coldwallet.Util;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import com.google.gson.LongSerializationPolicy;

import org.json.JSONObject;

import systems.v.coldwallet.Activity.ColdWalletActivity;
import systems.v.coldwallet.Activity.ConfirmTxActivity;
import systems.v.coldwallet.Wallet.Account;
import systems.v.coldwallet.Wallet.Wallet;

public class JsonUtil {
    private final static String TAG = "Winston";

    @NonNull
    public static void checkTransferTx(Activity activity, HashMap<String, Object> jsonMap,
                                       ArrayList<Account> accounts) {
        String senderPublicKey, recipient, attachment, assetId, feeAssetId;
        long amount, fee, timestamp;
        String[] keys = {"senderPublicKey", "recipient", "attachment",
                "assetId", "feeAssetId", "amount", "fee", "timestamp"};
        Account senderAcc = null;

        if (JsonUtil.containsKeys(jsonMap, keys)){
            senderPublicKey = (String) jsonMap.get("senderPublicKey");
            recipient = (String) jsonMap.get("recipient");
            attachment = (String) jsonMap.get("attachment");
            assetId = (String) jsonMap.get("assetId");
            feeAssetId = (String) jsonMap.get("feeAssetId");
            amount = Long.valueOf((String) jsonMap.get("amount")).longValue();
            fee = Double.valueOf((double)jsonMap.get("fee")).longValue();
            timestamp = Double.valueOf((double)jsonMap.get("timestamp")).longValue();

            for(Account account:accounts){
                if(account.isAccount(senderPublicKey)){
                    // Log.d(TAG, "Private key: " + account.getPriKey());
                    senderAcc = account;
                }
            }

            if (senderAcc != null) {
                Gson gson = new Gson();
                Wallet wallet = ((ColdWalletActivity) activity).getWallet();
                String walletStr = gson.toJson(wallet);
                Intent intent = new Intent(activity, ConfirmTxActivity.class);
                intent.putExtra("ACTION", "TRANSFER");
                intent.putExtra("WALLET", walletStr);
                intent.putExtra("SENDER", gson.toJson(senderAcc));
                intent.putExtra("RECIPIENT", recipient);
                intent.putExtra("AMOUNT", amount);
                intent.putExtra("ASSET_ID", assetId);
                intent.putExtra("FEE", fee);
                intent.putExtra("FEE_ASSET_ID", feeAssetId);
                intent.putExtra("ATTACHMENT", attachment);
                intent.putExtra("TIMESTAMP", timestamp);

                activity.startActivity(intent);
            }
            else {
                UIUtil.createNonexistentSenderDialog(activity);
                //Toast.makeText(activity, "Wallet does not contain sender", Toast.LENGTH_LONG).show();
                Log.d(TAG,"Private key cannot be found");
            }
        }
        else {
            UIUtil.createUpdateAppDialog(activity);
            //Toast.makeText(activity, "Invalid transaction format", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Map does not contain all keys");
        }
    }

    @NonNull
    public static void checkPaymentTx(Activity activity, HashMap<String, Object> jsonMap,
                                      ArrayList<Account> accounts) {
        String senderPublicKey, recipient, attachment, op_code, protocol;
        int api_version;
        long amount, fee, timestamp;
        short feeScale;
        String[] keys = {"senderPublicKey", "recipient", "amount", "fee", "feeScale", "timestamp"};
        Account senderAcc = null;

        if (JsonUtil.containsKeys(jsonMap, keys)){
            protocol = (String) jsonMap.get("protocol");
            api_version = Double.valueOf((double)jsonMap.get("api")).intValue();
            op_code = (String) jsonMap.get("opc");
            senderPublicKey = (String) jsonMap.get("senderPublicKey");
            recipient = (String) jsonMap.get("recipient");
            amount = Long.valueOf((String)jsonMap.get("amount")).longValue();
            attachment = (String) jsonMap.get("attachment");
            fee = Double.valueOf((double)jsonMap.get("fee")).longValue();
            feeScale = Double.valueOf((double)jsonMap.get("feeScale")).shortValue();
            timestamp = Double.valueOf((double)jsonMap.get("timestamp")).longValue();
            for(Account account:accounts){
                if(account.isAccount(senderPublicKey)){
                    // Log.d(TAG, "Private key: " + account.getPriKey());
                    senderAcc = account;
                }
            }

            if (senderAcc != null) {
                Gson gson = new Gson();
                Wallet wallet = ((ColdWalletActivity) activity).getWallet();
                String walletStr = gson.toJson(wallet);
                Intent intent = new Intent(activity, ConfirmTxActivity.class);
                intent.putExtra("PROTOCOL", protocol);
                intent.putExtra("API", api_version);
                intent.putExtra("OPC", op_code);
                intent.putExtra("ACTION", "PAYMENT");
                intent.putExtra("WALLET", walletStr);
                intent.putExtra("SENDER", gson.toJson(senderAcc));
                intent.putExtra("RECIPIENT", recipient);
                intent.putExtra("AMOUNT", amount);
                intent.putExtra("FEE", fee);
                intent.putExtra("FEESCALE", feeScale);
                intent.putExtra("ATTACHMENT", attachment);
                intent.putExtra("TIMESTAMP", timestamp);

                activity.startActivity(intent);
            }
            else {
                UIUtil.createNonexistentSenderDialog(activity);
                //Toast.makeText(activity, "Wallet does not contain sender", Toast.LENGTH_LONG).show();
                Log.d(TAG,"Private key cannot be found");
            }
        }
        else {
            UIUtil.createUpdateAppDialog(activity);
            //Toast.makeText(activity, "Invalid transaction format", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Map does not contain all keys");
        }
    }

    @NonNull
    public static void checkLeaseTx(Activity activity, HashMap<String, Object> jsonMap,
                                    ArrayList<Account> accounts) {
        String senderPublicKey, recipient, op_code, protocol;
        int api_version;
        long amount, fee, timestamp;
        short feeScale;
        String[] keys = {"senderPublicKey", "recipient", "amount", "fee", "feeScale", "timestamp"};
        Account senderAcc = null;

        if (JsonUtil.containsKeys(jsonMap, keys)){
            protocol = (String) jsonMap.get("protocol");
            api_version = Double.valueOf((double)jsonMap.get("api")).intValue();
            op_code = (String) jsonMap.get("opc");

            senderPublicKey = (String) jsonMap.get("senderPublicKey");
            recipient = (String) jsonMap.get("recipient");
            amount = Long.valueOf((String)jsonMap.get("amount")).longValue();
            fee = Double.valueOf((double)jsonMap.get("fee")).longValue();
            feeScale = Double.valueOf((double)jsonMap.get("feeScale")).shortValue();
            timestamp = Double.valueOf((double)jsonMap.get("timestamp")).longValue();

            for(Account account:accounts){
                if(account.isAccount(senderPublicKey)){
                    // Log.d(TAG, "Private key: " + account.getPriKey());
                    senderAcc = account;
                }
            }

            if (senderAcc != null) {
                Gson gson = new Gson();
                Wallet wallet = ((ColdWalletActivity) activity).getWallet();
                String walletStr = gson.toJson(wallet);
                Intent intent = new Intent(activity, ConfirmTxActivity.class);
                intent.putExtra("PROTOCOL", protocol);
                intent.putExtra("API", api_version);
                intent.putExtra("OPC", op_code);
                intent.putExtra("ACTION", "LEASE");
                intent.putExtra("WALLET", walletStr);
                intent.putExtra("SENDER", gson.toJson(senderAcc));
                intent.putExtra("RECIPIENT", recipient);
                intent.putExtra("AMOUNT", amount);
                intent.putExtra("FEE", fee);
                intent.putExtra("FEESCALE", feeScale);
                intent.putExtra("TIMESTAMP", timestamp);

                activity.startActivity(intent);
            }
            else {
                UIUtil.createNonexistentSenderDialog(activity);
                //Toast.makeText(activity, "Wallet does not contain sender", Toast.LENGTH_LONG).show();
                Log.d(TAG,"Private key cannot be found");
            }
        }
        else {
            UIUtil.createUpdateAppDialog(activity);
            //Toast.makeText(activity, "Invalid transaction format", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Map does not contain all keys");
        }
    }

    @NonNull
    public static void checkCancelLeaseTx(Activity activity, HashMap<String, Object> jsonMap,
                                          ArrayList<Account> accounts) {
        String senderPublicKey, txId, op_code, protocol;
        int api_version;
        long fee, timestamp;
        short feeScale;
        String[] keys = {"senderPublicKey", "txId", "fee", "feeScale", "timestamp"};
        Account senderAcc = null;

        if (JsonUtil.containsKeys(jsonMap, keys)){
            protocol = (String) jsonMap.get("protocol");
            api_version = Double.valueOf((double)jsonMap.get("api")).intValue();
            op_code = (String) jsonMap.get("opc");

            senderPublicKey = (String) jsonMap.get("senderPublicKey");
            txId = (String) jsonMap.get("txId");
            fee = Double.valueOf((double)jsonMap.get("fee")).longValue();
            feeScale = Double.valueOf((double)jsonMap.get("feeScale")).shortValue();
            timestamp = Double.valueOf((double)jsonMap.get("timestamp")).longValue();

            for(Account account:accounts){
                if(account.isAccount(senderPublicKey)){
                    // Log.d(TAG, "Private key: " + account.getPriKey());
                    senderAcc = account;
                }
            }

            if (senderAcc != null) {
                Gson gson = new Gson();
                Wallet wallet = ((ColdWalletActivity) activity).getWallet();
                String walletStr = gson.toJson(wallet);
                Intent intent = new Intent(activity, ConfirmTxActivity.class);
                intent.putExtra("PROTOCOL", protocol);
                intent.putExtra("API", api_version);
                intent.putExtra("OPC", op_code);
                intent.putExtra("ACTION", "CANCEL_LEASE");
                intent.putExtra("WALLET", walletStr);
                intent.putExtra("SENDER", gson.toJson(senderAcc));
                intent.putExtra("TX_ID", txId);
                intent.putExtra("FEE", fee);
                intent.putExtra("FEESCALE", feeScale);
                intent.putExtra("TIMESTAMP", timestamp);

                activity.startActivity(intent);
            }
            else {
                UIUtil.createNonexistentSenderDialog(activity);
                //Toast.makeText(activity, "Wallet does not contain sender", Toast.LENGTH_LONG).show();
                Log.d(TAG,"Private key cannot be found");
            }
        }
        else {
            UIUtil.createUpdateAppDialog(activity);
            //Toast.makeText(activity, "Invalid transaction format", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Map does not contain all keys");
        }
    }

    @NonNull
    public static void checkExecContractTx(Activity activity, HashMap<String, Object> jsonMap,
                                    ArrayList<Account> accounts) {
        String address, attachment,contractId,function,functionTextual, op_code, protocol,functionExplain;
        int api_version;
        long  fee, timestamp;
        short feeScale,functionId;
        String[] keys = {"address","function", "fee", "feeScale", "timestamp"};
        Account senderAcc = null;

        if (JsonUtil.containsKeys(jsonMap, keys)){
            protocol = (String) jsonMap.get("protocol");
            api_version = Double.valueOf((double)jsonMap.get("api")).intValue();
            op_code = (String) jsonMap.get("opc");

            address = (String)jsonMap.get("address");
            Log.d(TAG, address);
            attachment = (String)jsonMap.get("attachment");
            contractId = (String )jsonMap.get("contractId");
            function = (String )jsonMap.get("function");
            functionTextual = (String)jsonMap.get("functionTextual");
            functionExplain = (String)jsonMap.get("functionExplain");

            functionId  = Double.valueOf((double)jsonMap.get("functionId")).shortValue();
            fee = Double.valueOf((double)jsonMap.get("fee")).longValue();
            feeScale = Double.valueOf((double)jsonMap.get("feeScale")).shortValue();
            timestamp = Double.valueOf((double)jsonMap.get("timestamp")).longValue();

            for(Account account:accounts){
                if(account.isAccountByAddress(address)){
                    senderAcc = account;
                }
            }

            if (senderAcc != null) {
                Gson gson = new Gson();
                Wallet wallet = ((ColdWalletActivity) activity).getWallet();
                String walletStr = gson.toJson(wallet);
                Intent intent = new Intent(activity, ConfirmTxActivity.class);
                intent.putExtra("PROTOCOL", protocol);
                intent.putExtra("API", api_version);
                intent.putExtra("OPC", op_code);
                intent.putExtra("ACTION", "EXEC_CONTRACT");
                intent.putExtra("WALLET", walletStr);
                intent.putExtra("SENDER", gson.toJson(senderAcc));
                intent.putExtra("FEE", fee);
                intent.putExtra("FEESCALE", feeScale);
                intent.putExtra("TIMESTAMP", timestamp);
                intent.putExtra("ATTACHMENT", attachment);
                intent.putExtra("FUNCTION", function);
                intent.putExtra("FUNCTIONID", functionId);
                intent.putExtra("FUNCTIONTEXTUAL", functionTextual);
                intent.putExtra("FUNCTIONEXPLAIN",functionExplain);
                intent.putExtra("CONTRACTID", contractId);

                activity.startActivity(intent);
            }
            else {
                UIUtil.createNonexistentSenderDialog(activity);
                //Toast.makeText(activity, "Wallet does not contain sender", Toast.LENGTH_LONG).show();
                Log.d(TAG,"Private key cannot be found");
            }
        }
        else {
            UIUtil.createUpdateAppDialog(activity);
            //Toast.makeText(activity, "Invalid transaction format", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Map does not contain all keys");
        }
    }

    public static HashMap<String,Object> getJsonAsMap(String str){
        if (isJsonString(str)){
            try{
                Gson gsonBuilder = new GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.STRING).create();
                str = str.replaceAll("\"amount\":(\\d+)", "\"amount\":\"$1\"");
                HashMap<String,Object> gson =  gsonBuilder.fromJson(str, new TypeToken<HashMap<String, Object>>(){}.getType());
                Log.d(TAG, gson.toString());
                return gson;
            }
            catch(Exception e){
                return null;
            }
        }
        return null;
    }

    public static boolean containsKeys(HashMap<String,Object> jsonMap, String[] keys){
        for (String key:keys){
            if (!jsonMap.containsKey(key)) return false;
        }
        return true;
    }

    public static boolean isJsonString(String str){
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(str);
            return true;
        } catch (IOException e) {
            Log.d(TAG,"e.message" + e.getMessage());
            return false;
        }
    }
}
