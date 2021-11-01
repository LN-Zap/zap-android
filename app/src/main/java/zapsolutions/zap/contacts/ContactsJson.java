package zapsolutions.zap.contacts;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.Set;

public class ContactsJson {

    @SerializedName("contacts")
    Set<Contact> mContacts;

    @SerializedName("version")
    int version;

    public Contact getContactByContactData(@NonNull String contactData) {
        for (Contact contact : mContacts) {
            if (contact.getContactData().equals(contactData)) {
                return contact;
            }
        }
        return null;
    }

    public Contact getContactById(@NonNull String id) {
        for (Contact contact : mContacts) {
            if (contact.getId().equals(id)) {
                return contact;
            }
        }
        return null;
    }

    public Set<Contact> getContacts() {
        return mContacts;
    }

    boolean doesContactExist(@NonNull Contact contact) {
        return mContacts.contains(contact);
    }

    boolean addContact(@NonNull Contact contact) {
        return mContacts.add(contact);
    }

    boolean removeContact(Contact contact) {
        return mContacts.remove(contact);
    }

    boolean renameContact(Contact contact, @NonNull String newAlias) {
        if (doesContactExist(contact)) {
            Contact tempContact = getContactById(contact.getId());
            tempContact.setAlias(newAlias);
            mContacts.remove(contact);
            mContacts.add(tempContact);
            return true;
        } else {
            return false;
        }
    }
}
