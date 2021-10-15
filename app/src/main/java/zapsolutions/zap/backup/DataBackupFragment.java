package zapsolutions.zap.backup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import zapsolutions.zap.R;
import zapsolutions.zap.customView.CustomViewPager;
import zapsolutions.zap.util.EncryptionUtil;
import zapsolutions.zap.util.ZapLog;


public class DataBackupFragment extends Fragment implements DataBackupPagerAdapter.BackupAction {

    public static final String TAG = DataBackupFragment.class.getName();

    private CustomViewPager mViewPager;

    public static DataBackupFragment newInstance() {
        DataBackupFragment fragment = new DataBackupFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_data_backup, container, false);
        mViewPager = view.findViewById(R.id.data_backup_viewpager);

        DataBackupPagerAdapter dataBackupPagerAdapter = new DataBackupPagerAdapter(getContext(), this);
        mViewPager.setForceNoSwipe(true);
        mViewPager.setAdapter(dataBackupPagerAdapter);

        return view;
    }

    @Override
    public void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    @Override
    public void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getView().getRootView().getWindowToken(), 0);
    }

    @Override
    public void onCreateBackupStarted() {
        mViewPager.setCurrentItem(1);
    }

    @Override
    public void onCreateBackupPasswordEntered() {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        openSaveFileDialog("ZapBackup_" + timeStamp);
        mViewPager.setCurrentItem(3);
    }

    @Override
    public void onRestoreBackupStarted() {
        openOpenFileDialog();
    }

    @Override
    public void onConfirmed() {
        mViewPager.setCurrentItem(2);
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
                            OutputStream fileOutputStream = getActivity().getContentResolver().openOutputStream(data.getData());
                            byte[] backup = DataBackupUtil.createBackup("pw");
                            fileOutputStream.write(backup);
                            fileOutputStream.close();
                            ZapLog.d(TAG, "Backup file creation was successful.");
                        } catch (IOException | NullPointerException e) {
                            e.printStackTrace();
                            ZapLog.w(TAG, "Error writing backup file.");
                            Toast.makeText(getActivity(), getString(R.string.backup_data_error_writing_file), Toast.LENGTH_LONG).show();
                            getActivity().finish();
                        }
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.backup_data_error_writing_file), Toast.LENGTH_LONG).show();
                        ZapLog.w(TAG, "The result data was empty. Unable to create backup.");
                        getActivity().finish();
                    }
                }
            });

    ActivityResultLauncher<Intent> openDialogResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Uri uri = data.getData();

                    try {
                        InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
                        byte[] encryptedBackupBytes = ByteStreams.toByteArray(inputStream);
                        byte[] decryptedBackupBytes = EncryptionUtil.PasswordDecryptData(encryptedBackupBytes, "pw");
                        String decryptedBackup = new String(decryptedBackupBytes, StandardCharsets.UTF_8);
                        getActivity().finish();
                    } catch (IOException e) {
                        e.printStackTrace();
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

    public void openOpenFileDialog() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zdb");
        openDialogResultLauncher.launch(intent);
    }
}

