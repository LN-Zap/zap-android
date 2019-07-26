package zapsolutions.zap.setup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import zapsolutions.zap.HomeActivity;
import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.App;
import zapsolutions.zap.baseClasses.BaseAppCompatActivity;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.TimeOutUtil;
import zapsolutions.zap.util.UtilFunctions;


public class SetupActivity extends BaseAppCompatActivity {

    public static final int FULL_SETUP = 0;
    public static final int CHANGE_PIN = 1;
    public static final int CHANGE_CONNECTION = 2;
    public static final int ADD_WALLET = 3;

    private static final String LOG_TAG = SetupActivity.class.getName();

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
            mSetupMode = extras.getInt("setupMode", 0);
        }


        // Set pin fragment as beginning fragment
        showCreatePin();

        switch (mSetupMode) {
            case FULL_SETUP:
                showCreatePin();
                break;
            case CHANGE_PIN:
                showEnterPin();
                break;
            case CHANGE_CONNECTION:
                showEnterPin();
                break;
            case ADD_WALLET:
                showConnectChoice();
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

        // save pin hash in preferences
        PrefsUtil.edit()
                .putString(PrefsUtil.PIN_HASH, UtilFunctions.pinHash(value))
                .putInt(PrefsUtil.PIN_LENGTH, value.length())
                .commit();


        if (mSetupMode == FULL_SETUP) {
            showConnectChoice();
        }
        if (mSetupMode == CHANGE_PIN) {
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
        if (mSetupMode == CHANGE_CONNECTION){
            changeFragment(ConnectFragment.newInstance(ConnectFragment.MODE_MODIFY));
        } else {
            changeFragment(ConnectFragment.newInstance(ConnectFragment.MODE_ADD));
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
