package zapsolutions.zap.lnurl.pay;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import zapsolutions.zap.lnurl.LnUrlResponse;

/**
 * This class helps to work with the received response from a LNURL-pay request.
 *
 * Please refer to step 3 in the following reference:
 * https://github.com/btcontract/lnurl-rfc/blob/master/lnurl-pay.md
 */
public class LnUrlPayResponse extends LnUrlResponse implements Serializable {

    public static final String ARGS_KEY = "lnurlPayResponse";
    public static final String METADATA_TEXT = "text/plain";
    public static final String METADATA_IMAGE_PNG = "image/png;base64";
    public static final String METADATA_IMAGE_JPEG = "image/jpeg;base64";

    private String metadata;

    /**
     * In milliSatoshis
     */
    private long maxSendable;
    /**
     * In milliSatoshis
     */
    private long minSendable;

    public long getMaxSendable() {
        return maxSendable;
    }

    public long getMinSendable() {
        return minSendable;
    }

    public String getMetadata(String metadataName) {
        List<String[]> list = getMetadataAsList();
        for (String[] stringArray : list) {
            if (stringArray[0].equals(metadataName)){
                return stringArray[1];
            }
        }
        return null;
    }

    private List<String[]> getMetadataAsList(){
        Gson gson = new Gson();
        Type listType = new TypeToken<List<String[]>>() {}.getType();
        List<String[]> list = gson.fromJson(metadata, listType);
        return list;
    }
}
