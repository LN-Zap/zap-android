package zapsolutions.zap.backup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.fragment.app.Fragment;

import java.nio.charset.StandardCharsets;

import zapsolutions.zap.HomeActivity;
import zapsolutions.zap.R;
import zapsolutions.zap.customView.CustomViewPager;
import zapsolutions.zap.util.EncryptionUtil;
import zapsolutions.zap.util.RefConstants;


public class DataBackupRestoreFragment extends Fragment implements DataBackupRestorePagerAdapter.BackupAction {

    public static final String TAG = DataBackupRestoreFragment.class.getName();
    private static final String EXTRA_ENCRYPTED_BACKUP_BYTES = "encryptedBackupBytes";
    private static final String EXTRA_BACKUP_VALID_FILE = "dataBackupValidFile";
    private static final String EXTRA_BACKUP_VERSION = "dataBackupVersion";

    private CustomViewPager mViewPager;
    private DataBackupRestorePagerAdapter mAdapter;
    private byte[] mEncryptedBackupBytes;
    private boolean mBackupFileValid;
    private int mBackupVersion;
    private Handler mHandler;
    private String mTempPassword;

    public static DataBackupRestoreFragment newInstance(byte[] encryptedBackupBytes, boolean validFile, int backupVersion) {
        DataBackupRestoreFragment fragment = new DataBackupRestoreFragment();
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ENCRYPTED_BACKUP_BYTES, encryptedBackupBytes);
        intent.putExtra(EXTRA_BACKUP_VALID_FILE, validFile);
        intent.putExtra(EXTRA_BACKUP_VERSION, backupVersion);
        fragment.setArguments(intent.getExtras());
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_data_backup, container, false);

        Bundle args = getArguments();
        mEncryptedBackupBytes = args.getByteArray(EXTRA_ENCRYPTED_BACKUP_BYTES);
        mBackupFileValid = args.getBoolean(EXTRA_BACKUP_VALID_FILE);
        mBackupVersion = args.getInt(EXTRA_BACKUP_VERSION);
        mViewPager = view.findViewById(R.id.data_backup_viewpager);

        mAdapter = new DataBackupRestorePagerAdapter(getContext(), this);
        mViewPager.setForceNoSwipe(true);
        mViewPager.setAdapter(mAdapter);
        mHandler = new Handler();

        mHandler.postDelayed(() -> {
            if (mBackupFileValid) {
                if (mBackupVersion > RefConstants.DATA_BACKUP_VERSION) {
                    mAdapter.setBackupRestoreFinished(false, R.string.backup_data_version_too_old);
                    mViewPager.setCurrentItem(2);
                } else {
                    showKeyboard();
                }
            } else {
                mAdapter.setBackupRestoreFinished(false, R.string.backup_data_invalid_file_chosen);
                mViewPager.setCurrentItem(2);
            }
        }, 500);

        return view;
    }

    public void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    public void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getView().getRootView().getWindowToken(), 0);
    }

    @Override
    public void onRestoreBackupPasswordEntered(String password) {
        hideKeyboard();
        mTempPassword = password;
        mViewPager.setCurrentItem(2);
        startRestoreProcess();
    }

    @Override
    public void onFinish() {
        Intent homeIntent = new Intent(getActivity(), HomeActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        // FinishAffinity is needed here as this forces the on destroy events from previous activities to be executed before continuing.
        getActivity().finishAffinity();
        startActivity(homeIntent);
    }

    private void startRestoreProcess() {
        mHandler.postDelayed(() -> {
            byte[] decryptedBackupBytes = EncryptionUtil.PasswordDecryptData(mEncryptedBackupBytes, mTempPassword);
            mTempPassword = "";
            if (decryptedBackupBytes != null) {
                String decryptedBackup = new String(decryptedBackupBytes, StandardCharsets.UTF_8);
                DataBackupUtil.restoreBackup(decryptedBackup, mBackupVersion);
                mAdapter.setBackupRestoreFinished(true, 0);
            } else {
                mAdapter.setBackupRestoreFinished(false, R.string.backup_data_restore_failed_description);
            }
        }, 500);
    }
}

