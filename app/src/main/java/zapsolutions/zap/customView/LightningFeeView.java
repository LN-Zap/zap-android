package zapsolutions.zap.customView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import zapsolutions.zap.R;

public class LightningFeeView extends ConstraintLayout {

    private TextView mTvSendFeeAmount;
    private ProgressBar mPbCalculateFee;

    public LightningFeeView(Context context) {
        super(context);
        init();
    }

    public LightningFeeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LightningFeeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View view = inflate(getContext(), R.layout.view_lightning_fee, this);
        mTvSendFeeAmount = view.findViewById(R.id.sendFeeLightningAmount);
        mPbCalculateFee = view.findViewById(R.id.sendFeeLightningProgressBar);
    }

    /**
     * Show progress bar while calculating fee
     */
    public void onCalculating() {
        mTvSendFeeAmount.setText(null);
        mPbCalculateFee.setVisibility(View.VISIBLE);
        mTvSendFeeAmount.setVisibility(View.GONE);
    }

    public void setAmount(String amount) {
        mTvSendFeeAmount.setText(amount);
        mTvSendFeeAmount.setVisibility(View.VISIBLE);
        mPbCalculateFee.setVisibility(View.GONE);
    }

    public void onFeeFailure() {
        mTvSendFeeAmount.setText(R.string.fee_not_available);
        mTvSendFeeAmount.setVisibility(View.VISIBLE);
        mPbCalculateFee.setVisibility(View.GONE);
    }
}
