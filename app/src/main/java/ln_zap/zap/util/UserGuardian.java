package ln_zap.zap.util;

import android.content.DialogInterface;
import android.content.Context;

import ln_zap.zap.Interfaces.UserGuardianInterface;
import ln_zap.zap.R;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;


/*
 The UserGuardian is designed to help inexperienced people keeping their bitcoin safe.
 This is accomplished by showing security warnings whenever the user does something that harms
 his security or privacy.
 To avoid too many popups, these messages have a "do not show again" option.
 Please note that a dialog which will not be shown executes the callback like it does when "ok" is pressed.
*/
public class UserGuardian {

    public static final String COPY_TO_CLIPBOARD = "guardianCopyToClipboard";
    public static final String PASTE_FROM_CLIPBOARD = "guardianPasteFromClipboard";
    public static final String DISABLE_SCRAMBLED_PIN = "guardianDisableScrambledPin";
    public static final String DISABLE_SCREEN_PROTECTION = "guardianDisableScreenProtection";

    private final Context context;
    private final UserGuardianInterface action;
    private String currentDialogName;
    private CheckBox dontShowAgain;


    public UserGuardian(Context ctx, UserGuardianInterface caller) {
        context = ctx;
        action = caller;
    }


    // Warn the user about security issues when copying stuff to clipboard.
    public void securityCopyToClipboard(){
        currentDialogName = COPY_TO_CLIPBOARD;
        AlertDialog.Builder adb = createDontShowAgainDialog(true);
        adb.setTitle(R.string.guardian_Title);
        adb.setMessage(R.string.guardian_copyToClipboard);
        showGuardianDialog(adb);
    }

    // Warn the user about pasting a payment request from clipboard.
    public void securityPasteFromClipboard(){
        currentDialogName = PASTE_FROM_CLIPBOARD;
        AlertDialog.Builder adb = createDontShowAgainDialog(false);
        adb.setTitle(R.string.guardian_Title);
        adb.setMessage(R.string.guardian_pasteFromClipboard);
        showGuardianDialog(adb);
    }

    // Warn the user to not disable scrambled pin.
    public void securityScrambledPin(){
        currentDialogName = DISABLE_SCRAMBLED_PIN;
        AlertDialog.Builder adb = createDontShowAgainDialog(true);
        adb.setTitle(R.string.guardian_Title);
        adb.setMessage(R.string.guardian_disableScrambledPin);
        showGuardianDialog(adb);
    }

    // Warn the user to not disable screen protection.
    public void securityScreenProtection(){
        currentDialogName = DISABLE_SCREEN_PROTECTION;
        AlertDialog.Builder adb = createDontShowAgainDialog(true);
        adb.setTitle(R.string.guardian_Title);
        adb.setMessage(R.string.guardian_disableScreenProtection);
        showGuardianDialog(adb);
    }


    // Create a dialog with a "do not show again" option that has everything set
    // except title and message.
    private AlertDialog.Builder createDontShowAgainDialog (Boolean hasCancelOption){
        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        LayoutInflater adbInflater = LayoutInflater.from(context);
        View DialogLayout = adbInflater.inflate(R.layout.dialog_checkbox, null);
        dontShowAgain = DialogLayout.findViewById(R.id.skip);
        adb.setView(DialogLayout);
        adb.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                if (dontShowAgain.isChecked()){
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(currentDialogName, false);
                    editor.apply();
                }

                // Do what you want to do on "OK" action
                action.guardianDialogConfirmed(currentDialogName);
                return;
            }
        });
        if (hasCancelOption) {
            adb.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });
        }
        return adb;
    }


    // Show the dialog or execute callback if it should not be shown.
    private void showGuardianDialog (AlertDialog.Builder adb){
       if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(currentDialogName, true)){
           adb.show();
       }
       else{
           action.guardianDialogConfirmed(currentDialogName);
       }
    }

    // Reset all "do not show again" selections
    public static void reenableAllSecurityWarnings(Context ctx){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(COPY_TO_CLIPBOARD, true);
        editor.putBoolean(PASTE_FROM_CLIPBOARD, true);
        editor.putBoolean(DISABLE_SCRAMBLED_PIN, true);
        editor.putBoolean(DISABLE_SCREEN_PROTECTION, true);
        editor.apply();
    }
}
