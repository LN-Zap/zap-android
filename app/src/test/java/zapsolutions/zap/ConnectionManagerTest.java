package zapsolutions.zap;

import com.google.gson.Gson;
import org.junit.Test;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfig;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfigsJson;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfigsManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.stream.Collectors;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;

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

        assertEquals("FirstWalletName", result.getAlias());
        assertEquals("remote", result.getType());
        assertEquals("TestHost", result.getHost());
        assertEquals(10009, result.getPort());
        assertEquals("TestCert", result.getCert());
        assertEquals("TestMacaroon", result.getMacaroon());
    }

    @Test
    public void givenNewAlias_whenAddWalletConfig_thenReceiveUpdatedWalletConfigs() throws UnsupportedEncodingException {
        WalletConfig expected = readFromFile("wallet_configs.json").getConnection("FirstWalletName");

        WalletConfigsManager manager = new WalletConfigsManager(null);
        manager.addWalletConfig("FirstWalletName", "remote", "TestHost", 10009, "TestCert", "TestMacaroon");
        WalletConfig actual = manager.getWalletConfig("FirstWalletName");

        assertEquals(expected.getAlias(), actual.getAlias());
        assertEquals(expected.getCert(), actual.getCert());
        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.getHost(), actual.getHost());
        assertEquals(expected.getPort(), actual.getPort());
        assertEquals(expected.getMacaroon(), actual.getMacaroon());
    }

    @Test
    public void givenExistingAlias_whenAddWalletConfig_thenReceiveUpdatedWalletConfigs() throws UnsupportedEncodingException {
        WalletConfig expected = readFromFile("wallet_configs_modify.json").getConnection("FirstWalletName");

        String configJson = readStringFromFile("wallet_configs_create.json");
        WalletConfigsManager manager = new WalletConfigsManager(configJson);
        manager.addWalletConfig("FirstWalletName", "remote", "ModifiedHost", 10009, "TestCert", "TestMacaroon");
        WalletConfig actual = manager.getWalletConfig("FirstWalletName");

        assertEquals(expected.getHost(), actual.getHost());
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
        String configJson = readStringFromFile("wallet_configs.json");

        WalletConfigsManager manager = new WalletConfigsManager(configJson);
        boolean removed = manager.removeWalletConfig("secondwalletname");

        assertTrue(removed);
        assertNull(manager.getWalletConfig("SecondWalletName"));
        assertNotNull(manager.getWalletConfig("FirstWalletName"));
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
    public void givenExistingAlias_whenRenameToExitingWalletConfig_thenReturnFalse() {
        String configJson = readStringFromFile("wallet_configs.json");
        WalletConfigsManager manager = new WalletConfigsManager(configJson);
        String expected = new Gson().toJson(manager.getWalletConfigsJson());
        boolean renamed = manager.renameWalletConfig("FirstWalletName", "SecondWalletName");
        String result = new Gson().toJson(manager.getWalletConfigsJson());

        assertFalse(renamed);
        assertEquals(expected, result);
    }

    @Test
    public void givenExistingAlias_whenRenameWalletConfig_thenReceiveUpdatedWalletConfigs() throws UnsupportedEncodingException {
        WalletConfig expected = readFromFile("wallet_configs_rename.json").getConnection("NewWalletName");
        String configJson = readStringFromFile("wallet_configs_create.json");
        WalletConfigsManager manager = new WalletConfigsManager(configJson);
        boolean renamed = manager.renameWalletConfig("FirstWalletName", "NewWalletName");
        WalletConfig actual = manager.getWalletConfig("newWalletName");

        assertTrue(renamed);
        assertNotNull(manager.getWalletConfig("NewWalletName"));
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