package zapsolutions.zap.lnurl.pay;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.github.lightningnetwork.lnd.lnrpc.PayReqString;
import com.github.lightningnetwork.lnd.lnrpc.SendRequest;
import com.github.lightningnetwork.lnd.lnrpc.SendResponse;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import zapsolutions.zap.R;
import zapsolutions.zap.connection.HttpClient;
import zapsolutions.zap.connection.establishConnectionToLnd.LndConnection;
import zapsolutions.zap.customView.NumpadView;
import zapsolutions.zap.fragments.RxBSDFragment;
import zapsolutions.zap.util.ClipBoardUtil;
import zapsolutions.zap.util.MonetaryUtil;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.RefConstants;
import zapsolutions.zap.util.TorUtil;
import zapsolutions.zap.util.Wallet;
import zapsolutions.zap.util.ZapLog;


public class LnUrlPayBSDFragment extends RxBSDFragment {

    private static final String LOG_TAG = LnUrlPayBSDFragment.class.getName();

    private ConstraintLayout mRootLayout;
    private ImageView mIvBsdIcon;
    private TextView mTvTitle;
    private View mSendAmountView;
    private EditText mEtAmount;
    private EditText mEtDescription;
    private TextView mTvUnit;
    private View mDescriptionView;
    private NumpadView mNumpad;
    private Button mBtnSend;
    private View mProgressScreen;
    private View mFinishedScreen;
    private Button mOkButton;
    private ImageView mProgressFinishedIcon;
    private ImageView mIvProgressPaymentTypeIcon;
    private ImageView mIvFinishedPaymentTypeIcon;
    private TextView mTvFinishedText;
    private TextView mTvFinishedText2;
    private TextView mTvFinishedText3;
    private View mProgressBar;
    private TextView mTvPayee;
    private BottomSheetBehavior mBehavior;
    private long mFixedAmount;
    private boolean mAmountValid = true;
    private long mMinSendable;
    private long mMaxSendable;
    private String mServiceURLString;
    private boolean mCurrencyJustSwitched;
    private boolean mValueModifiedSinceSwitch;
    private long mTempCurrentSatoshiValue;
    private long mFinalChosenAmount;

    private Handler mHandler;
    private LnUrlPayResponse mPaymentData;

    public static LnUrlPayBSDFragment createLnUrlPayDialog(LnUrlPayResponse response) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(LnUrlPayResponse.ARGS_KEY, response);
        Intent intent = new Intent();
        intent.putExtras(bundle);
        LnUrlPayBSDFragment lnUrlPayBSDFragment = new LnUrlPayBSDFragment();
        lnUrlPayBSDFragment.setArguments(intent.getExtras());
        return lnUrlPayBSDFragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle args = getArguments();
        mPaymentData = (LnUrlPayResponse) args.getSerializable(LnUrlPayResponse.ARGS_KEY);

        // Calculate correct min and max withdrawal value for LNURL. Zap limits withdrawal to full satoshis.
        mMaxSendable = Math.min((mPaymentData.getMaxSendable() / 1000), Wallet.getInstance().getMaxLightningSendAmount());
        mMinSendable = mPaymentData.getMinSendable() % 1000 == 0 ? Math.max((mPaymentData.getMinSendable() / 1000), 1L) : Math.max((mPaymentData.getMinSendable() / 1000) + 1L, 1L);

        // Extract the URL from the service
        try {
            URL url = new URL(mPaymentData.getCallback());
            mServiceURLString = url.getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        mHandler = new Handler();

        View view = inflater.inflate(R.layout.bsd_lnurl_pay, container);

        // Apply FLAG_SECURE to dialog to prevent screen recording
        if (PrefsUtil.preventScreenRecording()) {
            getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        mRootLayout = view.findViewById(R.id.rootLayout);
        mIvBsdIcon = view.findViewById(R.id.bsdIcon);
        mTvTitle = view.findViewById(R.id.bsdTitle);

        mSendAmountView = view.findViewById(R.id.sendInputsView);
        mEtAmount = view.findViewById(R.id.sendAmount);
        mTvUnit = view.findViewById(R.id.unit);
        mEtDescription = view.findViewById(R.id.sendDescription);
        mDescriptionView = view.findViewById(R.id.sendDescriptionTopLayout);
        mTvPayee = view.findViewById(R.id.sendPayee);

        mNumpad = view.findViewById(R.id.numpadView);
        mBtnSend = view.findViewById(R.id.sendButton);

        mProgressScreen = view.findViewById(R.id.paymentProgressLayout);
        mFinishedScreen = view.findViewById(R.id.paymentFinishedLayout);
        mOkButton = view.findViewById(R.id.okButton);
        mProgressFinishedIcon = view.findViewById(R.id.progressFinishedIcon);
        mIvProgressPaymentTypeIcon = view.findViewById(R.id.progressPaymentTypeIcon);
        mIvFinishedPaymentTypeIcon = view.findViewById(R.id.finishedPaymentTypeIcon);
        mTvFinishedText = view.findViewById(R.id.finishedText);
        mTvFinishedText2 = view.findViewById(R.id.finishedText2);
        mTvFinishedText3 = view.findViewById(R.id.finishedText3);
        mProgressBar = view.findViewById(R.id.progressBar);

        mNumpad.bindEditText(mEtAmount);

        // deactivate default keyboard for number input.
        mEtAmount.setShowSoftInputOnFocus(false);

        // set unit to current primary unit
        mTvUnit.setText(MonetaryUtil.getInstance().getPrimaryDisplayUnit());

        // Action when clicked on "x" (close) button
        ImageButton btnCloseBSD = view.findViewById(R.id.closeButton);
        btnCloseBSD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


        // Input validation for the amount field.
        mEtAmount.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {

                // remove the last inputted character if not valid
                if (!mAmountValid) {
                    mNumpad.removeOneDigit();
                    return;
                }

                if (!mEtAmount.getText().toString().equals(".") && !mCurrencyJustSwitched) {

                    if (mFixedAmount != 0L) {
                        mEtAmount.setTextColor(getResources().getColor(R.color.white));
                        mBtnSend.setEnabled(true);
                        mBtnSend.setTextColor(getResources().getColor(R.color.lightningOrange));
                        return;
                    }
                    mValueModifiedSinceSwitch = true;
                    long currentValue = Long.parseLong(MonetaryUtil.getInstance().convertPrimaryToSatoshi(mEtAmount.getText().toString()));

                    // make text red if input is too large or too small
                    if (currentValue > mMaxSendable) {
                        mEtAmount.setTextColor(getResources().getColor(R.color.superRed));
                        String maxAmount = getResources().getString(R.string.max_amount) + " " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(mMaxSendable);
                        Toast.makeText(getActivity(), maxAmount, Toast.LENGTH_SHORT).show();
                        mBtnSend.setEnabled(false);
                        mBtnSend.setTextColor(getResources().getColor(R.color.gray));
                    } else if (currentValue < mMinSendable) {
                        mEtAmount.setTextColor(getResources().getColor(R.color.superRed));
                        String minAmount = getResources().getString(R.string.min_amount) + " " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(mMinSendable);
                        Toast.makeText(getActivity(), minAmount, Toast.LENGTH_SHORT).show();
                        mBtnSend.setEnabled(false);
                        mBtnSend.setTextColor(getResources().getColor(R.color.gray));
                    } else {
                        mEtAmount.setTextColor(getResources().getColor(R.color.white));
                        mBtnSend.setEnabled(true);
                        mBtnSend.setTextColor(getResources().getColor(R.color.lightningOrange));
                    }
                    if (currentValue == 0) {
                        mBtnSend.setEnabled(false);
                        mBtnSend.setTextColor(getResources().getColor(R.color.gray));
                    }
                }
                mCurrencyJustSwitched = false;
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
            }
        });

        mIvBsdIcon.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_icon_modal_lightning));
        mIvFinishedPaymentTypeIcon.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_nav_wallet_balck_24dp));
        mIvProgressPaymentTypeIcon.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_nav_wallet_balck_24dp));
        mTvTitle.setText(R.string.pay);
        if (mServiceURLString != null) {
            mTvPayee.setText(mServiceURLString);
        } else {
            mTvPayee.setText(R.string.unknown);
        }

        if (mPaymentData.getMetadata(LnUrlPayResponse.METADATA_TEXT) == null) {
            mDescriptionView.setVisibility(View.GONE);
        } else {
            mDescriptionView.setVisibility(View.VISIBLE);
            mEtDescription.setText(mPaymentData.getMetadata(LnUrlPayResponse.METADATA_TEXT));
        }

        if (mPaymentData.getMinSendable() == mPaymentData.getMaxSendable()) {
            // A specific amount was requested. We are not allowed to change the amount.
            mFixedAmount = mPaymentData.getMaxSendable() / 1000;
            mEtAmount.setText(MonetaryUtil.getInstance().convertSatoshiToPrimary(mFixedAmount));
            mEtAmount.clearFocus();
            mEtAmount.setFocusable(false);
            mEtAmount.setEnabled(false);
        } else {
            // No specific amount was requested. Let User input an amount, but pre fill with maxWithdraw amount.
            mNumpad.setVisibility(View.VISIBLE);
            mTempCurrentSatoshiValue = mMinSendable;
            mCurrencyJustSwitched = true;
            mValueModifiedSinceSwitch = false;
            mEtAmount.setText(MonetaryUtil.getInstance().convertSatoshiToPrimary(mMinSendable));

            mHandler.postDelayed(() -> {
                // We have to call this delayed, as otherwise it will still bring up the softKeyboard
                mEtAmount.requestFocus();
                mEtAmount.setSelection(mEtAmount.getText().length());
            }, 200);
        }


        // Action when clicked on "send"
        mBtnSend.setText(R.string.activity_send);
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switchToWithdrawProgressScreen();

                ZapLog.v(LOG_TAG, "Lnurl pay initiated...");

                if (mFixedAmount == 0L) {
                    if (!mValueModifiedSinceSwitch) {
                        mFinalChosenAmount = mTempCurrentSatoshiValue;
                    } else {
                        mFinalChosenAmount = Long.parseLong(MonetaryUtil.getInstance().convertPrimaryToSatoshi(mEtAmount.getText().toString()));
                    }
                } else {
                    mFinalChosenAmount = mFixedAmount;
                }

                // Create send request
                LnUrlSecondPayRequest lnUrlSecondPayRequest = new LnUrlSecondPayRequest.Builder()
                        .setCallback(mPaymentData.getCallback())
                        .setAmount(mFinalChosenAmount * 1000)
                        .build();

                ZapLog.v(LOG_TAG, "Sent following request to service: " + lnUrlSecondPayRequest.requestAsString());

                StringRequest lnUrlRequest = new StringRequest(Request.Method.GET, lnUrlSecondPayRequest.requestAsString(),
                        response -> validateSecondResponse(response),
                        error -> {
                            if (mServiceURLString != null) {
                                switchToFailedScreen(getResources().getString(R.string.lnurl_service_not_responding, mServiceURLString));
                            } else {
                                String host = getResources().getString(R.string.host);
                                switchToFailedScreen(getResources().getString(R.string.lnurl_service_not_responding, host));
                            }
                        });

                // Send request to LNURL service
                HttpClient.getInstance().addToRequestQueue(lnUrlRequest, "LnUrlSecondPayRequest");
            }
        });


        // Action when clicked on receive unit
        LinearLayout llUnit = view.findViewById(R.id.unitLayout);
        llUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrencyJustSwitched = true;

                if (MonetaryUtil.getInstance().getPrimaryCurrency().isBitcoin()) {
                    mTempCurrentSatoshiValue = Long.parseLong(MonetaryUtil.getInstance().convertPrimaryToSatoshi(mEtAmount.getText().toString()));
                }

                if (mEtAmount.getText().toString().equals(".")) {
                    mEtAmount.setText("");
                }
                if (mFixedAmount == 0L) {
                    String convertedAmount;
                    if (!mValueModifiedSinceSwitch) {
                        convertedAmount = MonetaryUtil.getInstance().convertSatoshiToSecondary(mTempCurrentSatoshiValue);
                    } else {
                        convertedAmount = MonetaryUtil.getInstance().convertPrimaryToSecondaryCurrency(mEtAmount.getText().toString());
                    }
                    mValueModifiedSinceSwitch = false;
                    MonetaryUtil.getInstance().switchCurrencies();
                    mEtAmount.setText(convertedAmount);
                } else {
                    mValueModifiedSinceSwitch = false;
                    MonetaryUtil.getInstance().switchCurrencies();
                    mEtAmount.setText(MonetaryUtil.getInstance().convertSatoshiToPrimary(mFixedAmount));
                }
                mTvUnit.setText(MonetaryUtil.getInstance().getPrimaryDisplayUnit());
                mEtAmount.setSelection(mEtAmount.getText().length());
            }
        });

        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


        if (mMinSendable > mMaxSendable) {
            // There is no way the payment can be routed... show an error immediately
            switchToWithdrawProgressScreen();
            switchToFailedScreen(getResources().getString(R.string.lnurl_pay_insufficient_channel_balance));
        }

        return view;
    }

    private void validateSecondResponse(@NonNull String secondPayResponse) {

        ZapLog.d(LOG_TAG, "Second pay response: " + secondPayResponse);

        LnUrlPaySecondResponse lnUrlPaySecondResponse = new Gson().fromJson(secondPayResponse, LnUrlPaySecondResponse.class);

        if (lnUrlPaySecondResponse.hasError()) {
            ZapLog.d(LOG_TAG, "LNURL: Failed to pay. " + lnUrlPaySecondResponse.getReason());
            switchToFailedScreen(lnUrlPaySecondResponse.getReason());
        } else {
            PayReqString decodePaymentRequest = PayReqString.newBuilder()
                    .setPayReq(lnUrlPaySecondResponse.getPaymentRequest())
                    .build();

            getCompositeDisposable().add(LndConnection.getInstance().getLightningService().decodePayReq(decodePaymentRequest)
                    .timeout(RefConstants.TIMEOUT_SHORT * TorUtil.getTorTimeoutMultiplier(), TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(invoice -> {
                        ZapLog.v(LOG_TAG, invoice.toString());

                        if (invoice.getTimestamp() + invoice.getExpiry() < System.currentTimeMillis() / 1000) {
                            // Show error: payment request expired.
                            ZapLog.e(LOG_TAG, "LNURL: Payment request expired.");
                            switchToFailedScreen(getString(R.string.lnurl_pay_received_invalid_payment_request, mServiceURLString));
                        } else if (invoice.getNumSatoshis() == 0) {
                            // Disable 0 sat invoices
                            ZapLog.e(LOG_TAG, "LNURL: 0 sat payments are not allowed.");
                            switchToFailedScreen(getString(R.string.lnurl_pay_received_invalid_payment_request, mServiceURLString));
                        } else if (invoice.getNumSatoshis() != mFinalChosenAmount) {
                            ZapLog.e(LOG_TAG, "LNURL: The amount in the payment request is not equal to what you wanted to send.");
                            switchToFailedScreen(getString(R.string.lnurl_pay_received_invalid_payment_request, mServiceURLString));
                        } else if (!invoice.getDescriptionHash().equals(mPaymentData.getMetadataHash())) {
                            ZapLog.e(LOG_TAG, "LNURL: The hash in the invoice does not match the hash of from the metadata send before.");
                            switchToFailedScreen(getString(R.string.lnurl_pay_received_invalid_payment_request, mServiceURLString));
                        } else {
                            SendRequest sendRequest = SendRequest.newBuilder()
                                    .setPaymentRequest(lnUrlPaySecondResponse.getPaymentRequest())
                                    .build();

                            sendPayment(lnUrlPaySecondResponse.getSuccessAction(), sendRequest);
                        }
                    }, throwable -> {
                        // If LND can't decode the payment request, show the error LND throws (always english)
                        switchToFailedScreen(throwable.getMessage());
                        ZapLog.e(LOG_TAG, throwable.getMessage());
                    }));
        }
    }


    private void sendPayment(LnUrlPaySuccessAction successAction, SendRequest sendRequest) {

        ZapLog.d(LOG_TAG, "Trying to send lightning payment...");

        getCompositeDisposable().add(LndConnection.getInstance().getLightningService().sendPaymentSync(sendRequest)
                .subscribe(sendResponse -> {
                    // updated the history, so it is shown the next time the user views it
                    Wallet.getInstance().updateLightningPaymentHistory();

                    ZapLog.d(LOG_TAG, sendResponse.toString());

                    // show success animation
                    mHandler.postDelayed(() -> {
                        if (sendResponse.getPaymentError().equals("")) {
                            executeSuccessAction(successAction, sendResponse);
                        } else {
                            String errorPrefix = getResources().getString(R.string.error).toUpperCase() + ": ";
                            String error = errorPrefix + sendResponse.getPaymentError();
                            switchToFailedScreen(error);
                        }

                    }, 300);
                }, throwable -> {
                    ZapLog.d(LOG_TAG, "Exception in send payment task.");
                    ZapLog.d(LOG_TAG, throwable.getMessage());

                    String errorPrefix = getResources().getString(R.string.error).toUpperCase() + ":";
                    String errormessage = throwable.getMessage().replace("UNKNOWN:", errorPrefix);
                    mHandler.postDelayed(() -> switchToFailedScreen(errormessage), 300);

                }));
    }


    private void executeSuccessAction(LnUrlPaySuccessAction successAction, SendResponse sendResponse) {

        mTvFinishedText2.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(mFinalChosenAmount));

        if (successAction == null) {
            ZapLog.d(LOG_TAG, "No Success action.");
            mTvFinishedText3.setVisibility(View.GONE);
        } else if (successAction.isMessage()) {
            ZapLog.d(LOG_TAG, "SuccessAction: Message: " + successAction.getMessage());
            mTvFinishedText3.setText(successAction.getMessage());
        } else if (successAction.isUrl()) {
            ZapLog.d(LOG_TAG, "SuccessAction: Url: " + successAction.getUrl());
            mTvFinishedText3.setVisibility(View.GONE);

            ClipBoardUtil.copyToClipboard(getActivity(), "URL", successAction.getUrl());
            String message = successAction.getDescription() + "\n\n" + successAction.getUrl() + "\n";
            LayoutInflater adbInflater = LayoutInflater.from(getActivity());
            View titleView = adbInflater.inflate(R.layout.dialog_warning_header, null);
            ((TextView) titleView.findViewById(R.id.warningMessage)).setText(R.string.lnurl_pay_save_url);
            AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
                    .setMessage(message)
                    .setCustomTitle(titleView)
                    .setCancelable(false)
                    .setPositiveButton(R.string.open, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Call the url
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(successAction.getUrl()));
                            getActivity().startActivity(browserIntent);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            Dialog dlg = adb.create();
            // Apply FLAG_SECURE to dialog to prevent screen recording
            if (PrefsUtil.preventScreenRecording()) {
                dlg.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
            }
            dlg.show();
        } else if (successAction.isAes()) {
            // Decrypt ciphertext with payment preimage
            ZapLog.d(LOG_TAG, "SuccessAction: Aes.");
            try {
                String decrypted = decrypt(successAction.getCiphertext(), sendResponse.getPaymentPreimage().toByteArray(), successAction.getIv());
                ZapLog.d(LOG_TAG, "Decrypted secret is: " + decrypted);
                mTvFinishedText3.setVisibility(View.GONE);

                ClipBoardUtil.copyToClipboard(getActivity(), "Code", decrypted);
                String message = successAction.getDescription() + "\n\n" + decrypted + "\n";
                LayoutInflater adbInflater = LayoutInflater.from(getActivity());
                View titleView = adbInflater.inflate(R.layout.dialog_warning_header, null);
                ((TextView) titleView.findViewById(R.id.warningMessage)).setText(R.string.lnurl_pay_save_secret);
                AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
                        .setMessage(message)
                        .setCustomTitle(titleView)
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        });
                Dialog dlg = adb.create();
                // Apply FLAG_SECURE to dialog to prevent screen recording
                if (PrefsUtil.preventScreenRecording()) {
                    dlg.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
                }
                dlg.show();

            } catch (Exception e) {
                e.printStackTrace();
                ZapLog.e(LOG_TAG, "Decryption error!");
                mTvFinishedText3.setText(R.string.lnurl_pay_success_secret_decrypt_error);
            }
        } else {
            ZapLog.d(LOG_TAG, "Success action not supported.");
            mTvFinishedText3.setVisibility(View.GONE);
        }
        switchToSuccessScreen();
    }


    public static String decrypt(String textToDecrypt, byte[] key, String iv) throws Exception {
        final String initializationVector = "8119745113154120";
        byte[] encrypted_bytes = Base64.decode(textToDecrypt, Base64.DEFAULT);
        byte[] iv_bytes = Base64.decode(iv, Base64.DEFAULT);
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(iv_bytes));
        byte[] decrypted = cipher.doFinal(encrypted_bytes);
        return new String(decrypted, "UTF-8");
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

    private void switchToWithdrawProgressScreen() {

        // make previous buttons and edit texts unclickable
        mNumpad.setEnabled(false);
        mBtnSend.setEnabled(false);
        mEtAmount.setEnabled(false);
        mNumpad.setEnabled(false);

        // Animate out

        AlphaAnimation animateOut = new AlphaAnimation(1.0f, 0f);
        animateOut.setDuration(200);
        animateOut.setFillAfter(true);
        mNumpad.startAnimation(animateOut);
        mSendAmountView.startAnimation(animateOut);
        mDescriptionView.startAnimation(animateOut);
        mBtnSend.startAnimation(animateOut);
        mTvTitle.startAnimation(animateOut);
        mIvBsdIcon.startAnimation(animateOut);

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
        mTvFinishedText.setText(R.string.send_fail);
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
