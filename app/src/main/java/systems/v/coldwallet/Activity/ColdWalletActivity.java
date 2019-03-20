package systems.v.coldwallet.Activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import systems.v.coldwallet.Util.FileUtil;
import systems.v.coldwallet.Util.NetworkUtil;
import systems.v.coldwallet.Util.PermissionUtil;
import systems.v.coldwallet.Util.UIUtil;
import systems.v.coldwallet.Wallet.Account;
import systems.v.coldwallet.Wallet.Chain;
import systems.v.coldwallet.Wallet.Transaction;
import systems.v.coldwallet.R;
import systems.v.coldwallet.Fragment.SettingsFragment;
import systems.v.coldwallet.Fragment.WalletFragment;
import systems.v.coldwallet.Util.JsonUtil;
import systems.v.coldwallet.Util.QRCodeUtil;
import systems.v.coldwallet.Wallet.Wallet;

public class ColdWalletActivity extends AppCompatActivity {
    private static final String TAG = "Winston";
    private static final String WALLET_FILE_NAME = "wallet.dat";

    private ActionBar actionBar;
    private ColdWalletActivity activity;

    private WalletFragment walletFrag;
    private SettingsFragment settingsFrag;
    private FragmentManager fragmentManager;

    private String qrContents;

    private Wallet wallet;
    private File walletFile;
    private String walletFilePath;
    private ArrayList<Account> accounts;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.custom_toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setLogo(R.drawable.ic_navigation_wallet);
        actionBar.setTitle(R.string.title_wallet);

        walletFrag = new WalletFragment();
        settingsFrag = new SettingsFragment();
        fragmentManager = null;

        walletFilePath = activity.getFilesDir().getPath() + "/" + WALLET_FILE_NAME;
        Log.d(TAG, "Wallet file path: " + walletFilePath);

        walletFile = new File(walletFilePath);
        password = "";

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        PermissionUtil.checkPermissions(activity);

        //Toast.makeText(activity, "Backup State: " + settingsFrag.getBackupState(), Toast.LENGTH_SHORT).show();
        String walletStr = getIntent().getStringExtra("WALLET");
        boolean wifi = false, bluetooth, data = false;
        NetworkUtil.NetworkType type;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean monitorState = preferences.getBoolean("settings_connectivity", true);

        if(walletStr != null) {
            Gson gson = new Gson();
            wallet = gson.fromJson(walletStr, Wallet.class);
            accounts = wallet.generateAccounts();
            switchToFragment(walletFrag);
        }
        else if (walletFile.exists()){
            if ((!NetworkUtil.bluetoothIsConnected() && !NetworkUtil.isConnected(activity)) || !monitorState) {
                UIUtil.createRequestPasswordDialog(activity);
            }
            else {
                bluetooth = NetworkUtil.bluetoothIsConnected();
                type = NetworkUtil.isConnectedType(activity);

                switch (type) {
                    case NoConnect:
                        break;

                    case Wifi:
                        wifi = true;
                        break;

                    case Mobile:
                        data = true;
                }

                UIUtil.createMonitorConnectivityDialog(activity, wifi, data, bluetooth);
            }
        }
        else {
            switchToFragment(walletFrag);
        }
    }

    public void onPause() {
        activity.unregisterReceiver(receiver);
        //activity.unregisterReceiver(networkReceiver);
        super.onPause();
    }

    public void onResume() {
        activity.registerReceiver(receiver, new IntentFilter("SELECT_APPEND_ACCOUNT_NUMBER"));
        activity.registerReceiver(receiver, new IntentFilter("CONFIRM_PASSWORD"));
        //activity.registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        //activity.registerReceiver(networkReceiver,new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        super.onResume();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_wallet:
                    actionBar.setLogo(R.drawable.ic_navigation_wallet);
                    actionBar.setTitle(R.string.title_wallet);
                    switchToFragment(walletFrag);
                    return true;

                case R.id.navigation_settings:
                    actionBar.setLogo(R.drawable.ic_navigation_settings);
                    actionBar.setTitle(R.string.title_settings);
                    switchToFragment(settingsFrag);
                    return true;
            }
            return false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_bar, menu);

        Drawable icon = menu.getItem(0).getIcon();
        icon.mutate();
        icon.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.scan:
                QRCodeUtil.scan(activity);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PermissionUtil.PERMISSION_REQUEST_CODE) {
            if (!PermissionUtil.permissionGranted(this)) {
                Toast.makeText(activity, "Please grant all permissions", Toast.LENGTH_LONG).show();
                finish();
            }
            else {
                UIUtil.createFirstRunWarningDialog(activity);
            }
        }
    }

    /**
     * Contain logic for decoding the results of a qr code
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        qrContents = result.getContents();
        int val = QRCodeUtil.processQrContents(qrContents);
        if (wallet != null && (val != 1 && val != 0 )) { val = 4; }

        if(result != null) {
            switch (val) {
                case 0:
                    Toast.makeText(activity, "Cancelled", Toast.LENGTH_LONG).show();
                    break;

                case 1:
                    HashMap<String, Object> jsonMap = JsonUtil.getJsonAsMap(qrContents);
                    byte txType = -1;

                    if (jsonMap.containsKey("api")) {
                        byte api = Double.valueOf((double)jsonMap.get("api")).byteValue();
                        if (api > Wallet.API_VERSION) {
                            UIUtil.createUpdateAppDialog(activity);
                            break;
                        }
                    }

                    if (jsonMap.containsKey("transactionType")) {
                        txType = Double.valueOf((double)jsonMap.get("transactionType")).byteValue();
                    }

                    if (accounts == null) {
                        txType = -1;
                        Toast.makeText(activity, "No wallet found", Toast.LENGTH_LONG).show();
                    }

                    if (txType != 2 && txType != 3 && txType != 4) {
                        Toast.makeText(activity, "Incorrect transaction format", Toast.LENGTH_LONG).show();
                    }

                    switch (txType) {
                        case 2: JsonUtil.checkPaymentTx(activity, jsonMap, accounts);
                                break;
                        case 3: JsonUtil.checkLeaseTx(activity, jsonMap, accounts);
                                break;
                        case 4: JsonUtil.checkCancelLeaseTx(activity, jsonMap, accounts);
                    }
                    break;

                default:
                    try {
                        Gson gson = new Gson();
                        HashMap<String,Object> gsonMap =  gson.fromJson(qrContents, new TypeToken<HashMap<String, Object>>(){}.getType());
                        if (gsonMap.containsKey("api")) {
                            byte api = Double.valueOf((double) gsonMap.get("api")).byteValue();
                            if (api > Wallet.API_VERSION) {
                                UIUtil.createUpdateAppDialog(activity);
                                break;
                            }
                        } else {
                            Toast.makeText(activity, "Incorrect transaction format", Toast.LENGTH_LONG).show();
                        }
                    } catch(Exception e){
                        Toast.makeText(activity, "Invalid QrCode", Toast.LENGTH_LONG).show();
                    }
                    //UIUtil.createWrongTransactionDialog(activity);

            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
        accounts = wallet.generateAccounts();
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    /**
     * Used to switch fragments when icon on bottom navigation menu is clicked
     * @param fragment
     */
    private void switchToFragment(Fragment fragment){
        if (fragment != null) {
            fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_container,fragment).commit();
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == "SELECT_APPEND_ACCOUNT_NUMBER") {
                int accountNum = intent.getIntExtra("ACCOUNT_NUMBER", 1);

                //Toast.makeText(activity, "Seed: " + seed
                //        + "\nAccount Number " + accountNum, Toast.LENGTH_LONG).show();

                wallet.append(accountNum);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                boolean monitorState = preferences.getBoolean("settings_auto_backup", true);

                if (monitorState) {
                    FileUtil.save(activity, wallet.getJson(), password, walletFilePath, WALLET_FILE_NAME);
                }
                else {
                    FileUtil.save(wallet.getJson(), password, walletFilePath);
                }

                Log.d(TAG, wallet.getJson());
                intent = new Intent(activity, ColdWalletActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            else if (intent.getAction() == "CONFIRM_PASSWORD") {
                password = intent.getStringExtra("PASSWORD");
                Log.d(TAG, "Password " + password);

                String seed = FileUtil.load(password, walletFilePath);
                if (seed != "" && seed != FileUtil.ERROR) {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    byte chainId = Chain.getChainId(preferences.getString("settings_network", "M"));
                    wallet = new Wallet(chainId, seed);
                    accounts = wallet.generateAccounts();
                    if (accounts == null) {Log.d(TAG, "Accounts null"); }
                    switchToFragment(walletFrag);
                }
                else {
                    UIUtil.createRequestPasswordDialog(activity);
                    Toast.makeText(activity, "Incorrect password", Toast.LENGTH_LONG).show();
                }
            }
        }
    };
}

