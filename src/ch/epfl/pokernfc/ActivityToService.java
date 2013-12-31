package ch.epfl.pokernfc;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;

public class ActivityToService extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Context con = getApplicationContext();
		Intent srv = new Intent(con, PokerNFCService.class);
		con.startService(srv); //doesn't matter if started multiple times
		finish();
	}
}
