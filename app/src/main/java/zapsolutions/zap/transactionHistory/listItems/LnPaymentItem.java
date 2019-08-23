package zapsolutions.zap.transactionHistory.listItems;

import com.github.lightningnetwork.lnd.lnrpc.Payment;
import com.google.protobuf.ByteString;

public class LnPaymentItem extends TransactionItem {
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

    @Override
    public ByteString getTransactionByteString() {
        return mPayment.toByteString();
    }
}
