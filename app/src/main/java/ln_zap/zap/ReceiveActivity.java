package ln_zap.zap;

import androidx.preference.PreferenceManager;
import ln_zap.zap.baseClasses.BaseAppCompatActivity;
import ln_zap.zap.interfaces.UserGuardianInterface;
import ln_zap.zap.util.MonetaryUtil;
import ln_zap.zap.util.UserGuardian;
import ln_zap.zap.util.ZapLog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ReceiveActivity extends BaseAppCompatActivity implements UserGuardianInterface {

    private UserGuardian mUG;
    private LinearLayout mLightningTab;
    private LinearLayout mOnChainTab;
    private TextView mTvUnit;
    private EditText mEtAmount;
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
        mTvUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPrefs.getBoolean("isBitcoinPrimary", true)){
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putBoolean("isBitcoinPrimary", false);
                    editor.apply();
                    mTvUnit.setText(MonetaryUtil.getInstance().getPrimaryDisplayUnit());
                    //setBalance();
                }
                else{
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putBoolean("isBitcoinPrimary", true);
                    editor.apply();
                    mTvUnit.setText(MonetaryUtil.getInstance().getPrimaryDisplayUnit());
                    //setBalance();
                }
            }
        });

        // Action when clicked on "Generate Request"
        Button btnGenerateRequest = findViewById(R.id.receiveGenerateRequest);
        btnGenerateRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Warn the user if his exchange rate is older than 1 hour and he requests the value in terms of fiat.
                if (!mPrefs.getBoolean("isBitcoinPrimary", true) && MonetaryUtil.getInstance().getExchangeRateAge() > 3600){
                    mUG.securityOldExchangeRate(MonetaryUtil.getInstance().getExchangeRateAge());
                }
                else{
                    generateRequest();
                }
            }
        });
    }

    public void generateRequest(){
        Intent intent = new Intent(ReceiveActivity.this, GenerateRequestActivity.class);
        intent.putExtra("onChain", mOnChain);
        startActivity(intent);
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
