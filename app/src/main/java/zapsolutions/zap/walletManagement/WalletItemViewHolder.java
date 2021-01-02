package zapsolutions.zap.walletManagement;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import zapsolutions.zap.R;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfig;
import zapsolutions.zap.util.OnSingleClickListener;
import zapsolutions.zap.util.PrefsUtil;

public class WalletItemViewHolder extends RecyclerView.ViewHolder {

    private static final String LOG_TAG = WalletItemViewHolder.class.getName();

    private ImageView mIcon;
    private TextView mWalletTypDescription;
    private TextView mWalletName;
    private View mRootView;
    private Context mContext;
    private ImageView mCurrentActiveIcon;

    public WalletItemViewHolder(View v) {
        super(v);

        mIcon = v.findViewById(R.id.walletTypeIcon);
        mWalletTypDescription = v.findViewById(R.id.walletTypeDescription);
        mWalletName = v.findViewById(R.id.walletName);
        mRootView = v.findViewById(R.id.transactionRootView);
        mCurrentActiveIcon = v.findViewById(R.id.currentlyActiveIcon);
        mContext = v.getContext();
    }

    public void bindRemoteWalletItem(WalletConfig walletConfig) {

        // Set Icon
        if (walletConfig.getType().equals(WalletConfig.WALLET_TYPE_LOCAL)) {
            mIcon.setImageResource(R.drawable.ic_local_black_24dp);
        } else {
            mIcon.setImageResource(R.drawable.ic_remote_black_24dp);
        }
        mIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.lightningOrange)));

        // Set current active icon visibility
        if (walletConfig.getId().equals(PrefsUtil.getCurrentWalletConfig())) {
            mCurrentActiveIcon.setVisibility(View.VISIBLE);
        } else {
            mCurrentActiveIcon.setVisibility(View.GONE);
        }

        // Set Wallet Type description
        mWalletTypDescription.setText(walletConfig.getType());

        // Set wallet Name
        mWalletName.setText(walletConfig.getAlias());

        // Set on click listener
        mRootView.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent intent = new Intent(mContext, WalletDetailsActivity.class);
                intent.putExtra(ManageWalletsActivity.WALLET_ID, walletConfig.getId());
                mContext.startActivity(intent);
            }
        });
    }
}
