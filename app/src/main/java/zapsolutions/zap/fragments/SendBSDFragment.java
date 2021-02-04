package zapsolutions.zap.fragments;


import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.transition.TransitionManager;

import com.github.lightningnetwork.lnd.lnrpc.EstimateFeeRequest;
import com.github.lightningnetwork.lnd.lnrpc.Failure;
import com.github.lightningnetwork.lnd.lnrpc.PayReq;
import com.github.lightningnetwork.lnd.lnrpc.Payment;
import com.github.lightningnetwork.lnd.lnrpc.PaymentFailureReason;
import com.github.lightningnetwork.lnd.lnrpc.Route;
import com.github.lightningnetwork.lnd.lnrpc.SendCoinsRequest;
import com.github.lightningnetwork.lnd.routerrpc.SendPaymentRequest;
import com.google.protobuf.InvalidProtocolBufferException;

import zapsolutions.zap.HomeActivity;
import zapsolutions.zap.R;
import zapsolutions.zap.connection.lndConnection.LndConnection;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfigsManager;
import zapsolutions.zap.customView.BSDProgressView;
import zapsolutions.zap.customView.BSDResultView;
import zapsolutions.zap.customView.BSDScrollableMainView;
import zapsolutions.zap.customView.LightningFeeView;
import zapsolutions.zap.customView.NumpadView;
import zapsolutions.zap.customView.OnChainFeeView;
import zapsolutions.zap.util.MonetaryUtil;
import zapsolutions.zap.util.PaymentUtil;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.RefConstants;
import zapsolutions.zap.util.Wallet;
import zapsolutions.zap.util.ZapLog;


public class SendBSDFragment extends ZapBSDFragment {

    private static final String LOG_TAG = SendBSDFragment.class.getName();

    private BSDScrollableMainView mBSDScrollableMainView;
    private BSDProgressView mProgressScreen;
    private BSDResultView mResultView;
    private ConstraintLayout mContentTopLayout;
    private ConstraintLayout mInputLayout;
    private EditText mEtAmount;
    private EditText mEtMemo;
    private TextView mTvUnit;
    private View mMemoView;
    private OnChainFeeView mOnChainFeeView;
    private LightningFeeView mLightningFeeView;
    private NumpadView mNumpad;
    private Button mBtnSend;
    private Button mFallbackButton;
    private TextView mPayee;

    private PayReq mLnPaymentRequest;
    private String mLnInvoice;
    private String mFallbackOnChainInvoice;
    private String mMemo;
    private String mOnChainAddress;
    private boolean mOnChain;
    private long mFixedAmount;
    private Handler mHandler;
    private boolean mAmountValid = true;
    private boolean mSendButtonEnabled_input;
    private boolean mSendButtonEnabled_feeCalculate;
    private Route mRoute;
    long mCalculatedFee;
    double mCalculatedFeePercent;

    public static SendBSDFragment createLightningDialog(PayReq paymentRequest, String invoice, String fallbackOnChainInvoice) {
        Intent intent = new Intent();
        intent.putExtra("onChain", false);
        intent.putExtra("lnPaymentRequest", paymentRequest.toByteArray());
        intent.putExtra("lnInvoice", invoice);
        intent.putExtra("fallbackOnChainInvoice", fallbackOnChainInvoice);
        SendBSDFragment sendBottomSheetDialog = new SendBSDFragment();
        sendBottomSheetDialog.setArguments(intent.getExtras());
        return sendBottomSheetDialog;
    }

    public static SendBSDFragment createOnChainDialog(String address, long amount, String message) {
        Intent intent = new Intent();
        intent.putExtra("onChain", true);
        intent.putExtra("onChainAddress", address);
        intent.putExtra("onChainAmount", amount);
        intent.putExtra("onChainMessage", message);
        SendBSDFragment sendBottomSheetDialog = new SendBSDFragment();
        sendBottomSheetDialog.setArguments(intent.getExtras());
        return sendBottomSheetDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle args = getArguments();
        mOnChain = args.getBoolean("onChain");

        if (mOnChain) {
            mFixedAmount = args.getLong("onChainAmount");
            mOnChainAddress = args.getString("onChainAddress");
            mMemo = args.getString("onChainMessage");
        } else {
            PayReq paymentRequest;
            try {
                paymentRequest = PayReq.parseFrom(args.getByteArray("lnPaymentRequest"));
                mLnPaymentRequest = paymentRequest;
                mLnInvoice = args.getString("lnInvoice");
                mFallbackOnChainInvoice = args.getString("fallbackOnChainInvoice");
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException("Invalid payment request forwarded." + e.getMessage());
            }
        }

        View view = inflater.inflate(R.layout.bsd_send, container);

        mBSDScrollableMainView = view.findViewById(R.id.scrollableBottomSheet);
        mProgressScreen = view.findViewById(R.id.paymentProgressLayout);
        mResultView = view.findViewById(R.id.resultLayout);
        mContentTopLayout = view.findViewById(R.id.contentTopLayout);
        mInputLayout = view.findViewById(R.id.inputLayout);
        mEtAmount = view.findViewById(R.id.sendAmount);
        mTvUnit = view.findViewById(R.id.sendUnit);
        mEtMemo = view.findViewById(R.id.sendMemo);
        mMemoView = view.findViewById(R.id.sendMemoTopLayout);
        mOnChainFeeView = view.findViewById(R.id.sendFeeOnChainLayout);
        mLightningFeeView = view.findViewById(R.id.sendFeeLightningLayout);
        mNumpad = view.findViewById(R.id.numpadView);
        mBtnSend = view.findViewById(R.id.sendButton);
        mFallbackButton = view.findViewById(R.id.fallbackButton);
        mPayee = view.findViewById(R.id.sendPayee);

        mBSDScrollableMainView.setOnCloseListener(this::dismiss);
        mBSDScrollableMainView.setTitleIconVisibility(true);
        mResultView.setOnOkListener(this::dismiss);

        mHandler = new Handler();

        mNumpad.bindEditText(mEtAmount);


        // deactivate default keyboard for number input.
        mEtAmount.setShowSoftInputOnFocus(false);

        // set unit to current primary unit
        mTvUnit.setText(MonetaryUtil.getInstance().getPrimaryDisplayUnit());

        // Input validation for the amount field.
        mEtAmount.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {

                // remove the last inputted character if not valid
                if (!mAmountValid) {
                    mNumpad.removeOneDigit();
                }

                if (!mEtAmount.getText().toString().equals(".")) {
                    // make text red if input is too large
                    long maxSendable;
                    if (mOnChain) {

                        if (WalletConfigsManager.getInstance().hasAnyConfigs()) {
                            maxSendable = Wallet.getInstance().getBalances().onChainConfirmed();
                        } else {
                            maxSendable = Wallet.getInstance().getDemoBalances().onChainConfirmed();
                        }
                    } else {
                        if (WalletConfigsManager.getInstance().hasAnyConfigs()) {
                            maxSendable = Wallet.getInstance().getMaxLightningSendAmount();
                        } else {
                            maxSendable = 750000;
                        }
                    }

                    long currentValue = Long.parseLong(MonetaryUtil.getInstance().convertPrimaryToSatoshi(mEtAmount.getText().toString()));
                    if (currentValue > maxSendable) {
                        mEtAmount.setTextColor(getResources().getColor(R.color.superRed));
                        String maxAmount = getResources().getString(R.string.max_amount) + " " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(maxSendable);
                        Toast.makeText(getActivity(), maxAmount, Toast.LENGTH_SHORT).show();
                        mSendButtonEnabled_input = false;
                    } else {
                        mEtAmount.setTextColor(getResources().getColor(R.color.white));
                        mSendButtonEnabled_input = true;
                    }
                    if (currentValue == 0 && mFixedAmount == 0L) {
                        mSendButtonEnabled_input = false;
                    }
                    updateSendButtonState();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onTextChanged(CharSequence arg0, int start, int before,
                                      int count) {
                if (arg0.length() == 0) {
                    // No entered text so will show hint
                    mEtAmount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                } else {
                    mEtAmount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
                }


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


        if (mOnChain) {
            mPayee.setText(mOnChainAddress);
            mOnChainFeeView.initialSetup();

            mOnChainFeeView.setVisibility(View.VISIBLE);
            mOnChainFeeView.setFeeTierChangedListener(onChainFeeTier -> {
                calculateFee();
            });
            mBSDScrollableMainView.setTitleIcon(R.drawable.ic_icon_modal_on_chain);
            mResultView.setTypeIcon(R.drawable.ic_onchain_black_24dp);
            mProgressScreen.setProgressTypeIcon(R.drawable.ic_onchain_black_24dp);
            mBSDScrollableMainView.setTitle(R.string.send_onChainPayment);

            if (mMemo == null) {
                mMemoView.setVisibility(View.GONE);
            } else {
                mMemoView.setVisibility(View.VISIBLE);
                mEtMemo.setText(mMemo);
            }

            if (mFixedAmount != 0L) {
                // A specific amount was requested. We are not allowed to change the amount.
                mEtAmount.setText(MonetaryUtil.getInstance().convertSatoshiToPrimary(mFixedAmount));
                mEtAmount.clearFocus();
                mEtAmount.setFocusable(false);
                mEtAmount.setEnabled(false);
            } else {
                // No specific amount was requested. Let User input an amount.
                mNumpad.setVisibility(View.VISIBLE);
                mSendButtonEnabled_input = false;
                updateSendButtonState();

                mHandler.postDelayed(() -> {
                    // We have to call this delayed, as otherwise it will still bring up the softKeyboard
                    mEtAmount.requestFocus();
                }, 200);

            }

            // Action when clicked on "Send payment"
            mBtnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ZapLog.d(LOG_TAG, "Trying to send on-chain payment...");
                    // Send on-chain payment

                    long sendAmount = 0L;
                    if (mFixedAmount != 0L) {
                        sendAmount = mFixedAmount;
                    } else {
                        sendAmount = Long.parseLong(MonetaryUtil.getInstance().convertPrimaryToSatoshi(mEtAmount.getText().toString()));
                    }

                    if (sendAmount != 0L) {

                        mResultView.setDetailsText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(sendAmount));

                        switchToSendProgressScreen();

                        SendCoinsRequest sendRequest = SendCoinsRequest.newBuilder()
                                .setAddr(mOnChainAddress)
                                .setAmount(sendAmount)
                                .setTargetConf(mOnChainFeeView.getFeeTier().getConfirmationBlockTarget())
                                .build();

                        getCompositeDisposable().add(LndConnection.getInstance().getLightningService().sendCoins(sendRequest)
                                .subscribe(sendCoinsResponse -> {
                                    // updated the history, so it is shown the next time the user views it
                                    Wallet.getInstance().updateOnChainTransactionHistory();

                                    ZapLog.v(LOG_TAG, sendCoinsResponse.toString());

                                    // show success animation
                                    mHandler.postDelayed(() -> switchToSuccessScreen(), 500);
                                }, throwable -> {
                                    ZapLog.e(LOG_TAG, "Exception in send coins request task.");
                                    ZapLog.e(LOG_TAG, throwable.getMessage());

                                    String errorPrefix = getResources().getString(R.string.error).toUpperCase() + ":";
                                    String errormessage = throwable.getMessage().replace("UNKNOWN:", errorPrefix);
                                    mHandler.postDelayed(() -> switchToFailedScreen(errormessage), 300);
                                }));
                    } else {
                        // Send amount == 0
                        Toast.makeText(getActivity(), "Send amount is to small.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } else {

            // Lightning Payment
            mPayee.setText(mLnPaymentRequest.getDestination());

            mLightningFeeView.setVisibility(View.VISIBLE);
            mBSDScrollableMainView.setTitleIcon(R.drawable.ic_icon_modal_lightning);
            mResultView.setTypeIcon(R.drawable.ic_nav_wallet_black_24dp);
            mProgressScreen.setProgressTypeIcon(R.drawable.ic_nav_wallet_black_24dp);
            mBSDScrollableMainView.setTitle(R.string.send_lightningPayment);

            if (mLnPaymentRequest.getDescription() == null || mLnPaymentRequest.getDescription().isEmpty()) {
                mMemoView.setVisibility(View.GONE);
            } else {
                mMemoView.setVisibility(View.VISIBLE);
                mEtMemo.setText(mLnPaymentRequest.getDescription());
            }

            if (mLnPaymentRequest.getNumSatoshis() != 0) {
                // A specific amount was requested. We are not allowed to change the amount
                mFixedAmount = mLnPaymentRequest.getNumSatoshis();
                mEtAmount.setText(MonetaryUtil.getInstance().convertSatoshiToPrimary(mFixedAmount));
                mEtAmount.clearFocus();
                mEtAmount.setFocusable(false);
            } else {
                // No specific amount was requested. Let User input an amount.
                mNumpad.setVisibility(View.VISIBLE);
                mSendButtonEnabled_input = false;
                updateSendButtonState();

                mHandler.postDelayed(() -> {
                    // We have to call this delayed, as otherwise it will still bring up the softKeyboard
                    mEtAmount.requestFocus();
                }, 200);
            }


            // Action when clicked on "Send payment"
            mBtnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (WalletConfigsManager.getInstance().hasAnyConfigs()) {

                        // sanity check for fees
                        if (mCalculatedFee != -1) {
                            if (mCalculatedFeePercent >= 1f) {
                                // fee higher or equal to payment amount
                                String feeLimitString = getString(R.string.fee_limit_exceeded_payment, mCalculatedFee, mLnPaymentRequest.getNumSatoshis());
                                showFeeAlertDialog(feeLimitString);
                                return;
                            }
                            if (mLnPaymentRequest.getNumSatoshis() > RefConstants.LN_PAYMENT_FEE_THRESHOLD)
                                if (mCalculatedFeePercent > PaymentUtil.getRelativeSettingsFeeLimit()) {
                                    // fee higher or equal to payment amount
                                    String feeLimitString = getString(R.string.fee_limit_exceeded, mCalculatedFeePercent * 100, PaymentUtil.getRelativeSettingsFeeLimit() * 100);
                                    showFeeAlertDialog(feeLimitString);
                                    return;
                                }
                        }
                        sendLightningPayment();
                    } else {
                        // Demo Mode
                        Toast.makeText(getActivity(), R.string.demo_setupWalletFirst, Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }


        // Action when clicked on receive unit
        LinearLayout llUnit = view.findViewById(R.id.sendUnitLayout);
        llUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEtAmount.getText().toString().equals(".")) {
                    mEtAmount.setText("");
                }
                if (mFixedAmount == 0L) {
                    String convertedAmount = MonetaryUtil.getInstance().convertPrimaryToSecondaryCurrency(mEtAmount.getText().toString());
                    MonetaryUtil.getInstance().switchCurrencies();
                    mEtAmount.setText(convertedAmount);
                } else {
                    MonetaryUtil.getInstance().switchCurrencies();
                    mEtAmount.setText(MonetaryUtil.getInstance().convertSatoshiToPrimary(mFixedAmount));
                }
                mTvUnit.setText(MonetaryUtil.getInstance().getPrimaryDisplayUnit());
            }
        });

        mFallbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HomeActivity) getActivity()).analyzeString(mFallbackOnChainInvoice);
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

    private void showFeeAlertDialog(String message) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getContext())
                .setTitle(R.string.fee_limit_title)
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton(R.string.yes, (dialog, whichButton) -> sendLightningPayment())
                .setNegativeButton(R.string.no, (dialog, whichButton) -> {
                });
        Dialog dlg = adb.create();
        // Apply FLAG_SECURE to dialog to prevent screen recording
        if (PrefsUtil.isScreenRecordingPrevented()) {
            dlg.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        dlg.show();
    }

    private void sendLightningPayment() {
        switchToSendProgressScreen();

        if (mLnPaymentRequest.getNumSatoshis() == 0) {
            mResultView.setDetailsText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(Long.parseLong(MonetaryUtil.getInstance().convertPrimaryToSatoshi(mEtAmount.getText().toString()))));
        } else {
            mResultView.setDetailsText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(mLnPaymentRequest.getNumSatoshis()));
        }

        if (mRoute != null) {
            PaymentUtil.sendToRoute(mLnPaymentRequest.getPaymentHash(), mRoute, getCompositeDisposable(), new PaymentUtil.OnSendToRouteResult() {
                @Override
                public void onSuccess() {
                    mHandler.postDelayed(() -> switchToSuccessScreen(), 300);
                }

                @Override
                public void onError(String error, Failure reason, int duration) {
                    String errorPrefix = getResources().getString(R.string.error).toUpperCase() + ":";
                    String errorMessage;
                    if (reason != null) {
                        switch (reason.getCode()) {
                            case INCORRECT_OR_UNKNOWN_PAYMENT_DETAILS:
                                errorMessage = errorPrefix + "\n\n" + getResources().getString(R.string.error_payment_invalid_details);
                                break;
                            default:
                                errorMessage = errorPrefix + "\n\n" + error;
                                break;
                        }
                    } else {
                        errorMessage = error.replace("UNKNOWN:", errorPrefix);
                    }
                    mHandler.postDelayed(() -> switchToFailedScreen(errorMessage), 300);
                }
            });
        } else {
            // Fallback to multi path payment as no route was found

            SendPaymentRequest mppSendRequest = PaymentUtil.prepareMultiPathPayment(mLnPaymentRequest, mLnInvoice);

            PaymentUtil.sendPayment(mppSendRequest, getCompositeDisposable(), new PaymentUtil.OnPaymentResult() {
                @Override
                public void onSuccess(Payment payment) {
                    mHandler.postDelayed(() -> switchToSuccessScreen(), 300);
                }

                @Override
                public void onError(String error, PaymentFailureReason reason, int duration) {
                    String errorPrefix = getResources().getString(R.string.error).toUpperCase() + ":";
                    String errorMessage;
                    if (reason != null) {
                        switch (reason) {
                            case FAILURE_REASON_TIMEOUT:
                                errorMessage = errorPrefix + "\n\n" + getResources().getString(R.string.error_payment_timeout);
                                break;
                            case FAILURE_REASON_NO_ROUTE:
                                errorMessage = errorPrefix + "\n\n" + getResources().getString(R.string.error_payment_no_route);
                                break;
                            case FAILURE_REASON_INSUFFICIENT_BALANCE:
                                errorMessage = errorPrefix + "\n\n" + getResources().getString(R.string.error_payment_insufficient_balance);
                                break;
                            case FAILURE_REASON_INCORRECT_PAYMENT_DETAILS:
                                errorMessage = errorPrefix + "\n\n" + getResources().getString(R.string.error_payment_invalid_details);
                                break;
                            default:
                                errorMessage = errorPrefix + "\n\n" + error;
                                break;
                        }
                    } else {
                        errorMessage = error.replace("UNKNOWN:", errorPrefix);
                    }
                    mHandler.postDelayed(() -> switchToFailedScreen(errorMessage), 300);
                }
            });
        }
    }

    private void switchToSendProgressScreen() {
        mProgressScreen.setVisibility(View.VISIBLE);
        mInputLayout.setVisibility(View.INVISIBLE);
        mProgressScreen.startSpinning();
        mBSDScrollableMainView.animateTitleOut();
    }

    private void switchToSuccessScreen() {
        mProgressScreen.spinningFinished(true);
        TransitionManager.beginDelayedTransition((ViewGroup) mContentTopLayout.getRootView());
        mInputLayout.setVisibility(View.GONE);
        mResultView.setVisibility(View.VISIBLE);
        mResultView.setHeading(R.string.send_success, true);
    }

    private void switchToFailedScreen(String error) {
        mProgressScreen.spinningFinished(false);
        TransitionManager.beginDelayedTransition(mContentTopLayout);
        mInputLayout.setVisibility(View.GONE);
        mResultView.setVisibility(View.VISIBLE);

        // Set failed states
        mResultView.setHeading(R.string.send_fail, false);
        mResultView.setDetailsText(error);

        if (!mOnChain && mFallbackOnChainInvoice != null) {
            mFallbackButton.setVisibility(View.VISIBLE);
        }
    }

    private void calculateFee() {
        setCalculatingFee();

        if (mOnChain) {
            long sendAmount = 0L;
            if (mFixedAmount != 0L) {
                sendAmount = mFixedAmount;
            } else {
                try {
                    sendAmount = Long.parseLong(MonetaryUtil.getInstance().convertPrimaryToSatoshi(mEtAmount.getText().toString()));
                } catch (NumberFormatException e) {

                }
            }

            estimateOnChainFee(mOnChainAddress, sendAmount, mOnChainFeeView.getFeeTier().getConfirmationBlockTarget());
        } else {
            if (mLnPaymentRequest.getNumSatoshis() == 0) {
                long sendAmount = 0L;
                try {
                    sendAmount = Long.parseLong(MonetaryUtil.getInstance().convertPrimaryToSatoshi(mEtAmount.getText().toString()));
                } catch (NumberFormatException e) {

                }
                SendPaymentRequest probeRequest = PaymentUtil.preparePaymentProbe(mLnPaymentRequest.getDestination(), sendAmount, mLnPaymentRequest.getPaymentAddr(), mLnPaymentRequest.getRouteHintsList(), mLnPaymentRequest.getFeaturesMap());
                sendPaymentProbe(probeRequest);
            } else {
                SendPaymentRequest probeRequest = PaymentUtil.preparePaymentProbe(mLnPaymentRequest);
                sendPaymentProbe(probeRequest);
            }
        }
    }

    /**
     * Show progress while calculating fee
     */
    private void setCalculatingFee() {
        mSendButtonEnabled_feeCalculate = false;
        updateSendButtonState();
        if (mOnChain) {
            mOnChainFeeView.onCalculating();
        } else {
            mLightningFeeView.onCalculating();
        }
    }

    /**
     * Show the calculated fee
     */
    private void setCalculatedFeeAmount(String amount) {
        mSendButtonEnabled_feeCalculate = true;
        updateSendButtonState();
        if (mOnChain) {
            mOnChainFeeView.onFeeSuccess(amount);
        } else {
            mLightningFeeView.setAmount(amount);
        }
    }

    /**
     * Show fee calculation failure
     */
    private void setFeeFailure() {
        mSendButtonEnabled_feeCalculate = true;
        updateSendButtonState();
        if (mOnChain) {
            mOnChainFeeView.onFeeFailure();
        } else {
            mRoute = null;
            mLightningFeeView.onFeeFailure();
        }
    }

    private void updateSendButtonState() {
        if (mSendButtonEnabled_feeCalculate && mSendButtonEnabled_input) {
            mBtnSend.setEnabled(true);
            mBtnSend.setTextColor(getResources().getColor(R.color.lightningOrange));
        } else {
            mBtnSend.setEnabled(false);
            mBtnSend.setTextColor(getResources().getColor(R.color.gray));
        }
    }

    /**
     * This function is used to calculate the expected on chain fee.
     */
    private void estimateOnChainFee(String address, long amount, int targetConf) {
        // let LND estimate fee
        EstimateFeeRequest asyncEstimateFeeRequest = EstimateFeeRequest.newBuilder()
                .putAddrToAmount(address, amount)
                .setTargetConf(targetConf)
                .build();

        getCompositeDisposable().add(LndConnection.getInstance().getLightningService().estimateFee(asyncEstimateFeeRequest)
                .subscribe(estimateFeeResponse -> setCalculatedFeeAmount(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(estimateFeeResponse.getFeeSat())),
                        throwable -> {
                            ZapLog.w(LOG_TAG, "Exception in fee estimation request task.");
                            ZapLog.w(LOG_TAG, throwable.getMessage());
                            setFeeFailure();
                        }));
    }


    private void sendPaymentProbe(SendPaymentRequest probeRequest) {
        PaymentUtil.sendPaymentProbe(probeRequest, getCompositeDisposable(), new PaymentUtil.OnPaymentProbeResult() {

            @Override
            public void onSuccess(long fee, Route route, long paymentAmountSat) {
                mRoute = route;
                mCalculatedFee = fee;
                mCalculatedFeePercent = (fee / (double) paymentAmountSat);
                String feePercentageString = " (" + String.format("%.1f", mCalculatedFeePercent * 100) + "%)";
                String feeString = MonetaryUtil.getInstance().getPrimaryDisplayAmount(fee) + " " + MonetaryUtil.getInstance().getPrimaryDisplayUnit();
                feeString = feeString + feePercentageString;
                setCalculatedFeeAmount(feeString);
            }

            @Override
            public void onNoRoute(long paymentAmountSat) {
                // Display fee according to max UserSetting
                mCalculatedFee = PaymentUtil.calculateAbsoluteFeeLimit(paymentAmountSat);
                mCalculatedFeePercent = PaymentUtil.calculateRelativeFeeLimit(paymentAmountSat);
                String feePercentageString = " (" + String.format("%.1f", mCalculatedFeePercent * 100) + "%)";
                long fee = PaymentUtil.calculateAbsoluteFeeLimit(paymentAmountSat);
                String feeString = MonetaryUtil.getInstance().getPrimaryDisplayAmount(fee) + " " + MonetaryUtil.getInstance().getPrimaryDisplayUnit();
                feeString = getString(R.string.maximum_abbreviation) + " " + feeString + feePercentageString;
                setCalculatedFeeAmount(feeString);
            }

            @Override
            public void onError(String error, int duration) {
                mCalculatedFee = -1;
                mCalculatedFeePercent = -1;
                setFeeFailure();
            }
        });
    }
}
