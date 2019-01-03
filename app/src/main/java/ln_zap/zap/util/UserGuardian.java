package ln_zap.zap.util;

import android.content.DialogInterface;
import android.content.Context;

import ln_zap.zap.interfaces.UserGuardianInterface;
import ln_zap.zap.R;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;


/**
 * The UserGuardian is designed to help inexperienced people keeping their bitcoin safe.
 * Use this class to show security warnings whenever the user does something that harms
 * his security or privacy.
 * To avoid too many popups, these messages have a "do not show again" option.
 *
 * A class using the UserGuardian class needs to implement the UserGuardianInterface
 * to handle users choice.
 *
 * Please note that a dialog which will not be shown executes the callback like it does when "ok" is pressed.
 */
public class UserGuardian {

    public static final String COPY_TO_CLIPBOARD = "guardianCopyToClipboard";
    public static final String PASTE_FROM_CLIPBOARD = "guardianPasteFromClipboard";
    public static final String DISABLE_SCRAMBLED_PIN = "guardianDisableScrambledPin";
    public static final String DISABLE_SCREEN_PROTECTION = "guardianDisableScreenProtection";
    public static final String HIGH_ONCHAIN_FEE = "guardianHighOnCainFees";
    public static final String OLD_EXCHANGE_RATE = "guardianOldExchangeRate";
    public static final String TOO_MUCH_MONEY = "guardianTooMuchMoney";

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
    public void securityCopyToClipboard(String data){
        mCurrentDialogName = COPY_TO_CLIPBOARD;

        String compareString = "";
        if (data.length()>7){
            compareString = "... " + data.substring(data.length()-6,data.length());
        }

        AlertDialog.Builder adb = createDontShowAgainDialog(true);
        adb.setTitle(R.string.guardian_Title);
        adb.setMessage(mContext.getResources().getString(R.string.guardian_copyToClipboard, compareString));
        showGuardianDialog(adb);
    }


    /**
     * Warn the user about pasting a payment request from clipboard.
     */
    public void securityPasteFromClipboard(){
        mCurrentDialogName = PASTE_FROM_CLIPBOARD;
        AlertDialog.Builder adb = createDontShowAgainDialog(false);
        adb.setTitle(R.string.guardian_Title);
        adb.setMessage(R.string.guardian_pasteFromClipboard);
        showGuardianDialog(adb);
    }


    /**
     * Warn the user to not disable scrambled pin input.
     */
    public void securityScrambledPin(){
        mCurrentDialogName = DISABLE_SCRAMBLED_PIN;
        AlertDialog.Builder adb = createDontShowAgainDialog(true);
        adb.setTitle(R.string.guardian_Title);
        adb.setMessage(R.string.guardian_disableScrambledPin);
        showGuardianDialog(adb);
    }


    /**
     * Warn the user to not disable screen protection.
     */
    public void securityScreenProtection(){
        mCurrentDialogName = DISABLE_SCREEN_PROTECTION;
        AlertDialog.Builder adb = createDontShowAgainDialog(true);
        adb.setTitle(R.string.guardian_Title);
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
    public void securityHighOnChainFee(float feeRate){
        mCurrentDialogName = HIGH_ONCHAIN_FEE;
        AlertDialog.Builder adb = createDontShowAgainDialog(true);
        adb.setTitle(R.string.guardian_Title);
        adb.setMessage(mContext.getResources().getString(R.string.guardian_highOnChainFee, String.format("%.1f", feeRate*100)));
        showGuardianDialog(adb);
    }


    /**
     * Warn the user if he tries to request some Bitcoin while his primary currency is a
     * fiat currency and the exchange rate data has come of age.
     *
     * @param age in seconds
     */
    public void securityOldExchangeRate(double age){
        mCurrentDialogName = OLD_EXCHANGE_RATE;
        AlertDialog.Builder adb = createDontShowAgainDialog(true);
        adb.setTitle(R.string.guardian_Title);
        adb.setMessage(mContext.getResources().getString(R.string.guardian_oldExchangeRate,String.format("%.1f", age/3600)));
        showGuardianDialog(adb);
    }


    /**
     * Warn the user if he stores a large amounts of Bitcoin in his wallet.
     */
    public void securityTooMuchMoney(){
        mCurrentDialogName = TOO_MUCH_MONEY;
        AlertDialog.Builder adb = createDontShowAgainDialog(false);
        adb.setTitle(R.string.guardian_Title);
        adb.setMessage(R.string.guardian_tooMuchMoney);
        showGuardianDialog(adb);
    }


    /**
     * Reset all "do not show again" selections.
     */
    public static void reenableAllSecurityWarnings(Context ctx){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(COPY_TO_CLIPBOARD, true);
        editor.putBoolean(PASTE_FROM_CLIPBOARD, true);
        editor.putBoolean(DISABLE_SCRAMBLED_PIN, true);
        editor.putBoolean(DISABLE_SCREEN_PROTECTION, true);
        editor.putBoolean(HIGH_ONCHAIN_FEE, true);
        editor.putBoolean(OLD_EXCHANGE_RATE, true);
        editor.putBoolean(TOO_MUCH_MONEY, true);
        editor.apply();
    }



    /**
     * Create a dialog with a "do not show again" option that is already set up
     * except title and message.
     * This helps keeping the dialog functions organized and simple.
     *
     * @param hasCancelOption wether it has a cancle option or not
     * @return returns a preconfigured AlertDialog.Builder which can be further configured later
     */
    private AlertDialog.Builder createDontShowAgainDialog (Boolean hasCancelOption){
        AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
        LayoutInflater adbInflater = LayoutInflater.from(mContext);
        View DialogLayout = adbInflater.inflate(R.layout.dialog_checkbox, null);
        mDontShowAgain = DialogLayout.findViewById(R.id.skip);
        adb.setView(DialogLayout);
        adb.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                if (mDontShowAgain.isChecked()){
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(mCurrentDialogName, false);
                    editor.apply();
                }

                // Execute interface callback on "OK"
                mAction.guardianDialogConfirmed(mCurrentDialogName);
            }
        });
        if (hasCancelOption) {
            adb.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) { }
            });
        }
        return adb;
    }


    /**
     * Show the dialog or execute callback if it should not be shown.
     *
     * @param adb The AlertDialog.Builder which should be shown.
     */
    private void showGuardianDialog (AlertDialog.Builder adb){
       if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(mCurrentDialogName, true)){
           adb.show();
       }
       else{
           mAction.guardianDialogConfirmed(mCurrentDialogName);
       }
    }
}
