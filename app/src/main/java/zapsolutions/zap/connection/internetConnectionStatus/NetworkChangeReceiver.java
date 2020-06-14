package zapsolutions.zap.connection.internetConnectionStatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import java.util.concurrent.RejectedExecutionException;

import zapsolutions.zap.connection.manageWalletConfigs.WalletConfigsManager;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.Wallet;
import zapsolutions.zap.util.ZapLog;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = NetworkChangeReceiver.class.getName();

    @Override
    public void onReceive(final Context context, final Intent intent) {
        ZapLog.debug("NetworkChangeReceiver: ", "Network status changed!");

        int status = NetworkUtil.getConnectivityStatusString(context);

        if (WalletConfigsManager.getInstance().hasAnyConfigs()) {
            if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                // The following command will find out, if we have a connection to LND
                Wallet.getInstance().fetchInfoFromLND();
            } else {
                // It needs some time to establish the connection to LND.
                // Therefore we check the connection after a delay.
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    try {
                        // The following command will find out, if we have a connection to LND
                        Wallet.getInstance().fetchInfoFromLND();
                    } catch (RejectedExecutionException ex) {
                        ZapLog.debug(LOG_TAG, "Execute of fetchFromLND() was rejected");
                    }
                }, 5000);
            }
        } else {
            // The wallet is not setup, simulate connection status exclusively on internet connection.
            Wallet.getInstance().simulateFetchInfoForDemo(status != NetworkUtil.NETWORK_STATUS_NOT_CONNECTED);
        }
    }
}
