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
public class ActivityDispatcher extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Context con = getApplicationContext();
		Intent intent = getIntent();
		
		System.out.println("Activity Dispatcher : onCreate");
		
		//dispatch intent to the right activity
		intent.setClass(this, PlayerActivity.class);
		startActivity(intent);
		
		finish();
	}
	
	protected void onNewIntent(Intent intent) {
		System.out.println("Activity Dispatcher : onNewIntent");
	}
}
