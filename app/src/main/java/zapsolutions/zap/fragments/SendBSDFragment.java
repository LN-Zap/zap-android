package zapsolutions.zap.fragments;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.inputmethod.InputMethodManager;
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
import androidx.preference.PreferenceManager;
import androidx.transition.ChangeBounds;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;


import com.github.lightningnetwork.lnd.lnrpc.EstimateFeeRequest;
import com.github.lightningnetwork.lnd.lnrpc.EstimateFeeResponse;
import com.github.lightningnetwork.lnd.lnrpc.FeeLimit;
import com.github.lightningnetwork.lnd.lnrpc.LightningGrpc;
import com.github.lightningnetwork.lnd.lnrpc.QueryRoutesRequest;
import com.github.lightningnetwork.lnd.lnrpc.QueryRoutesResponse;
import com.github.lightningnetwork.lnd.lnrpc.Route;
import com.github.lightningnetwork.lnd.lnrpc.SendCoinsRequest;
import com.github.lightningnetwork.lnd.lnrpc.SendCoinsResponse;
import com.github.lightningnetwork.lnd.lnrpc.SendRequest;
import com.github.lightningnetwork.lnd.lnrpc.SendResponse;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

import zapsolutions.zap.R;
import zapsolutions.zap.channelManagement.ManageChannelsActivity;
import zapsolutions.zap.connection.LndConnection;

import zapsolutions.zap.util.ExecuteOnCaller;
import zapsolutions.zap.util.MonetaryUtil;
import zapsolutions.zap.util.OnSingleClickListener;
import zapsolutions.zap.util.Wallet;
import zapsolutions.zap.util.ZapLog;


public class SendBSDFragment extends BottomSheetDialogFragment {

    private static final String LOG_TAG = "Receive Activity";

    private SharedPreferences mPrefs;

    private ConstraintLayout mRootLayout;
    private ImageView mIvBsdIcon;
    private ConstraintLayout mIconAnchor;
    private TextView mTvTitle;

    private View mSendAmountView;
    private EditText mEtAmount;
    private EditText mEtMemo;
    private TextView mTvUnit;
    private View mMemoView;
    private View mFeeView;
    private TextView mTvSendFee;
    private View mNumpad;
    private Button[] mBtnNumpad = new Button[10];
    private Button mBtnNumpadDot;
    private ImageButton mBtnNumpadBack;
    private Button mBtnSend;

    private View mProgressScreen;
    private View mFinishedScreen;
    private Button mOkButton;
    private ImageView mProgressFinishedIcon;
    private ImageView mIvProgressPaymentTypeIcon;
    private ImageView mIvFinishedPaymentTypeIcon;
    private TextView mTvFinishedText;
    private TextView mTvFinishedText2;
    private View mProgressBar;

    private TextView mTvNoOutgoingBalance;
    private View mViewNoOutgoingBalance;
    private Button mBtnManageChannels;

    private BottomSheetBehavior mBehavior;
    private FrameLayout mBottomSheet;

    private String mMemo;
    private String mOnChainAddress;
    private boolean mOnChain;
    private long mFixedAmount;

    private boolean mAmountValid = true;
    private long mMaxLightningFee = 0;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle args = getArguments();
        mOnChain = args.getBoolean("onChain");
        mFixedAmount = args.getLong("onChainAmount");
        mOnChainAddress = args.getString("onChainAddress");
        mMemo = args.getString("onChainMessage");

        View view = inflater.inflate(R.layout.bsd_send, container);

        // Apply FLAG_SECURE to dialog to prevent screen recording
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (mPrefs.getBoolean("preventScreenRecording", true)) {
            getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        mRootLayout = view.findViewById(R.id.rootLayout);
        mIvBsdIcon = view.findViewById(R.id.bsdIcon);
        mIconAnchor = view.findViewById(R.id.anchor);
        mTvTitle = view.findViewById(R.id.bsdTitle);

        mSendAmountView = view.findViewById(R.id.sendInputsView);
        mEtAmount = view.findViewById(R.id.sendAmount);
        mTvUnit = view.findViewById(R.id.sendUnit);
        mEtMemo = view.findViewById(R.id.sendMemo);
        mMemoView = view.findViewById(R.id.sendMemoTopLayout);
        mFeeView = view.findViewById(R.id.sendFeeTopLayout);
        mTvSendFee = view.findViewById(R.id.sendFee);
        mNumpad = view.findViewById(R.id.Numpad);
        mBtnSend = view.findViewById(R.id.sendButton);

        mProgressScreen = view.findViewById(R.id.paymentProgressLayout);
        mFinishedScreen = view.findViewById(R.id.paymentFinishedLayout);
        mOkButton = view.findViewById(R.id.okButton);
        mProgressFinishedIcon = view.findViewById(R.id.progressFinishedIcon);
        mIvProgressPaymentTypeIcon = view.findViewById(R.id.progressPaymentTypeIcon);
        mIvFinishedPaymentTypeIcon = view.findViewById(R.id.finishedPaymentTypeIcon);
        mTvFinishedText = view.findViewById(R.id.finishedText);
        mTvFinishedText2 = view.findViewById(R.id.finishedText2);
        mProgressBar = view.findViewById(R.id.progressBar);

        mTvNoOutgoingBalance = view.findViewById(R.id.noOutgoingChannelBalanceText);
        mViewNoOutgoingBalance = view.findViewById(R.id.noOutgoingChannelBalanceView);
        mBtnManageChannels = view.findViewById(R.id.manageChannels);


        // Get numpad buttons
        mBtnNumpad[0] = view.findViewById(R.id.Numpad1);
        mBtnNumpad[1] = view.findViewById(R.id.Numpad2);
        mBtnNumpad[2] = view.findViewById(R.id.Numpad3);
        mBtnNumpad[3] = view.findViewById(R.id.Numpad4);
        mBtnNumpad[4] = view.findViewById(R.id.Numpad5);
        mBtnNumpad[5] = view.findViewById(R.id.Numpad6);
        mBtnNumpad[6] = view.findViewById(R.id.Numpad7);
        mBtnNumpad[7] = view.findViewById(R.id.Numpad8);
        mBtnNumpad[8] = view.findViewById(R.id.Numpad9);
        mBtnNumpad[9] = view.findViewById(R.id.Numpad0);

        mBtnNumpadDot = view.findViewById(R.id.NumpadDot);
        mBtnNumpadBack = view.findViewById(R.id.NumpadBack);

        // Set action for numpad number buttons
        for (Button btn : mBtnNumpad) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Add input
                    int start = Math.max(mEtAmount.getSelectionStart(), 0);
                    int end = Math.max(mEtAmount.getSelectionEnd(), 0);
                    mEtAmount.getText().replace(Math.min(start, end), Math.max(start, end),
                            btn.getText(), 0, btn.getText().length());

                }
            });
        }

        // Set action for numpad "." button
        mBtnNumpadDot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add input
                int start = Math.max(mEtAmount.getSelectionStart(), 0);
                int end = Math.max(mEtAmount.getSelectionEnd(), 0);
                mEtAmount.getText().replace(Math.min(start, end), Math.max(start, end),
                        mBtnNumpadDot.getText(), 0, mBtnNumpadDot.getText().length());
            }
        });

        // Set action for numpad "delete" button
        mBtnNumpadBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // remove Input
                deleteAmountInput();
            }
        });


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
                    deleteAmountInput();
                }

                if (!mEtAmount.getText().toString().equals(".")) {
                    // make text red if input is too large
                    long maxSendable;
                    if (mOnChain) {

                        if (mPrefs.getBoolean("isWalletSetup", false)) {
                            maxSendable = Wallet.getInstance().getBalances().onChainConfirmed();
                        } else {
                            maxSendable = Wallet.getInstance().getDemoBalances().onChainConfirmed();
                        }
                    } else {
                        if (mPrefs.getBoolean("isWalletSetup", false)) {
                            maxSendable = Wallet.getInstance().getMaxChannelLocalBalance();
                        } else {
                            maxSendable = 750000;
                        }
                    }

                    long currentValue = Long.parseLong(MonetaryUtil.getInstance().convertPrimaryToSatoshi(mEtAmount.getText().toString()));
                    if (currentValue > maxSendable) {
                        mEtAmount.setTextColor(getResources().getColor(R.color.superRed));
                        String maxAmount = getResources().getString(R.string.max_amount) + " " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(maxSendable);
                        Toast.makeText(getActivity(), maxAmount, Toast.LENGTH_SHORT).show();
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
                    mTvSendFee.setText("");
                }
            }
        });


        if (mOnChain) {


            mIvBsdIcon.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_icon_modal_on_chain));
            mIvFinishedPaymentTypeIcon.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_onchain_black_24dp));
            mIvProgressPaymentTypeIcon.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_onchain_black_24dp));
            mTvTitle.setText(R.string.send_onChainPayment);

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
                mBtnSend.setEnabled(false);
                mBtnSend.setTextColor(getResources().getColor(R.color.gray));

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // We have to call this delayed, as otherwise it will still bring up the softKeyboard
                        mEtAmount.requestFocus();
                    }
                }, 200);

            }


            if (mMemo != null) {
                mEtMemo.setText(mMemo);
            }

            // Action when clicked on "Send payment"
            mBtnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ZapLog.debug(LOG_TAG, "Trying to send on-chain payment...");
                    // Send on-chain payment

                    long sendAmount = 0L;
                    if (mFixedAmount != 0L) {
                        sendAmount = mFixedAmount;
                    } else {
                        sendAmount = Long.parseLong(MonetaryUtil.getInstance().convertPrimaryToSatoshi(mEtAmount.getText().toString()));
                    }

                    if (sendAmount != 0L) {

                        mTvFinishedText2.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(sendAmount));

                        switchToSendProgressScreen();

                        LightningGrpc.LightningFutureStub asyncOnChainSendClient = LightningGrpc
                                .newFutureStub(LndConnection.getInstance().getSecureChannel())
                                .withCallCredentials(LndConnection.getInstance().getMacaroon());

                        SendCoinsRequest sendRequest = SendCoinsRequest.newBuilder()
                                .setAddr(mOnChainAddress)
                                .setAmount(sendAmount)
                                .setTargetConf(3)
                                .build();

                        final ListenableFuture<SendCoinsResponse> sendFuture = asyncOnChainSendClient.sendCoins(sendRequest);

                        sendFuture.addListener(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    SendCoinsResponse sendResponse = sendFuture.get();

                                    // updated the history, so it is shown the next time the user views it
                                    Wallet.getInstance().updateOnChainTransactionHistory();

                                    ZapLog.debug(LOG_TAG, sendResponse.toString());

                                    // show success animation
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            switchToSuccessScreen();
                                        }
                                    }, 300);

                                } catch (InterruptedException e) {
                                    ZapLog.debug(LOG_TAG, "send coins request interrupted.");
                                    switchToFailedScreen("Request interrupted");
                                } catch (ExecutionException e) {
                                    ZapLog.debug(LOG_TAG, "Exception in send coins request task.");
                                    ZapLog.debug(LOG_TAG, e.getMessage());

                                    // possible error messages: checksum mismatch, decoded address is of unknown format

                                    ZapLog.debug(LOG_TAG, "Error during payment!");
                                    ZapLog.debug(LOG_TAG, e.getMessage());

                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            String errorPrefix = getResources().getString(R.string.error).toUpperCase() + ":";
                                            switchToFailedScreen(e.getCause().getMessage().replace("UNKNOWN:", errorPrefix));
                                        }
                                    }, 300);
                                }

                            }
                        }, new ExecuteOnCaller());


                    } else {
                        // Send amount == 0
                        Toast.makeText(getActivity(), "Send amount is to small.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } else {

            // Lightning Payment


            mIvBsdIcon.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_icon_modal_lightning));
            mIvFinishedPaymentTypeIcon.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_nav_wallet_balck_24dp));
            mIvProgressPaymentTypeIcon.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_nav_wallet_balck_24dp));
            mTvTitle.setText(R.string.send_lightningPayment);


            if (Wallet.getInstance().mPaymentRequest.getDescription() == null) {
                mMemoView.setVisibility(View.VISIBLE);
            } else {
                mMemoView.setVisibility(View.VISIBLE);
                mEtMemo.setText(Wallet.getInstance().mPaymentRequest.getDescription());
            }


            if (Wallet.getInstance().mPaymentRequest.getNumSatoshis() != 0) {
                // A specific amount was requested. We are not allowed to change the amount
                mFixedAmount = Wallet.getInstance().mPaymentRequest.getNumSatoshis();
                mEtAmount.setText(MonetaryUtil.getInstance().convertSatoshiToPrimary(mFixedAmount));
                mEtAmount.clearFocus();
                mEtAmount.setFocusable(false);
            } else {
                // No specific amount was requested. Let User input an amount.
                mNumpad.setVisibility(View.VISIBLE);
                mBtnSend.setEnabled(false);
                mBtnSend.setTextColor(getResources().getColor(R.color.gray));

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // We have to call this delayed, as otherwise it will still bring up the softKeyboard
                        mEtAmount.requestFocus();
                    }
                }, 200);

            }


            // Action when clicked on "Send payment"
            mBtnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // send lightning payment
                    if (mPrefs.getBoolean("isWalletSetup", false)) {

                        switchToSendProgressScreen();

                        ZapLog.debug(LOG_TAG, "Trying to send lightning payment...");

                        SendRequest.Builder srb = SendRequest.newBuilder();
                        srb.setPaymentRequest(Wallet.getInstance().mPaymentRequestString);
                        srb.setFeeLimit(FeeLimit.newBuilder()
                                .setFixed(mMaxLightningFee)
                                .build());

                        if (Wallet.getInstance().mPaymentRequest.getNumSatoshis() == 0) {
                            srb.setAmt(Long.parseLong(MonetaryUtil.getInstance().convertPrimaryToSatoshi(mEtAmount.getText().toString())));
                            mTvFinishedText2.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(Long.parseLong(MonetaryUtil.getInstance().convertPrimaryToSatoshi(mEtAmount.getText().toString()))));
                        } else {
                            mTvFinishedText2.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(Wallet.getInstance().mPaymentRequest.getNumSatoshis()));
                        }

                        SendRequest sendRequest = srb.build();


                        LightningGrpc.LightningFutureStub asyncLightningSendClient = LightningGrpc
                                .newFutureStub(LndConnection.getInstance().getSecureChannel())
                                .withCallCredentials(LndConnection.getInstance().getMacaroon());

                        final ListenableFuture<SendResponse> sendFuture = asyncLightningSendClient.sendPaymentSync(sendRequest);

                        sendFuture.addListener(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    SendResponse sendResponse = sendFuture.get();

                                    // updated the history, so it is shown the next time the user views it
                                    Wallet.getInstance().updateLightningPaymentHistory();

                                    ZapLog.debug(LOG_TAG, sendResponse.toString());

                                    // show success animation
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (sendResponse.getPaymentError().equals("")) {
                                                switchToSuccessScreen();
                                            } else {
                                                String errorPrefix = getResources().getString(R.string.error).toUpperCase() + ": ";
                                                String error = errorPrefix + sendResponse.getPaymentError();
                                                switchToFailedScreen(error);
                                            }

                                        }
                                    }, 300);

                                } catch (InterruptedException e) {
                                    ZapLog.debug(LOG_TAG, "send request interrupted.");
                                    switchToFailedScreen("Request interrupted");
                                } catch (ExecutionException e) {
                                    ZapLog.debug(LOG_TAG, "Exception in send request task.");
                                    ZapLog.debug(LOG_TAG, e.getMessage());

                                    // possible error messages: checksum mismatch, decoded address is of unknown format

                                    ZapLog.debug(LOG_TAG, "Error during payment!");
                                    ZapLog.debug(LOG_TAG, e.getMessage());

                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            String errorPrefix = getResources().getString(R.string.error).toUpperCase() + ":";
                                            switchToFailedScreen(e.getCause().getMessage().replace("UNKNOWN:", errorPrefix));
                                        }
                                    }, 300);
                                }

                            }
                        }, new ExecuteOnCaller());

                    } else {
                        // Demo Mode
                        Toast.makeText(getActivity(), R.string.demo_setupWalletFirst, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


        // Action when clicked on "manage Channels" button
        mBtnManageChannels.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent intent = new Intent(getActivity(), ManageChannelsActivity.class);
                startActivity(intent);
                dismiss();
            }
        });


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

        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


        // Calculate fee
        calculateFee();

        return view;
    }

    // This gets executed after onCreateView. We edit the bottomSheetBehavior to not react to swipes
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FrameLayout bottomSheet = getDialog().findViewById(R.id.design_bottom_sheet);
        mBehavior = BottomSheetBehavior.from(bottomSheet);
        mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        mBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
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

    private void deleteAmountInput() {
        boolean selection = mEtAmount.getSelectionStart() != mEtAmount.getSelectionEnd();

        int start = Math.max(mEtAmount.getSelectionStart(), 0);
        int end = Math.max(mEtAmount.getSelectionEnd(), 0);

        String before = mEtAmount.getText().toString().substring(0, start);
        String after = mEtAmount.getText().toString().substring(end);

        if (selection) {
            String outputText = before + after;
            mEtAmount.setText(outputText);
            mEtAmount.setSelection(start);
        } else {
            if (before.length() >= 1) {
                String newBefore = before.substring(0, before.length() - 1);
                String outputText = newBefore + after;
                mEtAmount.setText(outputText);
                mEtAmount.setSelection(start - 1);
            }
        }
    }

    private void switchToSendProgressScreen() {

        // make previous buttons and edit texts unclickable
        for (Button btn : mBtnNumpad) {
            btn.setEnabled(false);
        }
        mBtnNumpadBack.setEnabled(false);
        mBtnNumpadDot.setEnabled(false);
        mBtnSend.setEnabled(false);
        mBtnManageChannels.setEnabled(false);
        mEtAmount.setEnabled(false);
        mNumpad.setEnabled(false);

        // Animate out

        AlphaAnimation animateOut = new AlphaAnimation(1.0f, 0f);
        animateOut.setDuration(200);
        animateOut.setFillAfter(true);

        mNumpad.startAnimation(animateOut);
        mSendAmountView.startAnimation(animateOut);
        mMemoView.startAnimation(animateOut);
        mBtnSend.startAnimation(animateOut);
        mTvTitle.startAnimation(animateOut);
        mIvBsdIcon.startAnimation(animateOut);
        mFeeView.startAnimation(animateOut);

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

    private void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    private void calculateFee() {

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

            estimateOnChainFee(mOnChainAddress, sendAmount, 3);
        } else {
            if (Wallet.getInstance().mPaymentRequest.getNumSatoshis() == 0) {
                long sendAmount = 0L;
                try {
                    sendAmount = Long.parseLong(MonetaryUtil.getInstance().convertPrimaryToSatoshi(mEtAmount.getText().toString()));
                } catch (NumberFormatException e) {

                }
                queryRoutes(Wallet.getInstance().mPaymentRequest.getDestination(), sendAmount);
            } else {
                queryRoutes(Wallet.getInstance().mPaymentRequest.getDestination(), Wallet.getInstance().mPaymentRequest.getNumSatoshis());
            }
        }
    }

    /**
     * This function is used to calculate the expected on chain fee.
     */
    private void estimateOnChainFee(String address, long amount, int targetConf) {
        // let LND estimate fee
        LightningGrpc.LightningFutureStub asyncEstimateFeeClient = LightningGrpc
                .newFutureStub(LndConnection.getInstance().getSecureChannel())
                .withCallCredentials(LndConnection.getInstance().getMacaroon());

        EstimateFeeRequest asyncEstimateFeeRequest = EstimateFeeRequest.newBuilder()
                .putAddrToAmount(address, amount)
                .setTargetConf(targetConf)
                .build();
        final ListenableFuture<EstimateFeeResponse> estimateFeeFuture = asyncEstimateFeeClient.estimateFee(asyncEstimateFeeRequest);


        estimateFeeFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    EstimateFeeResponse estimateFeeResponse = estimateFeeFuture.get();

                    mTvSendFee.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(estimateFeeResponse.getFeeSat()));

                    // ZapLog.debug(LOG_TAG, estimateFeeResponse.toString());
                } catch (InterruptedException e) {
                    ZapLog.debug(LOG_TAG, "Fee estimation request interrupted.");
                } catch (ExecutionException e) {
                    // ZapLog.debug(LOG_TAG, "Exception in fee estimation request task.");
                    ZapLog.debug(LOG_TAG, e.getMessage());
                }
            }
        }, new ExecuteOnCaller());
    }

    /**
     * Query Routes. This is used to determine the expected fees for a lightning payment.
     */
    private void queryRoutes(String pubKey, long amount) {
        LightningGrpc.LightningFutureStub asyncQueryRoutesClient = LightningGrpc
                .newFutureStub(LndConnection.getInstance().getSecureChannel())
                .withCallCredentials(LndConnection.getInstance().getMacaroon());

        QueryRoutesRequest asyncQueryRoutesRequest = QueryRoutesRequest.newBuilder()
                .setPubKey(pubKey)
                .setAmt(amount)
                .build();
        final ListenableFuture<QueryRoutesResponse> queryRoutesFuture = asyncQueryRoutesClient.queryRoutes(asyncQueryRoutesRequest);

        queryRoutesFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    QueryRoutesResponse queryRoutesResponse = queryRoutesFuture.get();

                    Route firstRoute = queryRoutesResponse.getRoutes(0);
                    Route lastRoute = queryRoutesResponse.getRoutes(queryRoutesResponse.getRoutesCount() - 1);

                    String feeLowerBound = MonetaryUtil.getInstance().getPrimaryDisplayAmount(firstRoute.getTotalFeesMsat() / 1000);
                    String feeUpperBound = MonetaryUtil.getInstance().getPrimaryDisplayAmount(lastRoute.getTotalFeesMsat() / 1000);

                    String feeString;

                    mMaxLightningFee = lastRoute.getTotalFeesMsat() / 1000;

                    if (feeLowerBound.equals(feeUpperBound)) {
                        feeString = feeLowerBound + " " + MonetaryUtil.getInstance().getPrimaryDisplayUnit();
                    } else {
                        feeString = feeLowerBound + "-" + feeUpperBound + " " + MonetaryUtil.getInstance().getPrimaryDisplayUnit();
                    }

                    mTvSendFee.setText(feeString);

                    //ZapLog.debug(LOG_TAG, queryRoutesResponse.toString());
                } catch (InterruptedException e) {
                    ZapLog.debug(LOG_TAG, "Query routes request interrupted.");
                } catch (ExecutionException e) {
                    // ZapLog.debug(LOG_TAG, "Exception in query routes request task.");
                    ZapLog.debug(LOG_TAG, e.getMessage());
                }
            }
        }, new ExecuteOnCaller());
    }

}
