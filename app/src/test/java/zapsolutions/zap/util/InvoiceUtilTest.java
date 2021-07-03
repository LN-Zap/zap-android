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

    @Test
    public void givenBitcoinAddress_whenIsBitcoinAddress_ThenIsBitcoinAddressReturnTrue() {
        // example addresses taken from: https://en.bitcoin.it/wiki/List_of_address_prefixes
        assertTrue(InvoiceUtil.isBitcoinAddress("bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4")); //bech32, mainnet, lower case
        assertTrue(InvoiceUtil.isBitcoinAddress("bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4".toUpperCase())); //bech32, mainnet, upper case
        assertTrue(InvoiceUtil.isBitcoinAddress("bc1p0xlxvlhemja6c4dqv22uapctqupfhlxm9h8z3k2e72q4k9hcz7vqzk5jj0")); //bech32m, mainnet (example from BIP 350)
        assertTrue(InvoiceUtil.isBitcoinAddress("tb1qw508d6qejxtdg4y5r3zarvary0c5xw7kxpjzsx")); //bech32, testnet, lower case
        assertTrue(InvoiceUtil.isBitcoinAddress("bcrt1q6rhpng9evdsfnn833a4f4vej0asu6dk5srld6x")); //bech32, regtest, lower case
        assertTrue(InvoiceUtil.isBitcoinAddress("17VZNX1SN5NtKa8UQFxwQbFeFc3iqRYhem")); //base58, mainnet, P2PKH
        assertTrue(InvoiceUtil.isBitcoinAddress("mipcBbFg9gMiCh81Kj8tqqdgoZub1ZJRfn")); //base58, testnet, P2PKH
        assertTrue(InvoiceUtil.isBitcoinAddress("3EktnHQD7RiAE6uzMj2ZifT9YgRrkSgzQX")); //base58, mainnet, P2SH
        assertTrue(InvoiceUtil.isBitcoinAddress("2MzQwSSnBHWHqSAqtTVQ6v47XtaisrJa1Vc")); //base58, testnet, P2SH
    }

    @Test
    public void givenInvalidBitcoinAddress_whenIsBitcoinAddress_ThenIsBitcoinAddressReturnFalse() {
        assertFalse(InvoiceUtil.isBitcoinAddress("bc1Qw508d6qEjxtdg4y5r3zarvArY0c5xw7kv8f3T4")); //bech32, mixed case
        assertFalse(InvoiceUtil.isBitcoinAddress("bc1qi508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4")); //bech32, invalid character (i)
        assertFalse(InvoiceUtil.isBitcoinAddress("b1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4")); //bech32, invalid prefix/hrp
        assertFalse(InvoiceUtil.isBitcoinAddress("1IVZNX1SN5NtKa8UQFxwQbFeFc3iqRYhem")); //base58, invalid character (I)
        assertFalse(InvoiceUtil.isBitcoinAddress("47VZNX1SN5NtKa8UQFxwQbFeFc3iqRYhem")); //base58, invalid prefix
        assertFalse(InvoiceUtil.isBitcoinAddress("bc1test")); //valid address prefix, but random ending
        assertFalse(InvoiceUtil.isBitcoinAddress("bitcoinFixesThis")); //arbitraryString
    }
    
}
