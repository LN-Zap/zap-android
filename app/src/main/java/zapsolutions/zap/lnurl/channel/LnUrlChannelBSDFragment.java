package zapsolutions.zap.lnurl.channel;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.transition.ChangeBounds;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.github.lightningnetwork.lnd.lnrpc.ConnectPeerRequest;
import com.github.lightningnetwork.lnd.lnrpc.LightningAddress;
import com.github.lightningnetwork.lnd.lnrpc.ListPeersRequest;
import com.github.lightningnetwork.lnd.lnrpc.Peer;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import zapsolutions.zap.R;
import zapsolutions.zap.connection.HttpClient;
import zapsolutions.zap.connection.establishConnectionToLnd.LndConnection;
import zapsolutions.zap.fragments.RxBSDFragment;
import zapsolutions.zap.lightning.LightningNodeUri;
import zapsolutions.zap.lightning.LightningParser;
import zapsolutions.zap.lnurl.LnUrlResponse;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.RefConstants;
import zapsolutions.zap.util.TorUtil;
import zapsolutions.zap.util.Wallet;
import zapsolutions.zap.util.ZapLog;


public class LnUrlChannelBSDFragment extends RxBSDFragment {

    private static final String LOG_TAG = LnUrlChannelBSDFragment.class.getName();

    private static final String EXTRA_LNURL_CHANNEL_RESPONSE = "lnurlChannelResponse";

    private ConstraintLayout mRootLayout;
    private ImageView mIvBsdIcon;
    private TextView mTvTitle;
    private LnUrlChannelResponse mLnUrlChannelResponse;

    private View mInfoView;
    private TextView mServiceName;
    private Button mBtnOpen;

    private View mProgressScreen;
    private View mFinishedScreen;
    private Button mOkButton;
    private ImageView mProgressFinishedIcon;
    private ImageView mIvProgressPaymentTypeIcon;
    private ImageView mIvFinishedPaymentTypeIcon;
    private TextView mTvFinishedText;
    private TextView mTvFinishedText2;
    private View mProgressBar;

    private BottomSheetBehavior mBehavior;

    private Handler mHandler;


    public static LnUrlChannelBSDFragment createLnURLChannelDialog(LnUrlChannelResponse lnUrlChannelResponse) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_LNURL_CHANNEL_RESPONSE, lnUrlChannelResponse);
        LnUrlChannelBSDFragment lnUrlChannelBottomSheetDialog = new LnUrlChannelBSDFragment();
        lnUrlChannelBottomSheetDialog.setArguments(intent.getExtras());
        return lnUrlChannelBottomSheetDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mHandler = new Handler();

        Bundle args = getArguments();
        mLnUrlChannelResponse = (LnUrlChannelResponse) args.getSerializable(EXTRA_LNURL_CHANNEL_RESPONSE);

        View view = inflater.inflate(R.layout.bsd_lnurl_channel, container);

        // Apply FLAG_SECURE to dialog to prevent screen recording
        if (PrefsUtil.preventScreenRecording()) {
            getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        mRootLayout = view.findViewById(R.id.rootLayout);
        mIvBsdIcon = view.findViewById(R.id.bsdIcon);
        mTvTitle = view.findViewById(R.id.bsdTitle);
        mServiceName = view.findViewById(R.id.serviceName);
        mInfoView = view.findViewById(R.id.infoView);
        mBtnOpen = view.findViewById(R.id.openButton);
        mProgressScreen = view.findViewById(R.id.paymentProgressLayout);
        mFinishedScreen = view.findViewById(R.id.paymentFinishedLayout);
        mOkButton = view.findViewById(R.id.okButton);
        mProgressFinishedIcon = view.findViewById(R.id.progressFinishedIcon);
        mIvProgressPaymentTypeIcon = view.findViewById(R.id.progressPaymentTypeIcon);
        mIvFinishedPaymentTypeIcon = view.findViewById(R.id.finishedPaymentTypeIcon);
        mTvFinishedText = view.findViewById(R.id.finishedText);
        mTvFinishedText2 = view.findViewById(R.id.finishedText2);
        mProgressBar = view.findViewById(R.id.progressBar);


        // Action when clicked on "x" (close) button
        ImageButton btnCloseBSD = view.findViewById(R.id.closeButton);
        btnCloseBSD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


        mIvBsdIcon.setVisibility(View.INVISIBLE);
        mTvTitle.setText("");//R.string.channel_open
        URL url = null;
        try {
            url = new URL(mLnUrlChannelResponse.getCallback());
            String host = url.getHost();
            mServiceName.setText(host);
        } catch (MalformedURLException e) {
            mServiceName.setText(R.string.unknown);
            e.printStackTrace();
        }


        // Action when clicked on "Open Channel"
        mBtnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToProgressScreen();
                openChannel();
            }
        });

        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        mHandler.removeCallbacksAndMessages(null);

        super.onDestroyView();
    }


    // This gets executed after onCreateView. We edit the bottomSheetBehavior to not react to swipes
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FrameLayout bottomSheet = getDialog().findViewById(R.id.design_bottom_sheet);
        mBehavior = BottomSheetBehavior.from(bottomSheet);
        mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        mBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

    }

    @Override
    public int getTheme() {
        return R.style.ZapBottomSheetDialogTheme;
    }

    private void openChannel() {

        LightningNodeUri nodeUri = LightningParser.parseNodeUri(mLnUrlChannelResponse.getUri());

        if (nodeUri == null) {
            ZapLog.e(LOG_TAG, "Node Uri could not be parsed");
            switchToFailedScreen(getActivity().getResources().getString(R.string.lnurl_service_provided_invalid_data));
            return;
        }

        getCompositeDisposable().add(LndConnection.getInstance().getLightningService().listPeers(ListPeersRequest.newBuilder().build())
                .timeout(RefConstants.TIMEOUT_LONG * TorUtil.getTorTimeoutMultiplier(), TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(listPeersResponse -> {
                    boolean connected = false;
                    for (Peer node : listPeersResponse.getPeersList()) {
                        if (node.getPubKey().equals(nodeUri.getPubKey())) {
                            connected = true;
                            break;
                        }
                    }

                    if (connected) {
                        ZapLog.v(LOG_TAG, "Already connected to peer, moving on...");
                        sendFinalRequestToService();
                    } else {
                        ZapLog.v(LOG_TAG, "Not connected to peer, trying to connect...");
                        connectPeer(nodeUri);
                    }
                }, throwable -> {
                    ZapLog.e(LOG_TAG, "Error listing peers request: " + throwable.getMessage());
                    if (throwable.getMessage().toLowerCase().contains("terminated")) {
                        switchToFailedScreen(getActivity().getResources().getString(R.string.error_get_peers_timeout));
                    } else {
                        switchToFailedScreen(getActivity().getResources().getString(R.string.error_get_peers));
                    }
                }));
    }

    private void connectPeer(LightningNodeUri nodeUri) {
        LightningAddress lightningAddress = LightningAddress.newBuilder()
                .setHostBytes(ByteString.copyFrom(nodeUri.getHost().getBytes(StandardCharsets.UTF_8)))
                .setPubkeyBytes(ByteString.copyFrom(nodeUri.getPubKey().getBytes(StandardCharsets.UTF_8))).build();
        ConnectPeerRequest connectPeerRequest = ConnectPeerRequest.newBuilder().setAddr(lightningAddress).build();

        getCompositeDisposable().add(LndConnection.getInstance().getLightningService().connectPeer(connectPeerRequest)
                .timeout(RefConstants.TIMEOUT_LONG * TorUtil.getTorTimeoutMultiplier(), TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(connectPeerResponse -> {
                    ZapLog.v(LOG_TAG, "Successfully connected to peer");
                    sendFinalRequestToService();
                }, throwable -> {
                    ZapLog.e(LOG_TAG, "Error connecting to peer: " + throwable.getMessage());

                    if (throwable.getMessage().toLowerCase().contains("refused")) {
                        switchToFailedScreen(getActivity().getResources().getString(R.string.error_connect_peer_refused));
                    } else if (throwable.getMessage().toLowerCase().contains("self")) {
                        switchToFailedScreen(getActivity().getResources().getString(R.string.error_connect_peer_self));
                    } else if (throwable.getMessage().toLowerCase().contains("terminated")) {
                        switchToFailedScreen(getActivity().getResources().getString(R.string.error_connect_peer_timeout));
                    } else {
                        switchToFailedScreen(throwable.getMessage());
                    }
                }));
    }

    private void sendFinalRequestToService() {
        LnUrlFinalOpenChannelRequest lnUrlFinalOpenChannelRequest = new LnUrlFinalOpenChannelRequest.Builder()
                .setCallback(mLnUrlChannelResponse.getCallback())
                .setK1(mLnUrlChannelResponse.getK1())
                .setRemoteId(Wallet.getInstance().getIdentityPubKey())
                .setIsPrivate(false)
                .build();

        ZapLog.v(LOG_TAG, lnUrlFinalOpenChannelRequest.requestAsString());

        StringRequest lnUrlRequest = new StringRequest(Request.Method.GET, lnUrlFinalOpenChannelRequest.requestAsString(),
                response -> {
                    ZapLog.v(LOG_TAG, response);
                    validateFinalResponse(response);
                },
                error -> {
                    ZapLog.e(LOG_TAG, "Final request failed");
                    switchToFailedScreen("Final request failed");
                });

        // Send final request to LNURL service
        HttpClient.getInstance().addToRequestQueue(lnUrlRequest, "LnUrlFinalChannelRequest");
    }

    private void validateFinalResponse(@NonNull String openChannelResponse) {
        LnUrlResponse lnUrlResponse = new Gson().fromJson(openChannelResponse, LnUrlResponse.class);

        if (lnUrlResponse.getStatus().equals("OK")) {
            ZapLog.d(LOG_TAG, "LNURL: Success. The service initiated the channel opening.");
            switchToSuccessScreen();
        } else {
            ZapLog.e(LOG_TAG, "LNURL: Failed to open channel. " + lnUrlResponse.getReason());
            switchToFailedScreen(lnUrlResponse.getReason());
        }
    }

    private void switchToProgressScreen() {

        // make previous buttons and edit texts unclickable
        mBtnOpen.setEnabled(false);

        // Animate out
        AlphaAnimation animateOut = new AlphaAnimation(1.0f, 0f);
        animateOut.setDuration(200);
        animateOut.setFillAfter(true);

        mInfoView.startAnimation(animateOut);

        mBtnOpen.startAnimation(animateOut);
        mTvTitle.startAnimation(animateOut);
        //mIvBsdIcon.startAnimation(animateOut);

        // Set size of progress finished icon to 0
        mProgressFinishedIcon.setScaleX(0);
        mProgressFinishedIcon.setScaleY(0);

        // Animate in
        mProgressScreen.setAlpha(1.0f);
        AlphaAnimation animateIn = new AlphaAnimation(0f, 1.0f);
        animateIn.setDuration(200);
        animateIn.setStartOffset(200);
        animateIn.setFillAfter(true);

        mProgressScreen.startAnimation(animateIn);
    }

    private void switchToSuccessScreen() {

        // Animate Layout changes
        ConstraintSet csRoot = new ConstraintSet();
        csRoot.clone(mRootLayout);
        csRoot.connect(mProgressScreen.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        csRoot.setVerticalBias(mProgressScreen.getId(), 0.0f);

        Transition transition = new ChangeBounds();
        transition.setInterpolator(new DecelerateInterpolator(3));
        transition.setDuration(1000);
        //transition.setStartDelay(200);
        TransitionManager.beginDelayedTransition(mRootLayout, transition);
        csRoot.applyTo(mRootLayout);

        // Animate finished Icon switch
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(mProgressFinishedIcon, "scaleX", 0f, 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(mProgressFinishedIcon, "scaleY", 0f, 1f);
        scaleUpX.setDuration(500);
        scaleUpY.setDuration(500);

        AnimatorSet scaleUpIcon = new AnimatorSet();
        //scaleUpIcon.setInterpolator(new AnticipateOvershootInterpolator(1.0f));
        scaleUpIcon.play(scaleUpX).with(scaleUpY);
        scaleUpIcon.start();

        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(mProgressBar, "scaleX", 1f, 0f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(mProgressBar, "scaleY", 1f, 0f);
        ObjectAnimator scaleDownX2 = ObjectAnimator.ofFloat(mIvProgressPaymentTypeIcon, "scaleX", 1f, 0f);
        ObjectAnimator scaleDownY2 = ObjectAnimator.ofFloat(mIvProgressPaymentTypeIcon, "scaleY", 1f, 0f);
        scaleDownX.setDuration(500);
        scaleDownY.setDuration(500);
        scaleDownX2.setDuration(500);
        scaleDownY2.setDuration(500);

        AnimatorSet scaleDownIcon = new AnimatorSet();
        //scaleUpIcon.setInterpolator(new AnticipateOvershootInterpolator(1.0f));
        scaleDownIcon.play(scaleDownX).with(scaleDownY).with(scaleDownX2).with(scaleDownY2);
        scaleDownIcon.start();

        // Animate in
        mFinishedScreen.setAlpha(1.0f);
        AlphaAnimation animateIn = new AlphaAnimation(0f, 1.0f);
        animateIn.setDuration(300);
        animateIn.setStartOffset(300);
        animateIn.setFillAfter(true);

        mFinishedScreen.startAnimation(animateIn);

        mTvFinishedText.setText(getActivity().getResources().getString(R.string.success));
        mTvFinishedText2.setText(getActivity().getResources().getString(R.string.lnurl_channel_pending_hint));

        // Enable Ok button
        mOkButton.setEnabled(true);
    }

    private void switchToFailedScreen(String error) {

        // Animate Layout changes
        ConstraintSet csRoot = new ConstraintSet();
        csRoot.clone(mRootLayout);
        csRoot.connect(mProgressScreen.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        csRoot.setVerticalBias(mProgressScreen.getId(), 0.0f);

        Transition transition = new ChangeBounds();
        transition.setInterpolator(new DecelerateInterpolator(3));
        transition.setDuration(1000);
        //transition.setStartDelay(200);
        TransitionManager.beginDelayedTransition(mRootLayout, transition);
        csRoot.applyTo(mRootLayout);

        // Animate finished Icon switch
        mProgressFinishedIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_failed_circle_black_60dp));
        mProgressFinishedIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.superRed)));
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(mProgressFinishedIcon, "scaleX", 0f, 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(mProgressFinishedIcon, "scaleY", 0f, 1f);
        scaleUpX.setDuration(500);
        scaleUpY.setDuration(500);

        AnimatorSet scaleUpIcon = new AnimatorSet();
        //scaleUpIcon.setInterpolator(new AnticipateOvershootInterpolator(1.0f));
        scaleUpIcon.play(scaleUpX).with(scaleUpY);
        scaleUpIcon.start();

        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(mProgressBar, "scaleX", 1f, 0f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(mProgressBar, "scaleY", 1f, 0f);
        ObjectAnimator scaleDownX2 = ObjectAnimator.ofFloat(mIvProgressPaymentTypeIcon, "scaleX", 1f, 0f);
        ObjectAnimator scaleDownY2 = ObjectAnimator.ofFloat(mIvProgressPaymentTypeIcon, "scaleY", 1f, 0f);
        scaleDownX.setDuration(500);
        scaleDownY.setDuration(500);
        scaleDownX2.setDuration(500);
        scaleDownY2.setDuration(500);

        AnimatorSet scaleDownIcon = new AnimatorSet();
        //scaleUpIcon.setInterpolator(new AnticipateOvershootInterpolator(1.0f));
        scaleDownIcon.play(scaleDownX).with(scaleDownY).with(scaleDownX2).with(scaleDownY2);
        scaleDownIcon.start();

        // Set failed states
        mTvFinishedText.setText(R.string.error);
        mTvFinishedText.setTextColor(getResources().getColor(R.color.superRed));
        mTvFinishedText2.setText(error);

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
}
