package zapsolutions.zap.lnurl.channel;

import java.io.Serializable;

import zapsolutions.zap.lnurl.LnUrlResponse;

/**
 * This class helps to work with the received response from a LNURL-channel request.
 * <p>
 * Please refer to step 3 in the following reference:
 * https://github.com/fiatjaf/lnurl-rfc/blob/luds/02.md
 */
public class LnUrlChannelResponse extends LnUrlResponse implements Serializable {

    public static final String ARGS_KEY = "lnurlChannelResponse";

    private String k1;
    private String uri;

    public String getK1() {
        return k1;
    }

    public String getUri() {
        return uri;
    }
}
