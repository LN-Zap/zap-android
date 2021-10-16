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

import zapsolutions.zap.R;
import zapsolutions.zap.customView.CustomViewPager;
import zapsolutions.zap.util.EncryptionUtil;


public class DataBackupRestoreFragment extends Fragment implements DataBackupRestorePagerAdapter.BackupAction {

    public static final String TAG = DataBackupRestoreFragment.class.getName();
    private static final String EXTRA_ENCRYPTED_BACKUP_BYTES = "encryptedBackupBytes";

    private CustomViewPager mViewPager;
    private DataBackupRestorePagerAdapter mAdapter;
    private byte[] mEncryptedBackupBytes;
    private Handler mHandler;
    private String mTempPassword;

    public static DataBackupRestoreFragment newInstance(byte[] encryptedBackupBytes) {
        DataBackupRestoreFragment fragment = new DataBackupRestoreFragment();
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ENCRYPTED_BACKUP_BYTES, encryptedBackupBytes);
        fragment.setArguments(intent.getExtras());
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_data_backup, container, false);

        Bundle args = getArguments();
        mEncryptedBackupBytes = args.getByteArray(EXTRA_ENCRYPTED_BACKUP_BYTES);
        mViewPager = view.findViewById(R.id.data_backup_viewpager);

        mAdapter = new DataBackupRestorePagerAdapter(getContext(), this);
        mViewPager.setForceNoSwipe(true);
        mViewPager.setAdapter(mAdapter);
        mHandler = new Handler();

        mHandler.postDelayed(() -> {
            showKeyboard();
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
        getActivity().finish();
    }

    private void startRestoreProcess() {
        mHandler.postDelayed(() -> {
            byte[] decryptedBackupBytes = EncryptionUtil.PasswordDecryptData(mEncryptedBackupBytes, mTempPassword);
            mTempPassword = "";
            if (decryptedBackupBytes != null) {
                String decryptedBackup = new String(decryptedBackupBytes, StandardCharsets.UTF_8);
                DataBackupUtil.restoreBackup(decryptedBackup);
                mAdapter.setBackupRestoreFinished(true);
            } else {
                mAdapter.setBackupRestoreFinished(false);
            }
        }, 500);
    }
}

