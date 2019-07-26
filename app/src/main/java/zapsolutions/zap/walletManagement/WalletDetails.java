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
import android.widget.Toast;
import zapsolutions.zap.HomeActivity;
import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.BaseAppCompatActivity;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfigsManager;
import zapsolutions.zap.setup.ConnectRemoteNodeActivity;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.TimeOutUtil;
import zapsolutions.zap.util.Wallet;


public class WalletDetails extends BaseAppCompatActivity {

    private String mAlias;
    private InputMethodManager mInputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_details);

        mInputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);


        // Receive data from last activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mAlias = extras.getString("walletAlias");
        }

        Button switchBtn = findViewById(R.id.buttonActivate);
        switchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PrefsUtil.edit().putString(PrefsUtil.CURRENT_WALLET_CONFIG, mAlias).commit();

                // Do not ask for pin again...
                TimeOutUtil.getInstance().restartTimer();

                Wallet.getInstance().reset();

                // Open home and clear history
                Intent intent = new Intent(WalletDetails.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        Button renameBtn = findViewById(R.id.buttonRename);
        renameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showWalletNameInput();
            }
        });

        Button changeBtn = findViewById(R.id.buttonChangeConnection);
        changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WalletDetails.this, ConnectRemoteNodeActivity.class);
                intent.putExtra("walletAlias", mAlias);
                startActivity(intent);
            }
        });

        Button deleteBtn = findViewById(R.id.buttonDelete);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PrefsUtil.getCurrentWalletConfig().equals(mAlias)) {
                    Toast.makeText(WalletDetails.this, "You cannot delete the currently active wallet.", Toast.LENGTH_LONG).show();
                } else {
                    WalletConfigsManager walletConfigsManager = WalletConfigsManager.getInstance();
                    walletConfigsManager.removeWalletConfig(mAlias);
                    try {
                        walletConfigsManager.apply();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    finish();
                }
            }
        });
    }

    private void showWalletNameInput() {
        // Show unlock dialog
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("Wallet Name");
        adb.setCancelable(false);
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_input_text, null, false);

        final EditText input = viewInflated.findViewById(R.id.input);
        input.setShowSoftInputOnFocus(true);
        input.setText(mAlias);
        input.requestFocus();


        mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        adb.setView(viewInflated);

        adb.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (WalletConfigsManager.getInstance().doesWalletConfigExist(input.getText().toString())) {
                    Toast.makeText(WalletDetails.this, "This name already exists.", Toast.LENGTH_LONG).show();
                    mInputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    showWalletNameInput();
                } else {
                    WalletConfigsManager walletConfigsManager = WalletConfigsManager.getInstance();
                    walletConfigsManager.renameWalletConfig(mAlias, input.getText().toString());
                    try {
                        walletConfigsManager.apply();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // If we renamed the currently active wallet, we have to change what is loaded by default.
                    if (PrefsUtil.getCurrentWalletConfig().equals(mAlias)) {
                        PrefsUtil.edit().putString(PrefsUtil.CURRENT_WALLET_CONFIG, input.getText().toString()).commit();
                    }

                    mAlias = input.getText().toString();
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
}
