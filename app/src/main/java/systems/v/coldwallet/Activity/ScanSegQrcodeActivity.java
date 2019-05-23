package systems.v.coldwallet.Activity;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.Util;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.HashMap;

import systems.v.coldwallet.R;
import systems.v.coldwallet.Util.Base58;
import systems.v.coldwallet.Util.FileUtil;
import systems.v.coldwallet.Util.HashUtil;
import systems.v.coldwallet.Util.JsonUtil;
import systems.v.coldwallet.Util.QRCodeUtil;
import systems.v.coldwallet.Util.SegQrcode;
import systems.v.coldwallet.Util.UIUtil;
import systems.v.coldwallet.Wallet.Account;
import systems.v.coldwallet.Wallet.Chain;
import systems.v.coldwallet.Wallet.Transaction;
import systems.v.coldwallet.Wallet.Wallet;
import android.util.Log;
import systems.v.coldwallet.Activity.ColdWalletActivity;

import org.bouncycastle.jcajce.provider.digest.SHA256;

public class ScanSegQrcodeActivity extends AppCompatActivity {
    private static final String TAG = "Winston";

    private ScanSegQrcodeActivity activity;
    private ActionBar actionBar;

    private TextView curPage;
    private TextView totalPage;
    // private ImageView scan;
    private Button confirm;
    private Button back;

    private String qrContents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seg_qrcode);

        activity = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.custom_toolbar);
        setSupportActionBar(toolbar);

        Drawable icon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_import);
        icon.mutate();
        icon.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);

        actionBar = getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setLogo(icon);
        actionBar.setTitle(R.string.title_seg_qrcode);

        curPage = findViewById(R.id.qrcode_cur_page);
        totalPage = findViewById(R.id.qrcode_total_page);
        confirm = findViewById(R.id.qrcode_continue_button);
        back = findViewById(R.id.qrcode_back_button);

        SegQrcode segQrcode;
        segQrcode = (SegQrcode) getApplication();

        int cur = segQrcode.getCurPage();
        int total = segQrcode.getTotalPage();
        curPage.setText(String.valueOf(cur));
        totalPage.setText(String.valueOf(total));

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QRCodeUtil.scan(activity);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        qrContents = result.getContents();

        if(result != null) {
            if (QRCodeUtil.processQrContents(qrContents) == 10) {

                    SegQrcode qrcode;
                    qrcode = (SegQrcode) getApplication();

                    int tmpCur = qrcode.getCurPage();
                    int tmpTotal = qrcode.getTotalPage();
                    String tmpCheck = qrcode.getCheckSum();
                    String tmpBody = qrcode.getBody();

                    int cur = Integer.valueOf((String) qrContents.substring(4,5)).intValue();
                    int total = Integer.valueOf((String) qrContents.substring(6,7)).intValue();
                    String check = qrContents.substring(8,16);
                    String body = qrContents.substring(17);

                    String totalBody = tmpBody + body;

                    if (!tmpCheck.equals(check) || tmpCur!= cur -1)
                    {
                        Toast.makeText(activity, "Incorrect QR code, scan again ", Toast.LENGTH_LONG).show();
                        // activity = this;
                        // activity.finish();
                    }
                    else if (cur == total)
                    {
                        Log.d(TAG, "the total body " + totalBody);
                        byte[] bodyBytes = (totalBody == null ? "" : totalBody).getBytes();
                        byte[] hashByte = HashUtil.hash(bodyBytes, 0, bodyBytes.length, HashUtil.SHA256);

                        byte[] arr = hashByte;
                        StringBuilder bufString = new StringBuilder();

                        for (int i = 0; i < 4; ++i)
                        {
                            byte b = arr[i];
                            bufString.append(String.format("%02X ", b));
                        }
                        String buffer = bufString.toString();
                        buffer = buffer.replaceAll(" ", "");
                        buffer = buffer.toLowerCase();

                        if (buffer.toString().equals(check))
                        {
                            HashMap<String, Object> jsonMap = JsonUtil.getJsonAsMap(totalBody);
                            // JsonUtil.isJsonString(totalBody);
                            Log.d(TAG,"createJsonMap" + JsonUtil.isJsonString(totalBody) + totalBody);
                            String address, attachment,contract, op_code, protocol,description, contractInit,contractInitTextual,contractInitExplain,
                            contractId, function, functionTextual, functionExplain;
                            int api_version;
                            long  fee, timestamp;
                            short feeScale,functionId;
                            String[] registerKeys = {"address", "fee", "feeScale", "timestamp","contract"};
                            String[] execKeys = {"address","fee","feeScale","timestamp", "contractId"};
                            Account senderAcc = null;
                            ArrayList<Account> accounts = qrcode.getAccounts();

                            if (JsonUtil.containsKeys(jsonMap, registerKeys))
                            {

                                protocol = (String) jsonMap.get("protocol");
                                api_version = Double.valueOf((double) jsonMap.get("api")).intValue();
                                op_code = (String) jsonMap.get("opc");

                                address = (String) jsonMap.get("address");
                                fee = Double.valueOf((double) jsonMap.get("fee")).longValue();
                                feeScale = Double.valueOf((double) jsonMap.get("feeScale")).shortValue();
                                timestamp = Double.valueOf((double) jsonMap.get("timestamp")).longValue();
                                contract = (String) jsonMap.get("contract");

                                description = (String) jsonMap.get("description");
                                contractInit = (String) jsonMap.get("contractInit");
                                contractInitTextual = (String) jsonMap.get("contractInitTextual");
                                contractInitExplain = (String) jsonMap.get("contractInitExplain");

                                for(Account account:accounts){
                                    if(account.isAccountByAddress(address)){
                                        senderAcc = account;
                                        Log.d(TAG,"account ++++++" + account);
                                    }
                                }

                                if (senderAcc != null) {
                                    Gson gson = new Gson();
                                    // Wallet wallet = ((ColdWalletActivity) activity).getWallet();
                                    // String walletStr = gson.toJson(wallet);
                                    Intent intent = new Intent(activity, ConfirmTxActivity.class);
                                    intent.putExtra("PROTOCOL", protocol);
                                    intent.putExtra("API", api_version);
                                    intent.putExtra("OPC", op_code);
                                    intent.putExtra("ACTION", "CREATE_CONTRACT");
                                    // intent.putExtra("WALLET", walletStr);
                                    intent.putExtra("SENDER", gson.toJson(senderAcc));
                                    intent.putExtra("FEE", fee);
                                    intent.putExtra("FEESCALE", feeScale);
                                    intent.putExtra("TIMESTAMP", timestamp);
                                    intent.putExtra("DESCRIPTION", description);
                                    intent.putExtra("CONTRACT", contract);
                                    intent.putExtra("CONTRACTINIT", contractInit);
                                    intent.putExtra("CONTRACTINITTEXTUAL", contractInitTextual);
                                    intent.putExtra("CONTRACTINITEXPLAIN", contractInitExplain);

                                    activity.startActivity(intent);
                                }
                                else
                                {
                                    activity.finish();
                                    Toast.makeText(activity, "Wallet does not contain sender", Toast.LENGTH_LONG).show();
                                }
                            }
                            else if (JsonUtil.containsKeys(jsonMap, execKeys))
                            {
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
                                    // Wallet wallet = ((ColdWalletActivity) activity).getWallet();
                                    // String walletStr = gson.toJson(wallet);
                                    Intent intent = new Intent(activity, ConfirmTxActivity.class);
                                    intent.putExtra("PROTOCOL", protocol);
                                    intent.putExtra("API", api_version);
                                    intent.putExtra("OPC", op_code);
                                    intent.putExtra("ACTION", "EXEC_CONTRACT");
                                    // intent.putExtra("WALLET", walletStr);
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
                                else
                                {
                                    activity.finish();
                                    Toast.makeText(activity, "Wallet does not contain sender", Toast.LENGTH_LONG).show();
                                }

                            }
                            else
                            {
                                activity.finish();
                                Toast.makeText(activity, "Invalid transaction format", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    else
                    {
                        qrcode.setCurPage(cur);
                        qrcode.setTotalPage(total);
                        qrcode.setCheckSum(check);
                        qrcode.setBody(totalBody);
                        Intent intent = new Intent(activity, ScanSegQrcodeActivity.class);
                        activity.startActivity(intent);
                        activity.finish();
                    }
            }
            else
            {
                Toast.makeText(activity, "Incorrect QR code", Toast.LENGTH_LONG).show();
                activity.finish();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
