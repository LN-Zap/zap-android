package ln_zap.zap;

import ln_zap.zap.baseClasses.BaseAppCompatActivity;
import ln_zap.zap.util.MonetaryUtil;
import ln_zap.zap.util.UserGuardian;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SendActivity extends BaseAppCompatActivity {

    private UserGuardian mUG;
    private String mDataToEncode;
    private boolean mOnChain;
    private String mAddress;
    private String mMemo;
    private String mAmount;
    private String mLnInvoice;
    private TextView mTvUnit;
    private EditText mEtAmount;
    private EditText mEtMemo;
    private String mContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Receive data from last activity
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            mOnChain = extras.getBoolean("onChain");
            mAddress = extras.getString("address");
            mAmount = extras.getString("amount");
            mMemo = extras.getString("memo");
            mLnInvoice = extras.getString("lnInvoice");
            mContent = extras.getString("content");
        }

        setContentView(R.layout.activity_send);

        // Display correct payment type
        if(mOnChain){
            // Show "On Chain" at top
            ImageView ivTypeIcon = findViewById(R.id.sendTypeIcon);
            ivTypeIcon.setImageResource(R.drawable.ic_onchain_black_24dp);
            TextView tvTypeText = findViewById(R.id.sendTypeText);
            tvTypeText.setText(R.string.onChain);
        }

        mTvUnit = findViewById(R.id.sendUnit);
        mTvUnit.setText(MonetaryUtil.getInstance().getPrimaryDisplayUnit());
        mEtAmount = findViewById(R.id.sendAmount);
        mEtMemo = findViewById(R.id.sendMemo);


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


        // Action when clicked on "Send payment"
        Button btnSend = findViewById(R.id.sendGenerateRequest);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }
}
