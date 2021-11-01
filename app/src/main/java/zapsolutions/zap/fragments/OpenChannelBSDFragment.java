package zapsolutions.zap.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.transition.TransitionManager;

import com.github.lightningnetwork.lnd.lnrpc.EstimateFeeRequest;
import com.google.android.material.snackbar.Snackbar;

import zapsolutions.zap.R;
import zapsolutions.zap.connection.lndConnection.LndConnection;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfigsManager;
import zapsolutions.zap.contacts.ContactsManager;
import zapsolutions.zap.customView.BSDProgressView;
import zapsolutions.zap.customView.BSDResultView;
import zapsolutions.zap.customView.BSDScrollableMainView;
import zapsolutions.zap.customView.NumpadView;
import zapsolutions.zap.customView.OnChainFeeView;
import zapsolutions.zap.lightning.LightningNodeUri;
import zapsolutions.zap.util.HelpDialogUtil;
import zapsolutions.zap.util.MonetaryUtil;
import zapsolutions.zap.util.OnSingleClickListener;
import zapsolutions.zap.util.Wallet;
import zapsolutions.zap.util.ZapLog;

public class OpenChannelBSDFragment extends ZapBSDFragment implements Wallet.ChannelOpenUpdateListener {

    public static final String TAG = OpenChannelBSDFragment.class.getName();
    public static final String ARGS_NODE_URI = "NODE_URI";

    private BSDScrollableMainView mBSDScrollableMainView;
    private BSDResultView mResultView;
    private BSDProgressView mProgressView;
    private ConstraintLayout mContentTopLayout;
    private ConstraintLayout mOpenChannelInputLayout;
    private NumpadView mNumpad;
    private EditText mEtAmount;
    private TextView mTvUnit;
    private boolean mAmountValid = false;
    private Button mOpenChannelButton;
    private TextView mTvNodeAlias;
    private TextView mTvOnChainFunds;
    private LightningNodeUri mLightningNodeUri;
    private String mValueBeforeUnitSwitch;
    private OnChainFeeView mOnChainFeeView;
    private CheckBox mPrivateCheckbox;
    private boolean mUseValueBeforeUnitSwitch = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bsd_open_channel, container);

        mBSDScrollableMainView = view.findViewById(R.id.scrollableBottomSheet);
        mResultView = view.findViewById(R.id.resultLayout);
        mContentTopLayout = view.findViewById(R.id.contentTopLayout);
        mProgressView = view.findViewById(R.id.paymentProgressLayout);
        mOpenChannelInputLayout = view.findViewById(R.id.openChannelInputLayout);
        mNumpad = view.findViewById(R.id.numpadView);
        mTvNodeAlias = view.findViewById(R.id.nodeAliasText);
        mTvOnChainFunds = view.findViewById(R.id.onChainFunds);
        mEtAmount = view.findViewById(R.id.localAmount);
        mTvUnit = view.findViewById(R.id.localAmountUnit);
        mOpenChannelButton = view.findViewById(R.id.openChannelButton);
        mOnChainFeeView = view.findViewById(R.id.sendFeeOnChainLayout);
        mPrivateCheckbox = view.findViewById(R.id.privateCheckBox);

        mBSDScrollableMainView.setOnCloseListener(this::dismiss);
        mBSDScrollableMainView.setTitleIconVisibility(true);
        mBSDScrollableMainView.setTitle(R.string.channel_open);
        mResultView.setOnOkListener(this::dismiss);
        mNumpad.bindEditText(mEtAmount);

        mOnChainFeeView.initialSetup();
        mOnChainFeeView.setFeeTierChangedListener(onChainFeeTier -> {
            calculateFee();
        });

        Wallet.getInstance().registerChannelOpenUpdateListener(this);

        if (getArguments() != null) {
            mLightningNodeUri = (LightningNodeUri) getArguments().getSerializable(ARGS_NODE_URI);
            setAlias(mLightningNodeUri);
        }

        setAvailableFunds();
        ImageButton privateHelpButton = view.findViewById(R.id.privateHelpButton);
        privateHelpButton.setOnClickListener(view1 -> HelpDialogUtil.showDialog(getActivity(), R.string.help_dialog_private_channels));

        // Input validation for the amount field.
        mEtAmount.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // remove the last inputted character if not valid
                if (!mAmountValid) {
                    mNumpad.removeOneDigit();
                }

                mUseValueBeforeUnitSwitch = false;
            }

            @Override
            public void onTextChanged(CharSequence arg0, int start, int before,
                                      int count) {
                // validate input
                mAmountValid = MonetaryUtil.getInstance().validateCurrencyInput(arg0.toString(), MonetaryUtil.getInstance().getPrimaryCurrency());

                // calculate fees
                if (mAmountValid) {
                    calculateFee();
                } else {
                    setFeeFailure();
                }
            }
        });

        // Action when clicked on receive unit
        LinearLayout llUnit = view.findViewById(R.id.sendUnitLayout);
        llUnit.setOnClickListener(v -> {
            if (mEtAmount.getText().toString().equals(".")) {
                mEtAmount.setText("");
            }

            if (!mUseValueBeforeUnitSwitch) {
                mValueBeforeUnitSwitch = mEtAmount.getText().toString();
            }

            String convertedAmount = MonetaryUtil.getInstance().convertPrimaryToSecondaryCurrency(mEtAmount.getText().toString());
            MonetaryUtil.getInstance().switchCurrencies();

            if (mUseValueBeforeUnitSwitch) {
                mEtAmount.setText(mValueBeforeUnitSwitch);
                mUseValueBeforeUnitSwitch = false;
            } else {
                mEtAmount.setText(convertedAmount);
                mUseValueBeforeUnitSwitch = true;
            }

            mTvUnit.setText(MonetaryUtil.getInstance().getPrimaryDisplayUnit());

            setAvailableFunds();
        });

        mNumpad.setVisibility(View.VISIBLE);

        mOpenChannelButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!mAmountValid || mEtAmount.getText().toString().equals(".")) {
                    // no real amount
                    showError(getResources().getString(R.string.amount_invalid), Snackbar.LENGTH_LONG);
                    return;
                }

                long userInputAmount;
                if (mUseValueBeforeUnitSwitch) {
                    userInputAmount = Long.parseLong(MonetaryUtil.getInstance().convertSecondaryToSatoshi(mValueBeforeUnitSwitch));
                } else {
                    userInputAmount = Long.parseLong(MonetaryUtil.getInstance().convertPrimaryToSatoshi(mEtAmount.getText().toString()));
                }

                // values from LND
                long minSendAmount = 20000;
                long maxSendAmount = 17666215;

                if (WalletConfigsManager.getInstance().hasAnyConfigs()) {
                    long onChainAvailable = Wallet.getInstance().getBalances().onChainConfirmed();

                    if (onChainAvailable < maxSendAmount) {
                        maxSendAmount = onChainAvailable;
                    }

                    if (userInputAmount < minSendAmount) {
                        // amount is to small
                        String message = getResources().getString(R.string.min_amount) + " " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(minSendAmount);
                        showError(message, Snackbar.LENGTH_LONG);
                        return;
                    }

                    if (userInputAmount > maxSendAmount) {
                        // amount is to big
                        String message = getResources().getString(R.string.max_amount) + " " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(maxSendAmount);
                        showError(message, Snackbar.LENGTH_LONG);
                        return;
                    }

                } else {
                    // you need to setup wallet to open a channel
                    showError(getResources().getString(R.string.error_channel_open_wallet_setup), Snackbar.LENGTH_LONG);
                    return;
                }

                switchToProgressScreen();
                Wallet.getInstance().openChannel(mLightningNodeUri, userInputAmount, mOnChainFeeView.getFeeTier().getConfirmationBlockTarget(), mPrivateCheckbox.isChecked());
            }
        });

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            // We have to call this delayed, as otherwise it will still bring up the softKeyboard
            mEtAmount.requestFocus();
        }, 200);

        // deactivate default keyboard for number input.
        mEtAmount.setShowSoftInputOnFocus(false);

        // set unit to current primary unit
        mTvUnit.setText(MonetaryUtil.getInstance().getPrimaryDisplayUnit());

        return view;
    }

    private void setAlias(LightningNodeUri lightningNodeUri) {
        String alias;
        if (lightningNodeUri.getNickname() != null) {
            alias = lightningNodeUri.getNickname();
        } else if (ContactsManager.getInstance().doesContactDataExist(lightningNodeUri.getPubKey())) {
            alias = ContactsManager.getInstance().getNameByContactData(lightningNodeUri.getPubKey());
        } else if (!Wallet.getInstance().getNodeAliasFromPubKey(lightningNodeUri.getPubKey(), getContext()).contains(getResources().getString(R.string.channel_no_alias))) {
            // alias found in peers
            alias = Wallet.getInstance().getNodeAliasFromPubKey(lightningNodeUri.getPubKey(), getContext());
        } else {
            alias = lightningNodeUri.getHost();
        }

        if (alias == null || alias.isEmpty()) {
            // no alias - no host
            mTvNodeAlias.setText(lightningNodeUri.getPubKey());
        } else {
            mTvNodeAlias.setText(alias + " (" + lightningNodeUri.getPubKey() + ")");
        }
    }

    private void switchToProgressScreen() {
        mProgressView.setVisibility(View.VISIBLE);
        mOpenChannelInputLayout.setVisibility(View.INVISIBLE);
        mProgressView.startSpinning();
        mBSDScrollableMainView.animateTitleOut();
    }

    private void switchToFailedScreen(String error) {
        mProgressView.spinningFinished(false);
        TransitionManager.beginDelayedTransition((ViewGroup) mContentTopLayout.getRootView());
        mOpenChannelInputLayout.setVisibility(View.GONE);
        mResultView.setVisibility(View.VISIBLE);

        // Set failed states
        mResultView.setHeading(R.string.channel_open_error, false);
        mResultView.setDetailsText(error);
    }

    private void switchToSuccessScreen() {
        mProgressView.spinningFinished(true);
        TransitionManager.beginDelayedTransition((ViewGroup) mContentTopLayout.getRootView());
        mOpenChannelInputLayout.setVisibility(View.GONE);
        mResultView.setVisibility(View.VISIBLE);
        mResultView.setHeading(R.string.success, true);
        mResultView.setDetailsText(R.string.channel_open_success);
    }

    private void setAvailableFunds() {
        long available = Wallet.getInstance().getBalances().onChainConfirmed();
        String availableFundsOnChain = MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(available);
        mTvOnChainFunds.setText(getString(R.string.funds_available, availableFundsOnChain));
    }

    private void showError(String message, int duration) {
        Snackbar msg = Snackbar.make(getView().findViewById(R.id.coordinator), message, duration);
        View sbView = msg.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.superRed));
        msg.show();
    }

    @Override
    public void onDestroy() {
        Wallet.getInstance().unregisterChannelOpenUpdateListener(this);
        super.onDestroy();
    }

    @Override
    public void onChannelOpenUpdate(LightningNodeUri lightningNodeUri, int status, String message) {

        if (mLightningNodeUri.getPubKey().equals(lightningNodeUri.getPubKey())) {
            if (status == Wallet.ChannelOpenUpdateListener.SUCCESS) {
                // fetch channels after open
                Wallet.getInstance().updateLNDChannelsWithDebounce();
                getActivity().runOnUiThread(this::switchToSuccessScreen);
            } else {
                getActivity().runOnUiThread(() -> switchToFailedScreen(getDetailedErrorMessage(status, message)));
            }
        }
    }

    private String getDetailedErrorMessage(int error, String message) {
        switch (error) {
            case ERROR_GET_PEERS_TIMEOUT:
                return getString(R.string.error_get_peers_timeout);
            case ERROR_GET_PEERS:
                return getString(R.string.error_get_peers);
            case ERROR_CONNECTION_TIMEOUT:
                return getString(R.string.error_connect_peer_timeout);
            case ERROR_CONNECTION_REFUSED:
                return getString(R.string.error_connect_peer_refused);
            case ERROR_CONNECTION_SELF:
                return getString(R.string.error_connect_peer_self);
            case ERROR_CONNECTION_NO_HOST:
                return getString(R.string.error_connect_peer_no_host);
            case ERROR_CONNECTION:
                return getString(R.string.error_connect_peer);
            case ERROR_CHANNEL_TIMEOUT:
                return getString(R.string.error_channel_open_timeout);
            case ERROR_CHANNEL_PENDING_MAX:
                return getString(R.string.error_channel_open_pending_max);
            case ERROR_CHANNEL_OPEN:
            default:
                return getString(R.string.error_channel_open, message);
        }
    }


    private void calculateFee() {
        setCalculatingFee();
        long sendAmount = 0L;
        try {
            sendAmount = Long.parseLong(MonetaryUtil.getInstance().convertPrimaryToSatoshi(mEtAmount.getText().toString()));
        } catch (NumberFormatException ignored) {
        }
        estimateOnChainFee(sendAmount, mOnChainFeeView.getFeeTier().getConfirmationBlockTarget());
    }

    /**
     * Show progress while calculating fee
     */
    private void setCalculatingFee() {
        mOnChainFeeView.onCalculating();
    }

    /**
     * Show the calculated fee
     */
    private void setCalculatedFeeAmount(String amount) {
        mOnChainFeeView.onFeeSuccess(amount);
    }

    /**
     * Show fee calculation failure
     */
    private void setFeeFailure() {
        mOnChainFeeView.onFeeFailure();
    }


    /**
     * This function is used to calculate the expected on chain fee.
     */
    private void estimateOnChainFee(long amount, int targetConf) {
        if (WalletConfigsManager.getInstance().hasAnyConfigs()) {
            // We choose a dummy bech32 address. The fee amount depends only on the address type.
            String address;
            switch (Wallet.getInstance().getNetwork()) {
                case TESTNET:
                    address = "tb1qw508d6qejxtdg4y5r3zarvary0c5xw7kxpjzsx";
                    break;
                case REGTEST:
                    address = "bcrt1qsdtedxkv2mdgtstsv9fhyq03dsv9dyu5qmeh2w";
                    break;
                default:
                    address = "bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4"; // Mainnet
            }


            // let LND estimate fee
            EstimateFeeRequest asyncEstimateFeeRequest = EstimateFeeRequest.newBuilder()
                    .putAddrToAmount(address, amount)
                    .setTargetConf(targetConf)
                    .build();

            getCompositeDisposable().add(LndConnection.getInstance().getLightningService().estimateFee(asyncEstimateFeeRequest)
                    .subscribe(estimateFeeResponse -> setCalculatedFeeAmount(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(estimateFeeResponse.getFeeSat())),
                            throwable -> {
                                ZapLog.w(TAG, "Exception in fee estimation request task.");
                                ZapLog.w(TAG, throwable.getMessage());
                                setFeeFailure();
                            }));
        } else {
            setFeeFailure();
        }
    }
}
