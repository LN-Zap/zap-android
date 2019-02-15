package ln_zap.zap;

import androidx.preference.PreferenceManager;
import ln_zap.zap.baseClasses.BaseAppCompatActivity;
import ln_zap.zap.connection.LndConnection;
import ln_zap.zap.interfaces.UserGuardianInterface;
import ln_zap.zap.util.ExecuteOnCaller;
import ln_zap.zap.util.MonetaryUtil;
import ln_zap.zap.util.UserGuardian;
import ln_zap.zap.util.ZapLog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lightningnetwork.lnd.lnrpc.AddInvoiceResponse;
import com.github.lightningnetwork.lnd.lnrpc.Invoice;
import com.github.lightningnetwork.lnd.lnrpc.LightningGrpc;
import com.github.lightningnetwork.lnd.lnrpc.NewAddressRequest;
import com.github.lightningnetwork.lnd.lnrpc.NewAddressResponse;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class ReceiveActivity extends BaseAppCompatActivity implements UserGuardianInterface {

    private static final String LOG_TAG = "Receive Activity";

    private UserGuardian mUG;
    private LinearLayout mLightningTab;
    private LinearLayout mOnChainTab;
    private TextView mTvUnit;
    private EditText mEtAmount;
    private EditText mEtMemo;
    private SharedPreferences mPrefs;
    private boolean mOnChain = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(ReceiveActivity.this);
        mUG = new UserGuardian(this,this);

        mTvUnit = findViewById(R.id.receiveUnit);
        mTvUnit.setText(MonetaryUtil.getInstance().getPrimaryDisplayUnit());
        mEtAmount = findViewById(R.id.receiveAmount);
        mEtMemo = findViewById(R.id.receiveMemo);

        // add "optional" hint to optional fields
        mEtAmount.setHint(getResources().getString(R.string.amount) + " (" + getResources().getString(R.string.optional) + ")");
        mEtMemo.setHint(getResources().getString(R.string.memo) + " (" + getResources().getString(R.string.optional) + ")");

        // This will cause the default keyboard not to be shown. (Default keyboard is not secure against recording)
        //mEtAmount.setShowSoftInputOnFocus(false);


        // Action when clicked on "Lightning"
        mLightningTab = findViewById(R.id.receiveLightningButton);
        mLightningTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLightningTab.setAlpha(1);
                mOnChainTab.setAlpha(0.2f);
                mOnChain = false;
            }
        });


        // Action when clicked on "On Chain"
        mOnChainTab = findViewById(R.id.receiveOnChainButton);
        mOnChainTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLightningTab.setAlpha(0.2f);
                mOnChainTab.setAlpha(1);
                mOnChain = true;
            }
        });



        // Action when clicked on receive unit
        LinearLayout llUnit = findViewById(R.id.receiveUnitLayout);
        llUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEtAmount.setText(MonetaryUtil.getInstance().convertPrimaryToSecondaryCurrency(mEtAmount.getText().toString()));
                MonetaryUtil.getInstance().switchCurrencies();
                mTvUnit.setText(MonetaryUtil.getInstance().getPrimaryDisplayUnit());
            }
        });

        // Action when clicked on "Generate Request"
        Button btnGenerateRequest = findViewById(R.id.receiveGenerateRequest);
        btnGenerateRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Warn the user if his primary currency is not of type bitcoin and his exchange rate is older than 1 hour.
                if (!MonetaryUtil.getInstance().getPrimaryCurrency().isBitcoin() && MonetaryUtil.getInstance().getExchangeRateAge() > 3600){
                    mUG.securityOldExchangeRate(MonetaryUtil.getInstance().getExchangeRateAge());
                }
                else{
                    generateRequest();
                }
            }
        });

        mEtAmount.addTextChangedListener(new TextWatcher(){

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub

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
            }
        });
    }

    public void generateRequest(){
        if (mPrefs.getBoolean("isWalletSetup", false)) {
            // The wallet is setup. Communicate with LND and generate the request.
            if (mOnChain) {

                // generate onChain request

                // non blocking stub
                LightningGrpc.LightningFutureStub asyncAddressClient = LightningGrpc
                        .newFutureStub(LndConnection.getInstance().getSecureChannel())
                        .withCallCredentials(LndConnection.getInstance().getMacaroon());

                NewAddressRequest asyncNewAddressRequest = NewAddressRequest.newBuilder()
                        .setTypeValue(0) // 0 = bech32 (native segwit) , 1 = Segwit compatibility address
                        .build();

                final ListenableFuture<NewAddressResponse> addressFuture = asyncAddressClient.newAddress(asyncNewAddressRequest);


                addressFuture.addListener(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            NewAddressResponse addressResponse = addressFuture.get();
                            ZapLog.debug(LOG_TAG, addressResponse.toString());

                            Intent intent = new Intent(ReceiveActivity.this, GeneratedRequestActivity.class);
                            intent.putExtra("onChain", mOnChain);
                            intent.putExtra("address", addressResponse.getAddress());
                            intent.putExtra("amount", mEtAmount.getText().toString());
                            intent.putExtra("memo", mEtMemo.getText().toString());
                            startActivity(intent);

                        } catch (InterruptedException e) {
                            ZapLog.debug(LOG_TAG, "Interrupted");
                            Toast.makeText(ReceiveActivity.this, R.string.receive_generateRequest_failed, Toast.LENGTH_SHORT).show();
                        } catch (ExecutionException e) {
                            ZapLog.debug(LOG_TAG, "Exception in task");
                            Toast.makeText(ReceiveActivity.this, R.string.receive_generateRequest_failed, Toast.LENGTH_SHORT).show();
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
                        .build();

                final ListenableFuture<AddInvoiceResponse> invoiceFuture = asyncInvoiceClient.addInvoice(asyncInvoiceRequest);


                invoiceFuture.addListener(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            AddInvoiceResponse invoiceResponse = invoiceFuture.get();
                            ZapLog.debug(LOG_TAG, invoiceResponse.toString());

                            Intent intent = new Intent(ReceiveActivity.this, GeneratedRequestActivity.class);
                            intent.putExtra("onChain", mOnChain);
                            intent.putExtra("lnInvoice", invoiceResponse.getPaymentRequest());
                            startActivity(intent);

                        } catch (InterruptedException e) {
                            ZapLog.debug(LOG_TAG, "Interrupted");
                            Toast.makeText(ReceiveActivity.this, R.string.receive_generateRequest_failed, Toast.LENGTH_SHORT).show();
                        } catch (ExecutionException e) {
                            ZapLog.debug(LOG_TAG, "Exception in task");
                            Toast.makeText(ReceiveActivity.this, R.string.receive_generateRequest_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new ExecuteOnCaller());

            }
        } else {
            // The wallet is not setup. Continue for demo mode.
            Intent intent = new Intent(ReceiveActivity.this, GeneratedRequestActivity.class);
            intent.putExtra("onChain", mOnChain);
            startActivity(intent);
        }
    }

    @Override
    public void guardianDialogConfirmed(String DialogName) {
        switch (DialogName) {
            case UserGuardian.OLD_EXCHANGE_RATE:
                generateRequest();
                break;
        }
    }
}
