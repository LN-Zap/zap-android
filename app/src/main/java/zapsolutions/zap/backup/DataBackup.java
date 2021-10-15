package zapsolutions.zap.backup;

import zapsolutions.zap.connection.manageWalletConfigs.WalletConfig;
import zapsolutions.zap.contacts.Contact;

public class DataBackup {
    private WalletConfig[] connections;
    private Contact[] contacts;

    public WalletConfig[] getWalletConfigs() {
        return connections;
    }

    public Contact[] getContacts() {
        return contacts;
    }

    public void setWalletConfigs(WalletConfig[] mWalletConfigs) {
        this.connections = mWalletConfigs;
    }

    public void setContacts(Contact[] mContacts) {
        this.contacts = mContacts;
    }
}
