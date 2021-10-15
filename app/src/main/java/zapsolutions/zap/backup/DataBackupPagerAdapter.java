package zapsolutions.zap.backup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import zapsolutions.zap.R;
import zapsolutions.zap.util.OnSingleClickListener;

public class DataBackupPagerAdapter extends PagerAdapter {

    private Context mContext;
    private BackupAction mBackupAction;

    public DataBackupPagerAdapter(Context context, BackupAction backupAction) {
        mContext = context;
        mBackupAction = backupAction;
    }

    @Override
    public int getCount() {
        return 4; // 4 words and final confirmation
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View backupView;
        if (position == 0) {
            // intro
            backupView = inflater.inflate(R.layout.view_data_backup_intro, container, false);
            Button buttonStartBackup = backupView.findViewById(R.id.data_backup_intro_create_button);
            buttonStartBackup.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    mBackupAction.onCreateBackupStarted();
                }
            });
            Button buttonRestoreBackup = backupView.findViewById(R.id.data_backup_intro_restore_button);
            buttonRestoreBackup.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    mBackupAction.onRestoreBackupStarted();
                }
            });
        } else if (position == 1) {
            // backup create confirm
            backupView = inflater.inflate(R.layout.view_data_backup_confirm, container, false);

            CheckBox checkBoxConfirm = backupView.findViewById(R.id.data_backup_confirm_checkbox_confirm);
            Button buttonContinue = backupView.findViewById(R.id.data_backup_confirm_continue_button);
            Button buttonCancel = backupView.findViewById(R.id.data_backup_confirm_cancel_button);

            checkBoxConfirm.setOnCheckedChangeListener((buttonView, isChecked) -> {
                buttonContinue.setEnabled(isChecked);
                buttonContinue.setTextColor(isChecked ? mContext.getResources().getColor(R.color.lightningOrange) : mContext.getResources().getColor(R.color.gray));

            });

            buttonCancel.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    mBackupAction.onFinish();
                }
            });

            buttonContinue.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    // Before we finish we remove the seed from the preferences
                    // this will also trigger a notification update
                    mBackupAction.onConfirmed();
                    mBackupAction.showKeyboard();
                }
            });
        } else if (position == 2) {
            // password
            backupView = inflater.inflate(R.layout.view_data_backup_create, container, false);

            Button buttonCreate = backupView.findViewById(R.id.data_backup_create_button);
            EditText pw1 = backupView.findViewById(R.id.pw1_input);
            EditText pw2 = backupView.findViewById(R.id.pw2_input);

            pw1.requestFocus();

            buttonCreate.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    if (pw1.getText().toString().equals(pw2.getText().toString())) {
                        if (pw1.getText().toString().length() > 0) {
                            mBackupAction.hideKeyboard();
                            mBackupAction.onCreateBackupPasswordEntered();
                        } else {
                            Toast.makeText(mContext, mContext.getString(R.string.backup_data_password_empty), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(mContext, mContext.getString(R.string.backup_data_password_mismatch), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            // finished
            backupView = inflater.inflate(R.layout.view_data_backup_create_finish, container, false);

            Button finishButton = backupView.findViewById(R.id.data_backup_create_finish_button);
            finishButton.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    mBackupAction.onFinish();
                }
            });
        }

        container.addView(backupView);
        return backupView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    interface BackupAction {

        void hideKeyboard();

        void showKeyboard();

        void onCreateBackupStarted();

        void onCreateBackupPasswordEntered();

        void onRestoreBackupStarted();

        void onConfirmed();

        void onFinish();
    }
}

