package zapsolutions.zap.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import zapsolutions.zap.R;
import zapsolutions.zap.interfaces.UserGuardianInterface;

/**
 * This class allows showing details of On-Chain transactions and addresses using
 * 3rd Party services (Block Explorers)
 */
public class BlockExplorer implements UserGuardianInterface {

    private String mUrl;
    private boolean misNetworkSupported;
    private Context mContext;


    /**
     * Shows transaction details in a browser window using the preferred block explorer.
     *
     * @param transactionID transaction to show
     * @param ctx
     */
    public void showTransaction(String transactionID, Context ctx) {
        mContext = ctx;
        String explorer = PrefsUtil.getPrefs().getString("blockExplorer", "BlockCypher");
        boolean isMainnet = !Wallet.getInstance().isTestnet();
        String networkID = "";
        mUrl = "";
        misNetworkSupported = true;

        switch (explorer) {
            case "BlockCypher":
                networkID = isMainnet ? "btc" : "btc-testnet";
                mUrl = "https://live.blockcypher.com/" + networkID + "/tx/" + transactionID;
                break;
            case "Blockstream":
                networkID = isMainnet ? "" : "testnet/";
                mUrl = "https://blockstream.info/" + networkID + "tx/" + transactionID;
                break;
            case "Blockstream (v3 Tor)":
                networkID = isMainnet ? "" : "testnet/";
                mUrl = "http://explorerzydxu5ecjrkwceayqybizmpjjznk5izmitf2modhcusuqlid.onion/" + networkID + "tx/" + transactionID;
                break;
            case "Smartbit":
                networkID = isMainnet ? "www" : "testnet";
                mUrl = "https://" + networkID + ".smartbit.com.au/tx/" + transactionID;
                break;
            case "Blockchain Reader (Yogh)":
                misNetworkSupported = isMainnet;
                mUrl = "http://srv1.yogh.io/#tx:id:" + transactionID;
                break;
            case "OXT":
                misNetworkSupported = isMainnet;
                mUrl = "https://oxt.me/transaction/" + transactionID;
                break;
        }

        if (explorer.equals("Blockstream (v3 Tor)")) {
            startBlockExplorer();
        } else {
            new UserGuardian(mContext, this).privacyBlockExplorer();
        }
    }

    /**
     * Shows address details in a browser window using the preferred block explorer.
     *
     * @param address address to show
     * @param ctx
     */
    public void showAddress(String address, Context ctx) {
        mContext = ctx;
        String explorer = PrefsUtil.getPrefs().getString("blockExplorer", "BlockCypher");
        boolean isMainnet = !Wallet.getInstance().isTestnet();
        String networkID = "";
        mUrl = "";
        misNetworkSupported = true;

        switch (explorer) {
            case "BlockCypher":
                networkID = isMainnet ? "btc" : "btc-testnet";
                mUrl = "https://live.blockcypher.com/" + networkID + "/address/" + address;
                break;
            case "Blockstream":
                networkID = isMainnet ? "" : "testnet/";
                mUrl = "https://blockstream.info/" + networkID + "address/" + address;
                break;
            case "Blockstream (v3 Tor)":
                networkID = isMainnet ? "" : "testnet/";
                mUrl = "http://explorerzydxu5ecjrkwceayqybizmpjjznk5izmitf2modhcusuqlid.onion/" + networkID + "address/" + address;
                break;
            case "Smartbit":
                networkID = isMainnet ? "www" : "testnet";
                mUrl = "https://" + networkID + ".smartbit.com.au/address/" + address;
                break;
            case "Blockchain Reader (Yogh)":
                misNetworkSupported = isMainnet;
                mUrl = "http://srv1.yogh.io/#addr:id:" + address;
                break;
            case "OXT":
                misNetworkSupported = isMainnet;
                mUrl = "https://oxt.me/address/" + address;
                break;
        }

        if (explorer.equals("Blockstream (v3 Tor)")) {
            startBlockExplorer();
        } else {
            new UserGuardian(mContext, this).privacyBlockExplorer();
        }
    }

    @Override
    public void guardianDialogConfirmed(String DialogName) {
        if (DialogName.equals(UserGuardian.BLOCK_EXPLORER)) {
            startBlockExplorer();
        }
    }

    private void unsupportedNetwork(String explorer, String network, Context ctx) {
        new AlertDialog.Builder(ctx)
                .setTitle(R.string.error_blockExplorer_title)
                .setMessage(ctx.getResources().getString(R.string.error_blockExplorer_message, explorer, network))
                .setCancelable(true)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
    }

    private void startBlockExplorer() {
        if (misNetworkSupported) {
            // Call the url
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));
            mContext.startActivity(browserIntent);
        } else {
            String explorer = PrefsUtil.getPrefs().getString("blockExplorer", "Blockstream");
            boolean isMainnet = !Wallet.getInstance().isTestnet();
            unsupportedNetwork(explorer, isMainnet ? "mainnet" : "testnet", mContext);
        }
    }
}
