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

public class TransactionViewHolder extends RecyclerView.ViewHolder {
    // each data item is just a string in this case
    private TextView mAmount;
    private TextView mTransactionState;
    private TextView mDescription;
    private TextView mTransactionFee;
    private TextView mTimeOfDay;
    private View mRootView;
    private Context mContext;
    public TransactionViewHolder(View v) {
        super(v);
        mAmount = v.findViewById(R.id.transactionAmount);
        mDescription = v.findViewById(R.id.transactionDescription);
        mTransactionState = v.findViewById(R.id.transactionState);
        mTimeOfDay = v.findViewById(R.id.timeOfDay);
        mTransactionFee = v.findViewById(R.id.transactionFeeAmount);
        mRootView = v.findViewById(R.id.transactionRootView);
        mContext = v.getContext();
    }

    public void bindTransactionItem(TransactionItem transactionItem){

        // Set time of Day
        DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, mContext.getResources().getConfiguration().locale);
        String formattedTime = df.format(new Date(transactionItem.mCreationDate * 1000L));
        mTimeOfDay.setText(formattedTime);

        // Set amount
        Long amt = transactionItem.getOnChainTransaction().getAmount();

        // compare the amount with 0
        int result = amt.compareTo(0L);

        switch (result) {
            case 0:
                // amount = 0
                mAmount.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amt));
                mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                mTransactionState.setText(R.string.internal);
                mTransactionFee.setVisibility(View.GONE);
                mDescription.setVisibility(View.GONE);
                break;
            case 1:
                // amount > 0
                mAmount.setText("+ " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amt));
                mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.superGreen));
                mTransactionState.setText(mContext.getResources().getString(R.string.received));
                mTransactionFee.setVisibility(View.GONE);
                mDescription.setVisibility(View.GONE);
                break;
            case -1:
                // amount < 0
                mAmount.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amt).replace("-","- "));
                mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.superRed));
                mTransactionState.setText(mContext.getResources().getString(R.string.sent));
                Long feeAmt = transactionItem.getOnChainTransaction().getTotalFees();
                String feeText = mContext.getResources().getString(R.string.fee)
                        + ": " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(feeAmt);
                mTransactionFee.setVisibility(View.VISIBLE);
                mTransactionFee.setText(feeText);
                mDescription.setVisibility(View.GONE);
                break;
        }


        // Set on click listener
        mRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
