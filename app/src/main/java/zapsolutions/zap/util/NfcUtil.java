package zapsolutions.zap.util;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.Ndef;
import android.os.Parcelable;
import android.widget.Toast;

import java.util.Arrays;

import zapsolutions.zap.R;

public class NfcUtil {

    private static final String LOG_TAG = NfcUtil.class.getName();

    /**
     * This function reads the content of an NFC Ndef Tag.
     * As we want to read all NFC Tags while zap is open and a specific Activities (done with ForegroundDispatch),
     * we have to make sure that we validate the tag type properly in this function.
     * The filters from the manifest only apply to a tag that is read while the app is not in foreground
     * or at an activity that does not do any foreground dispatching.
     *
     * @param ctx      Context.
     * @param intent   The Intent
     * @param listener listener which provides the payload in its onSuccess function
     */
    public static void readTag(Context ctx, Intent intent, OnNfcResponseListener listener) {
        String action = intent.getAction();
        ZapLog.debug(LOG_TAG, "onNewIntent: " + action);
        if (action != null) {
            if (action.equals(NfcAdapter.ACTION_TAG_DISCOVERED) || action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {

                Parcelable[] rawMessages =
                        intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                if (rawMessages != null) {
                    NdefMessage[] messages = new NdefMessage[rawMessages.length];
                    for (int i = 0; i < rawMessages.length; i++) {
                        messages[i] = (NdefMessage) rawMessages[i];
                    }
                    // Process the messages array.
                    NdefMessage message = messages[0];

                    ZapLog.debug(LOG_TAG, "Ndef message: " + message);
                    NdefRecord[] records = message.getRecords();
                    ZapLog.debug(LOG_TAG, "Ndef record: " + records[0]);
                    if (records[0].getTnf() == NdefRecord.TNF_WELL_KNOWN) {
                        if (Arrays.equals(records[0].getType(), NdefRecord.RTD_URI)) {
                            byte[] rawPayload = records[0].getPayload();
                            StringBuilder sb = new StringBuilder();
                            for (int i = 1; i < rawPayload.length; i++) {
                                sb.append((char) rawPayload[i]);
                            }
                            String payload = sb.toString();
                            ZapLog.debug(LOG_TAG, "Ndef payload: " + payload);
                            if (PrefsUtil.isWalletSetup()) {
                                listener.onSuccess(payload);
                            } else {
                                ZapLog.debug(LOG_TAG, "Wallet not setup.");
                                Toast.makeText(ctx, R.string.demo_setupWalletFirst, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            ZapLog.debug(LOG_TAG, "This NdefRecord is not supported");
                            showNotSupportedToast(ctx);
                        }
                    } else {
                        ZapLog.debug(LOG_TAG, "This NdefRecord type name field (TNF) is not supported");
                        showNotSupportedToast(ctx);
                    }

                } else {
                    ZapLog.debug(LOG_TAG, "Tag message is empty");
                    showNotSupportedToast(ctx);
                }
            }
        }
    }

    public static IntentFilter[] IntentFilters() {
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        return new IntentFilter[]{techDetected, tagDetected, ndefDetected};
    }

    public static String[][] TechFilters() {
        return new String[][]{new String[]{Ndef.class.getName()}};
    }

    private static void showNotSupportedToast(Context ctx) {
        Toast.makeText(ctx, R.string.nfc_type_not_supported, Toast.LENGTH_SHORT).show();
    }

    public interface OnNfcResponseListener {
        void onSuccess(String payload);
    }
}
