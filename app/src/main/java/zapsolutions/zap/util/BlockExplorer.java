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
    private boolean mSupported;
    private Context mContext;


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

    /**
     * Shows transaction details in a browser window using the preferred block explorer.
     *
     * @param transactionID transaction to show
     * @param ctx
     */
    public void showTransaction(String transactionID, Context ctx) {
        mContext = ctx;
        String explorer = PrefsUtil.getPrefs().getString("blockExplorer", "BlockCypher");
        boolean mainnet = !Wallet.getInstance().isTestnet();
        String networkID = "";
        mUrl = "";
        mSupported = true;

        switch (explorer) {
            case "BlockCypher":
                networkID = mainnet ? "btc" : "btc-testnet";
                mUrl = "https://live.blockcypher.com/" + networkID + "/tx/" + transactionID;
                break;
            case "Blockstream":
                networkID = mainnet ? "" : "testnet/";
                mUrl = "https://blockstream.info/" + networkID + "tx/" + transactionID;
                break;
            case "Blockstream (v3 Tor)":
                networkID = mainnet ? "" : "testnet/";
                mUrl = "http://explorerzydxu5ecjrkwceayqybizmpjjznk5izmitf2modhcusuqlid.onion/" + networkID + "tx/" + transactionID;
                break;
            case "Smartbit":
                networkID = mainnet ? "www" : "testnet";
                mUrl = "https://" + networkID + ".smartbit.com.au/tx/" + transactionID;
                break;
            case "Blockchain Reader (Yogh)":
                mSupported = mainnet;
                mUrl = "http://srv1.yogh.io/#tx:id:" + transactionID;
                break;
            case "OXT":
                mSupported = mainnet;
                mUrl = "https://oxt.me/transaction/" + transactionID;
                break;
        }

        if (explorer.equals("Blockstream (v3 Tor)")){
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
        boolean mainnet = !Wallet.getInstance().isTestnet();
        String networkID = "";
        mUrl = "";
        mSupported = true;

        switch (explorer) {
            case "BlockCypher":
                networkID = mainnet ? "btc" : "btc-testnet";
                mUrl = "https://live.blockcypher.com/" + networkID + "/address/" + address;
                break;
            case "Blockstream":
                networkID = mainnet ? "" : "testnet/";
                mUrl = "https://blockstream.info/" + networkID + "address/" + address;
                break;
            case "Blockstream (v3 Tor)":
                networkID = mainnet ? "" : "testnet/";
                mUrl = "http://explorerzydxu5ecjrkwceayqybizmpjjznk5izmitf2modhcusuqlid.onion/" + networkID + "address/" + address;
                break;
            case "Smartbit":
                networkID = mainnet ? "www" : "testnet";
                mUrl = "https://" + networkID + ".smartbit.com.au/address/" + address;
                break;
            case "Blockchain Reader (Yogh)":
                mSupported = mainnet;
                mUrl = "http://srv1.yogh.io/#addr:id:" + address;
                break;
            case "OXT":
                mSupported = mainnet;
                mUrl = "https://oxt.me/address/" + address;
                break;
        }

        if (explorer.equals("Blockstream (v3 Tor)")){
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

    private void startBlockExplorer(){
        if (mSupported) {
            // Call the url
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));
            mContext.startActivity(browserIntent);
        } else {
            String explorer = PrefsUtil.getPrefs().getString("blockExplorer", "BlockCypher");
            boolean mainnet = !Wallet.getInstance().isTestnet();
            unsupportedNetwork(explorer, mainnet ? "mainnet" : "testnet", mContext);
        }
    }
}
