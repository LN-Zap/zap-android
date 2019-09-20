package zapsolutions.zap.lightning;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class LightningNodeUri implements Serializable {

    private String mPubKey;
    private String mHost;

    private LightningNodeUri(@NonNull String pubKey, String host) {
        mPubKey = pubKey;
        mHost = host;
    }

    @NonNull
    public String getPubKey() {
        return mPubKey;
    }

    public String getHost() {
        return mHost;
    }

    public static class Builder {
        private String mPubKey;
        private String mHost;

        public Builder setPubKey(@NonNull String pubKey) {
            this.mPubKey = pubKey;

            return this;
        }

        public Builder setHost(String host) {
            this.mHost = host;

            return this;
        }

        public LightningNodeUri build() {
            return new LightningNodeUri(mPubKey, mHost);
        }
    }
}
