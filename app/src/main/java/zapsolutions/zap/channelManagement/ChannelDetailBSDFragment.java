package zapsolutions.zap.channelManagement;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.transition.TransitionManager;

import com.github.lightningnetwork.lnd.lnrpc.Channel;
import com.github.lightningnetwork.lnd.lnrpc.PendingChannelsResponse;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import zapsolutions.zap.R;
import zapsolutions.zap.customView.BSDProgressView;
import zapsolutions.zap.customView.BSDResultView;
import zapsolutions.zap.customView.BSDScrollableMainView;
import zapsolutions.zap.fragments.ZapBSDFragment;
import zapsolutions.zap.util.BlockExplorer;
import zapsolutions.zap.util.ClipBoardUtil;
import zapsolutions.zap.util.MonetaryUtil;
import zapsolutions.zap.util.TimeFormatUtil;
import zapsolutions.zap.util.Wallet;
import zapsolutions.zap.util.ZapLog;

public class ChannelDetailBSDFragment extends ZapBSDFragment implements Wallet.ChannelCloseUpdateListener {

    static final String TAG = ChannelDetailBSDFragment.class.getName();
    static final String ARGS_CHANNEL = "CHANNEL";
    static final String ARGS_TYPE = "TYPE";

    private BSDScrollableMainView mBSDScrollableMainView;
    private BSDResultView mResultView;
    private BSDProgressView mProgressView;
    private ConstraintLayout mContentTopLayout;

    private TextView mNodeAlias;
    private ImageView mStatusDot;
    private TextView mRemotePubKey;
    private ProgressBar mBalanceBarLocal;
    private ProgressBar mBalanceBarRemote;
    private TextView mLocalBalance;
    private TextView mRemoteBalance;
    private TextView mFundingTx;
    private Button mCloseChannelButton;

    private ConstraintLayout mChannelDetailsLayout;
    private ConstraintLayout mClosingTxLayout;
    private TextView mClosingTxText;
    private ImageView mClosingTxCopyIcon;
    private TextView mForceClosingTxTimeLabel;
    private TextView mForceClosingTxTimeText;
    private String mChannelPoint;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bsd_channeldetail, container);

        mBSDScrollableMainView = view.findViewById(R.id.scrollableBottomSheet);
        mResultView = view.findViewById(R.id.resultLayout);
        mContentTopLayout = view.findViewById(R.id.contentTopLayout);
        mProgressView = view.findViewById(R.id.paymentProgressLayout);
        mNodeAlias = view.findViewById(R.id.nodeAlias);
        mStatusDot = view.findViewById(R.id.statusDot);
        mRemotePubKey = view.findViewById(R.id.remotePubKeyText);
        mBalanceBarLocal = view.findViewById(R.id.balanceBarLocal);
        mBalanceBarRemote = view.findViewById(R.id.balanceBarRemote);
        mLocalBalance = view.findViewById(R.id.localBalance);
        mRemoteBalance = view.findViewById(R.id.remoteBalance);
        mFundingTx = view.findViewById(R.id.fundingTxText);
        mChannelDetailsLayout = view.findViewById(R.id.channelDetailsLayout);
        mClosingTxLayout = view.findViewById(R.id.closingTxLayout);
        mClosingTxText = view.findViewById(R.id.closingTxText);
        mClosingTxCopyIcon = view.findViewById(R.id.closingTxCopyIcon);
        mCloseChannelButton = view.findViewById(R.id.channelCloseButton);
        mForceClosingTxTimeLabel = view.findViewById(R.id.closingTxTimeLabel);
        mForceClosingTxTimeText = view.findViewById(R.id.closingTxTimeText);

        mBSDScrollableMainView.setOnCloseListener(this::dismiss);
        mBSDScrollableMainView.setTitleIconVisibility(true);
        mBSDScrollableMainView.setTitleVisibility(false);
        mResultView.setOnOkListener(this::dismiss);

        ImageView remotePublicKeyIcon = view.findViewById(R.id.remotePubKeyCopyIcon);
        ImageView fundingTxIcon = view.findViewById(R.id.fundingTxCopyIcon);

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
            } catch (InvalidProtocolBufferException | NullPointerException exception) {
                Log.e(TAG, "Failed to parse channel.", exception);
                dismiss();
            }
        }

        remotePublicKeyIcon.setOnClickListener(view1 -> ClipBoardUtil.copyToClipboard(getContext(), "remotePubKey", mRemotePubKey.getText()));
        fundingTxIcon.setOnClickListener(view1 -> ClipBoardUtil.copyToClipboard(getContext(), "fundingTransaction", mFundingTx.getText()));
        mFundingTx.setOnClickListener(view1 -> new BlockExplorer().showTransaction(mFundingTx.getText().toString(), getActivity()));

        return view;
    }

    private void bindOpenChannel(ByteString channelString) throws InvalidProtocolBufferException {
        Channel channel = Channel.parseFrom(channelString);
        mNodeAlias.setText(Wallet.getInstance().getNodeAliasFromPubKey(channel.getRemotePubkey(), getContext()));
        mRemotePubKey.setText(channel.getRemotePubkey());
        mFundingTx.setText(channel.getChannelPoint().substring(0, channel.getChannelPoint().indexOf(':')));

        // register for channel close events and keep channel point for later comparison
        Wallet.getInstance().registerChannelCloseUpdateListener(this);
        mChannelPoint = channel.getChannelPoint();

        showClosingButton(!channel.getActive(), channel.getCsvDelay());

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

        showClosingTransaction(pendingCloseChannel.getClosingTxid());

        setBasicInformation(pendingCloseChannel.getChannel().getRemoteNodePub(),
                pendingCloseChannel.getChannel().getRemoteNodePub(),
                R.color.superRed,
                pendingCloseChannel.getChannel().getChannelPoint());

        setBalances(pendingCloseChannel.getChannel().getLocalBalance(), pendingCloseChannel.getChannel().getRemoteBalance(), pendingCloseChannel.getChannel().getCapacity());
    }

    private void bindForceClosingChannel(ByteString channelString) throws InvalidProtocolBufferException {
        PendingChannelsResponse.ForceClosedChannel forceClosedChannel = PendingChannelsResponse.ForceClosedChannel.parseFrom(channelString);

        showClosingTransaction(forceClosedChannel.getClosingTxid());

        setBasicInformation(forceClosedChannel.getChannel().getRemoteNodePub(),
                forceClosedChannel.getChannel().getRemoteNodePub(),
                R.color.superRed,
                forceClosedChannel.getChannel().getChannelPoint());

        showForceClosingTime(forceClosedChannel.getBlocksTilMaturity());
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

    private void showClosingTransaction(String closingTransaction) {
        mClosingTxLayout.setVisibility(View.VISIBLE);
        mClosingTxText.setText(closingTransaction);
        mClosingTxText.setOnClickListener(view1 -> new BlockExplorer().showTransaction(closingTransaction, getActivity()));
        mClosingTxCopyIcon.setOnClickListener(view1 -> ClipBoardUtil.copyToClipboard(getContext(), "closingTransaction", closingTransaction));
    }

    private void showForceClosingTime(int maturity) {
        String expiryText = TimeFormatUtil.formattedDuration(maturity * 10 * 60, getContext()).toLowerCase();
        mForceClosingTxTimeLabel.setVisibility(View.VISIBLE);
        mForceClosingTxTimeText.setVisibility(View.VISIBLE);
        mForceClosingTxTimeText.setText(expiryText);
    }

    private void showClosingButton(boolean forceClose, int csvDelay) {
        mCloseChannelButton.setVisibility(View.VISIBLE);
        mCloseChannelButton.setText(forceClose ? getText(R.string.channel_close_force) : getText(R.string.channel_close));
        mCloseChannelButton.setOnClickListener(view1 -> closeChannel(forceClose, csvDelay));
    }

    private void closeChannel(boolean force, int csvDelay) {
        String lockUpTime = TimeFormatUtil.formattedDuration(csvDelay * 10 * 60, getContext()).toLowerCase();
        new AlertDialog.Builder(getContext())
                .setTitle(force ? R.string.channel_close_force : R.string.channel_close)
                .setMessage(getString(force ? R.string.channel_close_force_confirmation : R.string.channel_close_confirmation, mNodeAlias.getText(), lockUpTime))
                .setCancelable(true)
                .setPositiveButton(R.string.ok, (dialog, whichButton) -> {
                    switchToProgressScreen();
                    Wallet.getInstance().closeChannel(mChannelPoint, force);
                })
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                })
                .show();
    }

    private void switchToProgressScreen() {
        mProgressView.setVisibility(View.VISIBLE);
        mChannelDetailsLayout.setVisibility(View.INVISIBLE);
        mProgressView.startSpinning();
        mBSDScrollableMainView.animateTitleOut();
    }

    private void switchToFinishScreen(boolean success, String error) {
        TransitionManager.beginDelayedTransition((ViewGroup) mContentTopLayout.getRootView());
        mResultView.setVisibility(View.VISIBLE);
        mProgressView.spinningFinished(success);
        mChannelDetailsLayout.setVisibility(View.GONE);

        if (success) {
            mResultView.setHeading(R.string.success, true);
            mResultView.setDetailsText(R.string.channel_close_success);
        } else {
            mResultView.setHeading(R.string.channel_close_error, false);
            mResultView.setDetailsText(error);
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        Wallet.getInstance().unregisterChannelCloseUpdateListener(this);
        super.onDismiss(dialog);
    }

    @Override
    public void onChannelCloseUpdate(String channelPoint, int status, String message) {
        ZapLog.d(TAG, "Channel close: " + channelPoint + " status=(" + status + ")");

        if (getActivity() != null && mChannelPoint.equals(channelPoint)) {

            // fetch channels after closing finished
            Wallet.getInstance().updateLNDChannelsWithDebounce();

            getActivity().runOnUiThread(() -> {
                if (status == Wallet.ChannelCloseUpdateListener.SUCCESS) {
                    switchToFinishScreen(true, null);
                } else {
                    switchToFinishScreen(false, getDetailedErrorMessage(status, message));
                }
            });
        }
    }

    private String getDetailedErrorMessage(int error, String message) {
        switch (error) {
            case ERROR_PEER_OFFLINE:
                return getString(R.string.error_channel_close_offline);
            case ERROR_CHANNEL_TIMEOUT:
                return getString(R.string.error_channel_close_timeout);
            case ERROR_CHANNEL_CLOSE:
            default:
                return getString(R.string.error_channel_close, message);
        }
    }
}
