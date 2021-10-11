package zapsolutions.zap.backup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.fragment.app.Fragment;

import zapsolutions.zap.R;
import zapsolutions.zap.customView.CustomViewPager;


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
        mViewPager.setCurrentItem(3);
    }

    @Override
    public void onRestoreBackupStarted() {

    }

    @Override
    public void onConfirmed() {
        mViewPager.setCurrentItem(2);
    }

    @Override
    public void onFinish() {
        getActivity().finish();
    }
}

