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


/**
 * This class can be used to generate imaginary transactions for demonstration purposes.
 */
public class DemoViewHolder extends RecyclerView.ViewHolder {
    // each data item is just a string in this case
    private TextView mAmount;
    private ImageView mTransactionTypeIcon;
    private TextView mTransactionState;
    private TextView mDescription;
    private TextView mTransactionFee;
    private TextView mTimeOfDay;
    private View mRootView;
    private Context mContext;

    public DemoViewHolder(View v) {
        super(v);
        mAmount = v.findViewById(R.id.transactionAmount);
        mTransactionTypeIcon = v.findViewById(R.id.transactionTypeIcon);
        mTransactionState = v.findViewById(R.id.transactionState);
        mDescription = v.findViewById(R.id.transactionDescription);
        mTimeOfDay = v.findViewById(R.id.timeOfDay);
        mTransactionFee = v.findViewById(R.id.transactionFeeAmount);
        mRootView = v.findViewById(R.id.transactionRootView);
        mContext = v.getContext();
    }

    public void bindDemoItem(DemoItem demoItem) {

        // Standard state. This prevents list entries to get mixed states because of recycling of the ViewHolder.
        mRootView.setAlpha(1f);
        mAmount.setVisibility(View.VISIBLE);

        // Set time of Day
        DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, mContext.getResources().getConfiguration().locale);
        String formattedTime = df.format(new Date(demoItem.mCreationDate * 1000L));
        mTimeOfDay.setText(formattedTime);

        // Set description
        if (demoItem.getDescription().equals("")) {
            mDescription.setVisibility(View.GONE);
        } else {
            mDescription.setVisibility(View.VISIBLE);
            mDescription.setText(demoItem.getDescription());
        }

        switch (demoItem.getTransactionType()) {
            case HistoryListItem.TYPE_TRANSACTION:
                if (demoItem.isInternal()) {
                    mTransactionTypeIcon.setImageResource(R.drawable.ic_internal_black_24dp);
                    mTransactionTypeIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.gray)));
                    if (demoItem.getAmount() > 0) {
                        // Set state
                        mTransactionState.setText(R.string.closed_channel);
                        mTransactionFee.setVisibility(View.GONE);
                        mAmount.setVisibility(View.GONE);
                    } else {
                        // Set state
                        mTransactionState.setText(R.string.opened_channel);
                        mAmount.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(demoItem.getAmount()).replace("-", "- "));
                        mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.superRed));
                        mTransactionFee.setVisibility(View.GONE);
                    }
                } else {
                    mTransactionTypeIcon.setImageResource(R.drawable.ic_onchain_black_24dp);
                    mTransactionTypeIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.lightningOrange)));
                    if (demoItem.getAmount() > 0) {
                        // Set state
                        mTransactionState.setText(R.string.received);
                        mAmount.setText("+ " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(demoItem.getAmount()));
                        mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.superGreen));
                        mTransactionFee.setVisibility(View.GONE);
                    } else {
                        // Set state
                        mTransactionState.setText(R.string.sent);
                        mAmount.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(demoItem.getAmount()).replace("-", "- "));
                        mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.superRed));
                        Long feeAmt = demoItem.getFee();
                        String feeText = mContext.getResources().getString(R.string.fee)
                                + ": " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(feeAmt);
                        mTransactionFee.setVisibility(View.VISIBLE);
                        mTransactionFee.setText(feeText);
                    }
                }
                break;
            case HistoryListItem.TYPE_LN_INVOICE:
                mTransactionTypeIcon.setImageResource(R.drawable.ic_clock_black_24dp);
                mTransactionTypeIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.gray)));
                mAmount.setText("+ " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(demoItem.getAmount()));
                mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.gray));
                mTransactionFee.setVisibility(View.GONE);
                if (demoItem.isExpired()) {
                    mTransactionState.setText(R.string.request_expired);
                    mRootView.setAlpha(0.5f);
                } else {
                    mTransactionState.setText(R.string.requested_payment);
                }
                break;
            case HistoryListItem.TYPE_LN_PAYMENT:
                mTransactionTypeIcon.setImageResource(R.drawable.bolt_black_filled_24dp);
                mTransactionTypeIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.lightningOrange)));
                if (demoItem.getAmount() > 0) {
                    // Set state
                    mTransactionState.setText(R.string.received);
                    mAmount.setText("+ " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(demoItem.getAmount()));
                    mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.superGreen));
                    mTransactionFee.setVisibility(View.GONE);
                } else {
                    // Set state
                    mTransactionState.setText(R.string.sent);
                    mAmount.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(demoItem.getAmount()).replace("-", "- "));
                    mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.superRed));
                    Long feeAmt = demoItem.getFee();
                    String feeText = mContext.getResources().getString(R.string.fee)
                            + ": " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(feeAmt);
                    mTransactionFee.setVisibility(View.VISIBLE);
                    mTransactionFee.setText(feeText);
                }
                break;
        }

        // Set on click listener
        mRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, R.string.demo_setupWalletFirst, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
