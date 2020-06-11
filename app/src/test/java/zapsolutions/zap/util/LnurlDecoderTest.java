package zapsolutions.zap.util;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;

public class LnurlDecoderTest {

    // Test vectors have been verified here: https://lnurl.bigsun.xyz/codec/

    private static String LNURL_PAY = "lnurl1dp68gurn8ghj7um0d4jjuer0d4skjm3w0puh5tmvde6hympdwpshj0mnv4ehx6t0dc7nxdpsv5mrwe35xcckxvn9xycrvc3kxdjr2vn9x9nrsv33x9jx2e3cx5ckze3hxejkyv3exu6kgwtzxd3rqdtxvg6xyvesxymnvwrr8qk20prr";
    private static String LNURL_PAY_WITH_URI = "lightning:lnurl1dp68gurn8ghj7um0d4jjuer0d4skjm3w0puh5tmvde6hympdwpshj0mnv4ehx6t0dc7nxdpsv5mrwe35xcckxvn9xycrvc3kxdjr2vn9x9nrsv33x9jx2e3cx5ckze3hxejkyv3exu6kgwtzxd3rqdtxvg6xyvesxymnvwrr8qk20prr";
    private static String LNURL_WITHDRAW = "lnurl1dp68gurn8ghj7um0d4jjuer0d4skjm3w0puh5tmvde6hympdwa5hg6rywfshw0mnv4ehx6t0dc7n2wp3vg6xvdfhxsek2wfsxsuxgd3nxy6nsd3n8pjrwvrzvd3x2erxxgmrgvmyvg6rxde38qckxc3exuunxwtyx9nrwepjxa3rvvphvsvqs2dw";
    private static String LNURL_PAY_DECODED = "https://some.domain.xyz/lnurl-pay?session=340e67f461c2e106b63d52e1f8211def851af76eb2975d9b3b05fb4b301768c8";
    private static String LNURL_WITHDRAW_DECODED = "https://some.domain.xyz/lnurl-withdraw?session=581b4f5743e9048d63158638d70bcbedf2643db437181cb97939d1f7d27b607d";
    private static String LNURL_TEST = "lnurl1w3jhxaqnadsez";
    private static String INVALID_LNURL_MIXED_CASE = "lnurl1W3jhxaqnadsez";
    private static String INVALID_LNURL_WRONG_CHECKSUM = "lnurl1w3jhxaqnadsee";
    private static String INVALID_LNURL_WRONG_CHARACTER = "lnurl1o3jhxaqnadsez";
    private static String INVALID_LNURL_INVALID_PREFIX = "prefix1w3jhxaqnadsez";

    @Test
    public void givenValidLnurl_WhenDecode_ThenReturnDecoded() throws Exception {
        assertEquals(LNURL_PAY_DECODED, LnurlDecoder.decode(LNURL_PAY));
        assertEquals(LNURL_PAY_DECODED, LnurlDecoder.decode(LNURL_PAY_WITH_URI));
        assertEquals(LNURL_WITHDRAW_DECODED, LnurlDecoder.decode(LNURL_WITHDRAW));
    }

    @Test
    public void givenInvalidLnurl_WhenDecode_ThenReturnError() {
        try {
            String decoded = LnurlDecoder.decode(LNURL_TEST);
            assertEquals("test", LnurlDecoder.decode(LNURL_TEST));
        } catch (Exception e) {
            assertEquals(e.getMessage(), "This should have worked");
        }

        try {
            String decoded = LnurlDecoder.decode(null);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "LNURL decoding failed: The data to decode is not a LNURL");
        }

        try {
            String decoded = LnurlDecoder.decode(INVALID_LNURL_INVALID_PREFIX);
            assertNull(decoded);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "LNURL decoding failed: The data to decode is not a LNURL");
        }

        try {
            String decoded = LnurlDecoder.decode(INVALID_LNURL_MIXED_CASE);
            assertNull(decoded);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "LNURL decoding failed: bech32 cannot mix upper and lower case");
        }

        try {
            String decoded = LnurlDecoder.decode(INVALID_LNURL_WRONG_CHECKSUM);
            assertNull(decoded);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "LNURL decoding failed: invalid bech32 checksum");
        }

        try {
            String decoded = LnurlDecoder.decode(INVALID_LNURL_WRONG_CHARACTER);
            assertNull(decoded);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "LNURL decoding failed: bech32 characters out of range");
        }
    }

}