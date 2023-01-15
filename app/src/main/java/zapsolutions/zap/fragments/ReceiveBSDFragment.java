package zapsolutions.zap.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.github.lightningnetwork.lnd.lnrpc.Invoice;
import com.github.lightningnetwork.lnd.lnrpc.NewAddressRequest;

import zapsolutions.zap.GeneratedRequestActivity;
import zapsolutions.zap.R;
import zapsolutions.zap.channelManagement.ManageChannelsActivity;
import zapsolutions.zap.connection.lndConnection.LndConnection;
import zapsolutions.zap.connection.manageNodeConfigs.NodeConfigsManager;
import zapsolutions.zap.customView.BSDScrollableMainView;
import zapsolutions.zap.customView.NumpadView;
import zapsolutions.zap.util.HelpDialogUtil;
import zapsolutions.zap.util.MonetaryUtil;
import zapsolutions.zap.util.OnSingleClickListener;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.UserGuardian;
import zapsolutions.zap.util.Wallet;
import zapsolutions.zap.util.ZapLog;


public class ReceiveBSDFragment extends ZapBSDFragment {

    private static final String LOG_TAG = ReceiveBSDFragment.class.getName();

    private BSDScrollableMainView mBSDScrollableMainView;
    private ConstraintLayout mContentTopLayout;
    private View mBtnLn;
    private View mBtnOnChain;
    private View mChooseTypeView;
    private View mReceiveAmountView;
    private EditText mEtAmount;
    private EditText mEtMemo;
    private TextView mTvUnit;
    private View mMemoView;
    private NumpadView mNumpad;
    private Button mBtnNext;
    private Button mBtnGenerateRequest;
    private boolean mOnChain;
    private TextView mTvNoIncomingBalance;
    private Button mBtnManageChannels;
    private View mViewNoIncomingBalance;
    private String mValueBeforeUnitSwitch;
    private boolean mUseValueBeforeUnitSwitch = true;
    private boolean mAmountValid = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bsd_receive, container);

        mBSDScrollableMainView = view.findViewById(R.id.scrollableBottomSheet);
        mContentTopLayout = view.findViewById(R.id.contentTopLayout);
        mBtnLn = view.findViewById(R.id.lnBtn);
        mBtnOnChain = view.findViewById(R.id.onChainBtn);
        mChooseTypeView = view.findViewById(R.id.chooseTypeLayout);
        mReceiveAmountView = view.findViewById(R.id.receiveInputsView);
        mEtAmount = view.findViewById(R.id.receiveAmount);
        mTvUnit = view.findViewById(R.id.receiveUnit);
        mEtMemo = view.findViewById(R.id.receiveMemo);
        mMemoView = view.findViewById(R.id.receiveMemoTopLayout);
        mNumpad = view.findViewById(R.id.numpadView);
        mBtnNext = view.findViewById(R.id.nextButton);
        mBtnGenerateRequest = view.findViewById(R.id.generateRequestButton);
        mTvNoIncomingBalance = view.findViewById(R.id.noIncomingChannelBalanceText);
        mViewNoIncomingBalance = view.findViewById(R.id.noIncomingChannelBalanceView);
        mBtnManageChannels = view.findViewById(R.id.manageChannels);

        mBSDScrollableMainView.setOnCloseListener(this::dismiss);
        mBSDScrollableMainView.setHelpButtonVisibility(true);
        mBSDScrollableMainView.setOnHelpListener(() -> HelpDialogUtil.showDialog(getActivity(), R.string.help_dialog_LightningVsOnChain));
        mBSDScrollableMainView.setTitle(R.string.receive);

        mNumpad.bindEditText(mEtAmount);

        // add "optional" hint to optional fields
        mEtAmount.setHint(getResources().getString(R.string.amount) + " (" + getResources().getString(R.string.optional) + ")");
        mEtMemo.setHint(getResources().getString(R.string.memo) + " (" + getResources().getString(R.string.optional) + ")");

        // deactivate default Keyboard for number input.
        mEtAmount.setShowSoftInputOnFocus(false);

        // set unit to current primary unit
        mTvUnit.setText(MonetaryUtil.getInstance().getPrimaryDisplayUnit());


        // Action when clicked on "Lightning" Button
        mBtnLn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnChain = false;
                boolean canReceiveLightningPayment = hasLightningIncomeBalance() || !NodeConfigsManager.getInstance().hasAnyConfigs();

                // Manage visibilities and animation
                AutoTransition autoTransition = new AutoTransition();
                autoTransition.setDuration(200);
                TransitionManager.beginDelayedTransition((ViewGroup) mContentTopLayout.getRootView(), autoTransition);
                mBSDScrollableMainView.setHelpButtonVisibility(false);
                mBSDScrollableMainView.setTitleIconVisibility(true);
                mBSDScrollableMainView.setTitleIcon(R.drawable.ic_icon_modal_lightning);
                mBSDScrollableMainView.setTitle(R.string.receive_lightning_request);

                mChooseTypeView.setVisibility(View.GONE);
                mMemoView.setVisibility(View.GONE);
                mBtnNext.setEnabled(false);
                mBtnNext.setTextColor(getResources().getColor(R.color.gray));
                mEtAmount.setHint(getResources().getString(R.string.amount));

                if (canReceiveLightningPayment) {
                    mTvNoIncomingBalance.setVisibility(View.GONE);
                    mReceiveAmountView.setVisibility(View.VISIBLE);
                    mNumpad.setVisibility(View.VISIBLE);
                    mBtnNext.setVisibility(View.VISIBLE);
                    // Request focus on amount input
                    mEtAmount.requestFocus();
                } else {
                    mViewNoIncomingBalance.setVisibility(View.VISIBLE);
                }
            }
        });

        // Action when clicked on "On-Chain" Button
        mBtnOnChain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnChain = true;

                // Manage visibilities and animation
                AutoTransition autoTransition = new AutoTransition();
                autoTransition.setDuration(200);
                TransitionManager.beginDelayedTransition((ViewGroup) mContentTopLayout.getRootView(), autoTransition);
                mBSDScrollableMainView.setHelpButtonVisibility(false);
                mBSDScrollableMainView.setTitleIcon(R.drawable.ic_icon_modal_on_chain);
                mBSDScrollableMainView.setTitle(R.string.receive_on_chain_request);
                mBSDScrollableMainView.setTitleIconVisibility(true);
                mReceiveAmountView.setVisibility(View.VISIBLE);
                mNumpad.setVisibility(View.VISIBLE);
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

                mEtAmount.setEnabled(false);
                mEtMemo.requestFocus();
                showKeyboard();
            }
        });

        // Action when clicked on "Generate Request" button
        mBtnGenerateRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MonetaryUtil.getInstance().getPrimaryCurrency().isBitcoin() && MonetaryUtil.getInstance().getExchangeRateAge() > 3600) {
                    // Warn the user if his primary currency is not of type bitcoin and his exchange rate is older than 1 hour.
                    new UserGuardian(getActivity(), ReceiveBSDFragment.this::generateRequest)
                            .securityOldExchangeRate(MonetaryUtil.getInstance().getExchangeRateAge());
                } else {
                    generateRequest();
                }
            }
        });

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
        LinearLayout llUnit = view.findViewById(R.id.receiveUnitLayout);
        llUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

            }
        });


        // Input validation for the amount field.
        mEtAmount.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {

                // remove the last inputted character if not valid
                if (!mAmountValid) {
                    mNumpad.removeOneDigit();
                }

                // make text red if input is too large
                if (mOnChain) {
                    // always make it white, we have no limit for on-chain
                    mEtAmount.setTextColor(getResources().getColor(R.color.white));
                    mUseValueBeforeUnitSwitch = false;
                } else {
                    long maxReceivable;
                    mUseValueBeforeUnitSwitch = false;
                    if (NodeConfigsManager.getInstance().hasAnyConfigs()) {
                        maxReceivable = Wallet.getInstance().getMaxLightningReceiveAmount();
                    } else {
                        maxReceivable = 500000000000L;
                    }
                    if (!mEtAmount.getText().toString().equals(".")) {
                        long currentValue = 0L;
                        try {
                            currentValue = Long.parseLong(MonetaryUtil.getInstance().convertPrimaryToSatoshi(mEtAmount.getText().toString()));
                        } catch (NumberFormatException e) {
                            mNumpad.clearInput();
                        }
                        if (currentValue > maxReceivable) {
                            mEtAmount.setTextColor(getResources().getColor(R.color.superRed));
                            String maxAmount = getResources().getString(R.string.max_amount) + " " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(maxReceivable);
                            Toast.makeText(getActivity(), maxAmount, Toast.LENGTH_SHORT).show();
                            mBtnNext.setEnabled(false);
                            mBtnNext.setTextColor(getResources().getColor(R.color.gray));
                        } else if (currentValue == 0) {
                            // Disable 0 sat ln invoices
                            mBtnNext.setEnabled(false);
                            mBtnNext.setTextColor(getResources().getColor(R.color.gray));
                        } else {
                            mEtAmount.setTextColor(getResources().getColor(R.color.white));
                            mBtnNext.setEnabled(true);
                            mBtnNext.setTextColor(getResources().getColor(R.color.lightningOrange));
                        }
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
            }
        });


        return view;
    }

    private void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    private void generateRequest() {
        if (NodeConfigsManager.getInstance().hasAnyConfigs()) {
            // The wallet is setup. Communicate with LND and generate the request.
            if (mOnChain) {

                // generate onChain request

                int addressType;
                if (PrefsUtil.getPrefs().getString("btcAddressType", "bech32").equals("bech32")) {
                    addressType = 0;
                } else {
                    addressType = 1;
                }

                NewAddressRequest asyncNewAddressRequest = NewAddressRequest.newBuilder()
                        .setTypeValue(addressType) // 0 = bech32 (native segwit) , 1 = Segwit compatibility address
                        .build();

                ZapLog.d(LOG_TAG, "OnChain generating...");
                getCompositeDisposable().add(LndConnection.getInstance().getLightningService().newAddress(asyncNewAddressRequest)
                        .subscribe(newAddressResponse -> {
                            String value;
                            if (mUseValueBeforeUnitSwitch) {
                                value = MonetaryUtil.getInstance().convertSecondaryToBitcoin(mValueBeforeUnitSwitch);
                            } else {
                                value = MonetaryUtil.getInstance().convertPrimaryToBitcoin(mEtAmount.getText().toString());
                            }

                            Intent intent = new Intent(getActivity(), GeneratedRequestActivity.class);
                            intent.putExtra("onChain", mOnChain);
                            intent.putExtra("address", newAddressResponse.getAddress());
                            intent.putExtra("amount", value);
                            intent.putExtra("memo", mEtMemo.getText().toString());
                            startActivity(intent);
                            dismiss();
                        }, throwable -> {
                            Toast.makeText(getActivity(), R.string.receive_generateRequest_failed, Toast.LENGTH_SHORT).show();
                            ZapLog.e(LOG_TAG, "New address request failed: " + throwable.fillInStackTrace());
                        }));

            } else {
                // generate lightning request

                long value;
                if (mUseValueBeforeUnitSwitch) {
                    value = Long.parseLong(MonetaryUtil.getInstance().convertSecondaryToSatoshi(mValueBeforeUnitSwitch));
                } else {
                    value = Long.parseLong(MonetaryUtil.getInstance().convertPrimaryToSatoshi(mEtAmount.getText().toString()));
                }

                Invoice asyncInvoiceRequest = Invoice.newBuilder()
                        .setValue(value)
                        .setMemo(mEtMemo.getText().toString())
                        .setExpiry(Long.parseLong(PrefsUtil.getPrefs().getString("lightning_expiry", "86400"))) // in seconds
                        .setPrivate(PrefsUtil.getPrefs().getBoolean("includePrivateChannelHints", true))
                        .build();

                getCompositeDisposable().add(LndConnection.getInstance().getLightningService().addInvoice(asyncInvoiceRequest)
                        .subscribe(addInvoiceResponse -> {
                            ZapLog.v(LOG_TAG, addInvoiceResponse.toString());

                            Intent intent = new Intent(getActivity(), GeneratedRequestActivity.class);
                            intent.putExtra("onChain", mOnChain);
                            intent.putExtra("lnInvoice", addInvoiceResponse.getPaymentRequest());
                            intent.putExtra("lnInvoiceAddIndex", addInvoiceResponse.getAddIndex());
                            startActivity(intent);
                            dismiss();
                        }, throwable -> {
                            Toast.makeText(getActivity(), R.string.receive_generateRequest_failed, Toast.LENGTH_SHORT).show();
                            ZapLog.e(LOG_TAG, "Add invoice request failed: " + throwable.getMessage());
                        }));
            }
        } else {
            // The wallet is not setup. Show setup wallet message.
            Toast.makeText(getActivity(), R.string.demo_setupNodeFirst, Toast.LENGTH_LONG).show();
        }
    }

    private boolean hasLightningIncomeBalance() {
        boolean hasActiveChannels = Wallet.getInstance().hasOpenActiveChannels();

        if (hasActiveChannels) {
            if (Wallet.getInstance().getMaxLightningReceiveAmount() > 0L) {
                // We have remote balances on at least one channel, so we can receive a lightning payment!
                return true;
            } else {
                mTvNoIncomingBalance.setText(R.string.receive_noIncomeBalance);
                return false;
            }
        } else {
            mTvNoIncomingBalance.setText(R.string.receive_noActiveChannels);
            return false;
        }
    }
}
