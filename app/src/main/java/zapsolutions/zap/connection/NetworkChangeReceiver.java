package zapsolutions.zap.connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.Wallet;
import zapsolutions.zap.util.ZapLog;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        ZapLog.debug("NetworkChangeReceiver: ", "Network status changed!");

        int status = NetworkUtil.getConnectivityStatusString(context);

        if (PrefsUtil.isWalletSetup()) {
            if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                // The following command will find out, if we have a connection to LND
                Wallet.getInstance().fetchInfoFromLND();
            } else {
                // It needs some time to establish the connection to LND.
                // Therefore we check the connection after a delay.
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // The following command will find out, if we have a connection to LND
                        Wallet.getInstance().fetchInfoFromLND();
                    }
                }, 5000);
            }
        } else {
            // The wallet is not setup, simulate connection status exclusively on internet connection.
            Wallet.getInstance().simulateFetchInfoForDemo(status != NetworkUtil.NETWORK_STATUS_NOT_CONNECTED);
        }
    }
}
