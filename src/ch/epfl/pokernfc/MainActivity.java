package ch.epfl.pokernfc;

import ch.epfl.pokernfc.Utils.NFCUtils;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.ServiceConnection;
import android.content.res.Resources.Theme;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends ListActivity {

	/**
     * This class describes an individual activity for easily switch between them.
     */
    private class AppActivity {
        private CharSequence title;
        private Class<? extends Activity> activityClass;

        public AppActivity(int titleResId, Class<? extends Activity> activityClass) {
            this.activityClass = activityClass;
            this.title = getResources().getString(titleResId);
        }

        @Override
        public String toString() {
            return title.toString();
        }
    }
    
    private static AppActivity[] appActivities;
    
    //nfc stuff
    private NfcAdapter nfcAdapter;
    private PendingIntent nfcPendingIntent;
    private IntentFilter[] writeTagFilters;
    private IntentFilter[] defExchangeFilters;
    private static String POTINTENT = "ch.epfl.pokernfc.PotActivity.NFCMESSAGE";
    private static String PLAYERINTENT = "ch.epfl.pokernfc.NFCMESSAGE";
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//fill main list
//		ListView lview = (ListView) findViewById(R.id.mainList);
		appActivities = new AppActivity[] {
				new AppActivity(R.string.pot, PotActivity.class),
				new AppActivity(R.string.player, PlayerActivity.class)
//				new AppActivity(R.string.player, BeamActivity.class)
		};
		
		setListAdapter(new ArrayAdapter<AppActivity>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                appActivities));
		
		//use service to dispatch nfc messages to the right activity
		//startService(new Intent(MainActivity.this, PokerNFCService.class));
		//bindService()
		
		//*********************setup nfc*********************
		//j'ai mit pot at player en SingleTask, comme �a quand il y a un nouveau nfc,
		//�a appelle onNewIntent sur pot (lire) ou player (�crire)
		
		//nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		
//		NFCUtils.setup(this);
		
		// Handle all the received NFC intents in the pot activity
		
//        nfcPendingIntent = PendingIntent.getActivity(this, 0,
//                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
//
//        // Intent filters for reading a note from a tag or exchanging over p2p.
//        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
//        try {
//            ndefDetected.addDataType("text/plain");
//        } catch (MalformedMimeTypeException e) { }
//        defExchangeFilters = new IntentFilter[] { ndefDetected };
//
//        // Intent filters for writing to a tag
//        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
//        writeTagFilters = new IntentFilter[] { tagDetected };
        
        System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
        
        Intent intent = new Intent(this, PokerNFCService.class);
        startService(intent);
	}
	
    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        // Launch activity
        startActivity(new Intent(MainActivity.this, appActivities[position].activityClass));
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);//must store the new intent unless getIntent() will return the old one
        System.out.println("dasadadlflkdsafkasldkflaskdfkalsdkfaldklfakdlkf");
        
    }
    
}
