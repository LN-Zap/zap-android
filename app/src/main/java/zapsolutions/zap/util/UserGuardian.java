package zapsolutions.zap.util;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Context;

import zapsolutions.zap.interfaces.UserGuardianInterface;
import zapsolutions.zap.R;

import android.app.AlertDialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;


/**
 * The UserGuardian is designed to help inexperienced people keeping their bitcoin safe.
 * Use this class to show security warnings whenever the user does something that harms
 * his security or privacy.
 * To avoid too many popups, these messages have a "do not show again" option.
 * <p>
 * A class using the UserGuardian class needs to implement the UserGuardianInterface
 * to handle users choice.
 * <p>
 * Please note that a dialog which will not be shown (do not show again checked) executes
 * the callback like it does when "ok" is pressed.
 */
public class UserGuardian {

    public static final String COPY_TO_CLIPBOARD = "guardianCopyToClipboard";
    public static final String PASTE_FROM_CLIPBOARD = "guardianPasteFromClipboard";
    public static final String DISABLE_SCRAMBLED_PIN = "guardianDisableScrambledPin";
    public static final String DISABLE_SCREEN_PROTECTION = "guardianDisableScreenProtection";
    public static final String HIGH_ONCHAIN_FEE = "guardianHighOnCainFees";
    public static final String OLD_EXCHANGE_RATE = "guardianOldExchangeRate";
    public static final String TOO_MUCH_MONEY = "guardianTooMuchMoney";
    public static final String MAINNET_NOT_READY = "guardianMainnetNotReady";

    private final Context mContext;
    private final UserGuardianInterface mAction;
    private String mCurrentDialogName;
    private CheckBox mDontShowAgain;


    public UserGuardian(Context ctx, UserGuardianInterface caller) {
        mContext = ctx;
        mAction = caller;
    }


    /**
     * Warn the user about security issues when copying stuff to clipboard.
     * Also provide the user with a check string to secure himself
     *
     * @param data the data that is copied to clipboard
     */
    public void securityCopyToClipboard(String data, int type) {
        mCurrentDialogName = COPY_TO_CLIPBOARD;

        String compareString;
        String message = "";
        switch (type) {
            case 0: // On-Chain Request
                if (data.length() > 15) {
                    compareString = data.substring(0, 15) + " ...";
                    message = mContext.getResources().getString(R.string.guardian_copyToClipboard_onChain, compareString);
                }
                break;
            case 1: // Lightning Request
                if (data.length() > 15) {
                    compareString = "... " + data.substring(data.length() - 8, data.length());
                    message = mContext.getResources().getString(R.string.guardian_copyToClipboard_lightning, compareString);
                }
                break;
        }

        AlertDialog.Builder adb = createDontShowAgainDialog(true);
        adb.setMessage(message);
        showGuardianDialog(adb);
    }


    /**
     * Warn the user about pasting a payment request from clipboard.
     */
    public void securityPasteFromClipboard() {
        mCurrentDialogName = PASTE_FROM_CLIPBOARD;
        AlertDialog.Builder adb = createDontShowAgainDialog(false);
        adb.setMessage(R.string.guardian_pasteFromClipboard);
        showGuardianDialog(adb);
    }

    /**
     * Warn the user about using the wallet on mainnet, while it is still not secure.
     */
    public void securityMainnetNotReady() {
        mCurrentDialogName = MAINNET_NOT_READY;
        AlertDialog.Builder adb = createDontShowAgainDialog(false);
        adb.setMessage(R.string.guardian_notReadyForMainnet);
        showGuardianDialog(adb);
    }


    /**
     * Warn the user to not disable scrambled pin input.
     */
    public void securityScrambledPin() {
        mCurrentDialogName = DISABLE_SCRAMBLED_PIN;
        AlertDialog.Builder adb = createDontShowAgainDialog(true);
        adb.setMessage(R.string.guardian_disableScrambledPin);
        showGuardianDialog(adb);
    }


    /**
     * Warn the user to not disable screen protection.
     */
    public void securityScreenProtection() {
        mCurrentDialogName = DISABLE_SCREEN_PROTECTION;
        AlertDialog.Builder adb = createDontShowAgainDialog(true);
        adb.setMessage(R.string.guardian_disableScreenProtection);
        showGuardianDialog(adb);
    }


    /**
     * Warn the user about high On-Chain fees.
     * The user will be displayed a message which shows the amount of fee compared to
     * the transactions value.
     *
     * @param feeRate 0 = 0% ; 1 = 100% (equal transaction amount) ; >1 you pay more fees than you transact
     */
    public void securityHighOnChainFee(float feeRate) {
        mCurrentDialogName = HIGH_ONCHAIN_FEE;
        AlertDialog.Builder adb = createDontShowAgainDialog(true);
        String feeRateString = String.format("%.1f", feeRate * 100);
        adb.setMessage(mContext.getResources().getString(R.string.guardian_highOnChainFee, feeRateString));
        showGuardianDialog(adb);
    }


    /**
     * Warn the user if he tries to request some Bitcoin while his primary currency is a
     * fiat currency and the exchange rate data has come of age.
     *
     * @param age in seconds
     */
    public void securityOldExchangeRate(double age) {
        mCurrentDialogName = OLD_EXCHANGE_RATE;
        AlertDialog.Builder adb = createDontShowAgainDialog(true);
        String ageString = String.format("%.1f", age / 3600);
        adb.setMessage(mContext.getResources().getString(R.string.guardian_oldExchangeRate, ageString));
        showGuardianDialog(adb);
    }


    /**
     * Warn the user if he stores large amounts of Bitcoin in his wallet.
     */
    public void securityTooMuchMoney() {
        mCurrentDialogName = TOO_MUCH_MONEY;
        AlertDialog.Builder adb = createDontShowAgainDialog(false);
        adb.setMessage(R.string.guardian_tooMuchMoney);
        showGuardianDialog(adb);
    }


    /**
     * Reset all "do not show again" selections.
     */
    public static void reenableAllSecurityWarnings(Context ctx) {
        PrefsUtil.edit()
                .putBoolean(COPY_TO_CLIPBOARD, true)
                .putBoolean(PASTE_FROM_CLIPBOARD, true)
                .putBoolean(DISABLE_SCRAMBLED_PIN, true)
                .putBoolean(DISABLE_SCREEN_PROTECTION, true)
                .putBoolean(HIGH_ONCHAIN_FEE, true)
                .putBoolean(OLD_EXCHANGE_RATE, true)
                .putBoolean(TOO_MUCH_MONEY, true)
                .putBoolean(MAINNET_NOT_READY, true)
                .apply();
    }


    /**
     * Create a dialog with a "do not show again" option that is already set up
     * except the message.
     * This helps keeping the dialog functions organized and simple.
     *
     * @param hasCancelOption wether it has a cancle option or not
     * @return returns a preconfigured AlertDialog.Builder which can be further configured later
     */
    private AlertDialog.Builder createDontShowAgainDialog(Boolean hasCancelOption) {
        AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
        LayoutInflater adbInflater = LayoutInflater.from(mContext);
        View DialogLayout = adbInflater.inflate(R.layout.dialog_checkbox, null);
        mDontShowAgain = DialogLayout.findViewById(R.id.skip);
        View titleView = adbInflater.inflate(R.layout.guardian_title, null);
        adb.setView(DialogLayout);
        adb.setCustomTitle(titleView);
        adb.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                if (mDontShowAgain.isChecked()) {
                    PrefsUtil.edit().putBoolean(mCurrentDialogName, false).apply();
                }

                // Execute interface callback on "OK"
                mAction.guardianDialogConfirmed(mCurrentDialogName);
            }
        });
        if (hasCancelOption) {
            adb.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
        }
        return adb;
    }


    /**
     * Show the dialog or execute callback if it should not be shown.
     *
     * @param adb The AlertDialog.Builder which should be shown.
     */
    private void showGuardianDialog(AlertDialog.Builder adb) {

        if (PrefsUtil.getPrefs().getBoolean(mCurrentDialogName, true)) {
            Dialog dlg = adb.create();
            // Apply FLAG_SECURE to dialog to prevent screen recording
            if (PrefsUtil.preventScreenRecording()) {
                dlg.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
            }
            dlg.show();
        } else {
            mAction.guardianDialogConfirmed(mCurrentDialogName);
        }
    }
}
