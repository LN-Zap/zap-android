package zapsolutions.zap.contacts;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import zapsolutions.zap.R;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.Wallet;

/**
 * This SINGLETON class is used to load and save contacts.
 * <p>
 * The contacts are stored encrypted in the default shared preferences.
 */
public class ContactsManager {

    private static final String LOG_TAG = ContactsManager.class.getName();
    private static ContactsManager mInstance;
    private ContactsJson mContactsJson;

    private ContactsManager() {

        String decrypted = null;
        try {
            decrypted = PrefsUtil.getEncryptedPrefs().getString(PrefsUtil.CONTACTS, "");
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        if (isValidJson(decrypted)) {
            mContactsJson = new Gson().fromJson(decrypted, ContactsJson.class);
        } else {
            mContactsJson = createEmptyContactsJson();
        }

        if (mContactsJson == null) {
            mContactsJson = createEmptyContactsJson();
        }
    }

    // used for unit tests
    public ContactsManager(String contactsJson) {
        try {
            mContactsJson = new Gson().fromJson(contactsJson, ContactsJson.class);
        } catch (JsonSyntaxException e) {
            mContactsJson = createEmptyContactsJson();
        }
        if (mContactsJson == null) {
            mContactsJson = createEmptyContactsJson();
        }
    }

    public static ContactsManager getInstance() {
        if (mInstance == null) {
            mInstance = new ContactsManager();
        }
        return mInstance;
    }

    /**
     * Used to determine if the provided String is a valid Contact JSON.
     *
     * @param contactsString parses as JSON
     * @return if the JSON syntax is valid
     */
    private static boolean isValidJson(String contactsString) {
        try {
            ContactsJson contacts = new Gson().fromJson(contactsString, ContactsJson.class);
            return contacts != null;
        } catch (JsonSyntaxException ex) {
            return false;
        }
    }

    public ContactsJson getContactsJson() {
        return mContactsJson;
    }

    private ContactsJson createEmptyContactsJson() {
        return new Gson().fromJson("{\"contacts\":[]}", ContactsJson.class);
    }

    /**
     * Checks if a contact already exists.
     *
     * @param contact
     * @return
     */
    public boolean doesContactExist(@NonNull Contact contact) {
        return mContactsJson.doesContactExist(contact);
    }

    /**
     * Checks if a contact already exists.
     *
     * @param nodePubKey
     * @return
     */
    public boolean doesContactExist(@NonNull String nodePubKey) {
        Contact tempContact = new Contact(nodePubKey, null);
        return doesContactExist(tempContact);
    }


    /**
     * Adds a contact.
     * Do not forget to call apply() afterwards to make this change permanent.
     *
     * @param alias Name of the contact
     */
    public Contact addContact(@NonNull String nodePubKey, @NonNull String alias) {

        // Create the config
        Contact contact = new Contact(nodePubKey, alias);

        // Add the config to our configurations array
        boolean contactAdded = mContactsJson.addContact(contact);

        if (contactAdded) {
            return contact;
        } else {
            return null;
        }
    }

    /**
     * Load a Contact by its node public key.
     *
     * @param nodePubKey pubkey of the contact
     * @return Returns null if no contact is found
     */
    public Contact getContactByNodePubKey(@NonNull String nodePubKey) {
        return mContactsJson.getContactByNodePubKey(nodePubKey);
    }

    /**
     * Returns the contact alias for a given node pubkey. Returns the node pubkey if no contact with that key exists.
     *
     * @param nodePubkey pubkey of the contact
     * @return Name if it exist, pubkey if it doesn exist
     */
    public String getNameByNodePubKey(@NonNull String nodePubkey) {
        Contact contact = getContactByNodePubKey(nodePubkey);
        if (contact != null) {
            return contact.getAlias();
        } else {
            return nodePubkey;
        }
    }

    /**
     * Returns a List of all contacts sorted alphabetically.
     *
     * @return
     */
    public List<Contact> getAllContacts() {
        List<Contact> sortedList = new ArrayList<>();
        sortedList.addAll(mContactsJson.getContacts());

        if (sortedList.size() > 1) {
            // Sort the list alphabetically
            Collections.sort(sortedList);
        }
        return sortedList;
    }


    /**
     * Renames the desired contact.
     * Do not forget to call apply() afterwards to make this change permanent.
     *
     * @param contact  The contact that should be renamed.
     * @param newAlias The new alias
     * @return false if the old alias did not exist.
     */
    public boolean renameContact(@NonNull Contact contact, @NonNull String newAlias) {
        return mContactsJson.renameContact(contact, newAlias);
    }

    /**
     * Removes the desired contact.
     * Do not forget to call apply() afterwards to make this change permanent.
     *
     * @param contact
     */
    public boolean removeContact(@NonNull Contact contact) {
        return mContactsJson.removeContact(contact);
    }


    public boolean hasAnyContacts() {
        return !mContactsJson.getContacts().isEmpty();
    }

    /**
     * Removes all contacts.
     * Do not forget to call apply() afterwards to make this change permanent.
     */
    public void removeAllContacts() {
        mContactsJson = createEmptyContactsJson();
    }


    /**
     * Displays a Name input dialog for the contact.
     * If the nodePubKey is not yet in your contacts confirming this dialog will create it and call apply() afterwards.
     * If the nodePubKey is already in your contacts, confirming this dialog will rename it and call apply() afterwards.
     *
     * @param ctx        Context
     * @param nodePubKey pubKey of the contact
     * @param listener   used to get a callback when name input finished. Can be null.
     */
    public void showContactNameInputDialog(Context ctx, @NonNull String nodePubKey, @Nullable OnNameConfirmedListener listener) {
        InputMethodManager inputMethodManager = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        AlertDialog.Builder adb = new AlertDialog.Builder(ctx);
        adb.setTitle(R.string.contact_name);
        adb.setCancelable(false);
        View viewInflated = LayoutInflater.from(ctx).inflate(R.layout.dialog_input_text, null, false);

        ContactsManager cm = ContactsManager.getInstance();

        final EditText input = viewInflated.findViewById(R.id.input);
        if (cm.doesContactExist(nodePubKey)) {
            input.setText(cm.getContactByNodePubKey(nodePubKey).getAlias());
        } else {
            String nodeAlias = Wallet.getInstance().getNodeAliasFromPubKey(nodePubKey, ctx);
            if (nodeAlias != ctx.getResources().getString(R.string.channel_no_alias)) {
                input.setText(nodeAlias);
            }
        }
        input.setShowSoftInputOnFocus(true);
        input.requestFocus();

        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        adb.setView(viewInflated);

        adb.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // This gets overridden below.
                // We need to do this to validate the input without closing the dialog.
            }
        });
        adb.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                if (listener != null) {
                    listener.onCancelled();
                }
                dialog.cancel();
            }
        });

        AlertDialog dialog = adb.create();
        dialog.show();
        Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (input.getText().toString().trim().isEmpty()) {
                    Toast.makeText(ctx, R.string.error_empty_wallet_name, Toast.LENGTH_LONG).show();
                } else {
                    if (cm.doesContactExist(nodePubKey)) {
                        cm.renameContact(cm.getContactByNodePubKey(nodePubKey), input.getText().toString());
                    } else {
                        cm.addContact(nodePubKey, input.getText().toString());
                    }
                    try {
                        cm.apply();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    if (listener != null) {
                        listener.onNameAccepted();
                    }
                    dialog.dismiss();
                }
            }
        });
    }


    /**
     * Saves the current state of all contacts encrypted to default shared preferences.
     * Always use this after you have changed anything on the configurations.
     *
     * @throws IOException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws UnrecoverableEntryException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchPaddingException
     * @throws NoSuchProviderException
     * @throws BadPaddingException
     * @throws KeyStoreException
     * @throws IllegalBlockSizeException
     */
    public void apply() throws IOException, CertificateException, NoSuchAlgorithmException, InvalidKeyException, UnrecoverableEntryException, InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchProviderException, BadPaddingException, KeyStoreException, IllegalBlockSizeException {
        // Convert JSON object to string
        String jsonString = new Gson().toJson(mContactsJson);

        // Save the new Contact configuration in encrypted prefs
        try {
            PrefsUtil.editEncryptedPrefs().putString(PrefsUtil.CONTACTS, jsonString).commit();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    public interface OnNameConfirmedListener {
        void onNameAccepted();

        void onCancelled();
    }
}
