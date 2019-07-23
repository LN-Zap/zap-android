package zapsolutions.zap;

import com.google.gson.Gson;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import zapsolutions.zap.connection.manageWalletConfigs.WalletConfig;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfigsManager;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.assertFalse;

public class ConnectionManagerTest {

    @Test
    public void givenNoConfigs_whenDoesWalletExist_thenReturnFalse() {
        WalletConfigsManager manager = new WalletConfigsManager(null);
        boolean result = manager.doesWalletConfigExist("test");

        assertFalse(result);
    }


    @Test
    public void givenExistingAlias_whenDoesWalletExist_thenReturnTrue() {
        String configJson = readStringFromFile("wallet_configs.json");
        WalletConfigsManager manager = new WalletConfigsManager(configJson);
        boolean result = manager.doesWalletConfigExist("firstwalletname");

        assertTrue(result);
    }

    @Test
    public void givenNoConfigs_whenLoadWalletConfig_thenReturnNull() {
        WalletConfigsManager manager = new WalletConfigsManager(null);
        WalletConfig result = manager.getWalletConfig("firstwalletname");

        assertNull(result);
    }

    @Test
    public void givenNonExistingAlias_whenLoadWalletConfig_thenReturnNull() {
        String configJson = readStringFromFile("wallet_configs.json");
        WalletConfigsManager manager = new WalletConfigsManager(configJson);
        WalletConfig result = manager.getWalletConfig("test");

        assertNull(result);
    }

    @Test
    public void givenExistingAlias_whenLoadWalletConfig_thenReceiveCorrectWalletConfig() {
        String configJson = readStringFromFile("wallet_configs.json");
        WalletConfigsManager manager = new WalletConfigsManager(configJson);
        WalletConfig result = manager.getWalletConfig("firstwalletname");

        assertEquals("firstwalletname", result.getAlias().toLowerCase());
        assertEquals("remote", result.getType());
        assertEquals("TestHost", result.getHost());
        assertEquals(10009, result.getPort());
        assertEquals("TestCert", result.getCert());
        assertEquals("TestMacaroon", result.getMacaroon());
    }

    @Test
    public void givenNewAlias_whenAddWalletConfig_thenReceiveUpdatedWalletConfigs() {
        String expected = readStringFromFile("wallet_configs_create.json");
        WalletConfigsManager manager = new WalletConfigsManager(null);
        manager.addWalletConfig("FirstWalletName", "remote", "TestHost", 10009, "TestCert", "TestMacaroon");
        String result = new Gson().toJson(manager.getWalletConfigsJson());

        assertEquals(expected, result);
    }

    @Test
    public void givenExistingAlias_whenAddWalletConfig_thenReceiveUpdatedWalletConfigs() {
        String expected = readStringFromFile("wallet_configs_modify.json");
        String configJson = readStringFromFile("wallet_configs_create.json");
        WalletConfigsManager manager = new WalletConfigsManager(configJson);
        manager.addWalletConfig("FirstWalletName", "remote", "ModifiedHost", 10009, "TestCert", "TestMacaroon");
        String result = new Gson().toJson(manager.getWalletConfigsJson());

        assertEquals(expected, result);
    }

    @Test
    public void givenNonExistingAlias_whenRemoveWalletConfig_thenReturnFalse() {

        String configJson = readStringFromFile("wallet_configs.json");
        WalletConfigsManager manager = new WalletConfigsManager(configJson);
        String expected = new Gson().toJson(manager.getWalletConfigsJson());
        boolean removed = manager.removeWalletConfig("test");
        String result = new Gson().toJson(manager.getWalletConfigsJson());

        assertFalse(removed);
        assertEquals(expected, result);
    }

    @Test
    public void givenExistingAlias_whenRemoveWalletConfig_thenReceiveUpdatedWalletConfigs() {
        String expected = readStringFromFile("wallet_configs_create.json");
        String configJson = readStringFromFile("wallet_configs.json");
        WalletConfigsManager manager = new WalletConfigsManager(configJson);
        boolean removed = manager.removeWalletConfig("secondwalletname");
        String result = new Gson().toJson(manager.getWalletConfigsJson());

        assertTrue(removed);
        assertEquals(expected, result);
    }

    @Test
    public void givenNonExistingAlias_whenRenameWalletConfig_thenReturnFalse() {

        String configJson = readStringFromFile("wallet_configs.json");
        WalletConfigsManager manager = new WalletConfigsManager(configJson);
        String expected = new Gson().toJson(manager.getWalletConfigsJson());
        boolean renamed = manager.renameWalletConfig("test", "test2");
        String result = new Gson().toJson(manager.getWalletConfigsJson());

        assertFalse(renamed);
        assertEquals(expected, result);
    }

    @Test
    public void givenExistingAlias_whenRenameWalletConfig_thenReceiveUpdatedWalletConfigs() {
        String expected = readStringFromFile("wallet_configs_rename.json");
        String configJson = readStringFromFile("wallet_configs_create.json");
        WalletConfigsManager manager = new WalletConfigsManager(configJson);
        boolean renamed = manager.renameWalletConfig("FirstWalletName", "NewWalletName");
        String result = new Gson().toJson(manager.getWalletConfigsJson());

        assertTrue(renamed);
        assertEquals(expected, result);
    }

    private String readStringFromFile(String filename) {
        InputStream inputstream = this.getClass().getClassLoader().getResourceAsStream(filename);
        return new BufferedReader(new InputStreamReader(inputstream))
                .lines().collect(Collectors.joining("\n"));
    }
}