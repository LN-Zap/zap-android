package zapsolutions.zap.channelManagement;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.github.lightningnetwork.lnd.lnrpc.NodeInfo;
import zapsolutions.zap.R;
import zapsolutions.zap.util.MonetaryUtil;
import zapsolutions.zap.util.Wallet;

public class ChannelViewHolder extends RecyclerView.ViewHolder {

    TextView mStatus;
    ImageView mStatusDot;
    View mRootView;
    Context mContext;

    private TextView mRemoteName;
    private TextView mLocalBalance;
    private TextView mRemoteBalance;
    private TextView mCapacity;
    private ProgressBar mLocalBar;
    private ProgressBar mRemoteBar;

    ChannelViewHolder(@NonNull View itemView) {
        super(itemView);

        mRemoteName = itemView.findViewById(R.id.remoteName);
        mStatus = itemView.findViewById(R.id.state);
        mStatusDot = itemView.findViewById(R.id.statusDot);
        mLocalBalance = itemView.findViewById(R.id.localBalance);
        mRemoteBalance = itemView.findViewById(R.id.remoteBalance);
        mCapacity = itemView.findViewById(R.id.capacity);
        mLocalBar = itemView.findViewById(R.id.localBar);
        mRemoteBar = itemView.findViewById(R.id.remoteBar);
        mRootView = itemView.findViewById(R.id.transactionRootView);
        mContext = itemView.getContext();
    }

    public void setName(String channelRemotePubKey) {
        // Set name
        for (NodeInfo i : Wallet.getInstance().mNodeInfos) {
            if (i.getNode().getPubKey().equals(channelRemotePubKey)) {
                if (i.getNode().getAlias().startsWith(i.getNode().getPubKey().substring(0, 8))) {
                    String unnamed = mContext.getResources().getString(R.string.channel_no_alias);
                    mRemoteName.setText(unnamed + " (" + i.getNode().getPubKey().substring(0, 5) + "...)");
                } else {
                    mRemoteName.setText(i.getNode().getAlias());
                }
                break;
            } else {
                mRemoteName.setText(channelRemotePubKey);
            }
        }
    }

    void setBalances(long local, long remote, long capacity) {
        float localBarValue = (float) ((double) local / (double) capacity);
        float remoteBarValue = (float) ((double) remote / (double) capacity);

        mLocalBar.setProgress((int) (localBarValue * 100f));
        mRemoteBar.setProgress((int) (remoteBarValue * 100f));

        mLocalBalance.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(local));
        mRemoteBalance.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(remote));

        mCapacity.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(capacity));
    }
}
