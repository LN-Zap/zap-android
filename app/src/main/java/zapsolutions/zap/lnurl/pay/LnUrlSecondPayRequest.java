package zapsolutions.zap.lnurl.pay;

import androidx.annotation.NonNull;

import java.util.Random;

import zapsolutions.zap.util.UtilFunctions;

/**
 * This class helps to construct the second pay request.
 * <p>
 * Please refer to step 5 in the following reference:
 * https://github.com/btcontract/lnurl-rfc/blob/master/lnurl-pay.md
 */
public class LnUrlSecondPayRequest {

    private String mCallback;
    private long mAmount;
    private String[] mFromNodes;

    private LnUrlSecondPayRequest(String callback, long amount, String[] fromNodes) {
        mCallback = callback;
        mAmount = amount;
        mFromNodes = fromNodes;
    }

    public String getCallback() {
        return mCallback;
    }

    public long getAmount() {
        return mAmount;
    }

    public String[] getFromNodes() {
        return mFromNodes;
    }

    public String requestAsString() {
        String paramStart = mCallback.contains("?") ? "&" : "?";
        if (mFromNodes == null) {
            return mCallback + paramStart + "amount=" + mAmount + "&nonce=" + generateNonce();
        } else {
            String fromNodesString = "";
            for (int i = 0; i < mFromNodes.length; i++) {
                if (i == mFromNodes.length - 1) {
                    fromNodesString = fromNodesString + mFromNodes[i];
                } else {
                    fromNodesString = fromNodesString + mFromNodes[i] + ",";
                }
            }
            return mCallback + paramStart + "amount=" + mAmount + "&nonce=" + generateNonce() + "&fromnodes=" + fromNodesString;
        }
    }


    public static class Builder {
        private String mCallback;
        private Long mAmount;
        private String[] mFromNodes;

        public Builder setCallback(@NonNull String callback) {
            this.mCallback = callback;

            return this;
        }

        public Builder setAmount(@NonNull Long amount) {
            this.mAmount = amount;

            return this;
        }

        public Builder setFromNodes(String[] fromNodes) {
            this.mFromNodes = fromNodes;

            return this;
        }

        public LnUrlSecondPayRequest build() {
            return new LnUrlSecondPayRequest(mCallback, mAmount, mFromNodes);
        }
    }

    private String generateNonce() {
        byte[] b = new byte[8];
        new Random().nextBytes(b);
        return UtilFunctions.bytesToHex(b);
    }
}
