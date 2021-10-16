package zapsolutions.zap.backup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import zapsolutions.zap.R;
import zapsolutions.zap.customView.CustomViewPager;
import zapsolutions.zap.util.ZapLog;


public class DataBackupCreateFragment extends Fragment implements DataBackupCreatePagerAdapter.BackupAction {

    public static final String TAG = DataBackupCreateFragment.class.getName();

    private CustomViewPager mViewPager;
    private DataBackupCreatePagerAdapter mAdapter;
    private Handler mHandler;
    private String mTempPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_data_backup, container, false);
        mViewPager = view.findViewById(R.id.data_backup_viewpager);

        mAdapter = new DataBackupCreatePagerAdapter(getContext(), this);
        mViewPager.setForceNoSwipe(true);
        mViewPager.setAdapter(mAdapter);
        mHandler = new Handler();

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
    public void onCreateBackupPasswordEntered(String password) {
        mTempPassword = password;
        hideKeyboard();
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        openSaveFileDialog("ZapBackup_" + timeStamp);
        mViewPager.setCurrentItem(2);
    }

    @Override
    public void onConfirmed() {
        mViewPager.setCurrentItem(1);
        showKeyboard();
    }

    @Override
    public void onFinish() {
        getActivity().finish();
    }

    ActivityResultLauncher<Intent> saveDialogResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data.getData() != null) {
                        try {
                            OutputStream outputStream = getActivity().getContentResolver().openOutputStream(data.getData());
                            startWritingBackupFile(outputStream);
                        } catch (IOException e) {
                            e.printStackTrace();
                            mAdapter.setBackupCreationFinished(false);
                            ZapLog.w(TAG, "Error writing backup file.");
                        }
                    } else {
                        mAdapter.setBackupCreationFinished(false);
                        ZapLog.w(TAG, "The result data was empty. Unable to create backup.");
                        getActivity().finish();
                    }
                }
            });


    public void openSaveFileDialog(String title) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zdb");
        intent.putExtra(Intent.EXTRA_TITLE, title + ".zdb");
        saveDialogResultLauncher.launch(intent);
    }

    public void startWritingBackupFile(OutputStream outputStream) {
        mHandler.postDelayed(() -> {
            hideKeyboard();
            try {
                byte[] backup = DataBackupUtil.createBackup(mTempPassword);
                mTempPassword = "";
                outputStream.write(backup);
                outputStream.close();
                mAdapter.setBackupCreationFinished(true);
                ZapLog.d(TAG, "Backup file creation was successful.");
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
                mAdapter.setBackupCreationFinished(false);
                ZapLog.w(TAG, "Error writing backup file.");
            }
        }, 500);

    }
}

