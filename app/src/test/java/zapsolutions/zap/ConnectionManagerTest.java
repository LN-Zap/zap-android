package zapsolutions.zap;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import zapsolutions.zap.connection.RemoteConfiguration;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfigsManager;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.assertFalse;

public class ConnectionManagerTest {

    @Test
    public void doesWalletExist_NullInput() {
        String connectionJson = readStringFromFile("connection_config.json");
        WalletConfigsManager manager = new WalletConfigsManager("blüb");

        boolean result = manager.doesWalletConfigExist("bla");

        assertFalse(result);
    }

    @Test
    public void doesWalletExist() {
        String connectionJson = readStringFromFile("connection_config.json");
        WalletConfigsManager manager = new WalletConfigsManager(connectionJson);

        boolean result = manager.doesWalletConfigExist("firstwalletname");

        assertTrue(result);
    }

    @Test
    public void loadConnection() {
        String connectionJson = readStringFromFile("connection_config.json");
        WalletConfigsManager manager = new WalletConfigsManager(connectionJson);

        RemoteConfiguration result = manager.loadWalletConfig("firstwalletname");

        assertTrue(result.getHost().equals("firstwalletname"));
    }

    private String readStringFromFile(String filename) {
        InputStream inputstream = this.getClass().getClassLoader().getResourceAsStream(filename);
        return new BufferedReader(new InputStreamReader(inputstream))
                .lines().collect(Collectors.joining("\n"));
    }
}