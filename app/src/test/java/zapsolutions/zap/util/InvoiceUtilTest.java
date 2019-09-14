package zapsolutions.zap.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InvoiceUtilTest {


    @Test
    public void givenLightningInvoice_whenGenerateLightningUri_ThenIsLightningUriReturnsTrue() {
        String invoice = "lnbc1pvjluezpp5qqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqypqdpl2pkx2ctnv5sxxmmwwd5kgetjypeh2ursdae8g6twvus8g6rfwvs8qun0dfjkxaq8rkx3yf5tcsyz3d73gafnh3cax9rn449d9p5uxz9ezhhypd0elx87sjle52x86fux2ypatgddc6k63n7erqz25le42c4u4ecky03ylcqca784w";

        String lightingUri = InvoiceUtil.generateLightningUri(invoice);

        assertTrue(InvoiceUtil.isLightningUri(lightingUri));
    }

    @Test
    public void givenLightningUri_whenGenerateLightningUri_ThenDidNotAddPrefix() {
        String lightningUri = "lightning:lnbc1pvjluezpp5qqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqypqdpl2pkx2ctnv5sxxmmwwd5kgetjypeh2ursdae8g6twvus8g6rfwvs8qun0dfjkxaq8rkx3yf5tcsyz3d73gafnh3cax9rn449d9p5uxz9ezhhypd0elx87sjle52x86fux2ypatgddc6k63n7erqz25le42c4u4ecky03ylcqca784w";

        String lightningUriGenerated = InvoiceUtil.generateLightningUri(lightningUri);

        assertEquals(countOfOccurrences(lightningUriGenerated, InvoiceUtil.URI_PREFIX_LIGHTNING), 1);
    }

    @Test
    public void givenBitcoinRequest_whenGenerateBitcoinUri_ThenIsBitcoinUriReturnTrue() {
        String request = "175tWpb8K1S7NmH4Zx6rewF9WQrcZv245W?amount=20.3&label=Luke-Jr";

        String bitcoinUri = InvoiceUtil.generateBitcoinUri(request);

        assertTrue(InvoiceUtil.isBitcoinUri(bitcoinUri));
    }

    @Test
    public void givenBitcoinUri_whenGenerateBitcoinUri_ThenDidNotAddPrefix() {
        String bitcoinUri = "bitcoin:175tWpb8K1S7NmH4Zx6rewF9WQrcZv245W?amount=20.3&label=Luke-Jr";

        String bitcoinUriGenerated = InvoiceUtil.generateBitcoinUri(bitcoinUri);

        assertEquals(countOfOccurrences(bitcoinUriGenerated, InvoiceUtil.URI_PREFIX_BITCOIN), 1);
    }

    @Test
    public void givenBitcoinRequest_whenIsBitcoinUri_ThenIsBitcoinUriReturnFalse() {
        String request = "175tWpb8K1S7NmH4Zx6rewF9WQrcZv245W?amount=20.3&label=Luke-Jr";

        assertFalse(InvoiceUtil.isBitcoinUri(request));
    }

    @Test
    public void givenBitcoinUri_whenIsBitcoinUri_ThenIsBitcoinUriReturnTrue() {
        String bitcoinUri = "bitcoin:175tWpb8K1S7NmH4Zx6rewF9WQrcZv245W?amount=20.3&label=Luke-Jr";

        assertTrue(InvoiceUtil.isBitcoinUri(bitcoinUri));
    }

    @Test
    public void givenLightningInvoice_whenIsLightningUri_ThenIsLightningUriReturnFalse() {
        String invoice = "lnbc1pvjluezpp5qqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqypqdpl2pkx2ctnv5sxxmmwwd5kgetjypeh2ursdae8g6twvus8g6rfwvs8qun0dfjkxaq8rkx3yf5tcsyz3d73gafnh3cax9rn449d9p5uxz9ezhhypd0elx87sjle52x86fux2ypatgddc6k63n7erqz25le42c4u4ecky03ylcqca784w";

        assertFalse(InvoiceUtil.isLightningUri(invoice));
    }

    @Test
    public void givenLightningUri_whenIsLightningUri_ThenIsLightningUriReturnTrue() {
        String lightningUri = "lightning:lnbc1pvjluezpp5qqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqypqdpl2pkx2ctnv5sxxmmwwd5kgetjypeh2ursdae8g6twvus8g6rfwvs8qun0dfjkxaq8rkx3yf5tcsyz3d73gafnh3cax9rn449d9p5uxz9ezhhypd0elx87sjle52x86fux2ypatgddc6k63n7erqz25le42c4u4ecky03ylcqca784w";

        assertTrue(InvoiceUtil.isLightningUri(lightningUri));
    }

    @Test
    public void givenLightningInvoiceMainnet_whenIsLightningInvoice_ThenIsLightningInvoiceReturnTrue() {
        String lightningUri = "lnbc1pvjluezpp5qqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqypqdpl2pkx2ctnv5sxxmmwwd5kgetjypeh2ursdae8g6twvus8g6rfwvs8qun0dfjkxaq8rkx3yf5tcsyz3d73gafnh3cax9rn449d9p5uxz9ezhhypd0elx87sjle52x86fux2ypatgddc6k63n7erqz25le42c4u4ecky03ylcqca784w";

        assertTrue(InvoiceUtil.isLightningInvoice(lightningUri));
    }

    @Test
    public void givenLightningInvoiceTestnet_whenIsLightningInvoice_ThenIsLightningInvoiceReturnTrue() {
        String lightningUri = "lntb1pvjluezpp5qqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqypqdpl2pkx2ctnv5sxxmmwwd5kgetjypeh2ursdae8g6twvus8g6rfwvs8qun0dfjkxaq8rkx3yf5tcsyz3d73gafnh3cax9rn449d9p5uxz9ezhhypd0elx87sjle52x86fux2ypatgddc6k63n7erqz25le42c4u4ecky03ylcqca784w";

        assertTrue(InvoiceUtil.isLightningInvoice(lightningUri));
    }

    @Test
    public void givenArbitraryString_whenIsLightningInvoice_ThenIsLightningInvoiceReturnFalse() {
        String arbitraryString = "bitcoinFixesThis";

        assertFalse(InvoiceUtil.isLightningInvoice(arbitraryString));
    }

    /**
     * Returns the amount of occurrences of findString inside sourceString.
     */
    private int countOfOccurrences(String sourceString, String findString) {
        return sourceString.split(findString, -1).length - 1;
    }
}
