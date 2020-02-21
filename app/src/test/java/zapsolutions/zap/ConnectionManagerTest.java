package zapsolutions.zap;

import com.google.gson.Gson;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.stream.Collectors;

import zapsolutions.zap.connection.manageWalletConfigs.WalletConfig;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfigsJson;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfigsManager;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;

public class ConnectionManagerTest {

    private static String WALLET_1_ID = "e4f2fcf7-82c7-46f4-8867-50c3f8a603f4";
    private static String WALLET_2_ID = "a4f2fcf7-82c7-46f4-8867-50c3f8a603f4";

    @Test
    public void givenNoConfigs_whenDoesWalletExist_thenReturnFalse() {
        WalletConfigsManager manager = new WalletConfigsManager(null);
        boolean result = manager.doesWalletConfigExist(WALLET_1_ID);

        assertFalse(result);
    }


    @Test
    public void givenExistingId_whenDoesWalletExist_thenReturnTrue() {
        String configJson = readStringFromFile("wallet_configs.json");
        WalletConfigsManager manager = new WalletConfigsManager(configJson);
        boolean result = manager.doesWalletConfigExist(WALLET_1_ID);

        assertTrue(result);
    }

    @Test
    public void givenNoConfigs_whenLoadWalletConfig_thenReturnNull() {
        WalletConfigsManager manager = new WalletConfigsManager(null);
        WalletConfig result = manager.getWalletConfig(WALLET_1_ID);

        assertNull(result);
    }

    @Test
    public void givenNonExistingId_whenLoadWalletConfig_thenReturnNull() {
        String configJson = readStringFromFile("wallet_configs.json");
        WalletConfigsManager manager = new WalletConfigsManager(configJson);
        WalletConfig result = manager.getWalletConfig("000");

        assertNull(result);
    }

    @Test
    public void givenExistingId_whenLoadWalletConfig_thenReceiveCorrectWalletConfig() {
        String configJson = readStringFromFile("wallet_configs.json");
        WalletConfigsManager manager = new WalletConfigsManager(configJson);
        WalletConfig result = manager.getWalletConfig(WALLET_1_ID);

        assertEquals(WALLET_1_ID, result.getId());
        assertEquals("FirstWalletName", result.getAlias());
        assertEquals("remote", result.getType());
        assertEquals("TestHost", result.getHost());
        assertEquals(10009, result.getPort());
        assertEquals("TestCert", result.getCert());
        assertEquals("TestMacaroon", result.getMacaroon());
    }

    @Test
    public void givenNewId_whenAddWalletConfig_thenReceiveUpdatedWalletConfigs() throws UnsupportedEncodingException {
        WalletConfig expected = readFromFile("wallet_configs.json").getConnection(WALLET_1_ID);

        WalletConfigsManager manager = new WalletConfigsManager(null);
        manager.addWalletConfig("FirstWalletName", "remote", "TestHost", 10009, "TestCert", "TestMacaroon");
        WalletConfig actual = manager.getWalletConfigsJson().getConnections().get(0);

        assertEquals(expected.getAlias(), actual.getAlias());
        assertEquals(expected.getCert(), actual.getCert());
        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.getHost(), actual.getHost());
        assertEquals(expected.getPort(), actual.getPort());
        assertEquals(expected.getMacaroon(), actual.getMacaroon());
    }

    @Test
    public void givenNonExistingId_whenRemoveWalletConfig_thenReturnFalse() {
        String configJson = readStringFromFile("wallet_configs.json");
        WalletConfigsManager manager = new WalletConfigsManager(configJson);

        String expected = new Gson().toJson(manager.getWalletConfigsJson());
        boolean removed = manager.removeWalletConfig("000");
        String result = new Gson().toJson(manager.getWalletConfigsJson());

        assertFalse(removed);
        assertEquals(expected, result);
    }

    @Test
    public void givenExistingId_whenRemoveWalletConfig_thenReceiveUpdatedWalletConfigs() {
        String configJson = readStringFromFile("wallet_configs.json");

        WalletConfigsManager manager = new WalletConfigsManager(configJson);
        boolean removed = manager.removeWalletConfig(WALLET_2_ID);

        assertTrue(removed);
        assertNull(manager.getWalletConfig(WALLET_2_ID));
        assertNotNull(manager.getWalletConfig(WALLET_1_ID));
    }

    @Test
    public void givenNonExistingId_whenRenameWalletConfig_thenReturnFalse() {
        String configJson = readStringFromFile("wallet_configs.json");
        WalletConfigsManager manager = new WalletConfigsManager(configJson);
        String expected = new Gson().toJson(manager.getWalletConfigsJson());
        boolean renamed = manager.renameWalletConfig("000", "test2");
        String result = new Gson().toJson(manager.getWalletConfigsJson());

        assertFalse(renamed);
        assertEquals(expected, result);
    }


    @Test
    public void givenExistingId_whenRenameWalletConfig_thenReceiveUpdatedWalletConfigs() throws UnsupportedEncodingException {
        WalletConfig expected = readFromFile("wallet_configs_rename.json").getConnection(WALLET_1_ID);
        String configJson = readStringFromFile("wallet_configs_create.json");
        WalletConfigsManager manager = new WalletConfigsManager(configJson);
        boolean renamed = manager.renameWalletConfig(WALLET_1_ID, "NewWalletName");
        WalletConfig actual = manager.getWalletConfig(WALLET_1_ID);

        assertTrue(renamed);
        assertNotNull(manager.getWalletConfig(WALLET_1_ID));
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getAlias(), actual.getAlias());
        assertEquals(expected.getCert(), actual.getCert());
        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.getHost(), actual.getHost());
        assertEquals(expected.getPort(), actual.getPort());
        assertEquals(expected.getMacaroon(), actual.getMacaroon());
    }

    private WalletConfigsJson readFromFile(String filename) throws UnsupportedEncodingException {
        InputStream inputstream = this.getClass().getClassLoader().getResourceAsStream(filename);
        Reader reader = new InputStreamReader(inputstream, "UTF-8");
        return new Gson().fromJson(reader, WalletConfigsJson.class);
    }

    private String readStringFromFile(String filename) {
        InputStream inputstream = this.getClass().getClassLoader().getResourceAsStream(filename);
        return new BufferedReader(new InputStreamReader(inputstream))
                .lines().collect(Collectors.joining("\n"));
    }
}