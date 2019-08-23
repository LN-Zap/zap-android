package zapsolutions.zap.transactionHistory.listItems;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import zapsolutions.zap.R;
import zapsolutions.zap.transactionHistory.TransactionSelectListener;
import zapsolutions.zap.util.MonetaryUtil;
import zapsolutions.zap.util.OnSingleClickListener;

import java.text.DateFormat;
import java.util.Date;

public class TransactionViewHolder extends RecyclerView.ViewHolder {

    Context mContext;
    View mRootView;

    private TransactionSelectListener mTransactionSelectListener;
    private ImageView mIcon;
    private TextView mTimeOfDay;
    private TextView mPrimaryDescription;
    private TextView mSecondaryDescription;
    private TextView mAmount;
    private TextView mTransactionFee;


    TransactionViewHolder(@NonNull View itemView) {
        super(itemView);

        mIcon = itemView.findViewById(R.id.transactionTypeIcon);
        mTimeOfDay = itemView.findViewById(R.id.timeOfDay);
        mPrimaryDescription = itemView.findViewById(R.id.primaryTransactionDescription);
        mSecondaryDescription = itemView.findViewById(R.id.secondaryTransactionDescription);
        mAmount = itemView.findViewById(R.id.transactionAmount);
        mTransactionFee = itemView.findViewById(R.id.transactionFeeAmount);
        mRootView = itemView.findViewById(R.id.transactionRootView);
        mContext = itemView.getContext();
    }

    void setTimeOfDay(long creationDate) {
        DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, mContext.getResources().getConfiguration().locale);
        String formattedTime = df.format(new Date(creationDate * 1000L));
        mTimeOfDay.setText(formattedTime);
    }

    void setIcon(@NonNull TransactionIcon transactionIcon) {
        switch (transactionIcon) {
            case LIGHTNING:
                mIcon.setImageResource(R.drawable.bolt_black_filled_24dp);
                mIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.lightningOrange)));
                break;
            case ONCHAIN:
                mIcon.setImageResource(R.drawable.ic_onchain_black_24dp);
                mIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.lightningOrange)));
                break;
            case INTERNAL:
                mIcon.setImageResource(R.drawable.ic_internal_black_24dp);
                mIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.gray)));
                break;
            case PENDING:
                mIcon.setImageResource(R.drawable.ic_clock_black_24dp);
                mIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.gray)));
                break;
            default:
                throw new IllegalStateException("Unknown transaction Icon");
        }

    }

    void setAmount(Long amount, boolean visible) {
        mAmount.setVisibility(visible ? View.VISIBLE : View.GONE);

        // compare the amount with 0
        int result = amount.compareTo(0L);
        switch (result) {
            case 0:
                // amount = 0
                mAmount.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amount));
                mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                break;
            case 1:
                // amount > 0
                mAmount.setText("+ " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amount));
                mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.superGreen));
                break;
            case -1:
                // amount < 0
                mAmount.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amount).replace("-", "- "));
                mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.superRed));
                break;
        }
    }

    void setAmount(Long amount, int color, boolean fixedValue, boolean visible) {
        mAmount.setVisibility(visible ? View.VISIBLE : View.GONE);

        if (fixedValue) {
            // compare the amount with 0
            int result = amount.compareTo(0L);
            mAmount.setTextColor(ContextCompat.getColor(mContext, color));
            switch (result) {
                case 0:
                    // amount = 0
                    mAmount.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amount));
                    break;
                case 1:
                    // amount > 0
                    mAmount.setText("+ " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amount));
                    break;
                case -1:
                    // amount < 0
                    mAmount.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amount).replace("-", "- "));
                    break;
            }
        } else {
            mAmount.setText("+ ? " + MonetaryUtil.getInstance().getPrimaryDisplayUnit());
        }
    }

    void setFee(long amount, boolean visible) {
        mTransactionFee.setVisibility(visible ? View.VISIBLE : View.GONE);

        String feeText = mContext.getResources().getString(R.string.fee)
                + ": " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amount);

        mTransactionFee.setText(feeText);
    }

    void setPrimaryDescription(String description) {
        mPrimaryDescription.setText(description);
    }

    void setSecondaryDescription(String description, boolean visible) {
        mSecondaryDescription.setVisibility(visible ? View.VISIBLE : View.GONE);
        mSecondaryDescription.setText(description);
    }

    void setSuccessState(boolean successful) {
        mRootView.setAlpha(successful ? 1f : 0.5f);
    }

    public void addOnTransactionSelectListener(TransactionSelectListener transactionSelectListener) {
        mTransactionSelectListener = transactionSelectListener;
    }

    void setOnRootViewClickListener(@NonNull TransactionItem item, int type) {
        mRootView.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (mTransactionSelectListener != null) {
                    mTransactionSelectListener.onTransactionSelect(item.getTransactionByteString(), type);
                }
            }
        });
    }

    enum TransactionIcon {
        LIGHTNING,
        ONCHAIN,
        INTERNAL,
        PENDING
    }
}
