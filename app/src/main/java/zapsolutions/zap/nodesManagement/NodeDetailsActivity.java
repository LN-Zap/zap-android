package zapsolutions.zap.nodesManagement;


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
import zapsolutions.zap.LandingActivity;
import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.BaseAppCompatActivity;
import zapsolutions.zap.connection.lndConnection.LndConnection;
import zapsolutions.zap.connection.manageNodeConfigs.ZapNodeConfig;
import zapsolutions.zap.connection.manageNodeConfigs.NodeConfigsManager;
import zapsolutions.zap.setup.ManualSetup;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.TimeOutUtil;
import zapsolutions.zap.util.Wallet;


public class NodeDetailsActivity extends BaseAppCompatActivity {


    private String mId;
    private InputMethodManager mInputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node_details);

        mInputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

        // Receive data from last activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mId = extras.getString(ManageNodesActivity.NODE_ID);
        }

        // Wallet name
        TextView tvWalletName = findViewById(R.id.nodeName);
        tvWalletName.setText(getWalletConfig().getAlias());

        // Wallet type
        ImageView ivTypeIcon = findViewById(R.id.nodeTypeIcon);
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
            if (getWalletConfig().getCert() != null) {
                tvCertificateLabel.setText(getResources().getString(R.string.certificate) + ":");
                tvCertificateLabel.setVisibility(View.VISIBLE);
                TextView tvCertificate = findViewById(R.id.cert);
                tvCertificate.setVisibility(View.VISIBLE);
                tvCertificate.setText(getWalletConfig().getCert());
            } else {
                tvCertificateLabel.setVisibility(View.GONE);
                TextView tvCertificate = findViewById(R.id.cert);
                tvCertificate.setVisibility(View.GONE);
            }

            Button changeBtn = findViewById(R.id.buttonChangeConnection);
            changeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(NodeDetailsActivity.this, ManualSetup.class);
                    intent.putExtra(ManageNodesActivity.NODE_ID, mId);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                }
            });
        }

        Button switchBtn = findViewById(R.id.buttonActivate);
        switchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PrefsUtil.editPrefs().putString(PrefsUtil.CURRENT_NODE_CONFIG, mId).commit();

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
                new AlertDialog.Builder(NodeDetailsActivity.this)
                        .setMessage(R.string.confirm_node_deletion)
                        .setCancelable(true)
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                deleteWallet();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }).show();
            }
        });
    }

    private void showWalletNameInput() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(R.string.node_name);
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
                // This gets overridden below.
                // We need to do this to validate the input without closing the dialog.
            }
        });
        adb.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                mInputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                dialog.cancel();
            }
        });

        AlertDialog dialog = adb.create();
        dialog.show();
        Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (input.getText().toString().trim().isEmpty()) {
                    Toast.makeText(NodeDetailsActivity.this, R.string.error_empty_node_name, Toast.LENGTH_LONG).show();
                } else {
                    NodeConfigsManager nodeConfigsManager = NodeConfigsManager.getInstance();
                    nodeConfigsManager.renameNodeConfig(NodeConfigsManager.getInstance().getNodeConfigById(mId), input.getText().toString());
                    try {
                        nodeConfigsManager.apply();
                        TextView tvWalletName = findViewById(R.id.nodeName);
                        tvWalletName.setText(input.getText().toString().trim());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    mInputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    dialog.dismiss();
                }
            }
        });
    }

    private ZapNodeConfig getWalletConfig() {
        return NodeConfigsManager.getInstance().getNodeConfigById(mId);
    }

    private void openHome() {
        // Open home and clear history
        Intent intent = new Intent(NodeDetailsActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void deleteWallet() {
        NodeConfigsManager nodeConfigsManager = NodeConfigsManager.getInstance();
        nodeConfigsManager.removeNodeConfig(NodeConfigsManager.getInstance().getNodeConfigById(mId));
        try {
            nodeConfigsManager.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (PrefsUtil.getCurrentNodeConfig().equals(mId)) {
            Wallet.getInstance().reset();
            LndConnection.getInstance().closeConnection();
            PrefsUtil.editPrefs().remove(PrefsUtil.CURRENT_NODE_CONFIG).commit();
            Intent intent = new Intent(NodeDetailsActivity.this, LandingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            finish();
        }
    }
}
