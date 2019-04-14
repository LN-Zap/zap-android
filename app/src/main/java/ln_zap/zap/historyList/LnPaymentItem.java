package ln_zap.zap.historyList;

import com.github.lightningnetwork.lnd.lnrpc.Payment;

public class LnPaymentItem extends HistoryListItem {
    private Payment mPayment;

    public LnPaymentItem(Payment payment) {
        mPayment = payment;
        mCreationDate = payment.getCreationDate();
    }

    @Override
    public int getType() {
        return TYPE_LN_PAYMENT;
    }

    public Payment getPayment() {
        return mPayment;
    }
}
