package zapsolutions.zap.customView;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.appcompat.widget.AppCompatSpinner;

import zapsolutions.zap.R;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfigsManager;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.walletManagement.ManageWalletsActivity;

public class WalletSpinner extends AppCompatSpinner {

    private boolean initFinished;

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

    private OnWalletSpinnerChangedListener mListener;

    private void init() {
        updateList();

        this.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (initFinished) {
                    int lastPos = adapterView.getCount() - 1;
                    if (position != lastPos) {
                        String selectedWalletId = WalletConfigsManager.getInstance().getAllWalletConfigs(true).get(position).getId();
                        if (!WalletConfigsManager.getInstance().getCurrentWalletConfig().getId().equals(selectedWalletId)) {
                            // Save selected Wallet ID in prefs making it the current wallet.
                            PrefsUtil.edit().putString(PrefsUtil.CURRENT_WALLET_CONFIG, selectedWalletId).commit();

                            // Update the wallet spinner list, so everything is at it's correct position again.
                            updateList();

                            // Inform the listener. This is where the new wallet is opened.
                            mListener.onWalletChanged();
                        }
                    } else {
                        // Open wallet Management
                        Intent intent = new Intent(getContext(), ManageWalletsActivity.class);
                        getContext().startActivity(intent);

                        // If going back we don't want to have "Manage.." selected
                        adapterView.setSelection(0);
                    }
                } else {
                    // When filling the list onItem Selected ist called for the first time.
                    // In this case we don't want to select something, but mark it ready for interaction instead.
                    initFinished = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public boolean performClick() {
        updateList();
        return super.performClick();
    }

    public void updateList() {

        initFinished = false;

        String[] items = new String[WalletConfigsManager.getInstance().getAllWalletConfigs(true).size() + 1];
        for (int i = 0; i < WalletConfigsManager.getInstance().getAllWalletConfigs(true).size(); i++) {
            items[i] = WalletConfigsManager.getInstance().getAllWalletConfigs(true).get(i).getAlias();
        }
        items[items.length - 1] = getContext().getResources().getString(R.string.spinner_manage_wallets);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, items);

        //set the spinners adapter to the previously created one.
        this.setAdapter(adapter);
    }

    public interface OnWalletSpinnerChangedListener {
        void onWalletChanged();
    }

    public void setOnWalletSpinnerChangedListener(OnWalletSpinnerChangedListener listener) {
        mListener = listener;
    }
}
