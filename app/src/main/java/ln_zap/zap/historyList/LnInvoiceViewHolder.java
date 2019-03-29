package ln_zap.zap.historyList;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import ln_zap.zap.R;
import ln_zap.zap.util.MonetaryUtil;


public class LnInvoiceViewHolder extends RecyclerView.ViewHolder {

    private TextView mAmount;
    private ImageView mTransactionTypeIcon;
    private TextView mTransactionType;
    private TextView mDescription;
    private TextView mTimeOfDay;
    private View mRootView;
    private Context  mContext;

    public LnInvoiceViewHolder(View v) {
        super(v);
        mAmount = v.findViewById(R.id.transactionAmount);
        mTransactionTypeIcon = v.findViewById(R.id.transactionTypeIcon);
        mTransactionType = v.findViewById(R.id.transactionType);
        mDescription = v.findViewById(R.id.transactionDescription);
        mTimeOfDay = v.findViewById(R.id.timeOfDay);
        mRootView = v.findViewById(R.id.transactionRootView);
        mContext = v.getContext();
    }

    public void bindLnInvoiceItem(LnInvoiceItem lnInvoiceItem){

        // Standard state. This prevents list entries to get mixed states because of recycling of the ViewHolder.
        mRootView.setAlpha(1f);
        mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.white));
        mTransactionTypeIcon.setImageResource(R.drawable.ic_clock_black_24dp);
        mTransactionTypeIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.gray)));

        // Set time of Day
        DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, mContext.getResources().getConfiguration().locale);
        String formattedTime = df.format(new Date(lnInvoiceItem.mCreationDate * 1000L));
        mTimeOfDay.setText(formattedTime);

        Long amt = lnInvoiceItem.getInvoice().getValue();
        Long amtPayed = lnInvoiceItem.getInvoice().getAmtPaidSat();

        if(amt.equals(0L)){
            // if no specific value was requested
            if (!amtPayed.equals(0L)) {
                // The invoice has been payed

                // Set transaction type text
                mTransactionType.setText(mContext.getResources().getString(R.string.received));
                mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.superGreen));
                mAmount.setText("+ " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amtPayed));
                mTransactionTypeIcon.setImageResource(R.drawable.bolt_black_filled_24dp);
                mTransactionTypeIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.lightningOrange)));
            } else {
                // The invoice has not been payed yet

                if (lnInvoiceItem.mCreationDate + lnInvoiceItem.getInvoice().getExpiry() < System.currentTimeMillis() / 1000) {
                    // The invoice has expired

                    // Set transaction type text
                    mTransactionType.setText(mContext.getResources().getString(R.string.request_expired));
                    mAmount.setText("+ ? " + MonetaryUtil.getInstance().getPrimaryDisplayUnit());
                    mRootView.setAlpha(0.5f);
                } else {
                    // The invoice has not yet expired

                    // Set transaction type text
                    mTransactionType.setText(mContext.getResources().getString(R.string.requested_payment));
                    mAmount.setText("+ ? " + MonetaryUtil.getInstance().getPrimaryDisplayUnit());
                }
            }

        } else {
            // if a specific value was requested
            if (amtPayed.equals(amt)) {
                // The invoice has been payed

                // Set transaction type text
                mTransactionType.setText(mContext.getResources().getString(R.string.received));
                mAmount.setTextColor(ContextCompat.getColor(mContext, R.color.superGreen));
                mAmount.setText("+ " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amt));
                mTransactionTypeIcon.setImageResource(R.drawable.bolt_black_filled_24dp);
                mTransactionTypeIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.lightningOrange)));
            } else {
                // The invoice has not been payed yet

                if (lnInvoiceItem.mCreationDate + lnInvoiceItem.getInvoice().getExpiry() < System.currentTimeMillis() / 1000) {
                    // The invoice has expired

                    // Set transaction type text
                    mTransactionType.setText(mContext.getResources().getString(R.string.request_expired));
                    mAmount.setText("+ " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amt));
                    mRootView.setAlpha(0.5f);
                } else {
                    // The invoice has not yet expired

                    // Set transaction type text
                    mTransactionType.setText(mContext.getResources().getString(R.string.requested_payment));
                    mAmount.setText("+ " + MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amt));
                }
            }
        }

        // Set description
        if (lnInvoiceItem.getInvoice().getMemo().equals("")){
            mDescription.setVisibility(View.GONE);
        } else {
            mDescription.setVisibility(View.VISIBLE);
            mDescription.setText(lnInvoiceItem.getInvoice().getMemo());
        }

        // Set amount



        // Only make it green if it was actually payed!
        if (amt>0){
            //mAmount.setTextColor(ContextCompat.getColor(mCtx, R.color.superGreen));

        }else{
           // mAmount.setTextColor(ContextCompat.getColor(mCtx, R.color.superRed));
        }
    }
}
