package ch.epfl.pokernfc;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class PokerNFCService extends IntentService {
	
	//binder stuff
	private final PokerNFCBinder mBinder;
	
	//others
	private boolean isPlayer;
	
	public PokerNFCService() {
		super("pokernfc");
		mBinder = new PokerNFCBinder(this);
		isPlayer = true;
		Log.v("Poker", "Service : constructor ------------------");
	}
	
	//we don't want to run the service indefinitely. Just bind it.
	//maybe need to kill it if started by error
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v("Poker", "Service : start ------------------");
        return START_STICKY;
    }

	//http://developer.android.com/guide/components/services.html
	@Override
	public IBinder onBind(Intent intent) {
		System.out.println("Service : on bind ------------------");
		Bundle bundle = intent.getExtras();
		System.out.println("-> bundle : player : " + bundle.get("ch.epfl.pokernfc.Player"));
		System.out.println("-> bundle : pot : " + bundle.get("ch.epfl.pokernfc.Pot"));
		if (bundle.get("ch.epfl.pokernfc.Player") != null) {
			isPlayer = true;
		} else if (bundle.get("ch.epfl.pokernfc.Pot") != null) {
			isPlayer = false;
		} else {
			System.err.println("GROSSE ERREUR !");
		}
		return mBinder;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		System.out.println("Service : on handle intent ------------------");
		//check if the intent is for the Player or the Pot, and redirect intent
		Intent nfcIntent = new Intent(intent);
		PokerActivity registeredActivity = mBinder.getRegisteredActivity();
		if (registeredActivity == null) { return; }
		
		if (isPlayer) {
			//call the player activity
			registeredActivity.onNFCIntent(nfcIntent);
		} else {
			//call the pot activity
			registeredActivity.onNFCIntent(nfcIntent);
		}
		
	}
	
	
}
