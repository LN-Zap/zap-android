package zapsolutions.zap.channelManagement;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.transition.ChangeBounds;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import com.github.lightningnetwork.lnd.lnrpc.Channel;
import com.github.lightningnetwork.lnd.lnrpc.PendingChannelsResponse;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import zapsolutions.zap.R;
import zapsolutions.zap.util.BlockExplorer;
import zapsolutions.zap.util.ClipBoardUtil;
import zapsolutions.zap.util.MonetaryUtil;
import zapsolutions.zap.util.TimeFormatUtil;
import zapsolutions.zap.util.Wallet;
import zapsolutions.zap.util.ZapLog;

public class ChannelDetailBSDFragment extends BottomSheetDialogFragment implements Wallet.ChannelCloseUpdateListener {

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
    private Button mCloseChannelButton;

    private ConstraintLayout mContentLayout;
    private ConstraintLayout mClosingTxLayout;
    private TextView mClosingTxText;
    private ImageView mClosingTxCopyIcon;
    private TextView mForceClosingTxTimeLabel;
    private TextView mForceClosingTxTimeText;
    private String mChannelPoint;

    private ConstraintLayout mRootLayout;
    private View mProgressScreen;
    private View mFinishedScreen;
    private Button mOkButton;
    private ImageView mProgressResultIcon;
    private ImageView mProgressThunderIcon;
    private TextView mTvFinishedText;
    private TextView mTvFinishedText2;
    private View mProgressBar;
    private ImageView mIvBsdIcon;

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

        mContentLayout = view.findViewById(R.id.contentLayout);
        mClosingTxLayout = view.findViewById(R.id.closingTxLayout);
        mClosingTxText = view.findViewById(R.id.closingTxText);
        mClosingTxCopyIcon = view.findViewById(R.id.closingTxCopyIcon);

        mCloseChannelButton = view.findViewById(R.id.channelCloseButton);

        mForceClosingTxTimeLabel = view.findViewById(R.id.closingTxTimeLabel);
        mForceClosingTxTimeText = view.findViewById(R.id.closingTxTimeText);

        mRootLayout = view.findViewById(R.id.rootLayout);
        mProgressScreen = view.findViewById(R.id.closeChannelProgressLayout);
        mFinishedScreen = view.findViewById(R.id.closeChannelFinishedLayout);
        mOkButton = view.findViewById(R.id.okButton);
        mProgressResultIcon = view.findViewById(R.id.progressResultIcon);
        mProgressThunderIcon = view.findViewById(R.id.closeChannelProgressThunderIcon);
        mTvFinishedText = view.findViewById(R.id.finishedText);
        mTvFinishedText2 = view.findViewById(R.id.finishedText2);
        mProgressBar = view.findViewById(R.id.progressBar);
        mIvBsdIcon = view.findViewById(R.id.bsdIcon);

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
            } catch (InvalidProtocolBufferException | NullPointerException exception) {
                Log.e(TAG, "Failed to parse channel.", exception);
                dismiss();
            }
        }

        remotePublicKeyIcon.setOnClickListener(view1 -> ClipBoardUtil.copyToClipboard(getContext(), "remotePubKey", mRemotePubKey.getText()));
        fundingTxIcon.setOnClickListener(view1 -> ClipBoardUtil.copyToClipboard(getContext(), "fundingTransaction", mFundingTx.getText()));
        mFundingTx.setOnClickListener(view1 -> new BlockExplorer().showTransaction(mFundingTx.getText().toString(), getActivity()));
        closeButton.setOnClickListener(view1 -> dismiss());

        mOkButton.setOnClickListener(v -> dismiss());

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
        mProgressScreen.setVisibility(View.VISIBLE);
        mCloseChannelButton.setEnabled(false);

        // Animate out
        AlphaAnimation animateOut = new AlphaAnimation(1.0f, 0f);
        animateOut.setDuration(200);
        animateOut.setFillAfter(true);

        mContentLayout.startAnimation(animateOut);
        mIvBsdIcon.startAnimation(animateOut);

        // Set size of progress result icon to 0
        mProgressResultIcon.setScaleX(0);
        mProgressResultIcon.setScaleY(0);

        // Animate in
        mProgressScreen.setAlpha(1.0f);
        AlphaAnimation animateIn = new AlphaAnimation(0f, 1.0f);
        animateIn.setDuration(200);
        animateIn.setStartOffset(200);
        animateIn.setFillAfter(true);

        mProgressScreen.startAnimation(animateIn);
    }

    private void switchToFinishScreen(boolean success, String error) {
        mFinishedScreen.setVisibility(View.VISIBLE);

        // Animate Layout changes
        ConstraintSet csRoot = new ConstraintSet();
        csRoot.clone(mRootLayout);
        csRoot.connect(mProgressScreen.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        csRoot.setVerticalBias(mProgressScreen.getId(), 0.0f);

        Transition transition = new ChangeBounds();
        transition.setInterpolator(new DecelerateInterpolator(3));
        transition.setDuration(1000);
        TransitionManager.beginDelayedTransition(mRootLayout, transition);
        csRoot.applyTo(mRootLayout);

        // Animate result icon switch
        if (!success) {
            mProgressResultIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_failed_circle_black_60dp));
            mProgressResultIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.superRed)));
        } else {
            mProgressResultIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_circle_black_60dp));
            mProgressResultIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.superGreen)));
        }

        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(mProgressResultIcon, "scaleX", 0f, 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(mProgressResultIcon, "scaleY", 0f, 1f);
        scaleUpX.setDuration(500);
        scaleUpY.setDuration(500);

        AnimatorSet scaleUpIcon = new AnimatorSet();
        scaleUpIcon.play(scaleUpX).with(scaleUpY);
        scaleUpIcon.start();

        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(mProgressBar, "scaleX", 1f, 0f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(mProgressBar, "scaleY", 1f, 0f);
        ObjectAnimator scaleDownX2 = ObjectAnimator.ofFloat(mProgressThunderIcon, "scaleX", 1f, 0f);
        ObjectAnimator scaleDownY2 = ObjectAnimator.ofFloat(mProgressThunderIcon, "scaleY", 1f, 0f);
        scaleDownX.setDuration(500);
        scaleDownY.setDuration(500);
        scaleDownX2.setDuration(500);
        scaleDownY2.setDuration(500);

        AnimatorSet scaleDownIcon = new AnimatorSet();
        scaleDownIcon.play(scaleDownX).with(scaleDownY).with(scaleDownX2).with(scaleDownY2);
        scaleDownIcon.start();

        if (success) {
            mTvFinishedText.setText(R.string.success);
            mTvFinishedText2.setText(R.string.channel_close_success);
        } else {
            mTvFinishedText.setText(R.string.channel_close_error);
            mTvFinishedText.setTextColor(getResources().getColor(R.color.superRed));
            mTvFinishedText2.setText(error);
        }

        // Animate in
        mFinishedScreen.setAlpha(1.0f);
        AlphaAnimation animateIn = new AlphaAnimation(0f, 1.0f);
        animateIn.setDuration(300);
        animateIn.setStartOffset(300);
        animateIn.setFillAfter(true);

        mFinishedScreen.startAnimation(animateIn);

        // Enable Ok button
        mOkButton.setEnabled(true);
    }

    @Override
    public int getTheme() {
        return R.style.ZapBottomSheetDialogTheme;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        Wallet.getInstance().unregisterChannelCloseUpdateListener(this);
        super.onDismiss(dialog);
    }

    @Override
    public void onChannelCloseUpdate(String channelPoint, int status, String message) {
        ZapLog.debug(TAG, "Channel close: " + channelPoint + " status=(" + status + ")");

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
