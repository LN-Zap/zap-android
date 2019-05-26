package ln_zap.zap.setup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import at.favre.lib.armadillo.Armadillo;
import at.favre.lib.armadillo.PBKDF2KeyStretcher;
import ln_zap.zap.HomeActivity;
import ln_zap.zap.R;
import ln_zap.zap.util.RefConstants;
import ln_zap.zap.util.UtilFunctions;
import ln_zap.zap.baseClasses.App;
import ln_zap.zap.baseClasses.BaseAppCompatActivity;
import ln_zap.zap.util.TimeOutUtil;


public class SetupActivity extends BaseAppCompatActivity {

    public static final int FULL_SETUP = 0;
    public static final int CHANGE_PIN = 1;
    public static final int CHANGE_CONNECTION = 2;

    private static final String LOG_TAG = "Setup Activity";

    private Fragment mCurrentFragment = null;
    private FragmentTransaction mFt;
    private SharedPreferences mPrefs;
    private int mSetupMode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        // Receive data from last activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mSetupMode = extras.getInt("setupMode", 0);
        }

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);


        // Set pin fragment as beginning fragment
        showCreatePin();

        switch (mSetupMode) {
            case FULL_SETUP:
                if (mPrefs.getBoolean("eulaAccepted", false))
                    showCreatePin();
                else
                    showEula();
                break;
            case CHANGE_PIN:
                showEnterPin();
                break;
            case CHANGE_CONNECTION:
                showEnterPin();
                break;
        }
    }

    public void eulaAccepted() {
        showCreatePin();
    }

    public void pinCreated(String value, Integer length) {
        App.getAppContext().pinTemp = value;
        showConfirmPin();
    }

    public void pinConfirmed(String value, Integer length) {

        String tempInMemoryPin = App.getAppContext().inMemoryPin;

        App.getAppContext().inMemoryPin = value;
        App.getAppContext().pinTemp = null;

        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(RefConstants.pin_hash, UtilFunctions.pinHash(value));
        // TODO: do away with pin length eventually because it is vulnerability to hint in UI how long pin is
        editor.putInt(RefConstants.pin_length, value.length());
        editor.commit();

        if (mSetupMode == FULL_SETUP) {
            showConnectChoice();
        }
        if (mSetupMode == CHANGE_PIN) {

            // Encrypt connection data with the new PIN
            App ctx = App.getAppContext();
            SharedPreferences prefsRemote = Armadillo.create(ctx, RefConstants.prefs_remote)
                    .encryptionFingerprint(ctx)
                    .keyStretchingFunction(new PBKDF2KeyStretcher(5000, null))
                    .password(tempInMemoryPin.toCharArray())
                    .contentKeyDigest(UtilFunctions.getZapsalt().getBytes())
                    .build();

            String connectionInfo = prefsRemote.getString(RefConstants.remote_combined, "");

            SharedPreferences newPrefsRemote = Armadillo.create(ctx, RefConstants.prefs_remote)
                    .encryptionFingerprint(ctx)
                    .keyStretchingFunction(new PBKDF2KeyStretcher(5000, null))
                    .password(ctx.inMemoryPin.toCharArray())
                    .contentKeyDigest(UtilFunctions.getZapsalt().getBytes())
                    .build();

            newPrefsRemote.edit()
                    // The following string contains host,port,cert and macaroon in one string separated with ";"
                    // This way we can read all necessary data in one call and do not have to execute the key stretching function 4 times.
                    .putString(RefConstants.remote_combined, connectionInfo)
                    .commit();

            // Show success message
            Toast.makeText(SetupActivity.this, "PIN changed!", Toast.LENGTH_SHORT).show();

            // Reset the PIN timeout. We don't want to ask for PIN again...
            TimeOutUtil.getInstance().restartTimer();

            // Go to home screen
            Intent intent = new Intent(SetupActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    public void correctPinEntered() {
        if (mSetupMode == CHANGE_PIN) {
            showCreatePin();
        }
        if (mSetupMode == CHANGE_CONNECTION) {
            showConnectChoice();
        }
    }

    private void showCreatePin() {
        if (mSetupMode == CHANGE_PIN) {
            changeFragment(PinFragment.newInstance(PinFragment.CREATE_MODE, getResources().getString(R.string.pin_enter_new)));
        } else {
            changeFragment(PinFragment.newInstance(PinFragment.CREATE_MODE, getResources().getString(R.string.pin_create)));
        }
    }

    private void showConfirmPin() {
        if (mSetupMode == CHANGE_PIN) {
            changeFragment(PinFragment.newInstance(PinFragment.CONFIRM_MODE, getResources().getString(R.string.pin_confirm_new)));
        } else {
            changeFragment(PinFragment.newInstance(PinFragment.CONFIRM_MODE, getResources().getString(R.string.pin_confirm)));
        }
    }

    private void showEnterPin() {
        if (mSetupMode == CHANGE_PIN) {
            changeFragment(PinFragment.newInstance(PinFragment.ENTER_MODE, getResources().getString(R.string.pin_enter_old)));
        } else {
            changeFragment(PinFragment.newInstance(PinFragment.ENTER_MODE, getResources().getString(R.string.pin_enter)));
        }
    }

    private void showConnectChoice() {
        changeFragment(new ConnectFragment());
    }

    private void showEula() {
        changeFragment(new EulaFragment());
    }

    private void changeFragment(Fragment fragment) {
        mFt = getSupportFragmentManager().beginTransaction();
        mFt.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        mCurrentFragment = fragment;
        mFt.replace(R.id.mainContent, mCurrentFragment);
        mFt.commit();
    }

}
