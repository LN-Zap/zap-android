package zapsolutions.zap.lnurl.pay;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;

import zapsolutions.zap.lnurl.LnUrlResponse;
import zapsolutions.zap.util.UtilFunctions;

/**
 * This class helps to work with the received response from a LNURL-pay request.
 * <p>
 * Please refer to step 3 in the following reference:
 * https://github.com/fiatjaf/lnurl-rfc/blob/luds/06.md
 */
public class LnUrlPayResponse extends LnUrlResponse implements Serializable {

    public static final String ARGS_KEY = "lnurlPayResponse";
    public static final String METADATA_TEXT = "text/plain";
    public static final String METADATA_IMAGE_PNG = "image/png;base64";
    public static final String METADATA_IMAGE_JPEG = "image/jpeg;base64";
    public static final String METADATA_IDENTIFIER = "text/identifier";
    public static final String METADATA_EMAIL = "text/email";

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

    public String getMetadataAsString(String metadataName) {
        JsonArray[] list = getMetadataAsList();
        for (JsonArray jsonArray : list) {
            if (jsonArray.get(0).getAsString().equals(metadataName)) {
                return jsonArray.get(1).getAsString();
            }
        }
        return null;
    }

    public JsonArray getMetadataAsJsonArray(String metadataName) {
        JsonArray[] list = getMetadataAsList();
        for (JsonArray jsonArray : list) {
            if (jsonArray.get(0).getAsString().equals(metadataName)) {
                return jsonArray;
            }
        }
        return null;
    }

    private JsonArray[] getMetadataAsList() {
        Gson gson = new Gson();
        Type listType = new TypeToken<JsonArray[]>() {
        }.getType();
        return gson.fromJson(metadata, listType);
    }

    public String getMetadataHash() {
        return UtilFunctions.sha256Hash(metadata);
    }
}
