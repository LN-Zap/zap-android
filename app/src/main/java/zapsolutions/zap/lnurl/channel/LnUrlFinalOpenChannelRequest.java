package zapsolutions.zap.lnurl.channel;

import androidx.annotation.NonNull;

/**
 * This class helps to construct the final withdraw request after the negotiation has happened.
 * <p>
 * Please refer to step 5 in the following reference:
 * https://github.com/fiatjaf/lnurl-rfc/blob/luds/02.md
 */
public class LnUrlFinalOpenChannelRequest {

    private String mCallback;
    private String mK1;
    private String mRemoteId;
    private boolean mIsPrivate;
    private boolean mCancel;

    private LnUrlFinalOpenChannelRequest(String callback, String k1, String remoteId, boolean isPrivate) {
        mCallback = callback;
        mK1 = k1;
        mRemoteId = remoteId;
        mIsPrivate = isPrivate;
    }

    public String getK1() {
        return mK1;
    }

    public String getCallback() {
        return mCallback;
    }

    public String getRemoteId() {
        return mRemoteId;
    }

    public boolean getIsPrivate() {
        return mIsPrivate;
    }

    public boolean getCancel() {
        return mCancel;
    }

    public String requestAsString() {
        String isPrivate = mIsPrivate ? "1" : "0";
        String paramStart = mCallback.contains("?") ? "&" : "?";
        if (mCancel) {
            return mCallback + paramStart + "k1=" + mK1 + "&remoteid=" + mRemoteId + "&cancel=1";
        } else {
            return mCallback + paramStart + "k1=" + mK1 + "&remoteid=" + mRemoteId + "&private=" + isPrivate;
        }
    }


    public static class Builder {
        private String mCallback;
        private String mK1;
        private String mRemoteId;
        private boolean mIsPrivate = false;
        private boolean mCancel = false;

        public Builder setCallback(@NonNull String callback) {
            this.mCallback = callback;

            return this;
        }

        public Builder setK1(@NonNull String k1) {
            this.mK1 = k1;

            return this;
        }

        public Builder setRemoteId(@NonNull String remoteId) {
            this.mRemoteId = remoteId;

            return this;
        }

        public Builder setIsPrivate(@NonNull boolean isPrivate) {
            this.mIsPrivate = isPrivate;

            return this;
        }

        public Builder setCancel(@NonNull boolean cancel) {
            this.mCancel = cancel;

            return this;
        }

        public LnUrlFinalOpenChannelRequest build() {
            return new LnUrlFinalOpenChannelRequest(mCallback, mK1, mRemoteId, mIsPrivate);
        }
    }
}
