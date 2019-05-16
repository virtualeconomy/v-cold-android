package systems.v.coldwallet.Activity;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.Util;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.HashMap;

import systems.v.coldwallet.R;
import systems.v.coldwallet.Util.Base58;
import systems.v.coldwallet.Util.JsonUtil;
import systems.v.coldwallet.Util.QRCodeUtil;
import systems.v.coldwallet.Util.UIUtil;
import systems.v.coldwallet.Wallet.Account;
import systems.v.coldwallet.Wallet.Transaction;
import systems.v.coldwallet.Wallet.Wallet;
import android.util.Log;

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

        Intent intent = getIntent();
        final String content = intent.getStringExtra("content");
        Log.d(TAG,"transfer to scan ?" + content);
        // String curPage = content.substring(4,5);
        // String totalPage = content.substring(6,7);
        // String checkSum = content.substring(8,15);


        int cur = 4;
        // int cur = intent.getStringExtra("content");
        int total = 10;
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
                Log.d(TAG,"back button ");
                Intent intent = new Intent(activity, ColdWalletActivity.class);
                activity.startActivity(intent);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        qrContents = result.getContents();

        if(result != null) {
            if (QRCodeUtil.processQrContents(qrContents) == 10) {
                    HashMap<String,Object> seedmap = new HashMap<>();
                    seedmap = JsonUtil.getJsonAsMap(qrContents);
                    Object seedObject = seedmap.get("seed");
                    String seed = seedObject.toString();

                    String seedmapProtocol = (String)seedmap.get("protocol");

                    byte seedmapApi = Double.valueOf((double)seedmap.get("api")).byteValue();

                    if(Wallet.PROTOCOL.equals(seedmapProtocol) && seedmapApi <= Wallet.API_VERSION) {
                        if (Wallet.validateSeedPhrase(activity, seed)) {
                            Intent intent = new Intent(activity, SetPasswordActivity.class);
                            intent.putExtra("SEED", seed);
                            startActivity(intent);
                        }
                        else {
                            UIUtil.createForeignSeedDialog(activity, seed);

                        }
                    }
                    else {
                        Toast.makeText(activity, "Incorrect QR code format or too old version", Toast.LENGTH_LONG).show();
                    }
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
