package zapsolutions.zap.historyList;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import zapsolutions.zap.R;
import zapsolutions.zap.util.MonetaryUtil;
import zapsolutions.zap.util.OnSingleClickListener;
import zapsolutions.zap.util.Wallet;

import java.text.DateFormat;
import java.util.Date;


public class LnInvoiceViewHolder extends RecyclerView.ViewHolder {

    private ImageView mIcon;
    private TextView mTimeOfDay;
    private TextView mTransactionState;
    private TextView mDescription;
    private TextView mAmount;
    private TextView mTransactionFee;
    private View mRootView;
    private Context mContext;

    public LnInvoiceViewHolder(View v) {
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

    public void bindLnInvoiceItem(LnInvoiceItem lnInvoiceItem) {

        // Standard state. This prevents list entries to get mixed states because of recycling of the ViewHolder.
        mRootView.setAlpha(1f);
        mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.gray));
        mIcon.setImageResource(R.drawable.ic_clock_black_24dp);
        mIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.gray)));
        mTransactionFee.setVisibility(View.GONE);

        // Set time of Day
        DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, mContext.getResources().getConfiguration().locale);
        String formattedTime = df.format(new Date(lnInvoiceItem.mCreationDate * 1000L));
        mTimeOfDay.setText(formattedTime);

        Long amt = lnInvoiceItem.getInvoice().getValue();
        Long amtPayed = lnInvoiceItem.getInvoice().getAmtPaidSat();

        if (amt.equals(0L)) {
            // if no specific value was requested
            if (!amtPayed.equals(0L)) {
                // The invoice has been payed

                // Set transaction type text
                mTransactionState.setText(mContext.getResources().getString(R.string.received));
                mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.superGreen));
                mAmount.setText("+ " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amtPayed));
                mIcon.setImageResource(R.drawable.bolt_black_filled_24dp);
                mIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.lightningOrange)));
            } else {
                // The invoice has not been payed yet

                if (Wallet.getInstance().isInvoiceExpired(lnInvoiceItem.getInvoice())) {
                    // The invoice has expired

                    // Set transaction type text
                    mTransactionState.setText(mContext.getResources().getString(R.string.request_expired));
                    mAmount.setText("+ ? " + MonetaryUtil.getInstance().getPrimaryDisplayUnit());
                    mRootView.setAlpha(0.5f);
                } else {
                    // The invoice has not yet expired

                    // Set transaction type text
                    mTransactionState.setText(mContext.getResources().getString(R.string.requested_payment));
                    mAmount.setText("+ ? " + MonetaryUtil.getInstance().getPrimaryDisplayUnit());
                }
            }

        } else {
            // if a specific value was requested
            if (amtPayed.equals(amt)) {
                // The invoice has been payed

                // Set transaction type text
                mTransactionState.setText(mContext.getResources().getString(R.string.received));
                mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.superGreen));
                mAmount.setText("+ " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amt));
                mIcon.setImageResource(R.drawable.bolt_black_filled_24dp);
                mIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.lightningOrange)));
            } else {
                // The invoice has not been payed yet

                if (Wallet.getInstance().isInvoiceExpired(lnInvoiceItem.getInvoice())) {
                    // The invoice has expired

                    // Set transaction type text
                    mTransactionState.setText(mContext.getResources().getString(R.string.request_expired));
                    mAmount.setText("+ " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amt));
                    mRootView.setAlpha(0.5f);
                } else {
                    // The invoice has not yet expired

                    // Set transaction type text
                    mTransactionState.setText(mContext.getResources().getString(R.string.requested_payment));
                    mAmount.setText("+ " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amt));
                }
            }
        }

        // Set description
        if (lnInvoiceItem.getInvoice().getMemo().equals("")) {
            mDescription.setVisibility(View.GONE);
        } else {
            mDescription.setVisibility(View.VISIBLE);
            mDescription.setText(lnInvoiceItem.getInvoice().getMemo());
        }

        // Set on click listener
        mRootView.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Toast.makeText(mContext, R.string.coming_soon, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
