package ch.epfl.pokernfc;

import ch.epfl.pokernfc.Utils.NFCUtils;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;

/**
 * Started when an NFC is discovered
 * @author Loic
 *
 */
public class ActivityToService extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Context con = getApplicationContext();
		
		Intent intentForService = NFCUtils.reforgeIntentForService(getIntent(), con, PokerNFCService.class);
		System.out.println("Activity To Service : onNewIntent 1");
		if (intentForService == null) { return; }
		
		con.startService(intentForService); //doesn't matter if called multiple times
		System.out.println("Activity To Service : onNewIntent 2");
		finish();
	}
	
	protected void onNewIntent(Intent intent) {
		System.out.println("Activity To Service : onNewIntent");
	}
}
