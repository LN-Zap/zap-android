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
import ln_zap.zap.util.Wallet;

public class TransactionViewHolder extends RecyclerView.ViewHolder {

    private ImageView mIcon;
    private TextView mTimeOfDay;
    private TextView mTransactionState;
    private TextView mDescription;
    private TextView mAmount;
    private TextView mTransactionFee;
    private View mRootView;
    private Context mContext;

    public TransactionViewHolder(View v) {
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

    public void bindTransactionItem(TransactionItem transactionItem) {

        // Normal state
        mAmount.setVisibility(View.VISIBLE);

        // Set time of Day
        DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, mContext.getResources().getConfiguration().locale);
        String formattedTime = df.format(new Date(transactionItem.mCreationDate * 1000L));
        mTimeOfDay.setText(formattedTime);

        // Set amount
        Long amt = transactionItem.getOnChainTransaction().getAmount();

        // compare the amount with 0
        int result = amt.compareTo(0L);

        if (Wallet.getInstance().isTransactionInternal(transactionItem.getOnChainTransaction())) {

            // internal transaction like opening a channel etc.
            mTransactionState.setText(R.string.internal);
            mIcon.setImageResource(R.drawable.ic_internal_black_24dp);
            mIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.gray)));

            switch (result) {
                case 0:
                    // amount = 0
                    mAmount.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amt));
                    mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                    mTransactionFee.setVisibility(View.GONE);
                    mDescription.setVisibility(View.GONE);
                    break;
                case 1:
                    // amount > 0
                    mAmount.setText("+ " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amt));
                    mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.superGreen));
                    mAmount.setVisibility(View.GONE);
                    mTransactionState.setText(R.string.closed_channel);
                    mTransactionFee.setVisibility(View.GONE);
                    String aliasClosed = Wallet.getInstance().getNodeAliasFromChannelTransaction(transactionItem.getOnChainTransaction(), mContext);
                    mDescription.setText(aliasClosed);
                    mDescription.setVisibility(View.VISIBLE);
                    break;
                case -1:
                    // amount < 0
                    Long feeAmt = transactionItem.getOnChainTransaction().getTotalFees();
                    mAmount.setText("- " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(feeAmt));
                    mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.superRed));
                    mTransactionState.setText(R.string.opened_channel);
                    mTransactionFee.setVisibility(View.GONE);
                    String aliasOpened = Wallet.getInstance().getNodeAliasFromChannelTransaction(transactionItem.getOnChainTransaction(), mContext);
                    mDescription.setText(aliasOpened);
                    mDescription.setVisibility(View.VISIBLE);
                    break;
            }

        } else {

            // It is a normal transaction
            mIcon.setImageResource(R.drawable.ic_onchain_black_24dp);
            mIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.lightningOrange)));

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
                    mAmount.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amt).replace("-", "- "));
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

        }

        // Set on click listener
        mRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, R.string.coming_soon, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
