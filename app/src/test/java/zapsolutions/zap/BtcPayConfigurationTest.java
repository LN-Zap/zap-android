package zapsolutions.zap;

import com.google.gson.Gson;
import org.junit.Test;
import zapsolutions.zap.connection.btcPay.BTCPayConfigurationJson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;

public class BtcPayConfigurationTest {

    @Test
    public void givenBTCGrpcJson_WhenGetBTCConfiguration_ThenReceiveBTCConfiguration() throws UnsupportedEncodingException {
        BTCPayConfigurationJson btcPayConfigurationJson = readFromFile("btcpayconfig_grpc_btc.json");

        assertNotNull(btcPayConfigurationJson.getConfiguration("GRPC","BTC"));
    }

    @Test
    public void givenBTCGrpcJson_WhenGetBTCLowerCaseConfiguration_ThenReceiveBTCConfiguration() throws UnsupportedEncodingException {
        BTCPayConfigurationJson btcPayConfigurationJson = readFromFile("btcpayconfig_grpc_btc.json");

        assertNotNull(btcPayConfigurationJson.getConfiguration("grpc","btc"));
    }

    @Test
    public void givenLTCGrpcJson_WhenGetBTCConfiguration_ThenReceiveNoConfiguration() throws UnsupportedEncodingException {
        BTCPayConfigurationJson btcPayConfigurationJson = readFromFile("btcpayconfig_grpc_ltc.json");

        assertNull(btcPayConfigurationJson.getConfiguration("GRPC","BTC"));
    }

    @Test
    public void givenBTCRestJson_WhenGetBTCConfiguration_ThenReceiveNoConfiguration() throws UnsupportedEncodingException {
        BTCPayConfigurationJson btcPayConfigurationJson = readFromFile("btcpayconfig_rest_btc.json");

        assertNull(btcPayConfigurationJson.getConfiguration("GRPC","BTC"));
    }


    private BTCPayConfigurationJson readFromFile(String filename) throws UnsupportedEncodingException {
        InputStream inputstream = this.getClass().getClassLoader().getResourceAsStream(filename);
        Reader reader = new InputStreamReader(inputstream, "UTF-8");
        return new Gson().fromJson(reader, BTCPayConfigurationJson.class);
    }
}
