package zapsolutions.zap;

import com.google.gson.Gson;
import org.junit.Test;
import zapsolutions.zap.connection.btcPay.BTCPayConfigJson;
import zapsolutions.zap.connection.btcPay.BTCPayConfigParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;

public class BtcPayConfigurationParserTest {

    @Test
    public void givenInvalidJson_WhenParse_ThenReturnInvalidJsonError() {
        BTCPayConfigParser payConfigParser = new BTCPayConfigParser("invalid_json").parse();

        assertTrue(payConfigParser.hasError());
        assertEquals(BTCPayConfigParser.ERROR_INVALID_JSON, payConfigParser.getError());
    }

    @Test
    public void givenValidRestJson_WhenParse_ThenReturnMissingConfig() {
        String btcPayConfigJson = readStringFromFile("btcpayconfig_rest_btc.json");
        BTCPayConfigParser payConfigParser = new BTCPayConfigParser(btcPayConfigJson).parse();

        assertTrue(payConfigParser.hasError());
        assertEquals(BTCPayConfigParser.ERROR_MISSING_BTC_GRPC_CONFIG, payConfigParser.getError());
    }

    @Test
    public void givenValidLTCJson_WhenParse_ThenReturnMissingConfig() {
        String btcPayConfigJson = readStringFromFile("btcpayconfig_grpc_ltc.json");
        BTCPayConfigParser payConfigParser = new BTCPayConfigParser(btcPayConfigJson).parse();

        assertTrue(payConfigParser.hasError());
        assertEquals(BTCPayConfigParser.ERROR_MISSING_BTC_GRPC_CONFIG, payConfigParser.getError());
    }

    @Test
    public void givenValidBTCWithoutMacaroonJson_WhenParse_ThenReturnMissingMacaroon() {
        String btcPayConfigJson = readStringFromFile("btcpayconfig_grpc_btc_empty_macaroon.json");
        BTCPayConfigParser payConfigParser = new BTCPayConfigParser(btcPayConfigJson).parse();

        assertTrue(payConfigParser.hasError());
        assertEquals(BTCPayConfigParser.ERROR_NO_MACAROON, payConfigParser.getError());
    }

    @Test
    public void givenValidBTCWithoutHostJson_WhenParse_ThenReturnMissingMacaroon() {
        String btcPayConfigJson = readStringFromFile("btcpayconfig_grpc_btc_empty_host.json");
        BTCPayConfigParser payConfigParser = new BTCPayConfigParser(btcPayConfigJson).parse();

        assertTrue(payConfigParser.hasError());
        assertEquals(BTCPayConfigParser.ERROR_INVALID_HOST_OR_PORT, payConfigParser.getError());
    }

    @Test
    public void givenBTCGrpcJson_WhenGetBTCConfiguration_ThenReceiveBTCConfiguration() throws UnsupportedEncodingException {
        BTCPayConfigJson btcPayConfigJson = readFromFile("btcpayconfig_grpc_btc.json");

        assertNotNull(btcPayConfigJson.getConfiguration("GRPC", "BTC"));
    }

    @Test
    public void givenBTCGrpcJson_WhenGetBTCLowerCaseConfiguration_ThenReceiveBTCConfiguration() throws UnsupportedEncodingException {
        BTCPayConfigJson btcPayConfigJson = readFromFile("btcpayconfig_grpc_btc.json");

        assertNotNull(btcPayConfigJson.getConfiguration("grpc", "btc"));
    }

    @Test
    public void givenLTCGrpcJson_WhenGetBTCConfiguration_ThenReceiveNoConfiguration() throws UnsupportedEncodingException {
        BTCPayConfigJson btcPayConfigJson = readFromFile("btcpayconfig_grpc_ltc.json");

        assertNull(btcPayConfigJson.getConfiguration("GRPC", "BTC"));
    }

    @Test
    public void givenBTCRestJson_WhenGetBTCConfiguration_ThenReceiveNoConfiguration() throws UnsupportedEncodingException {
        BTCPayConfigJson btcPayConfigJson = readFromFile("btcpayconfig_rest_btc.json");

        assertNull(btcPayConfigJson.getConfiguration("GRPC", "BTC"));
    }

    private BTCPayConfigJson readFromFile(String filename) throws UnsupportedEncodingException {
        InputStream inputstream = this.getClass().getClassLoader().getResourceAsStream(filename);
        Reader reader = new InputStreamReader(inputstream, "UTF-8");
        return new Gson().fromJson(reader, BTCPayConfigJson.class);
    }

    private String readStringFromFile(String filename) {
        InputStream inputstream = this.getClass().getClassLoader().getResourceAsStream(filename);
        return new BufferedReader(new InputStreamReader(inputstream))
                .lines().collect(Collectors.joining("\n"));
    }
}
