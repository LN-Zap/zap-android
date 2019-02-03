package ln_zap.zap.Setup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import ln_zap.zap.HomeActivity;
import ln_zap.zap.R;
import ln_zap.zap.baseClasses.BaseAppCompatActivity;


public class SetupActivity extends BaseAppCompatActivity {

    public static final int FULL_SETUP = 0;
    public static final int CHANGE_PIN = 1;
    public static final int CHANGE_CONNECTION = 2;

    private static final String LOG_TAG = "Setup Activity";

    private Fragment mCurrentFragment = null;
    private FragmentTransaction mFt;
    private int mSetupMode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        // Receive data from last activity
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            mSetupMode = extras.getInt("setupMode",0);
        }


        // Set pin fragment as beginning fragment
        showCreatePin();

        switch(mSetupMode) {
            case FULL_SETUP:
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

    public void eulaAccepted(){
        showCreatePin();
    }

    public void pinCreated(){
        showConfirmPin();
    }

    public void pinConfirmed(){
        if (mSetupMode == FULL_SETUP){
            showConnectChoice();
        }
        if (mSetupMode == CHANGE_PIN){
            Intent intent = new Intent(SetupActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    public void correctPinEntered(){
        if (mSetupMode == CHANGE_PIN){
            showCreatePin();
        }
        if (mSetupMode == CHANGE_CONNECTION){

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isWalletSetup", false);
            editor.apply();

            showConnectChoice();

        }
    }

    private void showCreatePin(){
        if (mSetupMode == CHANGE_PIN) {
            changeFragment(PinFragment.newInstance(PinFragment.CREATE_MODE, getResources().getString(R.string.pin_enter_new)));
        }
        else{
            changeFragment(PinFragment.newInstance(PinFragment.CREATE_MODE, getResources().getString(R.string.pin_create)));
        }
    }

    private void showConfirmPin(){
        if (mSetupMode == CHANGE_PIN) {
            changeFragment(PinFragment.newInstance(PinFragment.CONFIRM_MODE, getResources().getString(R.string.pin_confirm_new)));
        }else{
            changeFragment(PinFragment.newInstance(PinFragment.CONFIRM_MODE, getResources().getString(R.string.pin_confirm)));
        }
    }

    private void showEnterPin(){
        if (mSetupMode == CHANGE_PIN) {
            changeFragment(PinFragment.newInstance(PinFragment.ENTER_MODE, getResources().getString(R.string.pin_enter_old)));
        }else{
            changeFragment(PinFragment.newInstance(PinFragment.ENTER_MODE, getResources().getString(R.string.pin_enter)));
        }
    }

    private void showConnectChoice(){
        changeFragment(new ConnectFragment());
    }

    private void showEula(){
        changeFragment(new EulaFragment());
    }

    private void changeFragment(Fragment fragment) {
        mFt = getSupportFragmentManager().beginTransaction();
        mFt.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        mCurrentFragment = fragment;
        mFt.replace(R.id.mainContent,mCurrentFragment);
        mFt.commit();
    }

}
