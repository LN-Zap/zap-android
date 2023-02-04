package zapsolutions.zap.backup;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import zapsolutions.zap.connection.BaseNodeConfig;
import zapsolutions.zap.connection.manageNodeConfigs.NodeConfigsManager;
import zapsolutions.zap.connection.manageNodeConfigs.ZapNodeConfig;
import zapsolutions.zap.contacts.Contact;
import zapsolutions.zap.contacts.ContactsManager;
import zapsolutions.zap.util.EncryptionUtil;
import zapsolutions.zap.util.RefConstants;
import zapsolutions.zap.util.UtilFunctions;

public class DataBackupUtil {

    public static final String BACKUP_FILE_IDENTIFIER = "ZapBackup:";

    public static byte[] createBackup(String password, int backupVersion) {
        String backupJson = "{";
        // Contacts
        if (ContactsManager.getInstance().hasAnyContacts()) {
            String contactsJsonString = new Gson().toJson(ContactsManager.getInstance().getContactsJson());
            contactsJsonString = contactsJsonString.substring(1, contactsJsonString.length() - 1);
            backupJson = backupJson + contactsJsonString + ",";
        }
        // Wallets
        if (NodeConfigsManager.getInstance().hasAnyConfigs()) {
            String walletsJsonString = new Gson().toJson(NodeConfigsManager.getInstance().getNodeConfigsJson());
            walletsJsonString = walletsJsonString.substring(1, walletsJsonString.length() - 1);
            backupJson = backupJson + walletsJsonString + ",";
        }
        backupJson = backupJson.substring(0, backupJson.length() - 1) + "}";


        // Encrypting the backup.

        // Convert json backup to bytes
        byte[] backupBytes = backupJson.getBytes(StandardCharsets.UTF_8);

        // Encrypt backup
        byte[] encryptedBackupBytes = EncryptionUtil.PasswordEncryptData(backupBytes, password, RefConstants.DATA_BACKUP_NUM_HASH_ITERATIONS);

        // Construct final backup. (10 bytes file identifier + 4 bytes backupVersion + encrypted backup)
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(BACKUP_FILE_IDENTIFIER.getBytes(StandardCharsets.UTF_8));
            outputStream.write(UtilFunctions.intToByteArray(backupVersion));
            outputStream.write(encryptedBackupBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Return final backup as UTF-8 string
        return outputStream.toByteArray();
    }

    public static boolean isThereAnythingToBackup() {
        return NodeConfigsManager.getInstance().hasAnyConfigs() || ContactsManager.getInstance().hasAnyContacts();
    }

    public static boolean restoreBackup(String backup, int backupVersion) {

        if (backupVersion < 2) {
            DataBackup dataBackup = new Gson().fromJson(backup, DataBackup.class);

            // restore wallets
            if (dataBackup.getWalletConfigs() != null && dataBackup.getWalletConfigs().length > 0) {
                NodeConfigsManager.getInstance().removeAllNodeConfigs();
                for (ZapNodeConfig zapNodeConfig : dataBackup.getWalletConfigs()) {
                    try {
                        NodeConfigsManager.getInstance().addNodeConfig(zapNodeConfig.getAlias(), zapNodeConfig.getType(), zapNodeConfig.getImplementation(), zapNodeConfig.getHost(), zapNodeConfig.getPort(), zapNodeConfig.getCert(), zapNodeConfig.getMacaroon(), zapNodeConfig.getUseTor(), zapNodeConfig.getVerifyCertificate());
                        NodeConfigsManager.getInstance().apply();
                    } catch (GeneralSecurityException | IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            }

            if (backupVersion == 0) {
                // This is an old backup that did not contain info for implementation, useTor & verify certificate. Apply defaults.
                List<ZapNodeConfig> nodeConfigs = NodeConfigsManager.getInstance().getAllNodeConfigs(false);
                for (ZapNodeConfig nodeConfig : nodeConfigs) {
                    // Adds the defaults to the newly introduced node properties.
                    NodeConfigsManager.getInstance().updateNodeConfig(nodeConfig.getId(), nodeConfig.getAlias(), BaseNodeConfig.NODE_IMPLEMENTATION_LND, nodeConfig.getType(), nodeConfig.getHost(), nodeConfig.getPort(), nodeConfig.getCert(), nodeConfig.getMacaroon(), nodeConfig.isTorHostAddress(), !nodeConfig.isTorHostAddress());
                    try {
                        NodeConfigsManager.getInstance().apply();
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            // restore contacts
            if (dataBackup.getContacts() != null && dataBackup.getContacts().length > 0) {
                ContactsManager.getInstance().removeAllContacts();
                for (Contact contact : dataBackup.getContacts()) {
                    try {
                        ContactsManager.getInstance().addContact(contact.getContactType(), contact.getContactData(), contact.getAlias());
                        ContactsManager.getInstance().apply();
                    } catch (IOException | CertificateException | NoSuchAlgorithmException | InvalidKeyException | UnrecoverableEntryException | InvalidAlgorithmParameterException | NoSuchPaddingException | NoSuchProviderException | BadPaddingException | KeyStoreException | IllegalBlockSizeException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }
}
