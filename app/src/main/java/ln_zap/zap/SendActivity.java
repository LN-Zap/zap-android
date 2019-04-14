package ln_zap.zap;

import androidx.preference.PreferenceManager;
import io.grpc.StatusRuntimeException;
import ln_zap.zap.baseClasses.BaseAppCompatActivity;
import ln_zap.zap.connection.LndConnection;
import ln_zap.zap.util.MonetaryUtil;
import ln_zap.zap.util.Wallet;
import ln_zap.zap.util.ZapLog;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lightningnetwork.lnd.lnrpc.SendCoinsRequest;
import com.github.lightningnetwork.lnd.lnrpc.SendCoinsResponse;
import com.github.lightningnetwork.lnd.lnrpc.SendRequest;
import com.github.lightningnetwork.lnd.lnrpc.SendResponse;

import java.util.concurrent.TimeUnit;

public class SendActivity extends BaseAppCompatActivity {
    private static final String LOG_TAG = "Send Activity";

    private boolean mOnChain;
    // fixed amount (satoshis) is used as basis when switching display currency to avoid rounding issues
    private long mFixedAmount = 0L;
    private TextView mTvUnit;
    private EditText mEtAmount;
    private EditText mEtMemo;
    private SharedPreferences mPrefs;
    private boolean mAmountValid = true;
    private String mMemo;
    private String mOnChainAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Receive data from last activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mOnChain = extras.getBoolean("onChain");
            mOnChainAddress = extras.getString("onChainAddress");
            mFixedAmount = extras.getLong("amount");
            mMemo = extras.getString("message");
        }

        setContentView(R.layout.activity_send);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(SendActivity.this);

        mTvUnit = findViewById(R.id.sendUnit);
        mTvUnit.setText(MonetaryUtil.getInstance().getPrimaryDisplayUnit());
        mEtAmount = findViewById(R.id.sendAmount);
        mEtMemo = findViewById(R.id.sendMemo);


        if (mOnChain) {

            // Show "On Chain" at top
            ImageView ivTypeIcon = findViewById(R.id.sendTypeIcon);
            ivTypeIcon.setImageResource(R.drawable.ic_onchain_black_24dp);
            TextView tvTypeText = findViewById(R.id.sendTypeText);
            tvTypeText.setText(R.string.onChain);
        }

        if (mPrefs.getBoolean("isWalletSetup", false)) {

            if (mOnChain) {
                if (mFixedAmount != 0L) {
                    mEtAmount.setText(MonetaryUtil.getInstance().convertSatoshiToPrimary(mFixedAmount));
                    mEtAmount.clearFocus();
                    mEtAmount.setFocusable(false);
                }
                if (mMemo != null) {
                    mEtMemo.setText(mMemo);
                }

                // Action when clicked on "Send payment"
                Button btnSend = findViewById(R.id.sendButton);
                btnSend.setOnClickListener(new View.OnClickListener() {
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

                            SendCoinsRequest sendRequest = SendCoinsRequest.newBuilder()
                                    .setAddr(mOnChainAddress)
                                    .setAmount(sendAmount)
                                    .setSatPerByte(5)
                                    .build();
                            try {
                                SendCoinsResponse sendResponse = LndConnection.getInstance()
                                        .getBlockingClient()
                                        .withDeadlineAfter(5, TimeUnit.SECONDS)
                                        .sendCoins(sendRequest);
                                ZapLog.debug(LOG_TAG, sendResponse.toString());
                                Toast.makeText(SendActivity.this, "Send successful!", Toast.LENGTH_SHORT).show();
                            } catch (StatusRuntimeException e) {
                                // possible error messages: checksum mismatch, decoded address is of unknown format
                                Toast.makeText(SendActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                ZapLog.debug(LOG_TAG, "Error during payment!");
                                ZapLog.debug(LOG_TAG, e.getMessage());
                            }

                        } else {
                            // Send amount == 0
                            Toast.makeText(SendActivity.this, "Send amount is to small.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            } else {

                if (Wallet.getInstance().mPaymentRequest.getNumSatoshis() != 0) {
                    mFixedAmount = Wallet.getInstance().mPaymentRequest.getNumSatoshis();
                    mEtAmount.setText(MonetaryUtil.getInstance().convertSatoshiToPrimary(mFixedAmount));
                    mEtAmount.clearFocus();
                    mEtAmount.setFocusable(false);
                }

                if (Wallet.getInstance().mPaymentRequest.getDescription() != null) {
                    mEtMemo.setText(Wallet.getInstance().mPaymentRequest.getDescription());
                }

                // Action when clicked on "Send payment"
                Button btnSend = findViewById(R.id.sendButton);
                btnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        ZapLog.debug(LOG_TAG, "Trying to send lightning payment...");
                        // send lightning payment
                        SendRequest sendRequest;

                        if (Wallet.getInstance().mPaymentRequest.getNumSatoshis() == 0) {
                            sendRequest = SendRequest.newBuilder()
                                    .setPaymentRequest(Wallet.getInstance().mPaymentRequestString)
                                    .setAmt(Long.parseLong(MonetaryUtil.getInstance().convertPrimaryToSatoshi(mEtAmount.getText().toString())))
                                    .build();
                        } else {
                            sendRequest = SendRequest.newBuilder()
                                    .setPaymentRequest(Wallet.getInstance().mPaymentRequestString)
                                    .build();
                        }

                        try {
                            SendResponse sendResponse = LndConnection.getInstance()
                                    .getBlockingClient()
                                    .withDeadlineAfter(5, TimeUnit.SECONDS)
                                    .sendPaymentSync(sendRequest);
                            ZapLog.debug(LOG_TAG, sendResponse.toString());

                            if (sendResponse.hasPaymentRoute()) {
                                Toast.makeText(SendActivity.this, "Send successful!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SendActivity.this, sendResponse.getPaymentError(), Toast.LENGTH_SHORT).show();
                            }

                        } catch (StatusRuntimeException e) {
                            ZapLog.debug(LOG_TAG, "Error during payment!");
                            ZapLog.debug(LOG_TAG, e.toString());
                        }

                    }
                });
            }
        } else {
            // Wallet is not setup yet, show demo send screen

            mEtAmount.setText(MonetaryUtil.getInstance().convertSatoshiToPrimary(153267l));
            mEtMemo.setText(getResources().getString(R.string.demo_exampleMemo));

            // Action when clicked on send in demo mode
            Button btnSend = findViewById(R.id.sendButton);
            btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(SendActivity.this, R.string.demo_setupWalletFirst, Toast.LENGTH_LONG).show();
                }
            });
        }

        mEtAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // validate input
                mAmountValid = MonetaryUtil.getInstance().validateCurrencyInput(s.toString(), MonetaryUtil.getInstance().getPrimaryCurrency());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // cut off last inputted character if not valid
                if (!mAmountValid) {
                    String input = s.toString();
                    int length = s.length();
                    s.delete(length - 1, length);
                }
            }
        });

        // Action when clicked on receive unit
        LinearLayout llUnit = findViewById(R.id.sendUnitLayout);
        llUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFixedAmount == 0L) {
                    String convertedAmount = MonetaryUtil.getInstance().convertPrimaryToSecondaryCurrency(mEtAmount.getText().toString());
                    MonetaryUtil.getInstance().switchCurrencies();
                    mEtAmount.setText(convertedAmount);
                    mTvUnit.setText(MonetaryUtil.getInstance().getPrimaryDisplayUnit());
                } else {
                    MonetaryUtil.getInstance().switchCurrencies();
                    mEtAmount.setText(MonetaryUtil.getInstance().convertSatoshiToPrimary(mFixedAmount));
                }
            }
        });

    }
}
