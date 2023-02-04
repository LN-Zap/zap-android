package zapsolutions.zap.backup;

import zapsolutions.zap.connection.manageNodeConfigs.ZapNodeConfig;
import zapsolutions.zap.contacts.Contact;

public class DataBackup {
    private ZapNodeConfig[] connections;
    private Contact[] contacts;

    public ZapNodeConfig[] getWalletConfigs() {
        return connections;
    }

    public Contact[] getContacts() {
        return contacts;
    }

    public void setWalletConfigs(ZapNodeConfig[] mZapNodeConfigs) {
        this.connections = mZapNodeConfigs;
    }

    public void setContacts(Contact[] mContacts) {
        this.contacts = mContacts;
    }
}
