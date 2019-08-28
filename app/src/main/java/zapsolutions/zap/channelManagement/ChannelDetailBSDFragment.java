package zapsolutions.zap.channelManagement;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.github.lightningnetwork.lnd.lnrpc.Channel;
import com.github.lightningnetwork.lnd.lnrpc.PendingChannelsResponse;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import zapsolutions.zap.R;
import zapsolutions.zap.util.BlockExplorer;
import zapsolutions.zap.util.ClipBoardUtil;
import zapsolutions.zap.util.MonetaryUtil;
import zapsolutions.zap.util.Wallet;

public class ChannelDetailBSDFragment extends BottomSheetDialogFragment {

    static final String TAG = ChannelDetailBSDFragment.class.getName();
    static final String ARGS_CHANNEL = "CHANNEL";
    static final String ARGS_TYPE = "TYPE";

    private TextView mNodeAlias;
    private ImageView mStatusDot;
    private TextView mRemotePubKey;
    private ProgressBar mBalanceBarLocal;
    private ProgressBar mBalanceBarRemote;
    private TextView mLocalBalance;
    private TextView mRemoteBalance;
    private TextView mFundingTx;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bsd_channeldetail, container);

        mNodeAlias = view.findViewById(R.id.nodeAlias);
        mStatusDot = view.findViewById(R.id.statusDot);
        mRemotePubKey = view.findViewById(R.id.remotePubKeyText);
        mBalanceBarLocal = view.findViewById(R.id.balanceBarLocal);
        mBalanceBarRemote = view.findViewById(R.id.balanceBarRemote);
        mLocalBalance = view.findViewById(R.id.localBalance);
        mRemoteBalance = view.findViewById(R.id.remoteBalance);
        mFundingTx = view.findViewById(R.id.fundingTxText);

        ImageView remotePublicKeyIcon = view.findViewById(R.id.remotePubKeyCopyIcon);
        ImageView fundingTxIcon = view.findViewById(R.id.fundingTxCopyIcon);
        ImageButton closeButton = view.findViewById(R.id.closeButton);

        if (getArguments() != null) {
            ByteString channelString = (ByteString) getArguments().getSerializable(ARGS_CHANNEL);
            int type = getArguments().getInt(ARGS_TYPE);

            try {
                switch (type) {
                    case ChannelListItem.TYPE_OPEN_CHANNEL:
                        bindOpenChannel(channelString);
                        break;
                    case ChannelListItem.TYPE_PENDING_OPEN_CHANNEL:
                        bindPendingOpenChannel(channelString);
                        break;
                    case ChannelListItem.TYPE_WAITING_CLOSE_CHANNEL:
                        bindWaitingCloseChannel(channelString);
                        break;
                    case ChannelListItem.TYPE_PENDING_CLOSING_CHANNEL:
                        bindPendingCloseChannel(channelString);
                        break;
                    case ChannelListItem.TYPE_PENDING_FORCE_CLOSING_CHANNEL:
                        bindForceClosingChannel(channelString);
                        break;
                }
            } catch (InvalidProtocolBufferException exception) {
                Log.e(TAG, "Failed to parse channel.", exception);
                dismiss();
            } catch (NullPointerException npException) {
                Log.e(TAG, "Failed to parse channel.", npException);
                dismiss();
            }
        }

        remotePublicKeyIcon.setOnClickListener(view1 -> ClipBoardUtil.copyToClipboard(getContext(), "remotePubKey", mRemotePubKey.getText()));
        fundingTxIcon.setOnClickListener(view1 -> ClipBoardUtil.copyToClipboard(getContext(), "fundingTransaction", mFundingTx.getText()));
        mFundingTx.setOnClickListener(view1 -> new BlockExplorer().showTransaction(mFundingTx.getText().toString(), getActivity()));
        closeButton.setOnClickListener(view1 -> dismiss());

        return view;
    }


    private void bindOpenChannel(ByteString channelString) throws InvalidProtocolBufferException {
        Channel channel = Channel.parseFrom(channelString);
        mNodeAlias.setText(Wallet.getInstance().getNodeAliasFromPubKey(channel.getRemotePubkey(), getContext()));
        mRemotePubKey.setText(channel.getRemotePubkey());
        mFundingTx.setText(channel.getChannelPoint().substring(0, channel.getChannelPoint().indexOf(':')));

        if (channel.getActive()) {
            mStatusDot.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.superGreen)));
        } else {
            mStatusDot.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.gray)));
        }

        long availableCapacity = channel.getCapacity() - channel.getCommitFee();
        setBalances(channel.getLocalBalance(), channel.getRemoteBalance(), availableCapacity);
    }

    private void bindPendingOpenChannel(ByteString channelString) throws InvalidProtocolBufferException {
        PendingChannelsResponse.PendingOpenChannel pendingOpenChannel = PendingChannelsResponse.PendingOpenChannel.parseFrom(channelString);

        setBasicInformation(pendingOpenChannel.getChannel().getRemoteNodePub(),
                pendingOpenChannel.getChannel().getRemoteNodePub(),
                R.color.lightningOrange,
                pendingOpenChannel.getChannel().getChannelPoint());

        setBalances(pendingOpenChannel.getChannel().getLocalBalance(), pendingOpenChannel.getChannel().getRemoteBalance(), pendingOpenChannel.getChannel().getCapacity());
    }


    private void bindWaitingCloseChannel(ByteString channelString) throws InvalidProtocolBufferException {
        PendingChannelsResponse.WaitingCloseChannel waitingCloseChannel = PendingChannelsResponse.WaitingCloseChannel.parseFrom(channelString);

        setBasicInformation(waitingCloseChannel.getChannel().getRemoteNodePub(),
                waitingCloseChannel.getChannel().getRemoteNodePub(),
                R.color.superRed,
                waitingCloseChannel.getChannel().getChannelPoint());

        setBalances(waitingCloseChannel.getChannel().getLocalBalance(), waitingCloseChannel.getChannel().getRemoteBalance(), waitingCloseChannel.getChannel().getCapacity());
    }

    private void bindPendingCloseChannel(ByteString channelString) throws InvalidProtocolBufferException {
        PendingChannelsResponse.ClosedChannel pendingCloseChannel = PendingChannelsResponse.ClosedChannel.parseFrom(channelString);

        setBasicInformation(pendingCloseChannel.getChannel().getRemoteNodePub(),
                pendingCloseChannel.getChannel().getRemoteNodePub(),
                R.color.superRed,
                pendingCloseChannel.getChannel().getChannelPoint());

        setBalances(pendingCloseChannel.getChannel().getLocalBalance(), pendingCloseChannel.getChannel().getRemoteBalance(), pendingCloseChannel.getChannel().getCapacity());
    }

    private void bindForceClosingChannel(ByteString channelString) throws InvalidProtocolBufferException {
        PendingChannelsResponse.ForceClosedChannel forceClosedChannel = PendingChannelsResponse.ForceClosedChannel.parseFrom(channelString);

        setBasicInformation(forceClosedChannel.getChannel().getRemoteNodePub(),
                forceClosedChannel.getChannel().getRemoteNodePub(),
                R.color.superRed,
                forceClosedChannel.getChannel().getChannelPoint());

        setBalances(forceClosedChannel.getChannel().getLocalBalance(), forceClosedChannel.getChannel().getRemoteBalance(), forceClosedChannel.getChannel().getCapacity());
    }

    private void setBasicInformation(@NonNull String remoteNodePublicKey, @NonNull String remotePubKey, int statusDot, @NonNull String channelPoint) {
        mNodeAlias.setText(Wallet.getInstance().getNodeAliasFromPubKey(remoteNodePublicKey, getContext()));
        mRemotePubKey.setText(remotePubKey);
        mStatusDot.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), statusDot)));
        mFundingTx.setText(channelPoint.substring(0, channelPoint.indexOf(':')));
    }

    private void setBalances(long local, long remote, long capacity) {
        float localBarValue = (float) ((double) local / (double) capacity);
        float remoteBarValue = (float) ((double) remote / (double) capacity);

        mBalanceBarLocal.setProgress((int) (localBarValue * 100f));
        mBalanceBarRemote.setProgress((int) (remoteBarValue * 100f));

        mLocalBalance.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(local));
        mRemoteBalance.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(remote));
    }

    @Override
    public int getTheme() {
        return R.style.ZapBottomSheetDialogTheme;
    }
}
