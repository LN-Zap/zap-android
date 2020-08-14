package zapsolutions.zap.transactionHistory.listItems;

import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.text.DateFormat;
import java.util.Date;

import zapsolutions.zap.R;
import zapsolutions.zap.transactionHistory.TransactionSelectListener;
import zapsolutions.zap.util.MonetaryUtil;
import zapsolutions.zap.util.OnSingleClickListener;

public class TransactionViewHolder extends HistoryItemViewHolder {

    View mRootView;
    View mContentView;

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
        mContentView = itemView.findViewById(R.id.transactionContent);
        mContext = itemView.getContext();

        mAmount.setOnClickListener(v -> MonetaryUtil.getInstance().switchCurrencies());
        mTransactionFee.setOnClickListener(v -> MonetaryUtil.getInstance().switchCurrencies());
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
        setAmount(amount, visible, false);
    }

    void setAmountPending(Long amount, boolean fixedValue, boolean visible) {
        if (fixedValue) {
            setAmount(amount, visible, true);
        } else {
            mAmount.setText("+ ? " + MonetaryUtil.getInstance().getPrimaryDisplayUnit());
        }
    }

    private void setAmount(Long amount, boolean visible, boolean pending) {
        mAmount.setVisibility(visible ? View.VISIBLE : View.GONE);

        // compare the amount with 0
        int result = amount.compareTo(0L);
        switch (result) {
            case 0:
                // amount = 0
                mAmount.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amount));
                mAmount.setTextColor(ContextCompat.getColor(mContext, pending ? R.color.gray : R.color.white));
                break;
            case 1:
                // amount > 0
                mAmount.setText("+ " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amount));
                mAmount.setTextColor(ContextCompat.getColor(mContext, pending ? R.color.gray : R.color.superGreen));
                break;
            case -1:
                // amount < 0
                mAmount.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amount).replace("-", "- "));
                mAmount.setTextColor(ContextCompat.getColor(mContext, pending ? R.color.gray : R.color.superRed));
                break;
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
        mContentView.setAlpha(successful ? 1f : 0.5f);
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
