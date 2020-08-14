package zapsolutions.zap.transactionHistory.listItems;

import com.github.lightningnetwork.lnd.lnrpc.Payment;
import com.google.protobuf.ByteString;

import zapsolutions.zap.util.PaymentRequestUtil;

public class LnPaymentItem extends TransactionItem {
    private Payment mPayment;
    private String mMemo;

    public LnPaymentItem(Payment payment) {
        mPayment = payment;
        mCreationDate = payment.getCreationDate();

        if (payment.getPaymentRequest() != null && !payment.getPaymentRequest().isEmpty()) {
            // This will only be true for payments done with LND 0.7.0-beta and later
            mMemo = PaymentRequestUtil.getMemo(payment.getPaymentRequest());
        }
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

    public String getMemo() {
        return mMemo;
    }
}
