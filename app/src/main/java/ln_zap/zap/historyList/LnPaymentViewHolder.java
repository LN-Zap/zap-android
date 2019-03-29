package ln_zap.zap.historyList;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import ln_zap.zap.R;
import ln_zap.zap.util.MonetaryUtil;

public class LnPaymentViewHolder extends RecyclerView.ViewHolder {
    // each data item is just a string in this case
    private TextView mAmount;
    private TextView mTransactionType;
    private TextView mDescription;
    private TextView mTransactionFee;
    private TextView mTimeOfDay;
    private Context mContext;
    public LnPaymentViewHolder(View v) {
        super(v);
        mAmount = v.findViewById(R.id.transactionAmount);
        mTransactionType = v.findViewById(R.id.transactionType);
        mTimeOfDay = v.findViewById(R.id.timeOfDay);
        mTransactionFee = v.findViewById(R.id.transactionFeeAmount);
        mContext = v.getContext();
    }

    public void bindLnPaymentItem(LnPaymentItem lnPaymentItem){

        // Set time of Day
        DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, mContext.getResources().getConfiguration().locale);
        String formattedTime = df.format(new Date(lnPaymentItem.mCreationDate * 1000L));
        mTimeOfDay.setText(formattedTime);

        // Set amount
        Long amt = lnPaymentItem.getPayment().getValueSat();
        mAmount.setText("- " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amt));
        mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.superRed));

        // Set fee
        Long feeAmt = lnPaymentItem.getPayment().getFee();

        String feeText = mContext.getResources().getString(R.string.fee)
                        + ": " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(feeAmt);
        mTransactionFee.setText(feeText);
    }
}
