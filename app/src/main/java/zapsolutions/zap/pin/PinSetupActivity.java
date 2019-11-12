package zapsolutions.zap.pin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import zapsolutions.zap.HomeActivity;
import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.App;
import zapsolutions.zap.baseClasses.BaseAppCompatActivity;
import zapsolutions.zap.connection.manageWalletConfigs.Cryptography;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.RefConstants;
import zapsolutions.zap.util.TimeOutUtil;
import zapsolutions.zap.util.UtilFunctions;


public class PinSetupActivity extends BaseAppCompatActivity implements PinActivityInterface {

    public static final int ADD_PIN = 0;
    public static final int CHANGE_PIN = 1;

    private static final String LOG_TAG = PinSetupActivity.class.getName();

    private Fragment mCurrentFragment = null;
    private FragmentTransaction mFt;
    private int mSetupMode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        // Receive data from last activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mSetupMode = extras.getInt(RefConstants.SETUP_MODE, 0);
        }


        // Set pin fragment as beginning fragment
        showCreatePin();

        switch (mSetupMode) {
            case ADD_PIN:
                showCreatePin();
                break;
            case CHANGE_PIN:
                showEnterPin();
                break;
        }
    }

    public void pinCreated(String value) {
        App.getAppContext().pinTemp = value;
        showConfirmPin();
    }

    public void pinConfirmed(String value) {

        App.getAppContext().inMemoryPin = value;
        App.getAppContext().pinTemp = null;

        // save pin hash in preferences
        PrefsUtil.edit()
                .putString(PrefsUtil.PIN_HASH, UtilFunctions.pinHash(value))
                .putInt(PrefsUtil.PIN_LENGTH, value.length())
                .commit();


        if (mSetupMode == ADD_PIN) {
            try {
                new Cryptography(this).addPinActiveKey();
            } catch (Exception e) {
                e.printStackTrace();
            }
            finish();
        }
        if (mSetupMode == CHANGE_PIN) {
            // Show success message
            Toast.makeText(PinSetupActivity.this, "PIN changed!", Toast.LENGTH_SHORT).show();

            // Reset the PIN timeout. We don't want to ask for PIN again...
            TimeOutUtil.getInstance().restartTimer();

            // Go to home screen
            Intent intent = new Intent(PinSetupActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    public void correctPinEntered() {
        if (mSetupMode == CHANGE_PIN) {
            showCreatePin();
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

    private void changeFragment(Fragment fragment) {
        mFt = getSupportFragmentManager().beginTransaction();
        mFt.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        mCurrentFragment = fragment;
        mFt.replace(R.id.mainContent, mCurrentFragment);
        mFt.commit();
    }

}
