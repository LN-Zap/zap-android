package zapsolutions.zap.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import zapsolutions.zap.R;
import zapsolutions.zap.connection.manageWalletConfigs.Cryptography;
import zapsolutions.zap.pin.PinEntryActivity;

public class PinScreenUtil {

    static public void askForAccess(Activity activity, OnSecurityCheckPerformedListener onSecurityCheckPerformedListener) {
        if (PrefsUtil.isWalletSetup() && TimeOutUtil.getInstance().isTimedOut()) {
            if (PrefsUtil.isPinEnabled()) {
                // Go to PIN entry screen
                Intent pinIntent = new Intent(activity, PinEntryActivity.class);
                pinIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(pinIntent);
            } else {

                // Check if pin is active according to key store
                boolean isPinActive = false;
                try {
                    isPinActive = new Cryptography(activity).isPinActive();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Only allow access if pin is not active in key store!
                if (isPinActive) {
                    // According to the key store, the pin is still active. This happens if the pin got deleted from the prefs without also removing the keystore entry.
                    new AlertDialog.Builder(activity)
                            .setMessage(R.string.error_pin_deactivation_attempt)
                            .setCancelable(false)
                            .setPositiveButton(R.string.continue_string, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    activity.finish();
                                }
                            }).show();
                } else {
                    // Access granted
                    onSecurityCheckPerformedListener.onAccessGranted();
                }
            }
        } else {
            // Access granted
            onSecurityCheckPerformedListener.onAccessGranted();
        }
    }

    public interface OnSecurityCheckPerformedListener {
        void onAccessGranted();
    }

}


