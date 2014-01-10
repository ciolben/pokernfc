package ch.epfl.pokernfc;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public abstract class PokerActivity extends BeamActivity {
	
	/***
	 * Receive an NFC message
	 * @param nfcContent
	 */
	public abstract void onNFCIntent(String nfcContent);
    
   //shared fields
    protected PokerNFCBinder mBinder;
    protected boolean mBound = false;
    
	 /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to PokerNFCBinder, cast the IBinder and get PokerNFCBinder instance
            mBinder = (PokerNFCBinder) service;
//            mService = binder.getService();
            mBound = true;
            Log.i("Poker", "Activity : service Connected.");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            Log.i("Poker", "Activity : service Disconnected.");
        }
    };
    
    @Override
    protected void onStop() {
    	super.onStop();
    	unbind();
    }
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    }
    
    //common methods for service implementation
    protected void bind(String id) {
    	Intent bindingIntent = new Intent(this, PokerNFCService.class);
		bindingIntent.putExtra("ch.epfl.pokernfc." + id, true);
		boolean ok = getApplicationContext().bindService(bindingIntent, mConnection, 0);
		if (!ok) {
			System.err.println("Activity : unable to bind service.");
		} else {
			System.out.println("Activity : bind service : " + id);
		}
    }
    
    protected void unbind() {
    	System.out.print("Activity : unbind : ");
    	if (mBound) {
    		getApplicationContext().unbindService(mConnection);
    		System.out.println(" ok.");
    	} else {
    		System.out.println(" no connection.");
    	}
    	
    }
    
//    protected void onPause() {
//    	super.onPause();
//    	NFCUtils.callOnPause(this);
//    }
//    
//    protected void onResume() {
//    	super.onResume();
//    	NFCUtils.callOnResume(this);
//    }
//    
//    protected void onNewIntent(Intent intent) {
//    	super.onNewIntent(intent);
//    	NFCUtils.callOnNewIntent(true, intent, this, "TETEEESETSTSETSTTTT");
//    }
}
