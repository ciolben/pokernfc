package ch.epfl.pokernfc;

import ch.epfl.pokernfc.Logic.PokerObjects;
import ch.epfl.pokernfc.Logic.Pot;
import ch.epfl.pokernfc.Utils.MessageUtils;
import ch.epfl.pokernfc.Utils.NFCUtils;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;

public class PotActivity extends PokerActivity {

	private Pot mPot;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pot);
		// Show the Up button in the action bar.
		setupActionBar();
		
		mPot = PokerObjects.getPot();
		
		System.out.println("PotActivity : onCreate");
		/*
    	 * Manage state of the application.
    	 * If we were in the Player state to Pot state, or the opposite.
    	 */
    	
    	if (PokerState.lastActivityWasPlayer()) {
    		//TODO
    	}
		PokerState.currentActivityIsPlayer(false);
		
		//init first message to send
		prepareNextWelcomeMessage();
		
		//binding
//		bind("Pot");
	}
	
	private void prepareNextWelcomeMessage() {
		//generate a new id for the next client.
		if (mDataToSendBuffer.isEmpty()) {
			
		int id = PokerObjects.getGame().registerNextPlayerID();
		if (id == -1) {
			System.out.println("FATAL ERROR, next player id = 0");
			id = 0;
		}
		PokerState.getGameServer().listenToNewPlayer(id);
		
		String ip = PokerState.getGameServer().getServerIP();
		int port = 	PokerState.getGameServer().getServerPort();
		
		mDataToSendBuffer = MessageUtils.createNFCWelcome(ip, port, id);
		
		}
	}
	
	/**
	 * Ensure that the buffer is not empty.
	 */
	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		
			prepareNextWelcomeMessage();
			
			System.out.println("PotActivity : createNdefMessage");
		
		System.out.println("PotActivity : Super::createNdefMessage");
		return super.createNdefMessage(event);
	}
	
	@Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);//must store the new intent unless getIntent() will return the old one
        //TODO
        
        System.out.println("PotActivity : onNewIntent");
    }
	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.pot, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onNFCIntent(String nfcContent) {
		System.out.println("POT NFC INTENT");
		
		((TextView) findViewById(R.id.extraLabel)).setText(nfcContent);
	}

}
