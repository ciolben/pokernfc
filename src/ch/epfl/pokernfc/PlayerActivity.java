package ch.epfl.pokernfc;

import ch.epfl.pokernfc.Logic.VirtualPlayer;
import ch.epfl.pokernfc.Logic.PokerObjects;
import ch.epfl.pokernfc.Logic.network.Client;
import ch.epfl.pokernfc.Logic.network.Message;
import ch.epfl.pokernfc.Logic.network.NetworkMessageHandler;
import ch.epfl.pokernfc.Utils.MessageUtils;
import ch.epfl.pokernfc.Utils.NFCMessageReceivedHandler;
import ch.epfl.pokernfc.Utils.NFCUtils;
import android.opengl.Visibility;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.provider.ContactsContract.CommonDataKinds.Im;

public class PlayerActivity extends PokerActivity {

	private NetworkMessageHandler mMsgHandler;
	private ImageView card1;
	private ImageView card2;	
	private Drawable hiddenCard1;
	private Drawable hiddenCard2;
	private View bidPickerdialog;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);
		hiddenCard1= getResources().getDrawable(R.drawable.back_card);
		hiddenCard2 = getResources().getDrawable(R.drawable.back_card);
		card1 = (ImageView) findViewById(R.id.playerCard1);
		card1.setVisibility(View.INVISIBLE);
		card2 = (ImageView) findViewById(R.id.playerCard2);
		card2.setVisibility(View.INVISIBLE);
		
		// Show the Up button in the action bar.
		setupActionBar();

		

		System.out.println("PlayerActivity : onCreate");

		PokerObjects.getPlayer().addCash(1000.f);//TODO attention oncreate called also when screen turn!!
		
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
					
					registerMessageHandler(); //for receiving news from server
					
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
		card1.setImageResource(R.drawable.card_10_1);

		card1.setVisibility(View.VISIBLE);
		card2.setVisibility(View.VISIBLE);
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
		
		//TODO what if minbid > maxbid???
		final int minBid = 0; //should be = to call
		final int maxBid = (int) PokerObjects.getPlayer().getCash();// all in? equal to pot? depending on rule!
		
		if(minBid>=maxBid){//only solution is all in
			final int  bid = maxBid;
			
			
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
            			PokerObjects.getPlayer().removeCash(bid);
            			((TextView) findViewById(R.id.tvCashValue)).setText(String.valueOf(PokerObjects.getPlayer().getCash()));
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
			   //  new TimePickerDialog(this, null, 0, 0, true).show();
			    new AlertDialog.Builder(this)
			        .setTitle("Place Bid:")
			        .setView(bidPickerdialog)
			        .setPositiveButton("Bid",
			            new DialogInterface.OnClickListener() {
			                public void onClick(DialogInterface dialog, int whichButton) {
			                	
			                int bid;
			                bid = ((NumberPicker) bidPickerdialog.findViewById(R.id.nbPicker)).getValue();
			            		if (PokerState.getGameClient().sendMessage(new Message(Message.MessageType.BID, String.valueOf(bid)))) {
			            			PokerObjects.getPlayer().removeCash(bid);
			            			((TextView) findViewById(R.id.tvCashValue)).setText(String.valueOf(PokerObjects.getPlayer().getCash()));
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
			    
			    ((NumberPicker) bidPickerdialog).setMaxValue(maxBid);
			    ((NumberPicker) bidPickerdialog).setMinValue(minBid);
			    ((NumberPicker) bidPickerdialog).setBackgroundColor(Color.BLACK);
		}

	}
	
	public void fold(View view) {

		card1.setImageResource(getResources().
		         getIdentifier("drawable/card_14_4", null, this.getPackageName()));
		card2.setImageResource(R.drawable.card_2_4);

		card1.setVisibility(View.VISIBLE);
		card2.setVisibility(View.VISIBLE);
//		System.out.println("Player : PAY CASH");
//		card1.setVisibility(View.VISIBLE);
//		card2.setVisibility(View.VISIBLE);
//		//Credit the Pot by 10 chf
//		if (PokerState.getGameClient().sendMessage(new Message(Message.MessageType.BID, String.valueOf(10)))) {
//			PokerObjects.getPlayer().removeCash(10.f);
//			((TextView) findViewById(R.id.tvCashValue)).setText(String.valueOf(PokerObjects.getPlayer().getCash()));
//		} else {
//			log("Server not reachable.");
//		}
	}
	public void call(View view) {
		HideShowCards();
//		System.out.println("Player : PAY CASH");
//		card1.setVisibility(View.VISIBLE);
//		card2.setVisibility(View.VISIBLE);
//		//Credit the Pot by 10 chf
//		if (PokerState.getGameClient().sendMessage(new Message(Message.MessageType.BID, String.valueOf(10)))) {
//			PokerObjects.getPlayer().removeCash(10.f);
//			((TextView) findViewById(R.id.tvCashValue)).setText(String.valueOf(PokerObjects.getPlayer().getCash()));
//		} else {
//			log("Server not reachable.");
//		}
	}
	
	
	public void HideShowCards(){
		Drawable temp = hiddenCard1;
		hiddenCard1 = card1.getDrawable();
		card1.setImageDrawable(temp);
		temp = hiddenCard2;
		hiddenCard2 = card2.getDrawable();
		card2.setImageDrawable(temp);
		
	}
	
	private void log(String text) {
		((TextView) findViewById(R.id.txtStatusPlayer)).setText("Status : " + text);
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
					case CARD1:
						runOnUiThread(new Runnable() {
						     @Override
						     public void run() {

						    	 card1.setImageResource(getResources().
								         getIdentifier("drawable/card_"+message.getLoad(), null, getPackageName()));
								card1.setVisibility(View.VISIBLE);
						    }
						});
						
						break;
					case CARD2:
						runOnUiThread(new Runnable() {
						     @Override
						     public void run() {

						    	 card2.setImageResource(getResources().
								         getIdentifier("drawable/card_"+message.getLoad(), null, getPackageName()));
								card2.setVisibility(View.VISIBLE);
						    }
						});
						
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
