package zapsolutions.zap.lnurl.withdraw;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.github.lightningnetwork.lnd.lnrpc.Invoice;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;

import java.net.MalformedURLException;
import java.net.URL;

import zapsolutions.zap.R;
import zapsolutions.zap.connection.HttpClient;
import zapsolutions.zap.connection.establishConnectionToLnd.LndConnection;
import zapsolutions.zap.customView.NumpadView;
import zapsolutions.zap.fragments.RxBSDFragment;
import zapsolutions.zap.util.MonetaryUtil;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.Wallet;
import zapsolutions.zap.util.ZapLog;


public class LnUrlWithdrawBSDFragment extends RxBSDFragment {

    private static final String LOG_TAG = LnUrlWithdrawBSDFragment.class.getName();

    private ConstraintLayout mRootLayout;
    private ImageView mIvBsdIcon;
    private TextView mTvTitle;
    private View mWithdrawAmountView;
    private EditText mEtAmount;
    private EditText mEtDescription;
    private TextView mTvUnit;
    private View mDescriptionView;
    private NumpadView mNumpad;
    private Button mBtnWithdraw;
    private View mProgressScreen;
    private View mFinishedScreen;
    private Button mOkButton;
    private ImageView mProgressFinishedIcon;
    private ImageView mIvProgressPaymentTypeIcon;
    private ImageView mIvFinishedPaymentTypeIcon;
    private TextView mTvFinishedText;
    private TextView mTvFinishedText2;
    private View mProgressBar;
    private TextView mTvWithdrawSource;
    private BottomSheetBehavior mBehavior;
    private long mFixedAmount;
    private boolean mAmountValid = true;
    private long mMinWithdrawable;
    private long mMaxWithdrawable;
    private String mServiceURLString;
    private boolean mCurrencyJustSwitched;
    private boolean mValueModifiedSinceSwitch;
    private long mTempCurrentSatoshiValue;

    private Handler mHandler;
    private LnUrlWithdrawResponse mWithdrawData;

    public static LnUrlWithdrawBSDFragment createWithdrawDialog(LnUrlWithdrawResponse response) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(LnUrlWithdrawResponse.ARGS_KEY, response);
        Intent intent = new Intent();
        intent.putExtras(bundle);
        LnUrlWithdrawBSDFragment lnUrlWithdrawBSDFragment = new LnUrlWithdrawBSDFragment();
        lnUrlWithdrawBSDFragment.setArguments(intent.getExtras());
        return lnUrlWithdrawBSDFragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle args = getArguments();
        mWithdrawData = (LnUrlWithdrawResponse) args.getSerializable(LnUrlWithdrawResponse.ARGS_KEY);

        // Calculate correct min and max withdrawal value for LNURL. Zap limits withdrawal to full satoshis.
        mMaxWithdrawable = Math.min((mWithdrawData.getMaxWithdrawable() / 1000), Wallet.getInstance().getMaxLightningReceiveAmount());
        mMinWithdrawable = mWithdrawData.getMinWithdrawable() % 1000 == 0 ? Math.max((mWithdrawData.getMinWithdrawable() / 1000), 1L) : Math.max((mWithdrawData.getMinWithdrawable() / 1000) + 1L, 1L);

        // Extract the URL from the Withdraw service
        try {
            URL url = new URL(mWithdrawData.getCallback());
            mServiceURLString = url.getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        mHandler = new Handler();

        View view = inflater.inflate(R.layout.bsd_lnurl_withdraw, container);

        // Apply FLAG_SECURE to dialog to prevent screen recording
        if (PrefsUtil.preventScreenRecording()) {
            getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        mRootLayout = view.findViewById(R.id.rootLayout);
        mIvBsdIcon = view.findViewById(R.id.bsdIcon);
        mTvTitle = view.findViewById(R.id.bsdTitle);

        mWithdrawAmountView = view.findViewById(R.id.withdrawInputsView);
        mEtAmount = view.findViewById(R.id.withdrawAmount);
        mTvUnit = view.findViewById(R.id.unit);
        mEtDescription = view.findViewById(R.id.withdrawDescription);
        mDescriptionView = view.findViewById(R.id.withdrawDescriptionTopLayout);
        mTvWithdrawSource = view.findViewById(R.id.withdrawSource);

        mNumpad = view.findViewById(R.id.numpadView);
        mBtnWithdraw = view.findViewById(R.id.withdrawButton);

        mProgressScreen = view.findViewById(R.id.paymentProgressLayout);
        mFinishedScreen = view.findViewById(R.id.paymentFinishedLayout);
        mOkButton = view.findViewById(R.id.okButton);
        mProgressFinishedIcon = view.findViewById(R.id.progressFinishedIcon);
        mIvProgressPaymentTypeIcon = view.findViewById(R.id.progressPaymentTypeIcon);
        mIvFinishedPaymentTypeIcon = view.findViewById(R.id.finishedPaymentTypeIcon);
        mTvFinishedText = view.findViewById(R.id.finishedText);
        mTvFinishedText2 = view.findViewById(R.id.finishedText2);
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
                        mBtnWithdraw.setEnabled(true);
                        mBtnWithdraw.setTextColor(getResources().getColor(R.color.lightningOrange));
                        return;
                    }
                    mValueModifiedSinceSwitch = true;
                    long currentValue = Long.parseLong(MonetaryUtil.getInstance().convertPrimaryToSatoshi(mEtAmount.getText().toString()));

                    // make text red if input is too large or too small
                    if (currentValue > mMaxWithdrawable) {
                        mEtAmount.setTextColor(getResources().getColor(R.color.superRed));
                        String maxAmount = getResources().getString(R.string.max_amount) + " " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(mMaxWithdrawable);
                        Toast.makeText(getActivity(), maxAmount, Toast.LENGTH_SHORT).show();
                        mBtnWithdraw.setEnabled(false);
                        mBtnWithdraw.setTextColor(getResources().getColor(R.color.gray));
                    } else if (currentValue < mMinWithdrawable) {
                        mEtAmount.setTextColor(getResources().getColor(R.color.superRed));
                        String minAmount = getResources().getString(R.string.min_amount) + " " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(mMinWithdrawable);
                        Toast.makeText(getActivity(), minAmount, Toast.LENGTH_SHORT).show();
                        mBtnWithdraw.setEnabled(false);
                        mBtnWithdraw.setTextColor(getResources().getColor(R.color.gray));
                    } else {
                        mEtAmount.setTextColor(getResources().getColor(R.color.white));
                        mBtnWithdraw.setEnabled(true);
                        mBtnWithdraw.setTextColor(getResources().getColor(R.color.lightningOrange));
                    }
                    if (currentValue == 0) {
                        mBtnWithdraw.setEnabled(false);
                        mBtnWithdraw.setTextColor(getResources().getColor(R.color.gray));
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
        mTvTitle.setText(R.string.withdraw);
        if (mServiceURLString != null) {
            mTvWithdrawSource.setText(mServiceURLString);
        } else {
            mTvWithdrawSource.setText(R.string.unknown);
        }

        if (mWithdrawData.getDefaultDescription() == null) {
            mDescriptionView.setVisibility(View.GONE);
        } else {
            mDescriptionView.setVisibility(View.VISIBLE);
            mEtDescription.setText(mWithdrawData.getDefaultDescription());
        }

        if (mWithdrawData.getMinWithdrawable() == mWithdrawData.getMaxWithdrawable()) {
            // A specific amount was requested. We are not allowed to change the amount.
            mFixedAmount = mWithdrawData.getMaxWithdrawable() / 1000;
            mEtAmount.setText(MonetaryUtil.getInstance().convertSatoshiToPrimary(mFixedAmount));
            mEtAmount.clearFocus();
            mEtAmount.setFocusable(false);
            mEtAmount.setEnabled(false);
        } else {
            // No specific amount was requested. Let User input an amount, but pre fill with maxWithdraw amount.
            mNumpad.setVisibility(View.VISIBLE);
            mTempCurrentSatoshiValue = mMaxWithdrawable;
            mCurrencyJustSwitched = true;
            mValueModifiedSinceSwitch = false;
            mEtAmount.setText(MonetaryUtil.getInstance().convertSatoshiToPrimary(mMaxWithdrawable));

            mHandler.postDelayed(() -> {
                // We have to call this delayed, as otherwise it will still bring up the softKeyboard
                mEtAmount.requestFocus();
                mEtAmount.setSelection(mEtAmount.getText().length());
            }, 200);
        }


        // Action when clicked on "withdraw"
        mBtnWithdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switchToWithdrawProgressScreen();

                ZapLog.d(LOG_TAG, "Trying to withdraw...");

                // Create ln-invoice
                long value;
                if (mFixedAmount == 0L) {
                    if (!mValueModifiedSinceSwitch) {
                        value = mTempCurrentSatoshiValue;
                    } else {
                        value = Long.parseLong(MonetaryUtil.getInstance().convertPrimaryToSatoshi(mEtAmount.getText().toString()));
                    }
                } else {
                    value = mFixedAmount;
                }

                Invoice asyncInvoiceRequest = Invoice.newBuilder()
                        .setValue(value)
                        .setMemo(mWithdrawData.getDefaultDescription())
                        .setExpiry(60L) // in seconds
                        .build();

                getCompositeDisposable().add(LndConnection.getInstance().getLightningService().addInvoice(asyncInvoiceRequest)
                        .subscribe(addInvoiceResponse -> {

                            // Invoice was created. Now forward it to the LNURL service to initiate withdraw.
                            LnUrlFinalWithdrawRequest lnUrlFinalWithdrawRequest = new LnUrlFinalWithdrawRequest.Builder()
                                    .setCallback(mWithdrawData.getCallback())
                                    .setK1(mWithdrawData.getK1())
                                    .setInvoice(addInvoiceResponse.getPaymentRequest())
                                    .build();

                            StringRequest lnUrlRequest = new StringRequest(Request.Method.GET, lnUrlFinalWithdrawRequest.requestAsString(),
                                    response -> validateSecondResponse(response),
                                    error -> {
                                        if (mServiceURLString != null) {
                                            switchToFailedScreen(getResources().getString(R.string.lnurl_service_not_responding, mServiceURLString));
                                        } else {
                                            String host = getResources().getString(R.string.host);
                                            switchToFailedScreen(getResources().getString(R.string.lnurl_service_not_responding, host));
                                        }
                                    });

                            // Make sure this request is executed only once and it doesn't timeout to fast.
                            // If this is not done, then it can happen that Zap shows an error although everything was executed.
                            lnUrlRequest.setRetryPolicy(new DefaultRetryPolicy(30000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                            // Send final request to LNURL service
                            HttpClient.getInstance().addToRequestQueue(lnUrlRequest, "LnUrlFinalWithdrawRequest");

                        }, throwable -> {
                            Toast.makeText(getActivity(), R.string.receive_generateRequest_failed, Toast.LENGTH_SHORT).show();
                            ZapLog.d(LOG_TAG, "Add invoice request failed: " + throwable.getMessage());
                        }));
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


        if (mMinWithdrawable > mMaxWithdrawable) {
            // There is no way the withdraw can be routed... show an error immediately
            switchToWithdrawProgressScreen();
            switchToFailedScreen(getResources().getString(R.string.lnurl_withdraw_insufficient_channel_balance));
        }

        return view;
    }

    private void validateSecondResponse(@NonNull String withdrawResponse) {
        LnUrlWithdrawResponse lnUrlWithdrawResponse = new Gson().fromJson(withdrawResponse, LnUrlWithdrawResponse.class);

        if (lnUrlWithdrawResponse.getStatus().equals("OK")) {
            switchToSuccessScreen();
        } else {
            ZapLog.d(LOG_TAG, "LNURL: Failed to withdraw. " + lnUrlWithdrawResponse.getReason());
            switchToFailedScreen(lnUrlWithdrawResponse.getReason());
        }
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
        mBtnWithdraw.setEnabled(false);
        mEtAmount.setEnabled(false);
        mNumpad.setEnabled(false);

        // Animate out

        AlphaAnimation animateOut = new AlphaAnimation(1.0f, 0f);
        animateOut.setDuration(200);
        animateOut.setFillAfter(true);
        mNumpad.startAnimation(animateOut);
        mWithdrawAmountView.startAnimation(animateOut);
        mDescriptionView.startAnimation(animateOut);
        mBtnWithdraw.startAnimation(animateOut);
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
        mTvFinishedText.setText(R.string.lnurl_withdraw_fail);
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
