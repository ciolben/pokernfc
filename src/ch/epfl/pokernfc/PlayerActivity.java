package ch.epfl.pokernfc;

import ch.epfl.pokernfc.Logic.Player;
import ch.epfl.pokernfc.Logic.PokerObjects;
import ch.epfl.pokernfc.Logic.network.Client;
import ch.epfl.pokernfc.Utils.MessageUtils;
import ch.epfl.pokernfc.Utils.NFCMessageReceivedHandler;
import ch.epfl.pokernfc.Utils.NFCUtils;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;

public class PlayerActivity extends PokerActivity {

	private Player mPlayer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);
		// Show the Up button in the action bar.
		setupActionBar();
		
		mPlayer = PokerObjects.getPlayer();
		
		System.out.println("PlayerActivity : onCreate");
		
    	/*
    	 * Manage state of the application.
    	 * If we were in the Player state to Pot state, or the opposite.
    	 */
    	
    	if (!PokerState.lastActivityWasPlayer()) {
    		//TODO
    	}
		PokerState.currentActivityIsPlayer(true);
		
		// Register the handler for the NFC message received
		super.registerNFCMessageReceivedHandler(new NFCMessageReceivedHandler() {
			
			@Override
			public void handleMessage(String message) {
				Object[] connectionData = MessageUtils.parseNFCWelcomeMessage(message);
				System.out.print("PlayerActivity : handleNFCWelcomeMessage\n");
				if (connectionData != null) {
				
					String ip = (String) 	connectionData[0];
					int port = 	(Integer)	connectionData[1];
					int id = 	(Integer)	connectionData[2];
		
					//affiche le resultat
					System.out.println("--> decoded message : \n"
							+ "[0] \t" + ip
							+ "[1] \t" + port
							+ "[2] \t" + id);
					
					//Start client
					Client client = PokerState.getGameClient();
					if (client != null) {
						//TODO : some cleaning stuff
						//client.stop();
					}
					PokerState.createGameClient(id, ip, port);
					
				} else { System.out.println("--> unknown message"); }
			}});
		
		//Binding
//		bind("Player");
	}
	
	@Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);//must store the new intent unless getIntent() will return the old one
        //TODO
        
        System.out.println("PlayerActivity : onNewIntent");
//        processIntent(intent); not needed because of super.onNewIntent before
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
		getMenuInflater().inflate(R.menu.player, menu);
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
		System.out.println("PLAYER NFC INTENT");
	}

	public void onPayCash(View view) {
		System.out.println("Player : PAY CASH");
		
		NFCUtils.write("Test");
	}
}
