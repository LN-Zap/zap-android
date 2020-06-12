package zapsolutions.zap.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InvoiceUtilTest {

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

}
