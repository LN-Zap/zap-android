package zapsolutions.zap.fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.transition.ChangeBounds;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import zapsolutions.zap.R;
import zapsolutions.zap.lightning.LightningNodeUri;
import zapsolutions.zap.util.MonetaryUtil;
import zapsolutions.zap.util.OnSingleClickListener;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.Wallet;

public class OpenChannelBSDFragment extends BottomSheetDialogFragment implements Wallet.ChannelOpenUpdateListener {

    public static final String TAG = OpenChannelBSDFragment.class.getName();
    public static final String ARGS_NODE_URI = "NODE_URI";
    private ConstraintLayout mRootLayout;
    private View mNumpad;
    private Button[] mBtnNumpad = new Button[10];
    private Button mBtnNumpadDot;
    private ImageButton mBtnNumpadBack;
    private EditText mEtAmount;
    private TextView mTvUnit;
    private boolean mAmountValid = false;
    private Button mOpenChannelButton;
    private TextView mTvNodeAlias;
    private TextView mTvOnChainFunds;
    private LightningNodeUri mLightningNodeUri;
    private View mFinishedScreen;
    private View mProgressScreen;
    private ImageView mProgressFinishedIcon;
    private ImageView mIvProgressPaymentTypeIcon;
    private View mProgressBar;
    private View mLocalAmountsInputView;
    private View mNodeLayout;
    private Button mOkButton;
    private ImageView mIvBsdIcon;
    private TextView mTvFinishedText;
    private TextView mTvFinishedTextDetail;
    private String mValueBeforeUnitSwitch;
    private boolean mUseValueBeforeUnitSwitch = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bsd_open_channel, container);

        // Apply FLAG_SECURE to dialog to prevent screen recording
        if (PrefsUtil.preventScreenRecording()) {
            getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        mRootLayout = view.findViewById(R.id.rootLayout);
        mNumpad = view.findViewById(R.id.Numpad);
        mTvNodeAlias = view.findViewById(R.id.nodeAliasText);
        mTvOnChainFunds = view.findViewById(R.id.onChainFunds);
        mIvBsdIcon = view.findViewById(R.id.bsdIcon);
        Wallet.getInstance().registerChannelOpenUpdateListener(this);

        if (getArguments() != null) {
            mLightningNodeUri = (LightningNodeUri) getArguments().getSerializable(ARGS_NODE_URI);
            mTvNodeAlias.setText(mLightningNodeUri.getHost() + " (" + mLightningNodeUri.getPubKey() + ")");
        }
        mProgressScreen = view.findViewById(R.id.openChannelProgressLayout);

        mTvFinishedText = view.findViewById(R.id.finishedText);
        mTvFinishedTextDetail = view.findViewById(R.id.finishedTextDetail);

        mFinishedScreen = view.findViewById(R.id.openChannelFinishedLayout);
        mProgressFinishedIcon = view.findViewById(R.id.progressFinishedIcon);
        mIvProgressPaymentTypeIcon = view.findViewById(R.id.progressPaymentTypeIcon);
        mProgressBar = view.findViewById(R.id.progressBar);
        mLocalAmountsInputView = view.findViewById(R.id.localAmountInputsView);
        mNodeLayout = view.findViewById(R.id.nodeLayout);
        mOkButton = view.findViewById(R.id.okButton);
        mEtAmount = view.findViewById(R.id.localAmount);
        mTvUnit = view.findViewById(R.id.localAmountUnit);
        mOpenChannelButton = view.findViewById(R.id.openChannelButton);
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
            btn.setOnClickListener(v -> {
                // Add input
                int start = Math.max(mEtAmount.getSelectionStart(), 0);
                int end = Math.max(mEtAmount.getSelectionEnd(), 0);
                mEtAmount.getText().replace(Math.min(start, end), Math.max(start, end),
                        btn.getText(), 0, btn.getText().length());

            });
        }

        mOkButton.setOnClickListener(v -> dismiss());

        // Action when clicked on "x" (close) button
        ImageButton btnCloseBSD = view.findViewById(R.id.closeButton);
        btnCloseBSD.setOnClickListener(v -> dismiss());

        // Set action for numpad "." button
        mBtnNumpadDot.setOnClickListener(v -> {
            // Add input
            int start = Math.max(mEtAmount.getSelectionStart(), 0);
            int end = Math.max(mEtAmount.getSelectionEnd(), 0);
            mEtAmount.getText().replace(Math.min(start, end), Math.max(start, end),
                    mBtnNumpadDot.getText(), 0, mBtnNumpadDot.getText().length());
        });

        // Set action for numpad "delete" button
        mBtnNumpadBack.setOnClickListener(v -> {
            // remove Input
            deleteAmountInput();
        });

        setAvailableFunds();

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
                    deleteAmountInput();
                }

                mUseValueBeforeUnitSwitch = false;
            }

            @Override
            public void onTextChanged(CharSequence arg0, int start, int before,
                                      int count) {
                // validate input
                mAmountValid = MonetaryUtil.getInstance().validateCurrencyInput(arg0.toString(), MonetaryUtil.getInstance().getPrimaryCurrency());
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

                if (PrefsUtil.isWalletSetup()) {
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
                    showError(getResources().getString(R.string.channel_open_error_wallet_setup), Snackbar.LENGTH_LONG);
                    return;
                }

                Wallet.getInstance().openChannel(mLightningNodeUri, userInputAmount);
                switchToSendProgressScreen();
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

        // redraw layout when height is available, otherwise it won't be shown completely
        getDialog().setOnShowListener(dialog -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = d.findViewById(R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                CoordinatorLayout coordinatorLayout = (CoordinatorLayout) bottomSheet.getParent();
                BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
                bottomSheetBehavior.setPeekHeight(bottomSheet.getHeight());
                coordinatorLayout.getParent().requestLayout();
            }
        });
        return view;
    }

    private void switchToSendProgressScreen() {

        // make previous buttons and edit texts unclickable
        for (Button btn : mBtnNumpad) {
            btn.setEnabled(false);
        }
        mBtnNumpadBack.setEnabled(false);
        mBtnNumpadDot.setEnabled(false);
        mOpenChannelButton.setEnabled(false);
        mEtAmount.setEnabled(false);
        mNumpad.setEnabled(false);

        // Animate out
        AlphaAnimation animateOut = new AlphaAnimation(1.0f, 0f);
        animateOut.setDuration(200);
        animateOut.setFillAfter(true);

        mLocalAmountsInputView.startAnimation(animateOut);
        mNumpad.startAnimation(animateOut);
        mOpenChannelButton.startAnimation(animateOut);
        mNodeLayout.startAnimation(animateOut);
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
        scaleDownIcon.play(scaleDownX).with(scaleDownY).with(scaleDownX2).with(scaleDownY2);
        scaleDownIcon.start();

        // Set failed states
        mTvFinishedText.setText(getString(R.string.channel_open_error));
        mTvFinishedText.setTextColor(getResources().getColor(R.color.superRed));
        mTvFinishedTextDetail.setText(error);
        
        // Animate in
        mFinishedScreen.setAlpha(1.0f);
        AlphaAnimation animateIn = new AlphaAnimation(0f, 1.0f);
        animateIn.setDuration(300);
        animateIn.setStartOffset(300);
        animateIn.setFillAfter(true);

        mFinishedScreen.startAnimation(animateIn);

        mOkButton.setEnabled(true);
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

        mTvFinishedText.setText(R.string.success);
        mTvFinishedTextDetail.setText(R.string.channel_open_success);

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

    @Override
    public int getTheme() {
        return R.style.ZapBottomSheetDialogTheme;
    }

    @Override
    public void onChannelOpenUpdate(LightningNodeUri lightningNodeUri, boolean success, String message) {

        if (mLightningNodeUri.getPubKey().equals(lightningNodeUri.getPubKey())) {
            if (success) {
                // fetch channels after open
                Wallet.getInstance().updateLNDChannelsWithDebounce();
                getActivity().runOnUiThread(this::switchToSuccessScreen);
            } else {
                getActivity().runOnUiThread(() -> switchToFailedScreen(message));
            }
        }
    }
}
