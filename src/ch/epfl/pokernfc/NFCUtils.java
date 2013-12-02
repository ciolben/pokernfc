package ch.epfl.pokernfc;

import java.io.IOException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Parcelable;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class NFCUtils {
    private static final String TAG = "stickynotes";
    private boolean mResumed = false;
    private boolean mWriteMode = false;
    
	private NfcAdapter mNfcAdapter;
	private EditText mNote;

	private PendingIntent mNfcPendingIntent;
	private IntentFilter[] mWriteTagFilters;
	private IntentFilter[] mNdefExchangeFilters;

	
    public void setup(Activity activity) {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(activity);

        // Handle all of our received NFC intents in this activity.
        mNfcPendingIntent = PendingIntent.getActivity(activity, 0,
                new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // Intent filters for reading a note from a tag or exchanging over p2p.
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefDetected.addDataType("text/plain");
        } catch (MalformedMimeTypeException e) { }
        mNdefExchangeFilters = new IntentFilter[] { ndefDetected };

        // Intent filters for writing to a tag
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        mWriteTagFilters = new IntentFilter[] { tagDetected };
    }

    protected void callOnResume(Activity activity) {
        mResumed = true;
        // Sticky notes received from Android
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(activity.getIntent().getAction())) {
            NdefMessage[] messages = getNdefMessages(activity.getIntent());
            byte[] payload = messages[0].getRecords()[0].getPayload();
            activity.setIntent(new Intent()); // Consume this intent.
        }
        enableNdefExchangeMode(activity);
    }

    protected void callOnPause(Activity activity) {
        mResumed = false;
        mNfcAdapter.disableForegroundNdefPush(activity);
    }

    protected void callOnNewIntent(Activity activity, Intent intent) {
        // NDEF exchange mode
        if (!mWriteMode && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            NdefMessage[] msgs = getNdefMessages(intent);
        }

        // Tag writing mode
        if (mWriteMode && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            writeTag(activity, getNoteAsNdef(), detectedTag);
        }
    }

    public void write(Activity activity) {
            // Write to a tag for as long as the dialog is shown.
            disableNdefExchangeMode(activity);
            enableTagWriteMode(activity);

            //TODO (je crois qu'il n'y a pas besoin de faire ça, il faut utiliser l'autre truc set...)
            
        disableTagWriteMode(activity);
        enableNdefExchangeMode(activity);
        
    }

    public String readNDEFmsg (final NdefMessage msg) {
    	
    	return new String(msg.getRecords()[0].getPayload());
    }

    public NdefMessage getNoteAsNdef() {
        byte[] textBytes = mNote.getText().toString().getBytes();
        NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(),
                new byte[] {}, textBytes);
        return new NdefMessage(new NdefRecord[] {
            textRecord
        });
    }

    public NdefMessage[] getNdefMessages(Intent intent) {
        // Parse the intent
        NdefMessage[] msgs = null;
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            } else {
                // Unknown tag type
                byte[] empty = new byte[] {};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[] {
                    record
                });
                msgs = new NdefMessage[] {
                    msg
                };
            }
        } else {
            Log.d(TAG, "Unknown intent.");
        }
        return msgs;
    }

    private void enableNdefExchangeMode(Activity activity) {
        mNfcAdapter.enableForegroundNdefPush(activity, getNoteAsNdef());
        mNfcAdapter.enableForegroundDispatch(activity, mNfcPendingIntent, mNdefExchangeFilters, null);
    }

    private void disableNdefExchangeMode(Activity activity) {
        mNfcAdapter.disableForegroundNdefPush(activity);
        mNfcAdapter.disableForegroundDispatch(activity);
    }

    private void enableTagWriteMode(Activity activity) {
        mWriteMode = true;
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        mWriteTagFilters = new IntentFilter[] {
            tagDetected
        };
        mNfcAdapter.enableForegroundDispatch(activity, mNfcPendingIntent, mWriteTagFilters, null);
    }

    private void disableTagWriteMode(Activity activity) {
        mWriteMode = false;
        mNfcAdapter.disableForegroundDispatch(activity);
    }

    boolean writeTag(Activity activity, NdefMessage message, Tag tag) {
        int size = message.toByteArray().length;

        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();

                if (!ndef.isWritable()) {
                    toast(activity, "Tag is read-only.");
                    return false;
                }
                if (ndef.getMaxSize() < size) {
                    toast(activity, "Tag capacity is " + ndef.getMaxSize() + " bytes, message is " + size
                            + " bytes.");
                    return false;
                }

                ndef.writeNdefMessage(message);
                toast(activity, "Wrote message to pre-formatted tag.");
                return true;
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        toast(activity, "Formatted tag and wrote message");
                        return true;
                    } catch (IOException e) {
                        toast(activity, "Failed to format tag.");
                        return false;
                    }
                } else {
                    toast(activity, "Tag doesn't support NDEF.");
                    return false;
                }
            }
        } catch (Exception e) {
            toast(activity, "Failed to write tag");
        }

        return false;
    }

    private void toast(Activity activity, String text) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
    }
}
