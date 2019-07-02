package zapsolutions.zap.connection.btcPay;

import androidx.annotation.NonNull;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BTCPayConfigurationJson {

    public BTCPayConfiguration getConfiguration(@NonNull String type, @NonNull String cryptoCode) {
        for (BTCPayConfiguration btcPayConfiguration : configurations) {
            if (btcPayConfiguration.getType().toLowerCase().equals(type.toLowerCase())
                    && btcPayConfiguration.getCryptoCode().toLowerCase().equals(cryptoCode.toLowerCase())) {
                return btcPayConfiguration;
            }
        }

        return null;
    }

    @SerializedName("configurations")
    List<BTCPayConfiguration> configurations;
}
