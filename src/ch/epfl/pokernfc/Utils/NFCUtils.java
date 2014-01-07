package ch.epfl.pokernfc.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class NFCUtils {
    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NFC";
    public static final String NFC_EXTRA_TAG = "NFCTag";

    private static boolean mResumed = false;
    private static boolean mWriteMode = false;
    
	private static NfcAdapter mNfcAdapter;
	private static EditText mNote;

	private static PendingIntent mNfcPendingIntent;
	private static IntentFilter[] mWriteTagFilters;
	private static IntentFilter[] mNdefExchangeFilters;

	//for exchanging messages
	private static Tag lastDetectedTag = null;
	private static String messageToSend = null;
	
    public static void setup(Activity activity) {
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

    public static void callOnResume(Activity activity) {
        mResumed = true;
        // Sticky notes received from Android
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(activity.getIntent().getAction())) {
            NdefMessage[] messages = getNdefMessages(activity.getIntent());
            byte[] payload = messages[0].getRecords()[0].getPayload();
            activity.setIntent(new Intent()); // Consume this intent.
        }
        enableNdefExchangeMode(activity);
    }

    public static void callOnPause(Activity activity) {
        mResumed = false;
        mNfcAdapter.disableForegroundNdefPush(activity);
    }

    /**
     * From a intent received for a NFC event, this method create an intent for a service with
     * the content of the NFC tag.
     * The NFC type must be NDEF, else it will return NULL.
     * @param intent The intent with NFC info.
     * @param appContext The application context.
     * @param service The destination service class.
     * @return the intent for the service, or null if not of NDEF type.
     */
    public static Intent reforgeIntentForService(Intent intent, Context appContext, Class<?> service) {
    	Intent outIntent = null;
    	String action = intent.getAction();
    	System.out.println(":::reforge:::");
    	//we must read the message
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
        	System.out.println("NDEF");
            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                lastDetectedTag = tag;
                outIntent = new Intent(appContext, service);
                outIntent.putExtra(NFC_EXTRA_TAG, tag);
                write(messageToSend);
                return outIntent;
            } else {
                Log.d(TAG, "Wrong mime type: " + type);
            }
        //if tech is discovered instead
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
        	System.out.println("TECH");
            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            lastDetectedTag = tag;
            String[] techList = lastDetectedTag.getTechList();
            String searchedTech = Ndef.class.getName();
            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                	outIntent = new Intent(appContext, service);
                    outIntent.putExtra(NFC_EXTRA_TAG, tag);
                    write(messageToSend);
                    return outIntent;
                }
            }
        }
        
        System.out.println("NOT FOUND");
        return outIntent;
        
    }
    
    public static String callOnNewIntent(boolean writeMode, Intent intent, Activity activity, String message) {
        // NDEF exchange mode
        if (!writeMode && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            NdefMessage[] msgs = getNdefMessages(intent);
            return readNDEFmsg(msgs[0]);
        }

        // Tag writing mode
        if (writeMode && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            writeTag(activity, getMessageAsNdef(message), detectedTag);
        }
        
        return null;
    }

    public void write(Activity activity) {
            // Write to a tag for as long as the dialog is shown.
            disableNdefExchangeMode(activity);
            enableTagWriteMode(activity);

            //TODO (je crois qu'il n'y a pas besoin de faire ça, il faut utiliser l'autre truc set...)
            
        disableTagWriteMode(activity);
        enableNdefExchangeMode(activity);
        
    }

    public static String readNDEFmsg (final NdefMessage msg) {
    	return new String(msg.getRecords()[0].getPayload());
    }

    public static NdefMessage getMessageAsNdef(String message) {
        byte[] textBytes = message.getBytes();
        NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(),
                new byte[] {}, textBytes);
        return new NdefMessage(new NdefRecord[] {
            textRecord
        });
    }

    /**
     * Asynchronously read a tag from an intent.
     * @param tag
     */
    public static void getNDEFMessage(Intent intent, final AsynchHandler<String> handler ) {
    	class NdefReaderTask extends AsyncTask<Tag, Void, String> {
            @Override
            protected String doInBackground(Tag... params) {
                Tag tag = params[0];
                Ndef ndef = Ndef.get(tag);
                if (ndef == null) {
                    // NDEF is not supported by this Tag.
                    return null;
                }
                NdefMessage ndefMessage = ndef.getCachedNdefMessage();
                NdefRecord[] records = ndefMessage.getRecords();
                for (NdefRecord ndefRecord : records) {
                    if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                        try {
                            return readText(ndefRecord);
                        } catch (UnsupportedEncodingException e) {
                            Log.e(TAG, "Unsupported Encoding", e);
                        }
                    }
                }
                return null;
            }
            private String readText(NdefRecord record) throws UnsupportedEncodingException {
                /*
                 * See NFC forum specification for "Text Record Type Definition" at 3.2.1
                 *
                 * http://www.nfc-forum.org/specs/
                 *
                 * bit_7 defines encoding
                 * bit_6 reserved for future use, must be 0
                 * bit_5..0 length of IANA language code
                 */
                byte[] payload = record.getPayload();
                // Get the Text Encoding
                String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
                // Get the Language Code
                int languageCodeLength = payload[0] & 0063;
                // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
                // e.g. "en"
                // Get the Text
                return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
            }
            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    handler.resultReady(result);
                }
            }
        };
    	
        //execute task

    	System.out.println("INTENT decoding");
    	
        if (intent.getExtras() == null) {
        	System.out.println("-> extra : null");
        	return;
        }
        Tag tag = intent.getExtras().getParcelable(NFC_EXTRA_TAG);
        NdefReaderTask task = new NdefReaderTask();
        task.execute(tag);
    }
    
    public static NdefMessage[] getNdefMessages(Intent intent) {
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

    private static void enableNdefExchangeMode(Activity activity) {
        mNfcAdapter.enableForegroundNdefPush(activity, getMessageAsNdef(""));
        mNfcAdapter.enableForegroundDispatch(activity, mNfcPendingIntent, mNdefExchangeFilters, null);
    }

    private static void disableNdefExchangeMode(Activity activity) {
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

    //for the report
    //http://www.framentos.com/en/android-tutorial/2012/07/31/write-hello-world-into-a-nfc-tag-with-a/
    /**
     * Write a NFC message with Mifare classic.
     * @param text
     * @throws IOException
     * @throws FormatException
     */
    public static void write(String text) {
    	messageToSend = text; //act as a buffer
    	if (lastDetectedTag == null) { return; }
    	
    	try {
	        NdefRecord[] records = { createRecord(messageToSend) };
	        messageToSend = null;
	        
	        NdefMessage message = new NdefMessage(records); 
	        Ndef ndef = Ndef.get(lastDetectedTag);
	        ndef.connect();
	        ndef.writeNdefMessage(message);
	        ndef.close();
    	} catch (IOException ioe) {
    		System.err.println("IOE : " + ioe.getMessage());
    	} catch (FormatException fex) {
    		System.err.println("FEX : " + fex.getMessage());
    	}
    }
    // |
    private static NdefRecord createRecord(String text) throws UnsupportedEncodingException {

        //create the message in according with the standard
        String lang = "en";
        byte[] textBytes = text.getBytes();
        byte[] langBytes = lang.getBytes("US-ASCII");
        int langLength = langBytes.length;
        int textLength = textBytes.length;

        byte[] payload = new byte[1 + langLength + textLength];
        payload[0] = (byte) langLength;

        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1, langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
        return recordNFC;
   }
    
    //other implementation
    //http://www.tapwise.com/svn/nfcwritetag/trunk/src/com/tapwise/nfcwritetag/MainActivity.java
    public static boolean writeTag(Activity activity, NdefMessage message, Tag tag) {
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

    private static void toast(Activity activity, String text) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
    }
}
