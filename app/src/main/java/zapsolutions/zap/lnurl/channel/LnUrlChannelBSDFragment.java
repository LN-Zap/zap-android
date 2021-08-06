package zapsolutions.zap.lnurl.channel;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.transition.TransitionManager;

import com.github.lightningnetwork.lnd.lnrpc.ConnectPeerRequest;
import com.github.lightningnetwork.lnd.lnrpc.LightningAddress;
import com.github.lightningnetwork.lnd.lnrpc.ListPeersRequest;
import com.github.lightningnetwork.lnd.lnrpc.Peer;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import zapsolutions.zap.R;
import zapsolutions.zap.connection.HttpClient;
import zapsolutions.zap.connection.lndConnection.LndConnection;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfigsManager;
import zapsolutions.zap.customView.BSDProgressView;
import zapsolutions.zap.customView.BSDResultView;
import zapsolutions.zap.customView.BSDScrollableMainView;
import zapsolutions.zap.fragments.ZapBSDFragment;
import zapsolutions.zap.lightning.LightningNodeUri;
import zapsolutions.zap.lightning.LightningParser;
import zapsolutions.zap.lnurl.LnUrlResponse;
import zapsolutions.zap.util.HelpDialogUtil;
import zapsolutions.zap.util.RefConstants;
import zapsolutions.zap.util.TorUtil;
import zapsolutions.zap.util.Wallet;
import zapsolutions.zap.util.ZapLog;


public class LnUrlChannelBSDFragment extends ZapBSDFragment {

    public static final String TAG = LnUrlChannelBSDFragment.class.getName();
    private static final String EXTRA_LNURL_CHANNEL_RESPONSE = "lnurlChannelResponse";

    private BSDScrollableMainView mBSDScrollableMainView;
    private BSDResultView mResultView;
    private BSDProgressView mProgressView;
    private ConstraintLayout mContentTopLayout;
    private View mInfoView;
    private TextView mServiceName;
    private CheckBox mPrivateCheckbox;

    private LnUrlChannelResponse mLnUrlChannelResponse;

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

        Bundle args = getArguments();
        mLnUrlChannelResponse = (LnUrlChannelResponse) args.getSerializable(EXTRA_LNURL_CHANNEL_RESPONSE);

        View view = inflater.inflate(R.layout.bsd_lnurl_channel, container);

        mBSDScrollableMainView = view.findViewById(R.id.scrollableBottomSheet);
        mResultView = view.findViewById(R.id.resultLayout);
        mContentTopLayout = view.findViewById(R.id.contentTopLayout);
        mServiceName = view.findViewById(R.id.serviceName);
        mInfoView = view.findViewById(R.id.infoView);
        mProgressView = view.findViewById(R.id.paymentProgressLayout);
        mPrivateCheckbox = view.findViewById(R.id.privateCheckBox);


        mBSDScrollableMainView.setOnCloseListener(this::dismiss);
        mBSDScrollableMainView.setTitleIconVisibility(false);
        mBSDScrollableMainView.setTitle("");

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
        Button btnOpen = view.findViewById(R.id.openButton);
        btnOpen.setOnClickListener(v -> {
            if (WalletConfigsManager.getInstance().hasAnyConfigs()) {
                switchToProgressScreen();
                openChannel();
            } else {
                Toast.makeText(getActivity(), R.string.demo_setupWalletFirst, Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton privateHelpButton = view.findViewById(R.id.privateHelpButton);
        privateHelpButton.setOnClickListener(view1 -> HelpDialogUtil.showDialog(getActivity(), R.string.help_dialog_private_channels));

        mResultView.setOnOkListener(this::dismiss);

        return view;
    }

    private void openChannel() {

        ZapLog.v(TAG, "Remote Node uri: " + mLnUrlChannelResponse.getUri());
        LightningNodeUri nodeUri = LightningParser.parseNodeUri(mLnUrlChannelResponse.getUri());

        if (nodeUri == null) {
            ZapLog.e(TAG, "Node Uri could not be parsed");
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
                        ZapLog.v(TAG, "Already connected to peer, moving on...");
                        sendFinalRequestToService();
                    } else {
                        ZapLog.v(TAG, "Not connected to peer, trying to connect...");
                        connectPeer(nodeUri);
                    }
                }, throwable -> {
                    ZapLog.e(TAG, "Error listing peers request: " + throwable.getMessage());
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
                    ZapLog.v(TAG, "Successfully connected to peer");
                    sendFinalRequestToService();
                }, throwable -> {
                    ZapLog.e(TAG, "Error connecting to peer: " + throwable.getMessage());

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
                .setIsPrivate(mPrivateCheckbox.isChecked())
                .build();

        ZapLog.v(TAG, lnUrlFinalOpenChannelRequest.requestAsString());


        okhttp3.Request lnUrlRequest = new okhttp3.Request.Builder()
                .url(lnUrlFinalOpenChannelRequest.requestAsString())
                .build();

        HttpClient.getInstance().getClient().newCall(lnUrlRequest).enqueue(new Callback() {
            // We need to make sure the results are executed on the UI Thread to prevent crashes.
            Handler threadHandler = new Handler(Looper.getMainLooper());

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                threadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ZapLog.e(TAG, "Final request failed");
                        switchToFailedScreen("Final request failed");
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                threadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ZapLog.v(TAG, response.body().string());
                            validateFinalResponse(response.body().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void validateFinalResponse(@NonNull String openChannelResponse) {
        LnUrlResponse lnUrlResponse = new Gson().fromJson(openChannelResponse, LnUrlResponse.class);

        if (lnUrlResponse.getStatus().equals("OK")) {
            ZapLog.d(TAG, "LNURL: Success. The service initiated the channel opening.");
            switchToSuccessScreen();
        } else {
            ZapLog.e(TAG, "LNURL: Failed to open channel. " + lnUrlResponse.getReason());
            switchToFailedScreen(lnUrlResponse.getReason());
        }
    }

    private void switchToProgressScreen() {
        mProgressView.setVisibility(View.VISIBLE);
        mInfoView.setVisibility(View.INVISIBLE);
        mProgressView.startSpinning();
    }

    private void switchToSuccessScreen() {
        mProgressView.spinningFinished(true);
        TransitionManager.beginDelayedTransition((ViewGroup) mContentTopLayout.getRootView());
        mInfoView.setVisibility(View.GONE);
        mResultView.setVisibility(View.VISIBLE);
        mResultView.setHeading(R.string.opened_channel, true);
    }

    private void switchToFailedScreen(String error) {
        mProgressView.spinningFinished(false);
        TransitionManager.beginDelayedTransition((ViewGroup) mContentTopLayout.getRootView());
        mInfoView.setVisibility(View.GONE);
        mResultView.setVisibility(View.VISIBLE);

        // Set failed states
        mResultView.setHeading(R.string.error, false);
        mResultView.setDetailsText(error);
    }
}
