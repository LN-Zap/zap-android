package zapsolutions.zap.customView;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import androidx.appcompat.widget.AppCompatSpinner;
import zapsolutions.zap.HomeActivity;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfigsManager;
import zapsolutions.zap.setup.SetupActivity;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.TimeOutUtil;
import zapsolutions.zap.util.Wallet;

public class WalletSpinner extends AppCompatSpinner {
    public WalletSpinner(Context context) {
        super(context);
        init();
    }

    public WalletSpinner(Context context, int mode) {
        super(context, mode);
        init();
    }

    public WalletSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WalletSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public WalletSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        super(context, attrs, defStyleAttr, mode);
        init();
    }

    public WalletSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode, Resources.Theme popupTheme) {
        super(context, attrs, defStyleAttr, mode, popupTheme);
        init();
    }

    private void init() {
        updateList();

        this.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                int lastPos = adapterView.getCount() - 1;
                String selected = (String) adapterView.getItemAtPosition(position);
                if (position != lastPos) {
                    if (PrefsUtil.getCurrentWalletConfig().equals(selected)) {
                        // Do nothing, we already have loaded this wallet
                    } else if (selected.equals("Demo Wallet")) {

                    } else {
                        // Load the selected wallet
                        adapterView.getAdapter().getItem(position);
                        PrefsUtil.edit().putString(PrefsUtil.CURRENT_WALLET_CONFIG, selected).commit();

                        // Do not ask for pin again...
                        TimeOutUtil.getInstance().restartTimer();

                        Wallet.getInstance().reset();


                        // This has to be done, otherwise the OnDestroy method of the old
                        // HomeActivity will be called after the OnCreate Method of the new one.
                        // This would result in all listeners etc. to be unsubscribe right after wallet switch.
                        ((HomeActivity) getContext()).prepareWalletSwitch();

                        // Open home and clear history
                        Intent intent = new Intent(getContext(), HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().startActivity(intent);
                    }
                } else {
                    if (PrefsUtil.isWalletSetup()) {
                        // Open the create wallet ui
                        Intent intent = new Intent(getContext(), SetupActivity.class);
                        intent.putExtra("setupMode", SetupActivity.ADD_WALLET);
                        getContext().startActivity(intent);
                    } else {
                        // Initiate wallet setup
                        Intent intent = new Intent(getContext(), SetupActivity.class);
                        intent.putExtra("setupMode", SetupActivity.FULL_SETUP);
                        getContext().startActivity(intent);
                    }
                    // If going back we don't want to have "Add new..." selected
                    adapterView.setSelection(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void updateList() {

        if (PrefsUtil.isWalletSetup()) {
            String[] items = new String[WalletConfigsManager.getInstance().getAllWalletConfigs(true).size() + 1];
            for (int i = 0; i < WalletConfigsManager.getInstance().getAllWalletConfigs(true).size(); i++) {
                items[i] = WalletConfigsManager.getInstance().getAllWalletConfigs(true).get(i).getAlias();
            }
            items[items.length - 1] = "Add new...";
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, items);
            //set the spinners adapter to the previously created one.
            this.setAdapter(adapter);
        } else {
            String[] items = new String[2];
            items[0] = "Demo Wallet";
            items[1] = "Add new...";
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, items);
            //set the spinners adapter to the previously created one.
            this.setAdapter(adapter);
        }
    }
}
