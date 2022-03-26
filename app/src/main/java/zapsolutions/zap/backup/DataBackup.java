package zapsolutions.zap.backup;

import zapsolutions.zap.connection.manageNodeConfigs.NodeConfig;
import zapsolutions.zap.contacts.Contact;

public class DataBackup {
    private NodeConfig[] connections;
    private Contact[] contacts;

    public NodeConfig[] getWalletConfigs() {
        return connections;
    }

    public Contact[] getContacts() {
        return contacts;
    }

    public void setWalletConfigs(NodeConfig[] mNodeConfigs) {
        this.connections = mNodeConfigs;
    }

    public void setContacts(Contact[] mContacts) {
        this.contacts = mContacts;
    }
}
