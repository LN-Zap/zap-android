package zapsolutions.zap.backup;

import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;

import zapsolutions.zap.connection.manageWalletConfigs.WalletConfigsManager;
import zapsolutions.zap.contacts.ContactsManager;
import zapsolutions.zap.util.EncryptionUtil;
import zapsolutions.zap.util.RefConstants;

public class DataBackupUtil {
    public static byte[] createBackup(String password) {
        String backupJson = "{";
        // Contacts
        if (ContactsManager.getInstance().hasAnyContacts()) {
            String contactsJsonString = new Gson().toJson(ContactsManager.getInstance().getContactsJson());
            contactsJsonString = contactsJsonString.substring(1, contactsJsonString.length() - 1);
            backupJson = backupJson + contactsJsonString + ",";
        }
        // Wallets
        if (WalletConfigsManager.getInstance().hasAnyConfigs()) {
            String walletsJsonString = new Gson().toJson(WalletConfigsManager.getInstance().getWalletConfigsJson());
            walletsJsonString = walletsJsonString.substring(1, walletsJsonString.length() - 1);
            backupJson = backupJson + walletsJsonString + ",";
        }
        backupJson = backupJson.substring(0, backupJson.length() - 1) + "}";

        // Encrypting the backup.

        // Convert json backup to bytes
        byte[] backupBytes = backupJson.getBytes(StandardCharsets.UTF_8);

        // Encrypt backup
        byte[] encryptedBackupBytes = EncryptionUtil.PasswordEncryptData(backupBytes, password, RefConstants.DATA_BACKUP_NUM_HASH_ITERATIONS);

        // Return final backup as UTF-8 string
        return encryptedBackupBytes;
    }

    public boolean isThereAnythingToBackup() {
        return WalletConfigsManager.getInstance().hasAnyConfigs() || ContactsManager.getInstance().hasAnyContacts();
    }
}
