package ch.epfl.pokernfc;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import ch.epfl.pokernfc.Logic.PokerObjects;
import ch.epfl.pokernfc.Logic.VirtualPlayer;
import ch.epfl.pokernfc.Logic.network.Client;
import ch.epfl.pokernfc.Logic.network.Message;
import ch.epfl.pokernfc.Logic.network.NetworkMessageHandler;
import ch.epfl.pokernfc.Utils.MessageUtils;
import ch.epfl.pokernfc.Utils.NFCMessageReceivedHandler;

public class PlayerActivity extends PokerActivity {

	private NetworkMessageHandler mMsgHandler;
	private ImageView card1;
	private ImageView card2;
	private Drawable hiddenCard;
	private Drawable realCard1;
	private Drawable realCard2;
	private Boolean cardVisible = false;
	private long longpressMs = 2000;
	private View bidPickerdialog;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);
		hiddenCard= getResources().getDrawable(R.drawable.back_card);
		realCard1 = realCard2 = hiddenCard;
		card1 = (ImageView) findViewById(R.id.playerCard1);
		card1.setVisibility(View.INVISIBLE);
		card1.setImageDrawable(hiddenCard);
		card2 = (ImageView) findViewById(R.id.playerCard2);
		card2.setImageDrawable(hiddenCard);
		card2.setVisibility(View.INVISIBLE);
		
		
		//handle nfc not available
		if (mNfcAdapter == null) {
			Toast.makeText(this, "Sorry, NFC is not available on this device", 
					Toast.LENGTH_SHORT).show();
			
			
			
		
			// Set an EditText view to get user input 			
			LayoutInflater inflater = (LayoutInflater)
				    getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View connectionSettings    = inflater.inflate(R.layout.connection_dialog, null);
			connectionSettings.setBackgroundColor(Color.BLACK);
			
			new AlertDialog.Builder(this)
		    .setTitle("Connection:")
		    .setView(connectionSettings)
		    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
	               String id = ((EditText) connectionSettings.findViewById(R.id.player_id)).getText().toString();
	               String ip = ((EditText) connectionSettings.findViewById(R.id.pot_ip)).getText().toString();
	               String port = ((EditText) connectionSettings.findViewById(R.id.pot_port)).getText().toString();

		        	PokerState.createGameClient(Integer.valueOf(id), ip, Integer.valueOf(port));
					
					registerMessageHandler();
		            
		        }
		    }).show();

			
			
		}
		
		
		
		
		
		
		
		LinearLayout card = (LinearLayout) findViewById(R.id.player_card_layout);
		card.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(!card1.isShown()){
					return true;
				}
				if(event.getAction() == android.view.MotionEvent.ACTION_DOWN){
			    	if(!cardVisible){//redundant check enforced to avoid potential bugs
			    		HideShowCards();
			    	}
        			
			    	
			    	return true;
			    }
				  else if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
				    	if(longpressMs > (event.getEventTime() - event.getDownTime()) && cardVisible){
				    		HideShowCards();
				    	} else {
				    		//longpress don't do anything: keep showing cards
	            			Toast.makeText(getApplicationContext(), "Card view locked, tap to hide", Toast.LENGTH_LONG).show();

				    	}
				    } 
				  return true;
			}
			
		});
	
		
		// Show the Up button in the action bar.
		setupActionBar();

		

		System.out.println("PlayerActivity : onCreate");

		PokerObjects.getPlayer().addCash(1000.f);
		
    	/*
    	 * Manage state of the application.
    	 * If we were in the Player state to Pot state, or the opposite.
    	 */
    	

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
					
					PokerState.createGameClient(id, ip, port);
					
					registerMessageHandler(); //for receiving news from server
					
				} else { System.out.println("--> unknown message"); }
			}});
		
	}
	
	@Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);//must store the new intent unless getIntent() will return the old one
        
        System.out.println("PlayerActivity : onNewIntent");
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

		log("I've bid 10.-");
		//Credit the Pot by 10 chf
		Client client = PokerState.getGameClient();
		if ((client != null) && client.sendMessage(new Message(Message.MessageType.BID, String.valueOf(10)))) {
			PokerObjects.getPlayer().removeCash(10.f);
			((TextView) findViewById(R.id.tvCashValue)).setText(String.valueOf(PokerObjects.getPlayer().getCash()));
		} else {
			log("Server not reachable.");
		}
	}
	
	public void raise(View view) {
		System.out.println("Player : BID");
		final VirtualPlayer player = PokerObjects.getPlayer();
		
		final float minBid = PokerObjects.getPlayer().getFolowAmount(); //should be = to call
		final float maxBid = (int) PokerObjects.getPlayer().getCash();// all in? equal to pot? depending on rule!
		
		if(minBid>=maxBid){//only solution is all in
			final float  bid = maxBid;
			
			
			AlertDialog dialog = new AlertDialog.Builder(this).create();
		    dialog.setTitle("All-in?");
		    dialog.setMessage("Choose to bet all-in or not");
		    dialog.setCancelable(true);
		    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int buttonId) {
		        	
		        }
		    });
		    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "All-in", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int buttonId) {
		        	if (PokerState.getGameClient().sendMessage(new Message(Message.MessageType.BID, String.valueOf(bid)))) {
            			log("I've bid " + bid + ".-");
            			player.removeCash(bid);
            			TextView tv = (TextView) findViewById(R.id.tvCashValue);
            			tv.setText(String.valueOf(player.getCash()));
            			Toast.makeText(getApplicationContext(), "All-in placed", Toast.LENGTH_LONG).show();
            		} else {
            			log("Server not reachable.");
            		}
		        }
		    });
		    dialog.setIcon(android.R.drawable.ic_dialog_alert);
		    dialog.show();
		} else {
		
		
		
		
		
		
	    LayoutInflater inflater = (LayoutInflater)
			    getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			    bidPickerdialog = inflater.inflate(R.layout.number_picker_dialog_layout, null);
			    new AlertDialog.Builder(this)
			        .setTitle("Place Bid:")
			        .setView(bidPickerdialog)
			        .setPositiveButton("Bid",
			            new DialogInterface.OnClickListener() {
			                public void onClick(DialogInterface dialog, int whichButton) {
			                	
			                int bid;
			                bid = ((NumberPicker) bidPickerdialog.findViewById(R.id.nbPicker)).getValue();
			            		if (PokerState.getGameClient().sendMessage(new Message(Message.MessageType.BID, String.valueOf(bid)))) {
			            			log("I've bid " + bid + ".-");
			            			player.removeCash(bid);
			            			((TextView) findViewById(R.id.tvCashValue)).setText(String.valueOf(player.getCash()));
			            			Toast.makeText(getApplicationContext(), "bid of "+bid+" placed", Toast.LENGTH_LONG).show();
			            		} else {
			            			log("Server not reachable.");
			            		}
			                			
			                }
			            })
			            .setNegativeButton("Cancel",
			                new DialogInterface.OnClickListener() {
			                    public void onClick(DialogInterface dialog, int whichButton) {
			                    }
			                })
			            .show();
			    
			    ((NumberPicker) bidPickerdialog).setMaxValue((int)maxBid);
			    ((NumberPicker) bidPickerdialog).setMinValue((int)minBid);
			    ((NumberPicker) bidPickerdialog).setBackgroundColor(Color.BLACK);
		}

	}
	
	public void fold(View view) {

		realCard1 = hiddenCard;
		realCard2 = hiddenCard;
	
		card1.setVisibility(View.INVISIBLE);
		card2.setVisibility(View.INVISIBLE);
		
		PokerState.getGameClient().sendMessage(new Message(Message.MessageType.FOLD, "0"));
		
		log("I'm forfeited.");
	}
	public void call(View view) {
		
		float amount = PokerObjects.getPlayer().getFolowAmount();
		PokerState.getGameClient().sendMessage(new Message(Message.MessageType.BID,
				String.valueOf(amount)));
		
		log("I've bid " + amount + ".-");
		
		PokerObjects.getPlayer().removeCash(amount);
		((TextView) findViewById(R.id.tvCashValue)).setText(String.valueOf(PokerObjects.getPlayer().getCash()));
	}
	
	
	public void HideShowCards(){
		if(!cardVisible){
			synchronized (realCard1) {
				card1.setImageDrawable(realCard1);

			}
			synchronized (realCard2) {
				card2.setImageDrawable(realCard2);
			}
			cardVisible = true;
		} else {
			card1.setImageDrawable(hiddenCard);
			card2.setImageDrawable(hiddenCard);
			cardVisible = false;
		}
		
	}
	
	public void setCard(final Message m){
		runOnUiThread(new Runnable() {
		     @Override
		     public void run() {
		switch (m.getType()) {
		case CARD1:
			synchronized (realCard1) {
				realCard1 = getResources().getDrawable(getResources().
				         getIdentifier("drawable/card_"+m.getLoad(), null,getPackageName()));
				card1.setVisibility(View.VISIBLE);

			}
			break;
		case CARD2:
			synchronized (realCard2) {
				realCard2 = getResources().getDrawable(getResources().
				         getIdentifier("drawable/card_"+m.getLoad(), null,getPackageName()));
				card2.setVisibility(View.VISIBLE);

			}
			break;

		default:
			break;
		}
		     }
		});
	}
	
	private void log(String text) {
		updateUiTextView(R.id.txtStatusPlayer, text);
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
					
					final VirtualPlayer player = PokerObjects.getPlayer();
					
					switch (message.getType()) {
					case UNKNOWN:
						/*do nothing*/
						break;
					case ACK:
						log("Connected to server.");
						break;
					case ASKBLIND:
						System.out.println("ASKBLIND");
						log("Blind : " + message.getLoad());
						break;
					case ASKBID:
						System.out.println("ASKBID");
						log("BID at least : " + message.getLoad());
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								if (Float.valueOf(message.getLoad()) == 0) {
									//change text into check

									((Button)findViewById(R.id.buttonCall)).setText("Check");
								} else {
									//change text into call
									((Button)findViewById(R.id.buttonCall)).setText("Call");
								}
							}
						});
						PokerObjects.getPlayer().setFollowAmount(Float.parseFloat(message.getLoad()));
						break;
					case REFUND:
						float amount = Float.parseFloat(message.getLoad());
						
						//update player
						player.addCash(amount);
						
						//update view
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								((TextView) findViewById(R.id.tvCashValue)).setText(String.valueOf(player.getCash()));
							}
						});
						
						break;
					case ROLE:
						//update view
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								ImageView dealer = (ImageView)findViewById(R.id.dealer);
								ImageView smallBlind = (ImageView)findViewById(R.id.smallBlind);
								ImageView bigBlind = (ImageView)findViewById(R.id.bigBlind);

								dealer.setVisibility(View.GONE);
								bigBlind.setVisibility(View.GONE);
								smallBlind.setVisibility(View.GONE);

								if("Dealer".equalsIgnoreCase(message.getLoad())){
									dealer.setVisibility(View.VISIBLE);
									getActionBar().setTitle(message.getLoad());
								} else if("SmallBlind".equalsIgnoreCase(message.getLoad())){
									smallBlind.setVisibility(View.VISIBLE);
									getActionBar().setTitle(message.getLoad());

								}else if("BigBlind".equalsIgnoreCase(message.getLoad())){
									bigBlind.setVisibility(View.VISIBLE);
									getActionBar().setTitle(message.getLoad());
								}else{
									getActionBar().setTitle("Player");
								}
								
								  // provide compatibility to all the versions

								
								
								
							}
						});

						break;
					case CARD1:
					case CARD2:
						setCard(message);
						break;
					case END:
						realCard1 = hiddenCard;
						realCard2 = hiddenCard;
						if(cardVisible){
							HideShowCards();
						}
						card1.setVisibility(View.INVISIBLE);
						card2.setVisibility(View.INVISIBLE);
						log(message.getLoad());
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
		PokerState.getGameClient().registerNetworkMessageHandler(mMsgHandler);
	}
}
