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
    private static String INVALID_ID = "notExistingOrInvalid";

    @Test
    public void givenNoConfigs_whenDoesWalletExist_thenReturnFalse() {
        WalletConfigsManager manager = new WalletConfigsManager(null);
        WalletConfig walletConfigToFind = new WalletConfig(WALLET_1_ID);
        boolean result = manager.doesWalletConfigExist(walletConfigToFind);

        assertFalse(result);
    }


    @Test
    public void givenExistingId_whenDoesWalletExist_thenReturnTrue() {
        String configJson = readStringFromFile("wallet_configs.json");
        WalletConfigsManager manager = new WalletConfigsManager(configJson);
        WalletConfig walletConfigToFind = new WalletConfig(WALLET_1_ID);
        boolean result = manager.doesWalletConfigExist(walletConfigToFind);

        assertTrue(result);
    }

    @Test
    public void givenNoConfigs_whenLoadWalletConfig_thenReturnNull() {
        WalletConfigsManager manager = new WalletConfigsManager(null);
        WalletConfig result = manager.getWalletConfigById(WALLET_1_ID);

        assertNull(result);
    }

    @Test
    public void givenNonExistingId_whenLoadWalletConfig_thenReturnNull() {
        String configJson = readStringFromFile("wallet_configs.json");
        WalletConfigsManager manager = new WalletConfigsManager(configJson);
        WalletConfig result = manager.getWalletConfigById(INVALID_ID);

        assertNull(result);
    }

    @Test
    public void givenExistingId_whenLoadWalletConfig_thenReceiveCorrectWalletConfig() throws UnsupportedEncodingException {
        WalletConfig expected = readWalletConfigsJsonFromFile("wallet_configs.json").getConnectionById(WALLET_1_ID);
        String configJson = readStringFromFile("wallet_configs.json");
        WalletConfigsManager manager = new WalletConfigsManager(configJson);
        WalletConfig result = manager.getWalletConfigById(WALLET_1_ID);

        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getAlias(), result.getAlias());
        assertEquals(expected.getType(), result.getType());
        assertEquals(expected.getHost(), result.getHost());
        assertEquals(expected.getPort(), result.getPort());
        assertEquals(expected.getCert(), result.getCert());
        assertEquals(expected.getMacaroon(), result.getMacaroon());
    }

    @Test
    public void givenNewId_whenAddWalletConfig_thenReceiveUpdatedWalletConfigs() throws UnsupportedEncodingException {
        WalletConfig expected = readWalletConfigsJsonFromFile("wallet_configs.json").getConnectionById(WALLET_1_ID);

        WalletConfigsManager manager = new WalletConfigsManager(null);
        manager.addWalletConfig(expected.getAlias(), expected.getType(), expected.getHost(), expected.getPort(), expected.getCert(), expected.getMacaroon());
        WalletConfig actual = (WalletConfig) manager.getWalletConfigsJson().getConnections().toArray()[0];

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
        WalletConfig walletConfigToRemove = manager.getWalletConfigById(INVALID_ID);
        boolean removed = manager.removeWalletConfig(walletConfigToRemove);
        String result = new Gson().toJson(manager.getWalletConfigsJson());

        assertFalse(removed);
        assertEquals(expected, result);
    }

    @Test
    public void givenExistingId_whenRemoveWalletConfig_thenReceiveUpdatedWalletConfigs() {
        String configJson = readStringFromFile("wallet_configs.json");

        WalletConfigsManager manager = new WalletConfigsManager(configJson);
        WalletConfig walletConfigToRemove = manager.getWalletConfigById(WALLET_2_ID);
        boolean removed = manager.removeWalletConfig(walletConfigToRemove);

        assertTrue(removed);
        assertNull(manager.getWalletConfigById(WALLET_2_ID));
        assertNotNull(manager.getWalletConfigById(WALLET_1_ID));
    }

    @Test
    public void givenNonExistingId_whenRenameWalletConfig_thenReturnFalse() {
        String configJson = readStringFromFile("wallet_configs.json");
        WalletConfigsManager manager = new WalletConfigsManager(configJson);
        String expected = new Gson().toJson(manager.getWalletConfigsJson());
        WalletConfig walletConfigToRename = manager.getWalletConfigById(INVALID_ID);
        boolean renamed = manager.renameWalletConfig(walletConfigToRename, "NewWalletName");
        String result = new Gson().toJson(manager.getWalletConfigsJson());

        assertFalse(renamed);
        assertEquals(expected, result);
    }


    @Test
    public void givenExistingId_whenRenameWalletConfig_thenReceiveUpdatedWalletConfigs() throws UnsupportedEncodingException {
        WalletConfig expected = readWalletConfigsJsonFromFile("wallet_configs_rename.json").getConnectionById(WALLET_1_ID);
        String configJson = readStringFromFile("wallet_configs_create.json");
        WalletConfigsManager manager = new WalletConfigsManager(configJson);
        WalletConfig walletConfigToRename = manager.getWalletConfigById(WALLET_1_ID);
        boolean renamed = manager.renameWalletConfig(walletConfigToRename, "NewWalletName");
        WalletConfig actual = manager.getWalletConfigById(WALLET_1_ID);

        assertTrue(renamed);
        assertNotNull(manager.getWalletConfigById(WALLET_1_ID));
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getAlias(), actual.getAlias());
        assertEquals(expected.getCert(), actual.getCert());
        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.getHost(), actual.getHost());
        assertEquals(expected.getPort(), actual.getPort());
        assertEquals(expected.getMacaroon(), actual.getMacaroon());
    }

    private WalletConfigsJson readWalletConfigsJsonFromFile(String filename) throws UnsupportedEncodingException {
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