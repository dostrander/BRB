/*//////////////////////////////////////////
//		BRB-Android 	     	  //
//		Created by:		  //
//	      Derek Ostrander		  //
//		 Max Shwenk		  //
//		Stuart Lang		  //
//		 Evan Dodge		  //
//	        Jason Mather		  //
//		 Will Stahl		  //
//					  //
//	         Created on:		  //
//	      January 23rd, 2012	  //
//////////////////////////////////////////*/

package b.r.b;
// our stuff
import static b.r.b.Constants.*;
// for android
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TabActivity;
import android.app.AlertDialog.Builder;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;								
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
// java stuff
import java.util.ArrayList;
import java.util.Calendar;

/*	HomeScreenActivity
 * 		Starts the application bringing up the main screen	
 */

public class HomeScreenActivity extends TabActivity {
    // Convenience Variables
	private final String TAG = "HomeScreenActivity";
	private final String MESSAGE = "message";
	private final String LOG = "log";
	private final String SETTINGS = "settings";
	private final String NO_MESSAGE = "Click to Edit Message";
	
	public static Message mCurrent;

	// Variables
	IncomingListener listener;
	
	// Views
	ImageButton enableButton;
	ImageButton listButton;
	Button selectButton;
	TextView header;
	TabHost mTabHost;
	TabWidget mTabWidget;
	static TextView inputMessage;
	private static ListView messageList;
	ListView messageListView;
	
	AlarmManager alarmManager;

	private static MessageListCursorAdapter adapter;
	static ParentInteraction pDb;
	//static DatabaseInteraction db;

	
	/*	onCreate
	 * 		
	 */
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"in onCreate");
        Settings.Init(this);
        setTheme(Settings.Theme());
        setContentView(R.layout.main_screen);
        
        // Find views	
        enableButton = 		(ImageButton) findViewById(R.id.enable_away_button);						// To enable the message
        listButton	 =	 	(ImageButton) findViewById(R.id.show_list_button);							// Show messages
        messageList  = 		(ListView) findViewById(R.id.auto_complete_list);							// List of Messages 
        inputMessage = 		(TextView) findViewById(R.id.message_input);								// Textview showing current Message
        
        // Set Tabs to correct Activities 
        mTabHost 	= getTabHost();																		// Get TabHost
        
        // Message Tab
        mTabHost.addTab(mTabHost.newTabSpec(MESSAGE).													// Add Message Tab
        		setIndicator("Message",getResources().getDrawable(R.drawable.message_tab_selector)).	// Set MessageIcon Selector
        		setContent(new Intent(this,MessageActivity.class)));									// Set Intent for MessageActivity
        // Log Tab
        mTabHost.addTab(mTabHost.newTabSpec(LOG).														// Add Log Tab
        		setIndicator("Log",getResources().getDrawable(R.drawable.log_tab_selector)).			// Set LogIcon selector
        		setContent(new Intent(this,LogActivity.class)));										// Set Intent for LogActivity
        // Settings Tab
        mTabHost.addTab(mTabHost.newTabSpec(SETTINGS).													// Add Settings Tab
        		setIndicator("Settings",getResources().getDrawable(R.drawable.settings_tab_selector)).	// Set SettingsIcon selector
        		setContent(new Intent(this,SettingsActivity.class)));									// Set Intent for SettingsActivity
        
        // Set Current Tab
        mTabHost.setCurrentTab(0);        


        pDb = new ParentInteraction(this);
        View theader = ((LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.input_message_list_item, null, false);
        theader.setBackgroundColor(Color.DKGRAY);
        header = (TextView) theader.findViewById(R.id.input_message_list_item);
        header.setTextColor(Color.WHITE);
        header.setText("Create New Message");
        messageList.addHeaderView(header);
        adapter = new MessageListCursorAdapter(this,R.layout.input_message_list_item,pDb.GetAllParentMessages(),
        		new String[]{MESSAGE},new int[]{R.id.input_message_list_item});
        messageList.setAdapter(adapter);
        //
        

        
        messageList.setVisibility(View.GONE);
        registerListeners();
        
    	Log.d(TAG,String.valueOf(isEnabled()));
    	
   		int db_id = getSharedPreferences(PREFS,MODE_PRIVATE).getInt(DB_ID_KEY, -1);
   		Log.d(TAG,String.valueOf(db_id));
    	if(isEnabled() == MESSAGE_ENABLED && db_id >= 0){
    		Log.d(TAG,"message is enabled in onCreate");
   			changeCurrent(db_id);
   			enableMessage();
   		}else noMessage();
    }
    
    @Override
    public void onStart(){
    	super.onStart();
    	int db_id = getSharedPreferences(PREFS,MODE_PRIVATE).getInt(DB_ID_KEY, -1);
    	int enabled = isEnabled();
    	Log.d(TAG,"in onStart");
    	
   		if((enabled == MESSAGE_ENABLED) && (db_id >= 0)){
   			Log.d(TAG,"message Enabled");
   			changeCurrent(db_id);
   			enableMessage();
   		}else if((enabled == MESSAGE_DISABLED) && (db_id >= 0)){
   			Log.d(TAG,"message disabled");
   			changeCurrent(db_id);
   			disableMessage();
   		} else{
   			Log.d(TAG,"no message");
   			noMessage();
   		}

    }
    private String tempFunc(String num){
		Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(num));
		Cursor cursor = getContentResolver().query(contactUri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, 
				null, null, null);
		if(cursor.moveToFirst())
			if(!cursor.isAfterLast())
				return cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
		return null;

    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	Log.d(TAG,"in onResume");
    }
        
    @Override
    public void onPause(){
    	super.onPause();
    	Log.d(TAG,"in onPause");
    }
    
    @Override
    public void onStop(){
    	super.onStop();
    	Log.d(TAG, "in onStop");
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	Log.d(TAG,"in onDestroy");
    	SharedPreferences prefs = getSharedPreferences(PREFS,MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
    	if(isEnabled() != MESSAGE_ENABLED)
    		editor.putInt(MESSAGE_ENABLED_KEY, NO_MESSAGE_SELECTED);
    	else editor.putInt(MESSAGE_ENABLED_KEY, MESSAGE_ENABLED);
    	editor.commit();
    		
    	
    }
    
    public int isEnabled(){
    	SharedPreferences prefs = getSharedPreferences(PREFS,MODE_PRIVATE);
    	return prefs.getInt(MESSAGE_ENABLED_KEY, NO_MESSAGE_SELECTED);
    }
    
    
    public void popToast(String t){
    	Toast.makeText(this, t, Toast.LENGTH_LONG).show();
    }
    
    /*	registerClickListeners
     * 
     */
    
    private void registerListeners(){
    	Log.d(TAG,"in registerListener");
    	// Filter for CustomAutoComplete
    	
    	inputMessage.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(mCurrent == null)
					createNewMessageDialog();
				else editTextDialog();
			}
    	});
    	messageList.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

			}
    	});
    	enableButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(isEnabled() == MESSAGE_DISABLED) enableMessage();
				else disableMessage();
			}
    	});
    	listButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(messageList.getVisibility() == View.GONE){
					pDb.Cleanup();
					adapter.changeCursor(pDb.GetAllParentMessages());
					messageList.setVisibility(View.VISIBLE);
					messageList.bringToFront();
				}
				else messageList.setVisibility(View.GONE);
					
			}
    	});
    	
    	header.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				createNewMessageDialog();				
			}
    	});
    }
    
    private void createNewMessageDialog(){
		int myDialogColor = Color.rgb(33, 66, 99);
		LinearLayout ll = new LinearLayout(HomeScreenActivity.this);
		ll.setOrientation(LinearLayout.VERTICAL);
		final EditText input = new EditText(HomeScreenActivity.this);
		final ListView lv = new ListView(HomeScreenActivity.this);
		Cursor c = pDb.GetAllParentMessages();
		ArrayList<String> temp = new ArrayList<String>();
		if(c.moveToFirst()){
			do temp.add(c.getString(c.getColumnIndex(MESSAGE))); 
			while(c.moveToNext());
		}
		pDb.Cleanup();
		String[] messages = temp.toArray(new String[]{});
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(HomeScreenActivity.this,
				R.layout.dialog_message_item,R.id.textView1, messages);
		input.setLines(2);
		input.setGravity(Gravity.TOP);
		input.setHint("Start typing message...");
		input.addTextChangedListener(new TextWatcher(){
			public void afterTextChanged(Editable e) {}
			public void beforeTextChanged(CharSequence s, int arg1,
					int arg2, int arg3) {}
			public void onTextChanged(CharSequence s, int start,
					int before, int count) {adapter.getFilter().filter(s);}	});
		ll.setBackgroundColor(myDialogColor);
		lv.setBackgroundColor(myDialogColor);
		lv.setCacheColorHint(myDialogColor);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> adap, View v,
					int position, long id) {
				input.setText(adapter.getItem(position));
				input.setSelection(input.getText().length());
			}
		});
		ll.addView(input);
		ll.addView(lv);
		AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreenActivity.this);
		builder.setTitle("Create New Message")
		.setView(ll)
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String text = input.getText().toString().trim().toString();
				mCurrent = pDb.GetParentByMessage(text);
				
				if(mCurrent == null){
					Log.d(TAG,"new message");
					mCurrent = pDb.InsertMessage(text);
					//mCurrent = db.InsertMessage(text, new String[]{});
					HomeScreenActivity.adapter.changeCursor(pDb.GetAllParentMessages());
				}
				messageList.setVisibility(View.GONE);
				changeCurrent();
				
				// Maybe? -- agree with original comment, may need to be closed
				pDb.Cleanup();
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
				messageList.setVisibility(View.GONE);
			}
		}).create().show();
    }
    
    private void editTextDialog(){
		LinearLayout ll = new LinearLayout(HomeScreenActivity.this);
		final EditText input = new EditText(HomeScreenActivity.this);
		input.setText(mCurrent.text);
		ll.setOrientation(LinearLayout.VERTICAL);
		input.setLines(2);
		input.setGravity(Gravity.TOP);
		input.setHint("Start typing message...");
		ll.addView(input);
		AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreenActivity.this);
		builder.setTitle("Edit Message")
		.setView(ll)
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {				
				// update message
				String text = input.getText().toString().trim().toString();
				Log.d(TAG,String.valueOf(mCurrent.getID()));
				if(text.length() > 0){
					if(pDb.ParentEditMessage(mCurrent.getID(), text))
						editCurrentMessage(text);
				}
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {}
		}).create().show();
    	
    }

    private int getEndTime(){
    	SharedPreferences prefs = this.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    	Calendar current = Calendar.getInstance(); 
    	Calendar end = Calendar.getInstance();
    	
    	end.set(prefs.getInt("END_TIME_YEAR", 0), prefs.getInt("END_TIME_MONTH", 0), 
    			prefs.getInt("END_TIME_DAY", 0), prefs.getInt("END_TIME_HOUR", 0), 
    			prefs.getInt("END_TIME_MIN", 0));
    	
    	if(end.getTimeInMillis() == 0)
    		return(-1);
    	
    	int difference = (int)(end.getTimeInMillis() - current.getTimeInMillis());
    	difference = difference - end.get(end.SECOND)*1000;
    	
    	return(difference);
    }
    
    private void enableMessage(){
    	// Make an alarm for the end time
    	int time = getEndTime();
    	if(time >= 0)
    	{
    		Intent intent = new Intent(this, AlarmReceiver.class);
    		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

    		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    		alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+time, pendingIntent);
    	}
    	
    	AudioManager audiomanage = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
    	SharedPreferences.Editor editor = getSharedPreferences(PREFS, Activity.MODE_PRIVATE).edit();
    	inputMessage.setTextColor(Color.WHITE);
    	enableButton.setImageResource(R.drawable.enabled_message_selector);
    	// enable listener
    	enableButton.setClickable(true);
    	editor.putInt(DB_ID_KEY, mCurrent.getID());
    	editor.putInt(MESSAGE_ENABLED_KEY, MESSAGE_ENABLED);
    	editor.putInt("ringer_mode", audiomanage.getRingerMode());
    	audiomanage.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    	
    	// Updates the widget's Icon
    	Context context = this;
    	AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
    	RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
    	ComponentName thisWidget = new ComponentName(context, Widget.class);
    	remoteViews.setImageViewResource(R.id.widget_button, R.drawable.enabled_message_selector);
    	appWidgetManager.updateAppWidget(thisWidget, remoteViews);
    	
    	editor.commit();
    }
    
    private void disableMessage(){
    	
    	// Cancel the alarm
    	int time = getEndTime();
    	if(time >= 0)
    	{
    		Intent intent = new Intent(this, AlarmReceiver.class);
    		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

    		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    		alarmManager.cancel(pendingIntent);
    	}
    	
    	SharedPreferences prefs = getSharedPreferences(PREFS, Activity.MODE_PRIVATE);
    	SharedPreferences.Editor editor = prefs.edit();
    	AudioManager audiomanage = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
    	editor.putInt(MESSAGE_ENABLED_KEY, MESSAGE_DISABLED);
    	inputMessage.setTextColor(Color.WHITE);
    	enableButton.setImageResource(R.drawable.disabled_button_selector);
    	// disable listener
    	audiomanage.setRingerMode(prefs.getInt("ringer_mode",AudioManager.RINGER_MODE_NORMAL));
    	enableButton.setClickable(true);
    	
    	// Updates the widget's Icon
    	Context context = this;
    	AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
    	RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
    	ComponentName thisWidget = new ComponentName(context, Widget.class);
    	remoteViews.setImageViewResource(R.id.widget_button, R.drawable.disabled_button_selector);
    	appWidgetManager.updateAppWidget(thisWidget, remoteViews);
    	
    	editor.commit();
    }
    
    private void noMessage(){
    	SharedPreferences.Editor editor = getSharedPreferences(PREFS, Activity.MODE_PRIVATE).edit();
    	editor.putInt(MESSAGE_ENABLED_KEY, NO_MESSAGE_SELECTED);
    	inputMessage.setText(NO_MESSAGE);
    	inputMessage.setTextColor(Color.GRAY);
    	enableButton.setImageResource(R.drawable.nothing_button_selector);
    	mCurrent = null;
    	enableButton.setClickable(false);
    	
    	// Updates the widget's Icon
    	Context context = this;
    	AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
    	RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
    	ComponentName thisWidget = new ComponentName(context, Widget.class);
    	remoteViews.setImageViewResource(R.id.widget_button, R.drawable.nothing_button_selector);
    	remoteViews.setTextViewText(R.id.widget_textview, "Use the arrows to scroll through saved messages..." );
    	appWidgetManager.updateAppWidget(thisWidget, remoteViews);
    	
    	editor.commit();
    }
    
    private void noCurrent(){
    	mCurrent = null;
    	MessageActivity.noMessage();
    }
    
    private void editCurrentMessage(String t){
    	mCurrent.setText(t);
    	inputMessage.setText(mCurrent.text);
    	
    	MessageActivity.changeMessage(mCurrent);
    }
    private void changeCurrent(long db_id){
    	Log.d(TAG,"in changeCurrent");
    	Message temp = pDb.GetParentById((int) db_id);
    	mCurrent = temp;
    	changeCurrent();
   	}
    
    private void changeCurrent(){
    	MessageActivity.changeMessage(mCurrent);
    	if(mCurrent == null){
    		noMessage();
    	}
    	else{
    		inputMessage.setText(mCurrent.text);
    		disableMessage();
    	}
    	
    	// Updates the Widget's Textview
    	Context context = this;
    	AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
    	RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
    	ComponentName thisWidget = new ComponentName(context, Widget.class);
    	remoteViews.setTextViewText(R.id.widget_textview, mCurrent.text);
    	appWidgetManager.updateAppWidget(thisWidget, remoteViews);
    }
    private class MessageListCursorAdapter extends CursorAdapter {
    	private final int layout;
    	private final int textview_id;
		public MessageListCursorAdapter(Context ctx, int lout, Cursor c,
				String[] from, int[] to){
			super(ctx, c);
			layout = lout;
			textview_id = to[0];
		}
		
		@Override
		public View newView(Context ctx, Cursor cursor, ViewGroup parent){
			ViewHolder holder = new ViewHolder();
			View v = LayoutInflater.from(ctx).inflate(layout, null);
			v.setTag(holder);
			
			return v;
			
		}
		
		@Override
		public void bindView(View v, Context context, Cursor cursor){
			final ViewHolder holder = (ViewHolder) v.getTag();
			TextView tv = (TextView) v.findViewById(textview_id);
			tv.setText(cursor.getString(cursor.getColumnIndex(MESSAGE)));			
			holder.text = tv;
			holder.id = cursor.getLong(cursor.getColumnIndex(ID));
			v.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					Log.d(TAG,String.valueOf(holder.id));
					if(layout == R.layout.input_message_list_item){
						changeCurrent(holder.id);
						messageList.setVisibility(View.GONE);
					}
				}
			});
		}
    	
    	public class ViewHolder{
    		TextView text;
    		long id;
    	}
    	
    }
}