package zapsolutions.zap.transactionHistory.listItems;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import zapsolutions.zap.util.PrefsUtil;

public abstract class HistoryItemViewHolder extends RecyclerView.ViewHolder implements SharedPreferences.OnSharedPreferenceChangeListener {
    protected Context mContext;

    public HistoryItemViewHolder(View v) {
        super(v);
        mContext = v.getContext();
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    public void unregisterPrefListener() {
        PrefsUtil.getPrefs().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void registerPrefListener() {
        PrefsUtil.getPrefs().registerOnSharedPreferenceChangeListener(this);
    }
}
