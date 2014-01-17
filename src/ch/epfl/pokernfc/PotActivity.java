package ch.epfl.pokernfc;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.nfc.NdefMessage;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import ch.epfl.pokernfc.Logic.Game;
import ch.epfl.pokernfc.Logic.PokerObjects;
import ch.epfl.pokernfc.Logic.Pot;
import ch.epfl.pokernfc.Logic.network.Message;
import ch.epfl.pokernfc.Logic.network.NetworkMessageHandler;
import ch.epfl.pokernfc.Utils.MessageUtils;

public class PotActivity extends PokerActivity {

	private NetworkMessageHandler mMsgHandler = null;
	
	private ImageView card1;
	private ImageView card2;
	private ImageView card3;
	private ImageView card4;
	private ImageView card5;
	private ImageView tempCard;
	private Drawable hiddenCard;



	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pot);
		hiddenCard= getResources().getDrawable(R.drawable.back_card);
		card1 = (ImageView) findViewById(R.id.potCard1);
		card2 = (ImageView) findViewById(R.id.potCard2);
		card3 = (ImageView) findViewById(R.id.potCard3);
		card4 = (ImageView) findViewById(R.id.potCard4);
		card5 = (ImageView) findViewById(R.id.potCard5);


initCard();
		
		// Show the Up button in the action bar.
		setupActionBar();
		
		registerMessageHandler(); //for receiving news from client
		
		System.out.println("PotActivity : onCreate");
		/*
    	 * Manage state of the application.
    	 * If we were in the Player state to Pot state, or the opposite.
    	 */
    	
		PokerState.currentActivityIsPlayer(false);
		
		//init first message to send
		prepareNextWelcomeMessage();
		
	}
	
	private void prepareNextWelcomeMessage() {
		//generate a new id for the next client.
		if (mDataToSendBuffer.isEmpty()) {
			
		int id = PokerObjects.getGame().registerNextPlayerID();
		if (id == -1) {
			System.out.println("FATAL ERROR, next player id = 0");
			id = 0;
		}
		
		//listen to new client
		PokerState.getGameServer().listenToNewPlayer(id);
		
		String ip = PokerState.getGameServer().getServerIP();
		int port = 	PokerState.getGameServer().getServerPort();
		
		mDataToSendBuffer = MessageUtils.createNFCWelcome(ip, port, id);
		
		}
	}
	
	public void onNonNFCClient(View view) {
		if (mDataToSendBuffer.isEmpty()) {
			prepareNextWelcomeMessage();
		}
		Object[] parsed = MessageUtils.parseNFCWelcomeMessage(mDataToSendBuffer);
		int wantedID = (Integer) parsed[2];
		AlertDialog dialog = new AlertDialog.Builder(this).create();
	    dialog.setTitle("Connect to server");
	    dialog.setMessage("Use this id : " + wantedID+"\npot IP: "+PokerState.getGameServer().getServerIP()+"\npot Port: "+PokerState.getGameServer().getServerPort());
	    dialog.setCancelable(false);
	    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int buttonId) {
	        	mDataToSendBuffer = "";
	    	    prepareNextWelcomeMessage();
	        }
	    });
	    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int buttonId) {
	        
	        }
	    });	    dialog.setIcon(android.R.drawable.ic_dialog_alert);
	    dialog.show();
	    
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
		
	}

	/**
	 * For sharing the pot
	 * @param view
	 */
	public void reditributeCash() {
		System.out.println("PotActivity : onShare");
		
		int numberOfPlayer = PokerObjects.getGame().getNumberOfPlayer();
		System.out.println("PotActivity : number of player, got : " + numberOfPlayer);
		if (numberOfPlayer == 0) { return; }
		float capital = PokerObjects.getPot().clear();
		updateUiTextView(R.id.tvValue, String.valueOf(0));
		for (int i = 0; i < numberOfPlayer; ++i) {
			int id = PokerObjects.getGame().getNextPlayerID();
			System.out.println("PotActivity : distribute to player " + id);
			if (!PokerState.getGameServer().sendMessage(id,
					new Message(Message.MessageType.REFUND,
							String.valueOf(Math.floor(capital / numberOfPlayer))))) {
				log("Cannot refund player " + id + " with " + Math.floor(capital / numberOfPlayer) + " chf");
			}
		}
	}
	
	public void onStartGame(View view) {
		runOnUiThread(new Runnable() {
			 @Override
		     public void run() {
			    	
					initCard();
				     }
					});
		Game game = PokerObjects.getGame();
		
		//remove unconnected clients
		List<Integer> connected = new ArrayList<Integer>(PokerState.getGameServer().getConnectedIds());
		List<Integer> registered = new ArrayList<Integer>(game.getRegisteredIds());
		
		for (Integer id : registered) {
			if (!connected.contains(id)) {
				game.revokePlayer(id);
				System.out.println("PotActivity : removed unconnected player " + id);
				PokerState.getGameServer().closeConnection(id);
			} else {
				System.out.println("PotActivity : player " + id + " connected");
			}
		}
		
		game.startGame();
		log("Game started.");
	}
	
	private void log(String text) {
		updateUiTextView(R.id.txtStatusPot, "Status : " + text);
	}
	
	private void registerMessageHandler() {
		if (mMsgHandler == null) {
			mMsgHandler = new NetworkMessageHandler() {
				
				/**
				 * Handle message from Server
				 * @param message
				 */
				@Override
				public void handleMessage(final Message message) {
					
					final Pot pot = PokerObjects.getPot();
					System.out.println("PotActivity : received message " + message);
					switch (message.getType()) {
					case UNKNOWN:
						/*do nothing*/
						break;
					case BID:
						
						final float amount = Float.parseFloat(message.getLoad());
												
						//update view
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								
								TextView tv = (TextView) findViewById(R.id.tvValue);
								
								//avoid asynch problems
								float tot = amount + Float.parseFloat(tv.getText().toString());
								
								tv.setText(String.valueOf(tot));
						
							}
						});
						
						break;
					//next card
					//0 : erase, others, add new card	
					case CARD1:
						runOnUiThread(new Runnable() {
						     @Override
						     public void run() {
								tempCard.setImageDrawable(getResources().getDrawable(getResources().
								getIdentifier("drawable/card_"+message.getLoad(), null,getPackageName())));
								tempCard.setVisibility(View.VISIBLE);
								if(tempCard == card1) tempCard = card2;
								else if(tempCard == card2) tempCard = card3;
								else if(tempCard == card3) tempCard = card4;
								else if(tempCard == card4) tempCard = card5;
						     }
						});
						break;
					case END:
						updateUiTextView(R.id.tvValue, String.valueOf(pot.getCash()));
						log("Game ended.");
						break;
					//remove cash from Pot
					case REFUND:
						updateUiTextView(R.id.tvValue, String.valueOf(pot.getCash()));
						break;
					case ERROR:
						log(message.getLoad());
						break;
					default:
						break;
					}
						
				}
			};
		}
		PokerState.getGameServer().registerNetworkMessageHandler(mMsgHandler);
	}
	
	private void initCard(){
		card1.setVisibility(View.INVISIBLE);
		card2.setVisibility(View.INVISIBLE);
		card3.setVisibility(View.INVISIBLE);
		card4.setVisibility(View.INVISIBLE);
		card5.setVisibility(View.INVISIBLE);
		
		card1.setImageDrawable(hiddenCard);
		card2.setImageDrawable(hiddenCard);
		card3.setImageDrawable(hiddenCard);
		card4.setImageDrawable(hiddenCard);
		card5.setImageDrawable(hiddenCard);

				tempCard = card1;

	}
}
