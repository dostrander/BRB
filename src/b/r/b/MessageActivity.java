package b.r.b;

import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.widget.TextView;

public class MessageActivity extends Activity {
	private static final String TAG = "MessageActivity"; 
	TextView vPriority;
	TextView vStartTime;
	TextView vEndTime;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_view);
//		vPriority 	= (TextView) findViewById(R.id.priority_text);
//		vStartTime 	= (TextView) findViewById(R.id.starttime_text);
//		vEndTime 	= (TextView) findViewById(R.id.endtime_text);
		String current_date = DateFormat.getDateInstance().format(new Date());
		String current_time = DateFormat.getTimeInstance().format(new Date());
		Log.d(TAG,current_date);
		Log.d(TAG,current_time);
		
		
	}

}
