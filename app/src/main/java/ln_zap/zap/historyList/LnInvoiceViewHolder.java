package ln_zap.zap.historyList;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import ln_zap.zap.R;
import ln_zap.zap.util.MonetaryUtil;

public class LnInvoiceViewHolder extends RecyclerView.ViewHolder {
    // each data item is just a string in this case
    public TextView mAmount;
    public TextView mDescription;
    public Context  mCtx;
    public LnInvoiceViewHolder(View v) {
        super(v);
        mAmount = v.findViewById(R.id.transactionAmount);
        mDescription = v.findViewById(R.id.transactionDescription);
        mCtx = v.getContext();
    }

    public void bindLnInvoiceItem(LnInvoiceItem lnInvoiceItem){
        Long amt = lnInvoiceItem.getInvoice().getValue();
        mAmount.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amt));

        if (amt>0){
            mAmount.setTextColor(ContextCompat.getColor(mCtx, R.color.superGreen));

        }else{
            mAmount.setTextColor(ContextCompat.getColor(mCtx, R.color.superRed));
        }
    }
}
