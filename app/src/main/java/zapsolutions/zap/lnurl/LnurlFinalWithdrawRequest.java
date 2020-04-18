package zapsolutions.zap.lnurl;

import androidx.annotation.NonNull;

public class LnurlFinalWithdrawRequest {

    private String mCallback;
    private String mK1;
    private String mInvoice;

    private LnurlFinalWithdrawRequest(String callback, String k1, String invoice) {
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
        return mCallback + "?k1=" + mK1 + "&pr=" + mInvoice;
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

        public LnurlFinalWithdrawRequest build() {
            return new LnurlFinalWithdrawRequest(mCallback, mK1, mInvoice);
        }
    }
}
