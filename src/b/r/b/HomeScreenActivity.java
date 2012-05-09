/*//////////////////////////////////////////
//			BRB-Android 	   			  //
//			Created by:	  				  //
//	      Derek Ostrander				  //
//		 Max Shwenk		 				  //
//		Stuart Lang						  //
//		 Evan Dodge		  				  //
//	        Jason Mather				  //
//		 Will Stahl		  				  //
//					  					  //
//	         Created on:				  //
//	      January 23rd, 2012			  //
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
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;
// java stuff
import java.util.ArrayList;
import java.util.Calendar;

/*	HomeScreenActivity
 * 		First activity that comes up.
 * 		Holds the TabHost for displaying the Message, Response Log, and Settings tabs.
 * 		Holds the Current Message.
 * 		Controls the current message and when it is enabled/disabled and changed
 * 		
 */

public class HomeScreenActivity extends TabActivity {
    // Convenience Variables
	private final String TAG = "HomeScreenActivity";													// For Logs
	private final String MESSAGE = "message";															// message tab identifier
	private final String LOG = "log";																	// log tab identifier
	private final String SETTINGS = "settings";															// settings tab identifier
	private final String NO_MESSAGE = "Click to Edit Message";											// Default textview text
	// Views
	private ImageButton enableButton;																	// For enabling/disabling
	private ImageButton listButton;																		// To show the drop down list
	private TextView header;																			// header for the drop down listview
	private TextView inputMessage;																		// Top textview for inputting message 
	private ListView messageList;																		// list for drop down of messages
	private TabHost mTabHost;																			// For holding and controlling the different tabs
	// Utilities
	private AlarmManager alarmManager;																	// For waking up the phone to disable the message
	private static MessageListCursorAdapter adapter;														// Adapter for controlling the drop down message list
	private static ParentInteraction pDb;																// To access the Parent Message database
	public static boolean logStarted;																	// flag for when logActivity has been created
	public static Message mCurrent;																		// the current message enabled


	
	/*	onCreate
	 * 		get Views and tabhosts, set up intents to put in the tabhosts for switching.
	 * 		Set up the list and adapters and put the header on top ofthe list
	 */
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Settings.Init(this);
        setTheme(Settings.Theme());																		// set the theme we have chosen in the settings tabs
        setContentView(R.layout.main_screen);
        logStarted = false;																				// Flag for when the logActivity has been created
        // Find views	
        View theader = ((LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE))					// Inflate header view to put on top of drop 
        		.inflate(R.layout.input_message_list_item, null, false);									// down message list
        header 		= (TextView) theader.findViewById(R.id.input_message_list_item);					// Get header textview for direct control 

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
        mTabHost.setCurrentTab(0); 																		// Set the current tab to messageActivity       

        //open up a parent interaction so we can interact with the parent database
        pDb = new ParentInteraction(this);																// Initialize parentInteraction
        
        // Set to theme appropriate color
        TypedArray a = getTheme().obtainStyledAttributes(new int[] {R.attr.dark_color});
        a.recycle();
        
        // Set up Header
        theader.setBackgroundColor(a.getColor(0, Color.BLACK));											// set header color different then othere
        header.setTextColor(Color.WHITE);																// set header text color different as well
        header.setText("Create New Message");															// set the text
        messageList.addHeaderView(header);																// add it to the top of the listview
        Cursor temp = pDb.GetAllParentMessages();														// creating a temp so we can close it as soon as 
        																									// we're done with it
        adapter = new MessageListCursorAdapter(this,R.layout.input_message_list_item,temp,				// Create adapter for controlling the listview
        		new String[]{MESSAGE},new int[]{R.id.input_message_list_item});
        messageList.setAdapter(adapter);																// Set the adapter
        temp.close();																					//can close the getAllParentMessages cursor now that it's stored

        messageList.setVisibility(View.GONE);															// Dont show the list until the button is clicked
        registerListeners();																			// Set up Click Listeners
        
        
        // Set up color of button and change current message
        //		This is done twice, in onCreate and onStart because
        // 		of the Android LifeCycle. If the app has been closed 
        //		and destroyed i want to put no message selected up when created
        //		if there was not one disabled. And in onstart i do it in case
        //		the app just went down for a second
   		int db_id = getSharedPreferences(PREFS,MODE_PRIVATE).getInt(DB_ID_KEY, -1);						// Get db_id of the last mCurrent message						
    	if(isEnabled() == MESSAGE_ENABLED && db_id >= 0){												// if it enabled and there is a valid db_id
   			changeCurrent(db_id);																		// change the current message
   			enableMessage();																			// and enable it
   		} else noMessage();																				// else say that there is no message
    	
    }
    
    /*	onStart
     * 		Basically just does what was done at the end of onCreate, for
     * 		reasons that were explained in onCreate
     */
    @Override
    public void onStart(){
    	super.onStart();
    	int db_id = getSharedPreferences(PREFS,MODE_PRIVATE).getInt(DB_ID_KEY, -1);						// Get the db_id
    	int enabled = isEnabled();																		// check to see if it was enabled
   		if((enabled == MESSAGE_ENABLED) && (db_id >= 0)){												// if it is enabled and has a valid db_id
   			changeCurrent(db_id);																		// change the current message
   			enableMessage();																			// and enable it
   		}else if((enabled == MESSAGE_DISABLED) && (db_id >= 0)){										// else if it is disabled and the db_id is valid
   			changeCurrent(db_id);																		// change the current message
   			disableMessage();																			// and disable it
   		} else noMessage();																				// else just put no Message selected up
    }
    
    
    /*	onDestroy
     * 		Saves if the message was enabled or not and cleans up the DB
     */
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	SharedPreferences prefs = getSharedPreferences(PREFS,MODE_PRIVATE);								// get the preferences
		SharedPreferences.Editor editor = prefs.edit();													// and the editor to set the values
		//if we are not enabled, we dont want any message selected
    	if(isEnabled() != MESSAGE_ENABLED)																// if the message is not enabled
    		editor.putInt(MESSAGE_ENABLED_KEY, NO_MESSAGE_SELECTED);									// say there is no message selected next time
    	//if it is enabled, we need to store what the current message is
    	else editor.putInt(MESSAGE_ENABLED_KEY, MESSAGE_ENABLED);										// if enabled store it
    	editor.commit();																				// save the preferences
    	//if pDb is not null, make sure we clean it up to avoid database never closed errors!
    	if(pDb != null){																				// if the db isn't already null
    		pDb.Cleanup();																				// clean it up for no errors
    	}
    }
    
    /*	registerClickListeners
     *  pretty straightforward, just setting all the click listeners we need so the user can click on anything they want
     */
    private void registerListeners(){
    	// inputMessage 
    	//		If there isn't a message selected bring up the createNewMessage dialog
    	//		else just bring up the edit text dialog
    	inputMessage.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(mCurrent == null)																	// if there is no current message
					createNewMessageDialog();															// bring up the create new dialog								
				else editTextDialog();																	// else brign up the edit text dialog
			}
    	});
    	
    	//	enableButton
    	//		Enables and disables the message accordingly
    	enableButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(isEnabled() == MESSAGE_DISABLED) enableMessage();									// if it is disabled enable the message
				else disableMessage();																	// else just disable it
			}
    	});
    	
    	//	listButton
    	//		just makes the messageListView visible or invisible and updates
    	//		the cursor
    	listButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(messageList.getVisibility() == View.GONE){											// if the list is not there
					pDb.Cleanup();																		// close the original cursor just incase
					adapter.changeCursor(pDb.GetAllParentMessages());									// change it to a new one
					messageList.setVisibility(View.VISIBLE);											// show the listview
					messageList.bringToFront();															// make sure it is able to be seen
				}
				else{
					messageList.setVisibility(View.GONE);												// else just make it invisible again
					pDb.Cleanup();																		// and close the cursor
				}
					
			}
    	});
    	
    	//	header
    	//		brings up the createNewMessageDialog
    	header.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				createNewMessageDialog();																// bring it up	
			}
    	});
    }
    
    // -- Getters --
    
    /*	isEnabled
     * 		gets if it is enabled or not from the preferences
     */
    public int isEnabled(){
    	SharedPreferences prefs = getSharedPreferences(PREFS,MODE_PRIVATE);								// get the preferences
    	return prefs.getInt(MESSAGE_ENABLED_KEY, NO_MESSAGE_SELECTED);									// return if it is enabled or not
    }
    
    /*	getEndTime
     * 		gets the difference between the current time and endtime of the message
     * 		for the alarm manager to be set
     */
    private int getEndTime(){
    	SharedPreferences prefs = this.getSharedPreferences(PREFS, Context.MODE_PRIVATE);				// get the preferences
    	Calendar current = Calendar.getInstance(); 														// get the current time
    	Calendar end = Calendar.getInstance();															// and a instance to set to the end time
    	end.set(prefs.getInt("END_TIME_YEAR", 0), prefs.getInt("END_TIME_MONTH", 0),					// set end 
    			prefs.getInt("END_TIME_DAY", 0), prefs.getInt("END_TIME_HOUR", 0), 
    			prefs.getInt("END_TIME_MIN", 0));
    	if(end.getTimeInMillis() == 0)																	// error checking
    		return(-1);																					
    	
    	int difference = (int)(end.getTimeInMillis() - current.getTimeInMillis());						// get the difference of end - current in mills
    	difference = difference - end.get(Calendar.SECOND)*1000;										// also you need to get the seconds
    	return(difference);																				// return it to the alarm manager
    }
    
    // -- Dialog/Toast Functions --
    
    
    
    public void popToast(String t){
    	Toast.makeText(this, t, Toast.LENGTH_LONG).show();
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
					HomeScreenActivity.adapter.changeCursor(pDb.GetAllParentMessages());
				}
				messageList.setVisibility(View.GONE);
				changeCurrent();
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
    	SharedPreferences prefs = getSharedPreferences(PREFS, Activity.MODE_PRIVATE);
    	inputMessage.setTextColor(Color.WHITE);
    	enableButton.setImageResource(R.drawable.enabled_message_selector);
    	// enable listener
    	enableButton.setClickable(true);
    	editor.putInt(DB_ID_KEY, mCurrent.getID());
    	editor.putInt(MESSAGE_ENABLED_KEY, MESSAGE_ENABLED);
    	//save previous ringer volume
    	int volumePref = prefs.getInt(ENABLED_VOL, SILENT);
    	editor.putInt(PREVIOUS_RINGER_MODE,audiomanage.getRingerMode());
    	editor.putInt(PREVIOUS_VOL, audiomanage.getStreamVolume(AudioManager.STREAM_RING));
    	switch(volumePref){
    	case SILENT:
    		//
    		audiomanage.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    		break;
    	case VIBRATE:
    		//
    		audiomanage.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
    		break;
    	case LOW_VOLUME:
    		//
    		audiomanage.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        	audiomanage.setStreamVolume(AudioManager.STREAM_RING, audiomanage.getStreamMaxVolume(AudioManager.STREAM_RING)/4, 0);
    		break;
    	case MEDIUM_VOLUME:
    		//
    		audiomanage.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        	audiomanage.setStreamVolume(AudioManager.STREAM_RING, audiomanage.getStreamMaxVolume(AudioManager.STREAM_RING)/2, 0);
    		break;
    	case HIGH_VOLUME:
    		//
    		audiomanage.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        	audiomanage.setStreamVolume(AudioManager.STREAM_RING, audiomanage.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
    		break;
    	case MAINTAIN:
    		//do nothing
    		break;
    	}
    	
//    	check user preference for volume on disable
    	
    	
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
    	int volumePref = prefs.getInt(DISABLED_VOL, MAINTAIN);
    	switch(volumePref){
    	case SILENT:
    		//
    		audiomanage.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    		break;
    	case VIBRATE:
    		//
    		audiomanage.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
    		break;
    	case LOW_VOLUME:
    		//
    		audiomanage.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        	audiomanage.setStreamVolume(AudioManager.STREAM_RING, audiomanage.getStreamMaxVolume(AudioManager.STREAM_RING)/4, 0);
    		break;
    	case MEDIUM_VOLUME:
    		//
    		audiomanage.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        	audiomanage.setStreamVolume(AudioManager.STREAM_RING, audiomanage.getStreamMaxVolume(AudioManager.STREAM_RING)/2, 0);
    		break;
    	case HIGH_VOLUME:
    		//
    		audiomanage.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        	audiomanage.setStreamVolume(AudioManager.STREAM_RING, audiomanage.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
    		break;
    	case MAINTAIN:
    		int previousVolume = prefs.getInt(PREVIOUS_VOL, 0);
    		int previousRingMode = prefs.getInt(PREVIOUS_RINGER_MODE,audiomanage.getRingerMode());
    		audiomanage.setStreamVolume(AudioManager.STREAM_RING,previousVolume, 0);
    		audiomanage.setRingerMode(previousRingMode);
    		//do nothing
    		break;
    	}
    	
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

    private void editCurrentMessage(String t){
    	mCurrent.setText(t);
    	inputMessage.setText(mCurrent.text);
    	
    	MessageActivity.changeMessage(mCurrent);
    }
    private void changeCurrent(long db_id){
    	Log.d(TAG,"in changeCurrent");
    	Message temp = pDb.GetParentById((int) db_id);
    	mCurrent = temp;
    	Log.d("MCNULL",String.valueOf(mCurrent == null));
    	changeCurrent();
   	}
    
    private void changeCurrent(){
    	MessageActivity.changeMessage(mCurrent);
    	if(mCurrent == null){
    		Log.d("mCurrent","null");
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