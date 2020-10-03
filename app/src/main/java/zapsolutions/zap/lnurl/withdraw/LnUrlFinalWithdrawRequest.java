package zapsolutions.zap.lnurl.withdraw;

import androidx.annotation.NonNull;

/**
 * This class helps to construct the final withdraw request after the negotiation has happened.
 * <p>
 * Please refer to step 5 in the following reference:
 * https://github.com/btcontract/lnurl-rfc/blob/master/lnurl-withdraw.md
 */
public class LnUrlFinalWithdrawRequest {

    private String mCallback;
    private String mK1;
    private String mInvoice;

    private LnUrlFinalWithdrawRequest(String callback, String k1, String invoice) {
        mCallback = callback;
        mK1 = k1;
        mInvoice = invoice;
    }

    public String getK1() {
        return mK1;
    }

    public String getCallback() {
        return mCallback;
    }

    public String getInvoice() {
        return mInvoice;
    }

    public String requestAsString() {
        String paramStart = mCallback.contains("?") ? "&" : "?";
        return mCallback + paramStart + "k1=" + mK1 + "&pr=" + mInvoice;
    }


    public static class Builder {
        private String mCallback;
        private String mK1;
        private String mInvoice;

        public Builder setCallback(@NonNull String callback) {
            this.mCallback = callback;

            return this;
        }

        public Builder setK1(@NonNull String k1) {
            this.mK1 = k1;

            return this;
        }

        public Builder setInvoice(@NonNull String invoice) {
            this.mInvoice = invoice;

            return this;
        }

        public LnUrlFinalWithdrawRequest build() {
            return new LnUrlFinalWithdrawRequest(mCallback, mK1, mInvoice);
        }
    }
}
