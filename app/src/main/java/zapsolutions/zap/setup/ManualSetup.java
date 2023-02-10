package zapsolutions.zap.setup;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.appcompat.widget.SwitchCompat;

import zapsolutions.zap.HomeActivity;
import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.BaseAppCompatActivity;
import zapsolutions.zap.connection.BaseNodeConfig;
import zapsolutions.zap.connection.manageNodeConfigs.NodeConfigsManager;
import zapsolutions.zap.connection.manageNodeConfigs.ZapNodeConfig;
import zapsolutions.zap.connection.parseConnectionData.lndConnect.LndConnectConfig;
import zapsolutions.zap.nodesManagement.ManageNodesActivity;
import zapsolutions.zap.util.OnSingleClickListener;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.RefConstants;
import zapsolutions.zap.util.RemoteConnectUtil;
import zapsolutions.zap.util.TimeOutUtil;
import zapsolutions.zap.util.UserGuardian;
import zapsolutions.zap.util.UtilFunctions;
import zapsolutions.zap.util.Wallet;

public class ManualSetup extends BaseAppCompatActivity {

    private static final String LOG_TAG = ManualSetup.class.getName();

    private EditText mEtHost;
    private EditText mEtPort;
    private EditText mEtMacaroon;
    private EditText mEtCertificate;
    private SwitchCompat mSwTor;
    private SwitchCompat mSwVerify;
    private Button mBtnSave;
    private String mWalletUUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Receive data from last activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(ManageNodesActivity.NODE_ID)) {
                mWalletUUID = extras.getString(ManageNodesActivity.NODE_ID);
            }
        }

        setContentView(R.layout.activity_manual_setup);

        mEtHost = findViewById(R.id.hostEditText);
        mEtPort = findViewById(R.id.portEditText);
        mEtMacaroon = findViewById(R.id.macaroonEditText);
        mEtCertificate = findViewById(R.id.certificateEditText);
        mSwTor = findViewById(R.id.torSwitch);
        mSwVerify = findViewById(R.id.verifyCertSwitch);
        mBtnSave = findViewById(R.id.saveButton);

        // Fill in vales if existing wallet is edited
        if (mWalletUUID != null) {
            ZapNodeConfig zapNodeConfig = NodeConfigsManager.getInstance().getNodeConfigById(mWalletUUID);
            mEtHost.setText(zapNodeConfig.getHost());
            mEtPort.setText(String.valueOf(zapNodeConfig.getPort()));
            mEtMacaroon.setText(zapNodeConfig.getMacaroon());
            mSwTor.setChecked(zapNodeConfig.getUseTor());
            if (zapNodeConfig.getUseTor()) {
                mSwVerify.setChecked(false);
                mSwVerify.setVisibility(View.GONE);
            } else {
                mSwVerify.setChecked(zapNodeConfig.getVerifyCertificate());
            }
            if (zapNodeConfig.getCert() != null && !zapNodeConfig.getCert().isEmpty()) {
                mEtCertificate.setText(zapNodeConfig.getCert());
            }
        }

        mSwTor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mSwVerify.setChecked(false);
                    mSwVerify.setVisibility(View.GONE);
                } else {
                    mSwVerify.setChecked(true);
                    mSwVerify.setVisibility(View.VISIBLE);
                }
            }
        });

        mSwVerify.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!mSwVerify.isChecked()) {
                    // user wants to disable certificate verification
                    mSwVerify.setChecked(true);
                    new UserGuardian(ManualSetup.this, new UserGuardian.OnGuardianConfirmedListener() {
                        @Override
                        public void onGuardianConfirmed() {
                            mSwVerify.setChecked(false);
                        }
                    }).securityCertificateVerification();
                }
            }
        });

        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEtHost.getText().toString().isEmpty()) {
                    showError("Host must not be empty!", RefConstants.ERROR_DURATION_SHORT);
                    return;
                }
                if (mEtPort.getText().toString().isEmpty()) {
                    showError("Port must not be empty!", RefConstants.ERROR_DURATION_SHORT);
                    return;
                }
                if (mEtMacaroon.getText().toString().isEmpty()) {
                    showError("Macaroon must not be empty!", RefConstants.ERROR_DURATION_SHORT);
                    return;
                }
                if (!UtilFunctions.isHex(mEtMacaroon.getText().toString())) {
                    showError("Macaroon must be provided in hex format!", RefConstants.ERROR_DURATION_SHORT);
                    return;
                }

                // everything is ok
                LndConnectConfig lndConnectConfig = new LndConnectConfig();
                lndConnectConfig.setHost(mEtHost.getText().toString());
                lndConnectConfig.setPort(Integer.parseInt(mEtPort.getText().toString()));
                lndConnectConfig.setMacaroon(mEtMacaroon.getText().toString());
                lndConnectConfig.setUseTor(mSwTor.isChecked());
                lndConnectConfig.setVerifyCertificate(mSwVerify.isChecked());
                if (!mEtCertificate.getText().toString().isEmpty()) {
                    lndConnectConfig.setCert(mEtCertificate.getText().toString());
                }
                connect(lndConnectConfig);
            }
        });
    }

    private void connect(BaseNodeConfig baseNodeConfig) {
        // Connect using the supplied configuration
        RemoteConnectUtil.saveRemoteConfiguration(ManualSetup.this, baseNodeConfig, mWalletUUID, new RemoteConnectUtil.OnSaveRemoteConfigurationListener() {

            @Override
            public void onSaved(String id) {

                // The configuration was saved. Now make it the currently active wallet.
                PrefsUtil.editPrefs().putString(PrefsUtil.CURRENT_NODE_CONFIG, id).commit();

                // Do not ask for pin again...
                TimeOutUtil.getInstance().restartTimer();

                // In case another wallet was open before, we want to have all values reset.
                Wallet.getInstance().reset();

                // Show home screen, remove history stack. Going to HomeActivity will initiate the connection to our new remote configuration.
                Intent intent = new Intent(ManualSetup.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            @Override
            public void onAlreadyExists() {
                new AlertDialog.Builder(ManualSetup.this)
                        .setMessage(R.string.node_already_exists)
                        .setCancelable(true)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }).show();
            }

            @Override
            public void onError(String error, int duration) {
                showError(error, duration);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.scan_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        if (id == R.id.scanButton) {
            Intent intent = new Intent(ManualSetup.this, ConnectRemoteNodeActivity.class);
            intent.putExtra(ManageNodesActivity.NODE_ID, mWalletUUID);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}