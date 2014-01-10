package ch.epfl.pokernfc;

import ch.epfl.pokernfc.Utils.AsynchHandler;
import ch.epfl.pokernfc.Utils.NFCUtils;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class PokerNFCService extends IntentService implements AsynchHandler<String> {
	
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
		onHandleIntent(intent);
        return START_STICKY;
    }

	//http://mobile.tutsplus.com/tutorials/android/reading-nfc-tags-with-android/
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
		if (intent == null) {
			System.out.println("-> null intent.");
			return;
		}
		//check if the intent is for the Player or the Pot, and redirect intent
		Intent nfcIntent = new Intent(intent);
		
		//asynchronous decoding
		//NFCUtils.getNDEFMessage(nfcIntent, this);
		
	}

	@Override
	public void resultReady(String result) {
		PokerActivity registeredActivity = mBinder.getRegisteredActivity();
		if (registeredActivity == null) { return; }
		
		System.out.println("Service : decoded string : " + result);
		
		if (isPlayer) {
			//call the player activity
			registeredActivity.onNFCIntent(result);
		} else {
			//call the pot activity
			registeredActivity.onNFCIntent(result);
		}
	}
	
	
}
