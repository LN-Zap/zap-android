package ln_zap.zap.historyList;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import ln_zap.zap.R;
import ln_zap.zap.util.MonetaryUtil;

public class TransactionViewHolder extends RecyclerView.ViewHolder {
    // each data item is just a string in this case
    public TextView mAmount;
    public TextView mDescription;
    public Context  mCtx;
    public TransactionViewHolder(View v) {
        super(v);
        mAmount = v.findViewById(R.id.transactionAmount);
        mDescription = v.findViewById(R.id.transactionDescription);
        mCtx = v.getContext();
    }

    public void bindTransactionItem(TransactionItem transactionItem){
        Long amt = transactionItem.getOnChainTransaction().getAmount();
        mAmount.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(amt));

        if (amt>0){
            mAmount.setTextColor(ContextCompat.getColor(mCtx, R.color.superGreen));
            mDescription.setText("Empfangen");
        }else{
            mAmount.setTextColor(ContextCompat.getColor(mCtx, R.color.superRed));
        }
    }
}
