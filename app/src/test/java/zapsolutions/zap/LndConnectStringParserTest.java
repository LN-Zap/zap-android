package zapsolutions.zap;

import org.junit.Test;

import zapsolutions.zap.connection.lndConnect.LndConnectStringParser;

import static junit.framework.TestCase.assertEquals;

public class LndConnectStringParserTest {

    @Test
    public void parse_lndConnect_Null() {
        String input = null;
        int output;
        int expected = LndConnectStringParser.ERROR_INVALID_CONNECT_STRING;

        LndConnectStringParser parser = new LndConnectStringParser();
        output = parser.parse(input).getError();

        assertEquals(expected, output);
    }

    @Test
    public void parse_lndConnect_Empty() {
        String input = "";
        int output;
        int expected = LndConnectStringParser.ERROR_INVALID_CONNECT_STRING;

        LndConnectStringParser parser = new LndConnectStringParser();
        output = parser.parse(input).getError();

        assertEquals(expected, output);
    }

    @Test
    public void parse_lndConnect_Random() {
        String input = "test";
        int output;
        int expected = LndConnectStringParser.ERROR_INVALID_CONNECT_STRING;

        LndConnectStringParser parser = new LndConnectStringParser();
        output = parser.parse(input).getError();

        assertEquals(expected, output);
    }

    @Test
    public void parse_lndConnect_justScheme() {
        String input = "lndconnect://";
        int output;
        int expected = LndConnectStringParser.ERROR_INVALID_CONNECT_STRING;

        LndConnectStringParser parser = new LndConnectStringParser();
        output = parser.parse(input).getError();

        assertEquals(expected, output);
    }

    @Test
    public void parse_lndConnect_invalidHost() {
        String input = "lndconnect://192.168.1.312:10009?macaroon=exampleMacaroon";
        int output;
        int expected = LndConnectStringParser.ERROR_INVALID_HOST_OR_PORT;

        LndConnectStringParser parser = new LndConnectStringParser();
        output = parser.parse(input).getError();

        assertEquals(expected, output);
    }

    @Test
    public void parse_lndConnect_noPort() {
        String input = "lndconnect://0.0.0.0?macaroon=exampleMacaroon";
        int output;
        int expected = LndConnectStringParser.ERROR_INVALID_HOST_OR_PORT;

        LndConnectStringParser parser = new LndConnectStringParser();
        output = parser.parse(input).getError();

        assertEquals(expected, output);
    }

    @Test
    public void parse_lndConnect_noMacaroon() {
        String input = "lndconnect://0.0.0.0:00000?";
        int output;
        int expected = LndConnectStringParser.ERROR_NO_MACAROON;

        LndConnectStringParser parser = new LndConnectStringParser();
        output = parser.parse(input).getError();

        assertEquals(expected, output);
    }

    @Test
    public void parse_lndConnect_invalidCert() {
        String input = "lndconnect://0.0.0.0:00000?cert=exampleCert&macaroon=exampleMacaroon";
        int output;
        int expected = LndConnectStringParser.ERROR_INVALID_CERTIFICATE;

        LndConnectStringParser parser = new LndConnectStringParser();
        output = parser.parse(input).getError();

        assertEquals(expected, output);
    }

    @Test
    public void parse_lndConnect_invalidMacaroon() {
        String input = "lndconnect://0.0.0.0:00000?macaroon=exampleMacaroonWithUnsupportedCharacters:/";
        int output;
        int expected = LndConnectStringParser.ERROR_INVALID_MACAROON;

        LndConnectStringParser parser = new LndConnectStringParser();
        output = parser.parse(input).getError();

        assertEquals(expected, output);
    }

    @Test
    public void parse_lndConnect_noCert_valid() {
        String input = "lndconnect://1.2.3.4:10009?macaroon=exampleMacaroon";
        int output;
        int expected = -1;

        LndConnectStringParser parser = new LndConnectStringParser();
        output = parser.parse(input).getError();

        assertEquals(expected, output);
    }
}