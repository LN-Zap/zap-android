package zapsolutions.zap;

import com.google.gson.Gson;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import zapsolutions.zap.connection.manageWalletConfigs.WalletConfig;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfigsJson;
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
    public void givenAliasNull_whenDoesWalletExist_thenReturnFalse() {
        String configJson = readStringFromFile("wallet_configs.json");
        WalletConfigsManager manager = new WalletConfigsManager(configJson);

        boolean result = manager.doesWalletConfigExist(null);

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
        WalletConfig result = manager.loadWalletConfig("firstwalletname");

        assertNull(result);
    }

    @Test
    public void givenAliasNull_whenLoadWalletConfig_thenReturnNull() {
        String configJson = readStringFromFile("wallet_configs.json");

        WalletConfigsManager manager = new WalletConfigsManager(configJson);

        WalletConfig result = manager.loadWalletConfig(null);

        assertNull(result);
    }

    @Test
    public void givenNonExistingAlias_whenLoadWalletConfig_thenReturnNull() {
        String configJson = readStringFromFile("wallet_configs.json");

        WalletConfigsManager manager = new WalletConfigsManager(configJson);

        WalletConfig result = manager.loadWalletConfig("test");

        assertNull(result);
    }

    @Test
    public void givenExistingAlias_whenLoadWalletConfig_thenReceiveCorrectWalletConfig() {
        String configJson = readStringFromFile("wallet_configs.json");

        WalletConfigsManager manager = new WalletConfigsManager(configJson);

        WalletConfig result = manager.loadWalletConfig("firstwalletname");

        assertEquals("firstwalletname", result.getAlias().toLowerCase());
        assertEquals("remote", result.getType());
        assertEquals("TestHost", result.getHost());
        assertEquals(10009, result.getPort());
        assertEquals("TestCert", result.getCert());
        assertEquals("TestMacaroon", result.getMacaroon());
    }

    @Test
    public void givenNewAliasAnd_whenCreateWalletConfigJsonString_thenReceiveCorrectWalletConfigString() {
        String expected = readStringFromFile("wallet_configs_create.json");

        WalletConfigsManager manager = new WalletConfigsManager(null);

        WalletConfigsJson resultJson = manager.createWalletConfigJson("TestName", "remote", "TestHost", 10009, "TestCert", "TestMacaroon");

        String result = new Gson().toJson(resultJson);

        assertEquals(expected, result);
    }

    @Test
    public void givenExistingAlias_whenCreateWalletConfigJsonString_thenReceiveUpdatedWalletConfig() {
        String expected = readStringFromFile("wallet_configs_modify.json");
        String configJson = readStringFromFile("wallet_configs_create.json");

        WalletConfigsManager manager = new WalletConfigsManager(configJson);

        WalletConfigsJson resultJson = manager.createWalletConfigJson("TestName", "remote", "ModifiedHost", 10009, "TestCert", "TestMacaroon");

        String result = new Gson().toJson(resultJson);

        assertEquals(expected, result);
    }

    private String readStringFromFile(String filename) {
        InputStream inputstream = this.getClass().getClassLoader().getResourceAsStream(filename);
        return new BufferedReader(new InputStreamReader(inputstream))
                .lines().collect(Collectors.joining("\n"));
    }
}