package ch.epfl.pokernfc;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public abstract class PokerActivity extends Activity {
	public abstract void onNFCIntent(Intent intent);
    
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
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    
    //common methods
    protected void bind(String id) {
    	Intent bindingIntent = new Intent(this, PokerNFCService.class);
		bindingIntent.putExtra("ch.epfl.pokernfc." + id, true);
		bindService(bindingIntent, mConnection, 0);
    }
}
