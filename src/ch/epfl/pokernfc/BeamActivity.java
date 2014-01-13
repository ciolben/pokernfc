package ch.epfl.pokernfc;

import java.util.ArrayList;

import ch.epfl.pokernfc.Utils.NFCMessageReceivedHandler;
import ch.epfl.pokernfc.Utils.NFCUtils;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.app.Activity;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;

public class BeamActivity extends Activity implements CreateNdefMessageCallback, 
OnNdefPushCompleteCallback {

	NfcAdapter mNfcAdapter;
	
	private ArrayList<NFCMessageReceivedHandler> mNFCMsgHandlers
		= new ArrayList<NFCMessageReceivedHandler>();
	
	/***
	 * Buffer sent by NFC.
	 * Once consumed, the buffer is empty.
	 */
	protected String mDataToSendBuffer = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_beam);
		
		System.out.println("BeamActivtiy : OnCreate");

//		mEditText = (EditText) findViewById(R.id.beam_edit_text);

		// Check for available NFC Adapter
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter == null) {
			Toast.makeText(this, "Sorry, NFC is not available on this device", 
					Toast.LENGTH_SHORT).show();
		} else {
			// Register callback to set NDEF message
			mNfcAdapter.setNdefPushMessageCallback(this, this);
			// Register callback to listen for message-sent success
			mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
		}
		
		//in case application is just started
		processIntent(getIntent());
	}

	private static final String MIME_TYPE = "application/ch.epfl.pokernfc";
	private static final String PACKAGE_NAME = "ch.epfl.pokernfc";

	/**
	 * Implementation for the CreateNdefMessageCallback interface
	 */
	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		String text = mDataToSendBuffer;
		System.out.println("BeamActivity : createNDEFMessage with : " + text);
		NdefMessage msg = new NdefMessage(new NdefRecord[] { NFCUtils.createRecord(MIME_TYPE, text.getBytes()),
				NdefRecord.createApplicationRecord(PACKAGE_NAME) });
		return msg;
	}

	private static final int MESSAGE_SENT = 1;

	/** This handler receives a message from onNdefPushComplete */
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_SENT:
//				Toast.makeText(getApplicationContext(), "Message sent!", Toast.LENGTH_LONG).show();
				
				System.out.println("BeamActivity : Message sent, content = " + mDataToSendBuffer);
				// Empty the buffer
				mDataToSendBuffer = "";
				
				break;
			}
		}
	};

	/**
	 * Implementation for the OnNdefPushCompleteCallback interface
	 */
	@Override
	public void onNdefPushComplete(NfcEvent arg0) {
		// A handler is needed to send messages to the activity when this
		// callback occurs, because it happens from a binder thread
		mHandler.obtainMessage(MESSAGE_SENT).sendToTarget();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// onResume gets called after this to handle the intent
		setIntent(intent);
		processIntent(intent);
	}

	@Override
	public void onResume() {
		super.onResume();
		// Check to see that the Activity started due to an Android Beam
		processIntent(getIntent());
	}

	/***
	 * Register a handler for processing the NFC message received.
	 * @param handler
	 */
	public void registerNFCMessageReceivedHandler(NFCMessageReceivedHandler handler) {
		mNFCMsgHandlers.add(handler);
	}
	
	/**
	 * Parses the NDEF Message from the intent and toast to the user
	 */
	void processIntent(Intent intent) {
		if (!NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
			System.out.println("BeamActivity : processIntent (not NFC)");
			return;
		}
		System.out.println("BeamActivity : processIntent (NFC)");
		Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		// in this context, only one message was sent over beam
		NdefMessage msg = (NdefMessage) rawMsgs[0];
		
		// record 0 contains the MIME type, record 1 is the AAR, if present
		String payload = new String(msg.getRecords()[0].getPayload());
		System.out.println("BeamActivity : payload : " + payload);
		
//		Toast.makeText(getApplicationContext(), "Message received over beam: " + payload, Toast.LENGTH_LONG).show();
		for (NFCMessageReceivedHandler handler : mNFCMsgHandlers) {
			handler.handleMessage(payload);
		}
	}

}

