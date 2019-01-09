package systems.v.coldwallet.Util;

import android.app.Activity;
import android.graphics.Bitmap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;

import systems.v.coldwallet.Activity.ScannerActivity;
import systems.v.coldwallet.Wallet.Account;
import systems.v.coldwallet.Wallet.Wallet;

public class QRCodeUtil {
    private static final String TAG = "Winston";

    public static Bitmap generateQRCode(String message, int width) {
        Bitmap qrCode;
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            qrCode = barcodeEncoder.encodeBitmap(message, BarcodeFormat.QR_CODE, width, width);
        }
        catch(Exception e){
            qrCode = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
        }
        return qrCode;
    }

    public static String generatePubKeyAddrStr(Account account) {

        String accountOpc = "account";
        HashMap<String, Object> accountJson = new HashMap<>();

        accountJson.put("protocol", Wallet.PROTOCOL);
        accountJson.put("api", Wallet.API_VERSION);
        accountJson.put("opc", accountOpc);
        accountJson.put("address",account.getAddress());
        accountJson.put("publicKey",account.getPubKey());

        try {
            return new ObjectMapper().writeValueAsString(accountJson);
        } catch (JsonProcessingException e) {
            // not expected to ever happen
            return null;
        }
    }

    public static String generateSeedStr(Wallet wallet) {
        String seedOpc = "seed";
        HashMap<String,Object>seedJson = new HashMap<>();

        seedJson.put("protocol",Wallet.PROTOCOL);
        seedJson.put("api",Wallet.API_VERSION);
        seedJson.put("opc",seedOpc);
        seedJson.put("seed",wallet.getSeed());

        try {
            return new ObjectMapper().writeValueAsString(seedJson);
        } catch (JsonProcessingException e) {
            //not expected to ever happen
            return null;
        }

    }

    public static Bitmap exportPubKeyAddr(Account account, int width){
        String message;
        message = generatePubKeyAddrStr(account);
        return generateQRCode(message, width);
    }

    public static Bitmap exportSeed(Wallet wallet, int width){
        String message;
        message = generateSeedStr(wallet);
        return generateQRCode(message, width);
    }

    public static int processQrContents(String qrContents) {
        if (qrContents == null) return 0;

        HashMap<String,Object>  map = JsonUtil.getJsonAsMap(qrContents);
        if (map != null) {
            String mapOpc = (String )map.get("opc");
            if (mapOpc!=null && mapOpc.equals("seed"))
            {
                return 2;
            }
            else return 1;
        }
        else return 3;
    }

    public static void scan(Activity activity){
        IntentIntegrator integrator = new IntentIntegrator(activity);
        integrator.setCaptureActivity(ScannerActivity.class);
        integrator.setBeepEnabled(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.initiateScan();
    }
}
