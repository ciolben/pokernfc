package ch.epfl.pokernfc;

import android.os.Binder;

public class PokerNFCBinder extends Binder {

	private PokerNFCService mBoundedService;
	private PokerActivity mRegisteredActivity = null;
	
	public PokerNFCBinder (PokerNFCService callerService) {
		mBoundedService = callerService;
	}
	
	public PokerNFCService getService() {
        return mBoundedService;
    }
	
	public void registerActivity(PokerActivity activity) {
		mRegisteredActivity = activity;
	}
	
	public PokerActivity getRegisteredActivity() {
		return mRegisteredActivity;
	}
}
