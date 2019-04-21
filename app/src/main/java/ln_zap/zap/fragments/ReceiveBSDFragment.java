package ln_zap.zap.fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnticipateOvershootInterpolator;
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

import com.github.lightningnetwork.lnd.lnrpc.AddInvoiceResponse;
import com.github.lightningnetwork.lnd.lnrpc.Invoice;
import com.github.lightningnetwork.lnd.lnrpc.LightningGrpc;
import com.github.lightningnetwork.lnd.lnrpc.NewAddressRequest;
import com.github.lightningnetwork.lnd.lnrpc.NewAddressResponse;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.common.util.concurrent.ListenableFuture;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.preference.PreferenceManager;
import androidx.transition.ChangeBounds;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import java.util.concurrent.ExecutionException;

import java.util.concurrent.ScheduledExecutorService;

import ln_zap.zap.GeneratedRequestActivity;
import ln_zap.zap.R;
import ln_zap.zap.connection.LndConnection;
import ln_zap.zap.util.ExecuteOnCaller;
import ln_zap.zap.util.MonetaryUtil;
import ln_zap.zap.util.ZapLog;


public class ReceiveBSDFragment extends BottomSheetDialogFragment {

    private static final String LOG_TAG = "Receive Activity";

    private View mBtnLn;
    private View mBtnOnChain;
    private View mChooseTypeView;
    private ImageButton mBtnCloseBSD;
    private ImageView mIvBsdIcon;
    private ConstraintLayout mRootLayout;
    private ConstraintLayout mIconAnchor;
    private View mReceiveAmountView;
    private EditText mEtAmount;
    private EditText mEtMemo;
    private TextView mTvUnit;
    private View mMemoView;
    private TextView mModalTitle;
    private SharedPreferences mPrefs;
    private View mNumpad;
    private Button[] mBtnNumpad = new Button[10];
    private Button mBtnNumpadDot;
    private ImageButton mBtnNumpadBack;
    private Button mBtnNext;
    private Button mBtnGenerateRequest;
    private Boolean mOnChain;
    private BottomSheetBehavior mBehavior;
    private FrameLayout mBottomSheet;
    private ScheduledExecutorService mPeekAnimationScheduler;
    private int mPeekAnimationCounter;
    private boolean mAmountValid = true;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bsd_receive, container);

        // Apply FLAG_SECURE to dialog to prevent screen recording
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (mPrefs.getBoolean("preventScreenRecording", true)) {
            getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        mBtnCloseBSD = view.findViewById(R.id.closeButton);
        mBtnLn = view.findViewById(R.id.lnBtn);
        mBtnOnChain = view.findViewById(R.id.onChainBtn);
        mIvBsdIcon = view.findViewById(R.id.bsdIcon);
        mIconAnchor = view.findViewById(R.id.anchor);
        mRootLayout = view.findViewById(R.id.rootLayout);
        mChooseTypeView = view.findViewById(R.id.chooseTypeLayout);
        mReceiveAmountView = view.findViewById(R.id.receiveInputsView);
        mEtAmount = view.findViewById(R.id.receiveAmount);
        mTvUnit = view.findViewById(R.id.receiveUnit);
        mEtMemo = view.findViewById(R.id.receiveMemo);
        mMemoView = view.findViewById(R.id.receiveMemoTopLayout);
        mModalTitle = view.findViewById(R.id.bsdTitle);
        mNumpad = view.findViewById(R.id.Numpad);
        mBtnNext = view.findViewById(R.id.nextButton);
        mBtnGenerateRequest = view.findViewById(R.id.generateRequestButton);


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
        });

        // add "optional" hint to optional fields
        mEtAmount.setHint(getResources().getString(R.string.amount) + " (" + getResources().getString(R.string.optional) + ")");
        mEtMemo.setHint(getResources().getString(R.string.memo) + " (" + getResources().getString(R.string.optional) + ")");

        // deactivate default Keyboard for number input.
        mEtAmount.setShowSoftInputOnFocus(false);

        // set unit to current primary unit
        mTvUnit.setText(MonetaryUtil.getInstance().getPrimaryDisplayUnit());


        mBtnCloseBSD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


        // Action when clicked on "Lightning" Button
        mBtnLn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mOnChain = false;
                mIvBsdIcon.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_icon_modal_lightning));
                mModalTitle.setText(R.string.receive_lightning_request);

                // Animate bsd Icon size
                ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(mIvBsdIcon, "scaleX", 0f, 1f);
                ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(mIvBsdIcon, "scaleY", 0f, 1f);
                scaleUpX.setDuration(400);
                scaleUpY.setDuration(400);

                AnimatorSet scaleUpIcon = new AnimatorSet();
                scaleUpIcon.setInterpolator(new AnticipateOvershootInterpolator(1.0f));
                scaleUpIcon.play(scaleUpX).with(scaleUpY);
                scaleUpIcon.start();

                // Animate Layout changes
                ConstraintSet csRoot = new ConstraintSet();
                csRoot.clone(mRootLayout);
                csRoot.constrainHeight(mNumpad.getId(),ConstraintSet.WRAP_CONTENT);
                csRoot.constrainHeight(mReceiveAmountView.getId(),ConstraintSet.WRAP_CONTENT);
                csRoot.constrainHeight(mBtnNext.getId(),ConstraintSet.WRAP_CONTENT);

                Transition transition = new ChangeBounds();
                transition.setInterpolator(new DecelerateInterpolator(3));
                transition.setDuration(400);
                TransitionManager.beginDelayedTransition(mRootLayout, transition);
                csRoot.applyTo(mRootLayout);


                FrameLayout bottomSheet =  getDialog().findViewById(R.id.design_bottom_sheet);
                bottomSheet.setForegroundGravity(Gravity.BOTTOM);
                //CoordinatorLayout layout = (CoordinatorLayout) bottomSheet.getParent();
                mBehavior = BottomSheetBehavior.from(bottomSheet);
                mBehavior.setPeekHeight(bottomSheet.getHeight());
                //layout.getParent().requestLayout();

                // Expand bottom sheet after size has changed
                mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);


                mIvBsdIcon.setVisibility(View.VISIBLE);
                mChooseTypeView.setVisibility(View.GONE);
                mBtnNext.setVisibility(View.VISIBLE);
                mMemoView.setVisibility(View.GONE);


                // Request focus on amount input
                mEtAmount.requestFocus();

            }
        });

        // Action when clicked on "On-Chain" Button
        mBtnOnChain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mOnChain = true;
                mIvBsdIcon.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_icon_modal_on_chain));
                mModalTitle.setText(R.string.receive_on_chain_request);

                // Animate bsd Icon size
                ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(mIvBsdIcon, "scaleX", 0f, 1f);
                ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(mIvBsdIcon, "scaleY", 0f, 1f);
                scaleUpX.setDuration(400);
                scaleUpY.setDuration(400);

                AnimatorSet scaleUpIcon = new AnimatorSet();
                scaleUpIcon.setInterpolator(new AnticipateOvershootInterpolator(1.0f));
                scaleUpIcon.play(scaleUpX).with(scaleUpY);
                scaleUpIcon.start();

                // Animate Layout changes
                ConstraintSet csRoot = new ConstraintSet();
                csRoot.clone(mRootLayout);
                csRoot.constrainHeight(mNumpad.getId(),ConstraintSet.WRAP_CONTENT);
                csRoot.constrainHeight(mReceiveAmountView.getId(),ConstraintSet.WRAP_CONTENT);
                csRoot.constrainHeight(mBtnNext.getId(),ConstraintSet.WRAP_CONTENT);

                Transition transition = new ChangeBounds();
                transition.setInterpolator(new DecelerateInterpolator(3));
                transition.setDuration(400);
                TransitionManager.beginDelayedTransition(mRootLayout, transition);
                csRoot.applyTo(mRootLayout);


                FrameLayout bottomSheet =  getDialog().findViewById(R.id.design_bottom_sheet);
                bottomSheet.setForegroundGravity(Gravity.BOTTOM);
                //CoordinatorLayout layout = (CoordinatorLayout) bottomSheet.getParent();
                mBehavior = BottomSheetBehavior.from(bottomSheet);
                mBehavior.setPeekHeight(bottomSheet.getHeight());
                //layout.getParent().requestLayout();

                // Expand bottom sheet after size has changed
                mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);


                mIvBsdIcon.setVisibility(View.VISIBLE);
                mChooseTypeView.setVisibility(View.GONE);
                mBtnNext.setVisibility(View.VISIBLE);
                mMemoView.setVisibility(View.GONE);


                // Request focus on amount input
                mEtAmount.requestFocus();
            }
        });

        // Action when clicked on "next" button
        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNumpad.setVisibility(View.GONE);
                mBtnNext.setVisibility(View.GONE);
                mMemoView.setVisibility(View.VISIBLE);
                mBtnGenerateRequest.setVisibility(View.VISIBLE);

                mEtMemo.requestFocus();
                showKeyboard();
            }
        });

        // Action when clicked on "Generate Request" button
        mBtnGenerateRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateRequest();
            }
        });


        // Action when clicked on receive unit
        LinearLayout llUnit = view.findViewById(R.id.receiveUnitLayout);
        llUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String convertedAmount = MonetaryUtil.getInstance().convertPrimaryToSecondaryCurrency(mEtAmount.getText().toString());
                MonetaryUtil.getInstance().switchCurrencies();
                mEtAmount.setText(convertedAmount);
                mTvUnit.setText(MonetaryUtil.getInstance().getPrimaryDisplayUnit());
            }
        });


        // Input validation for the amount field.
        mEtAmount.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {

                // cut off last inputted character if not valid
                if (!mAmountValid) {
                    String input = arg0.toString();
                    int length = arg0.length();
                    arg0.delete(length - 1, length);
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
            }
        });


        return view;
    }

    @Override
    public int getTheme() {
        return R.style.ZapBottomSheetDialogTheme;
    }

    public void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    public void generateRequest() {
        if (mPrefs.getBoolean("isWalletSetup", false)) {
            // The wallet is setup. Communicate with LND and generate the request.
            if (mOnChain) {

                // generate onChain request

                int addressType;
                if (mPrefs.getString("btcAddressType", "p2psh").equals("bech32")) {
                    addressType = 0;
                } else {
                    addressType = 1;
                }

                // non blocking stub
                LightningGrpc.LightningFutureStub asyncAddressClient = LightningGrpc
                        .newFutureStub(LndConnection.getInstance().getSecureChannel())
                        .withCallCredentials(LndConnection.getInstance().getMacaroon());

                NewAddressRequest asyncNewAddressRequest = NewAddressRequest.newBuilder()
                        .setTypeValue(addressType) // 0 = bech32 (native segwit) , 1 = Segwit compatibility address
                        .build();

                final ListenableFuture<NewAddressResponse> addressFuture = asyncAddressClient.newAddress(asyncNewAddressRequest);


                addressFuture.addListener(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            NewAddressResponse addressResponse = addressFuture.get();
                            ZapLog.debug(LOG_TAG, addressResponse.toString());

                            Intent intent = new Intent(getActivity(), GeneratedRequestActivity.class);
                            intent.putExtra("onChain", mOnChain);
                            intent.putExtra("address", addressResponse.getAddress());
                            intent.putExtra("amount", mEtAmount.getText().toString());
                            intent.putExtra("memo", mEtMemo.getText().toString());
                            startActivity(intent);
                            dismiss();

                        } catch (InterruptedException e) {
                            ZapLog.debug(LOG_TAG, "Interrupted");
                            Toast.makeText(getActivity(), R.string.receive_generateRequest_failed, Toast.LENGTH_SHORT).show();
                        } catch (ExecutionException e) {
                            ZapLog.debug(LOG_TAG, "Exception in task");
                            Toast.makeText(getActivity(), R.string.receive_generateRequest_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new ExecuteOnCaller());

            } else {
                // generate lightning request

                // non blocking stub
                LightningGrpc.LightningFutureStub asyncInvoiceClient = LightningGrpc
                        .newFutureStub(LndConnection.getInstance().getSecureChannel())
                        .withCallCredentials(LndConnection.getInstance().getMacaroon());


                Invoice asyncInvoiceRequest = Invoice.newBuilder()
                        .setValue(Long.parseLong(MonetaryUtil.getInstance().convertPrimaryToSatoshi(mEtAmount.getText().toString())))
                        .setMemo(mEtMemo.getText().toString())
                        .setExpiry(Long.parseLong(mPrefs.getString("lightning_expiry", "86400"))) // in seconds
                        .build();

                final ListenableFuture<AddInvoiceResponse> invoiceFuture = asyncInvoiceClient.addInvoice(asyncInvoiceRequest);


                invoiceFuture.addListener(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            AddInvoiceResponse invoiceResponse = invoiceFuture.get();
                            ZapLog.debug(LOG_TAG, invoiceResponse.toString());

                            Intent intent = new Intent(getActivity(), GeneratedRequestActivity.class);
                            intent.putExtra("onChain", mOnChain);
                            intent.putExtra("lnInvoice", invoiceResponse.getPaymentRequest());
                            startActivity(intent);
                            dismiss();

                        } catch (InterruptedException e) {
                            ZapLog.debug(LOG_TAG, "Interrupted");
                            Toast.makeText(getActivity(), R.string.receive_generateRequest_failed, Toast.LENGTH_SHORT).show();
                        } catch (ExecutionException e) {
                            ZapLog.debug(LOG_TAG, "Exception in task");
                            Toast.makeText(getActivity(), R.string.receive_generateRequest_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new ExecuteOnCaller());

            }
        } else {
            // The wallet is not setup. Continue for demo mode.
            Intent intent = new Intent(getActivity(), GeneratedRequestActivity.class);
            intent.putExtra("onChain", mOnChain);
            startActivity(intent);
        }
    }
}
