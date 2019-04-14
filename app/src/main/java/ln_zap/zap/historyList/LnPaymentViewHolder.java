package ln_zap.zap.historyList;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import ln_zap.zap.R;
import ln_zap.zap.util.MonetaryUtil;

public class LnPaymentViewHolder extends RecyclerView.ViewHolder {

    private ImageView mIcon;
    private TextView mTimeOfDay;
    private TextView mTransactionState;
    private TextView mDescription;
    private TextView mAmount;
    private TextView mTransactionFee;
    private View mRootView;
    private Context mContext;

    public LnPaymentViewHolder(View v) {
        super(v);

        mIcon = v.findViewById(R.id.transactionTypeIcon);
        mTimeOfDay = v.findViewById(R.id.timeOfDay);
        mTransactionState = v.findViewById(R.id.transactionState);
        mDescription = v.findViewById(R.id.transactionDescription);
        mAmount = v.findViewById(R.id.transactionAmount);
        mTransactionFee = v.findViewById(R.id.transactionFeeAmount);
        mRootView = v.findViewById(R.id.transactionRootView);
        mContext = v.getContext();
    }

    public void bindLnPaymentItem(LnPaymentItem lnPaymentItem) {

        // Set Icon
        mIcon.setImageResource(R.drawable.bolt_black_filled_24dp);
        mIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.lightningOrange)));

        // Set time of Day
        DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, mContext.getResources().getConfiguration().locale);
        String formattedTime = df.format(new Date(lnPaymentItem.mCreationDate * 1000L));
        mTimeOfDay.setText(formattedTime);

        // Set state
        mTransactionState.setText(R.string.sent);

        // Set description
        mDescription.setVisibility(View.GONE);
        // memo is not yet available in lnPayments in LND;

        // Set amount
        long amt = lnPaymentItem.getPayment().getValueSat();
        mAmount.setText("- " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amt));
        mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.superRed));

        // Set fee
        long feeAmt = lnPaymentItem.getPayment().getFee();

        String feeText = mContext.getResources().getString(R.string.fee)
                + ": " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(feeAmt);
        mTransactionFee.setText(feeText);

        // Set on click listener
        mRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, R.string.coming_soon, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
