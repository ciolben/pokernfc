package ch.epfl.pokernfc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

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
