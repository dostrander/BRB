package b.r.b;

import static b.r.b.Constants.*;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class SettingsActivity extends Activity {
	private static final String TAG = "SettingsActivity";
	
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		Log.d(TAG,"in onCreate");
		
		setTheme(Settings.Theme());
		
		setContentView(R.layout.settings_view);
	}
}
