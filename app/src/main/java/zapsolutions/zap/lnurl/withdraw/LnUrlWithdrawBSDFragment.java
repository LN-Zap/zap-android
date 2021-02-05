package zapsolutions.zap.lnurl.withdraw;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.transition.TransitionManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.github.lightningnetwork.lnd.lnrpc.Invoice;
import com.google.gson.Gson;

import java.net.MalformedURLException;
import java.net.URL;

import zapsolutions.zap.R;
import zapsolutions.zap.connection.HttpClient;
import zapsolutions.zap.connection.lndConnection.LndConnection;
import zapsolutions.zap.customView.BSDProgressView;
import zapsolutions.zap.customView.BSDResultView;
import zapsolutions.zap.customView.BSDScrollableMainView;
import zapsolutions.zap.customView.NumpadView;
import zapsolutions.zap.fragments.ZapBSDFragment;
import zapsolutions.zap.util.MonetaryUtil;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.Wallet;
import zapsolutions.zap.util.ZapLog;


public class LnUrlWithdrawBSDFragment extends ZapBSDFragment {

    private static final String LOG_TAG = LnUrlWithdrawBSDFragment.class.getName();

    private BSDScrollableMainView mBSDScrollableMainView;
    private BSDResultView mResultView;
    private BSDProgressView mProgressView;
    private ConstraintLayout mContentTopLayout;
    private View mWithdrawInputs;
    private EditText mEtAmount;
    private EditText mEtDescription;
    private TextView mTvUnit;
    private View mDescriptionView;
    private NumpadView mNumpad;
    private Button mBtnWithdraw;
    private TextView mTvWithdrawSource;

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

        mBSDScrollableMainView = view.findViewById(R.id.scrollableBottomSheet);
        mResultView = view.findViewById(R.id.resultLayout);
        mContentTopLayout = view.findViewById(R.id.contentTopLayout);
        mProgressView = view.findViewById(R.id.paymentProgressLayout);

        mWithdrawInputs = view.findViewById(R.id.withdrawInputsView);
        mEtAmount = view.findViewById(R.id.withdrawAmount);
        mTvUnit = view.findViewById(R.id.unit);
        mEtDescription = view.findViewById(R.id.withdrawDescription);
        mDescriptionView = view.findViewById(R.id.withdrawDescriptionTopLayout);
        mTvWithdrawSource = view.findViewById(R.id.withdrawSource);

        mNumpad = view.findViewById(R.id.numpadView);
        mBtnWithdraw = view.findViewById(R.id.withdrawButton);


        mBSDScrollableMainView.setTitle(R.string.withdraw);
        mBSDScrollableMainView.setOnCloseListener(this::dismiss);
        mBSDScrollableMainView.setTitleIconVisibility(true);
        mResultView.setOnOkListener(this::dismiss);

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
                        .setPrivate(PrefsUtil.getPrefs().getBoolean("includePrivateChannelHints", true))
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

    private void switchToWithdrawProgressScreen() {
        mProgressView.setVisibility(View.VISIBLE);
        mWithdrawInputs.setVisibility(View.INVISIBLE);
        mProgressView.startSpinning();
        mBSDScrollableMainView.animateTitleOut();
    }

    private void switchToSuccessScreen() {
        mProgressView.spinningFinished(true);
        TransitionManager.beginDelayedTransition((ViewGroup) mContentTopLayout.getRootView());
        mWithdrawInputs.setVisibility(View.GONE);
        mResultView.setVisibility(View.VISIBLE);
        mResultView.setHeading(R.string.lnurl_withdraw_success, true);
    }

    private void switchToFailedScreen(String error) {
        mProgressView.spinningFinished(false);
        TransitionManager.beginDelayedTransition((ViewGroup) mContentTopLayout.getRootView());
        mWithdrawInputs.setVisibility(View.GONE);
        mResultView.setVisibility(View.VISIBLE);

        // Set failed states
        mResultView.setHeading(R.string.lnurl_withdraw_fail, false);
        mResultView.setDetailsText(error);
    }
}
