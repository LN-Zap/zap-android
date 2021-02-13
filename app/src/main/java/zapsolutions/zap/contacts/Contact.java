package zapsolutions.zap.contacts;

import androidx.annotation.Nullable;

import java.io.Serializable;

import zapsolutions.zap.lightning.LightningNodeUri;
import zapsolutions.zap.lightning.LightningParser;

public class Contact implements Comparable<Contact>, Serializable {


    private String nodePubKey;
    private String alias;

    public Contact(String nodePubKey, String alias) {
        this.nodePubKey = nodePubKey;
        this.alias = alias;
    }

    public String getNodePubKey() {
        return this.nodePubKey;
    }

    public String getAlias() {
        return this.alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public LightningNodeUri getAsNodeUri() {
        return LightningParser.parseNodeUri(nodePubKey);
    }


    @Override
    public int compareTo(Contact contact) {
        Contact other = contact;
        return this.getAlias().toLowerCase().compareTo(other.getAlias().toLowerCase());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Contact contact = (Contact) obj;
        return contact.getNodePubKey().equals(this.getNodePubKey());
    }

    @Override
    public int hashCode() {
        return this.nodePubKey.hashCode();
    }
}
