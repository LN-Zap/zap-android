package zapsolutions.zap.setup;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.App;
import zapsolutions.zap.baseClasses.BaseAppCompatActivity;
import zapsolutions.zap.pin.PinActivityInterface;
import zapsolutions.zap.pin.PinFragment;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.RefConstants;
import zapsolutions.zap.util.UtilFunctions;


public class SetupActivity extends BaseAppCompatActivity implements PinActivityInterface {

    public static final int FULL_SETUP = 0;
    public static final int CHANGE_CONNECTION = 1;

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
            mSetupMode = extras.getInt(RefConstants.SETUP_MODE, 0);
        }

        switch (mSetupMode) {
            case FULL_SETUP:
                showConnectChoice();
                break;
            case CHANGE_CONNECTION:
                showEnterPin();
                break;
        }
    }

    public void correctPinEntered() {
        if (mSetupMode == CHANGE_CONNECTION) {
            showConnectChoice();
        }
    }

    private void showEnterPin() {
        changeFragment(PinFragment.newInstance(PinFragment.ENTER_MODE, getResources().getString(R.string.pin_enter)));
    }

    private void showConnectChoice() {
        changeFragment(new ConnectFragment());
    }

    private void changeFragment(Fragment fragment) {
        mFt = getSupportFragmentManager().beginTransaction();
        mFt.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        mCurrentFragment = fragment;
        mFt.replace(R.id.mainContent, mCurrentFragment);
        mFt.commit();
    }

}
