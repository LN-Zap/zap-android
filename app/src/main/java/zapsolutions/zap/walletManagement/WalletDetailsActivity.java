package zapsolutions.zap.walletManagement;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import zapsolutions.zap.HomeActivity;
import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.BaseAppCompatActivity;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfig;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfigsManager;
import zapsolutions.zap.setup.ConnectRemoteNodeActivity;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.TimeOutUtil;
import zapsolutions.zap.util.Wallet;


public class WalletDetailsActivity extends BaseAppCompatActivity {


    private String mId;
    private InputMethodManager mInputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_details);

        mInputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

        // Receive data from last activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mId = extras.getString(ManageWalletsActivity.WALLET_ID);
        }

        // Wallet name
        TextView tvWalletName = findViewById(R.id.walletName);
        tvWalletName.setText(getWalletConfig().getAlias());

        // Wallet type
        ImageView ivTypeIcon = findViewById(R.id.walletTypeIcon);
        if (getWalletConfig().isLocal()) {
            ivTypeIcon.setImageResource(R.drawable.ic_local_black_24dp);
        } else {
            ivTypeIcon.setImageResource(R.drawable.ic_remote_black_24dp);
        }

        // Connection Data
        View vConnectionData = findViewById(R.id.connectionDataBox);
        if (getWalletConfig().isLocal()) {
            vConnectionData.setVisibility(View.GONE);
        } else {
            vConnectionData.setVisibility(View.VISIBLE);
            TextView tvHostLabel = findViewById(R.id.hostLabel);
            tvHostLabel.setText(getResources().getString(R.string.host) + ":");
            TextView tvHost = findViewById(R.id.host);
            tvHost.setText(getWalletConfig().getHost());
            TextView tvPortLabel = findViewById(R.id.portLabel);
            tvPortLabel.setText(getResources().getString(R.string.port) + ":");
            TextView tvPort = findViewById(R.id.port);
            tvPort.setText(String.valueOf(getWalletConfig().getPort()));
            TextView tvMacaroonLabel = findViewById(R.id.macaroonLabel);
            tvMacaroonLabel.setText(getResources().getString(R.string.macaroon) + ":");
            TextView tvMacaroon = findViewById(R.id.macaroon);
            tvMacaroon.setText(getWalletConfig().getMacaroon());
            TextView tvCertificateLabel = findViewById(R.id.certLabel);
            tvCertificateLabel.setText(getResources().getString(R.string.certificate) + ":");
            TextView tvCertificate = findViewById(R.id.cert);
            tvCertificate.setText(getWalletConfig().getCert());

            Button changeBtn = findViewById(R.id.buttonChangeConnection);
            changeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(WalletDetailsActivity.this, ConnectRemoteNodeActivity.class);
                    intent.putExtra(ManageWalletsActivity.WALLET_ID, mId);
                    startActivity(intent);
                }
            });
        }

        Button switchBtn = findViewById(R.id.buttonActivate);
        switchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PrefsUtil.edit().putString(PrefsUtil.CURRENT_WALLET_CONFIG, mId).commit();

                // Do not ask for pin again...
                TimeOutUtil.getInstance().restartTimer();

                Wallet.getInstance().reset();

                if (!getWalletConfig().isLocal()) {
                    // ToDo: Stop local LND if we switch to a remote wallet (right now it stops, but then it crashes when switching back to Local LND
                    openHome();
                } else {
                    openHome();
                }
            }
        });

        Button renameBtn = findViewById(R.id.buttonRename);
        renameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showWalletNameInput();
            }
        });

        Button deleteBtn = findViewById(R.id.buttonDelete);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PrefsUtil.getCurrentWalletConfig().equals(mId)) {
                    Toast.makeText(WalletDetailsActivity.this, "You cannot delete the currently active wallet.", Toast.LENGTH_LONG).show();
                } else {
                    deleteWallet();
                }
            }
        });
    }

    private void showWalletNameInput() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(R.string.wallet_name);
        adb.setCancelable(false);
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_input_text, null, false);

        final EditText input = viewInflated.findViewById(R.id.input);
        input.setShowSoftInputOnFocus(true);
        input.setText(getWalletConfig().getAlias());
        input.requestFocus();

        mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        adb.setView(viewInflated);

        adb.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (input.getText().toString().isEmpty()) {
                    Toast.makeText(WalletDetailsActivity.this, "An empty name is not allowed.", Toast.LENGTH_LONG).show();
                    mInputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    showWalletNameInput();
                } else {
                    WalletConfigsManager walletConfigsManager = WalletConfigsManager.getInstance();
                    walletConfigsManager.renameWalletConfig(WalletConfigsManager.getInstance().getWalletConfigById(mId), input.getText().toString());
                    try {
                        walletConfigsManager.apply();
                        TextView tvWalletName = findViewById(R.id.walletName);
                        tvWalletName.setText(input.getText().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    mInputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    dialog.dismiss();
                }
            }
        });
        adb.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                mInputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                dialog.cancel();
            }
        });

        adb.show();

    }

    private WalletConfig getWalletConfig() {
        return WalletConfigsManager.getInstance().getWalletConfigById(mId);
    }

    private void openHome() {
        // Open home and clear history
        Intent intent = new Intent(WalletDetailsActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void deleteWallet() {
        WalletConfigsManager walletConfigsManager = WalletConfigsManager.getInstance();
        walletConfigsManager.removeWalletConfig(WalletConfigsManager.getInstance().getWalletConfigById(mId));
        try {
            walletConfigsManager.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }

        finish();
    }
}
