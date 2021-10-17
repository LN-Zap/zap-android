package zapsolutions.zap.backup;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;

import zapsolutions.zap.R;
import zapsolutions.zap.util.OnSingleClickListener;


public class DataBackupIntroFragment extends Fragment {

    public static final String TAG = DataBackupIntroFragment.class.getName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_data_backup_intro, container, false);

        Button buttonStartBackup = view.findViewById(R.id.data_backup_intro_create_button);
        buttonStartBackup.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (DataBackupUtil.isThereAnythingToBackup()) {
                    ((BackupActivity) getActivity()).changeFragment(new DataBackupCreateFragment());
                } else {
                    Toast.makeText(getActivity(), R.string.backup_data_no_data, Toast.LENGTH_LONG).show();
                }
            }
        });
        Button buttonRestoreBackup = view.findViewById(R.id.data_backup_intro_restore_button);
        buttonRestoreBackup.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                openOpenFileDialog();
            }
        });

        return view;
    }

    ActivityResultLauncher<Intent> openDialogResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Uri uri = data.getData();

                    try {
                        InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
                        byte[] encryptedBackupBytes = ByteStreams.toByteArray(inputStream);
                        ((BackupActivity) getActivity()).changeFragment(DataBackupRestoreFragment.newInstance(encryptedBackupBytes));
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), R.string.backup_data_open_backupfile_error, Toast.LENGTH_LONG).show();
                        getActivity().finish();
                    }
                }
            });

    public void openOpenFileDialog() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zdb");
        openDialogResultLauncher.launch(intent);
    }
}

