package ln_zap.zap.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.preference.PreferenceManager;
import ln_zap.zap.R;

/**
 * This class allows showing details of On-Chain transactions and addresses using
 * 3rd Party services (Block Explorers)
 */
public class BlockExplorer {


    /**
     * Shows transaction details in a browser window using the preferred block explorer.
     *
     * @param transactionID transaction to show
     * @param ctx
     */
    public static void showTransaction(String transactionID, Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String explorer = prefs.getString("blockExplorer", "BlockCypher");
        boolean mainnet = !Wallet.getInstance().isTestnet();
        String networkID = "";
        String url = "";
        boolean supported = true;

        switch (explorer) {
            case "BlockCypher":
                networkID = mainnet ? "btc" : "btc-testnet";
                url = "https://live.blockcypher.com/" + networkID + "/tx/" + transactionID;
                break;
            case "Blockstream":
                networkID = mainnet ? "" : "testnet/";
                url = "https://blockstream.info/" + networkID + "tx/" + transactionID;
                break;
            case "Smartbit":
                networkID = mainnet ? "www" : "testnet";
                url = "https://" + networkID + ".smartbit.com.au/tx/" + transactionID;
                break;
            case "Blockchain Reader (Yogh)":
                supported = mainnet;
                url = "http://srv1.yogh.io/#tx:id:" + transactionID;
                break;
            case "OXT":
                supported = mainnet;
                url = "https://oxt.me/transaction/" + transactionID;
                break;
        }

        if (supported) {
            // Call the url
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            ctx.startActivity(browserIntent);
        } else {
            unsupportedNetwork(explorer, mainnet ? "mainnet" : "testnet", ctx);
        }
    }


    /**
     * Shows address details in a browser window using the preferred block explorer.
     *
     * @param address address to show
     * @param ctx
     */
    public static void showAddress(String address, Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String explorer = prefs.getString("blockExplorer", "BlockCypher");
        boolean mainnet = !Wallet.getInstance().isTestnet();
        String networkID = "";
        String url = "";
        boolean supported = true;

        switch (explorer) {
            case "BlockCypher":
                networkID = mainnet ? "btc" : "btc-testnet";
                url = "https://live.blockcypher.com/" + networkID + "/address/" + address;
                break;
            case "Blockstream":
                networkID = mainnet ? "" : "testnet/";
                url = "https://blockstream.info/" + networkID + "address/" + address;
                break;
            case "Smartbit":
                networkID = mainnet ? "www" : "testnet";
                url = "https://" + networkID + ".smartbit.com.au/address/" + address;
                break;
            case "Blockchain Reader (Yogh)":
                supported = mainnet;
                url = "http://srv1.yogh.io/#addr:id:" + address;
                break;
            case "OXT":
                supported = mainnet;
                url = "https://oxt.me/address/" + address;
                break;
        }

        if (supported) {
            // Call the url
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            ctx.startActivity(browserIntent);
        } else {
            unsupportedNetwork(explorer, mainnet ? "mainnet" : "testnet", ctx);
        }
    }


    private static void unsupportedNetwork(String explorer, String network, Context ctx) {
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
}
