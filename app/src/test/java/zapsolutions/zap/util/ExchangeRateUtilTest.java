package zapsolutions.zap.util;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;

public class ExchangeRateUtilTest {

    // The exchange rate read from the endpoints is in relation to BTC.
    // Internally we use Satoshis. That's why a received rate of 1.0 has to equal 1E-8 after parsing.

    @Test
    public void parseBlockchainInfoResponse() throws JSONException {
        JSONObject in = new JSONObject(readStringFromFile("exchange_rate_blockchain_info_response.json"));
        JSONObject out = ExchangeRateUtil.getInstance().parseBlockchainInfoResponse(in);

        assertEquals(2, out.length());
        assertEquals(1E-8, out.getJSONObject("USD").getDouble("rate"));
    }

    @Test
    public void parseCoinbaseResponse() throws JSONException {
        JSONObject in = new JSONObject(readStringFromFile("exchange_rate_coinbase_response.json"));
        JSONObject out = ExchangeRateUtil.getInstance().parseCoinbaseResponse(in);

        assertEquals(2, out.length());
        assertEquals(1E-8, out.getJSONObject("USD").getDouble("rate"));
    }

    private String readStringFromFile(String filename) {
        InputStream inputstream = this.getClass().getClassLoader().getResourceAsStream(filename);
        return new BufferedReader(new InputStreamReader(inputstream))
                .lines().collect(Collectors.joining("\n"));
    }

}