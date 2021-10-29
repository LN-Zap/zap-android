package zapsolutions.zap.backup;

import android.os.Bundle;
import android.view.WindowManager;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.BaseAppCompatActivity;

public class BackupActivity extends BaseAppCompatActivity {

    FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        mFragmentManager = getSupportFragmentManager();


        if (mFragmentManager.findFragmentByTag(DataBackupCreateFragment.TAG) == null) {
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            DataBackupIntroFragment fragment = new DataBackupIntroFragment();
            fragmentTransaction.add(R.id.content_frame, fragment, DataBackupCreateFragment.TAG);
            fragmentTransaction.commit();
        }
    }

    public void changeFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        fragmentTransaction.replace(R.id.content_frame, fragment);
        fragmentTransaction.commit();
    }
}