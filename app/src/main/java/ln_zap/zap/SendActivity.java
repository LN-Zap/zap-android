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

import com.github.lightningnetwork.lnd.lnrpc.SendRequest;
import com.github.lightningnetwork.lnd.lnrpc.SendResponse;

public class SendActivity extends BaseAppCompatActivity {
    private static final String LOG_TAG = "Send Activity";

    private boolean mOnChain;
    // fixed amount is used as basis when switching display currency to avoid rounding issues
    private long mFixedAmount;
    private TextView mTvUnit;
    private EditText mEtAmount;
    private EditText mEtMemo;
    private SharedPreferences mPrefs;
    private boolean mAmoutValid = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Receive data from last activity
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            mOnChain = extras.getBoolean("onChain");
        }

        setContentView(R.layout.activity_send);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(SendActivity.this);

        mTvUnit = findViewById(R.id.sendUnit);
        mTvUnit.setText(MonetaryUtil.getInstance().getPrimaryDisplayUnit());
        mEtAmount = findViewById(R.id.sendAmount);
        mEtMemo = findViewById(R.id.sendMemo);


        if(mOnChain) {

            // Show "On Chain" at top
            ImageView ivTypeIcon = findViewById(R.id.sendTypeIcon);
            ivTypeIcon.setImageResource(R.drawable.ic_onchain_black_24dp);
            TextView tvTypeText = findViewById(R.id.sendTypeText);
            tvTypeText.setText(R.string.onChain);
        }

        if(mPrefs.getBoolean("isWalletSetup", false)) {

            if (mOnChain) {
            } else {

                if (Wallet.getInstance().mPaymentRequest.getNumSatoshis() != 0) {
                    mFixedAmount = Wallet.getInstance().mPaymentRequest.getNumSatoshis();
                    mEtAmount.setText(MonetaryUtil.getInstance().convertSatoshiToPrimary(mFixedAmount));
                    mEtAmount.clearFocus();
                    mEtAmount.setFocusable(false);
                }

                if (Wallet.getInstance().mPaymentRequest.getDescription() != null) {
                    mEtMemo.setText(Wallet.getInstance().mPaymentRequest.getDescription());
                    mEtMemo.clearFocus();
                    mEtMemo.setFocusable(false);
                }

                // Action when clicked on "Send payment"
                Button btnSend = findViewById(R.id.sendButton);
                btnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        ZapLog.debug(LOG_TAG, "send it");
                        // send lightning payment

                        SendRequest sendRequest = SendRequest.newBuilder()
                                .setDestString(Wallet.getInstance().mPaymentRequest.getDestination())
                                .setPaymentHashString(Wallet.getInstance().mPaymentRequest.getPaymentHash())
                                .setAmt(Wallet.getInstance().mPaymentRequest.getNumSatoshis())
                                .build();
                        try {
                            SendResponse sendResponse = LndConnection.getInstance().getBlockingClient().sendPaymentSync(sendRequest);
                            ZapLog.debug(LOG_TAG, sendResponse.toString());
                            Toast.makeText(SendActivity.this, sendResponse.getPaymentError(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(SendActivity.this, R.string.demo_setupWalletFirst,Toast.LENGTH_LONG).show();
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
                mAmoutValid = MonetaryUtil.getInstance().validateCurrencyInput(s.toString(), MonetaryUtil.getInstance().getPrimaryCurrency());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // cut off last inputted character if not valid
                if (!mAmoutValid){
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
                mEtAmount.setText(MonetaryUtil.getInstance().convertPrimaryToSecondaryCurrency(mEtAmount.getText().toString()));
                MonetaryUtil.getInstance().switchCurrencies();
                mTvUnit.setText(MonetaryUtil.getInstance().getPrimaryDisplayUnit());
            }
        });

    }
}
