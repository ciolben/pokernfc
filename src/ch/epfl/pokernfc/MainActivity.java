package ch.epfl.pokernfc;

import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
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
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		appActivities = new AppActivity[] {
				new AppActivity(R.string.pot, PotActivity.class),
				new AppActivity(R.string.player, PlayerActivity.class)
		};
		
		setListAdapter(new ArrayAdapter<AppActivity>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                appActivities));
		
        
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
        
    }
    
}
