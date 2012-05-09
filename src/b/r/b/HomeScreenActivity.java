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
    
    
    /*	popToast
     * 		Shows a short text dialog to the user
     */
    public void popToast(String t){
    	Toast.makeText(this, t, Toast.LENGTH_LONG).show();		
    }
    
    /*	createNewMessageDialog
     * 		shows a dialog for creating a new message, is used in the header of the listview
     * 		and if no message is selected it will by default go here
     */
    private void createNewMessageDialog(){
		int myDialogColor = Color.rgb(33, 66, 99);														// get color for scrolling
		LinearLayout ll = new LinearLayout(HomeScreenActivity.this);									// layout holdig the dialog
		ll.setOrientation(LinearLayout.VERTICAL);														// make the orientation verticle
		final EditText input = new EditText(HomeScreenActivity.this);									// for creating a new message
		final ListView lv = new ListView(HomeScreenActivity.this);										// for selecting an exisitng message
		Cursor c = pDb.GetAllParentMessages();															// get a cursor for all of the messages
		ArrayList<String> temp = new ArrayList<String>();												// to hold the messages
		// puts all the messages in the cursor into temp
		if(c.moveToFirst()){																			// move the cursor to the first row
			do temp.add(c.getString(c.getColumnIndex(MESSAGE))); 										// add the message
			while(c.moveToNext());																		// while it is not after the last
		}
		pDb.Cleanup();																					// close the parent cursor
		String[] messages = temp.toArray(new String[]{});												// make the arraylist to a string
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(HomeScreenActivity.this,			// make the adapter for the listview
				R.layout.dialog_message_item,R.id.textView1, messages);
		input.setLines(2);																				// only 2 lines
		input.setGravity(Gravity.TOP);																	// make the textview cursor at the top
		input.setHint("Start typing message...");														// before the user starts writing it says this
		input.addTextChangedListener(new TextWatcher(){													// for filtering
			public void afterTextChanged(Editable e) {}
			public void beforeTextChanged(CharSequence s, int arg1,
					int arg2, int arg3) {}
			public void onTextChanged(CharSequence s, int start,										// when the text is changing
					int before, int count) {adapter.getFilter().filter(s);}	});							// tell the adapter to filter the listview
		ll.setBackgroundColor(myDialogColor);															// set the color to look like a dialog												
		lv.setBackgroundColor(myDialogColor);															// set the color to look like a dialog
		lv.setCacheColorHint(myDialogColor);															// set the color to look like a dialog
		lv.setAdapter(adapter);																			// set the adapter to the listview
		lv.setOnItemClickListener(new OnItemClickListener(){											// when one of the items is clicked
			public void onItemClick(AdapterView<?> adap, View v,
					int position, long id) {
				input.setText(adapter.getItem(position));												// set the edit text to that message
				input.setSelection(input.getText().length());											// set the cursor to the end
			}
		});
		ll.addView(input);																				// add the edittext to the layout
		ll.addView(lv);																					// add the listview to the dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreenActivity.this);					// for building the dialog
		builder.setTitle("Create New Message")															// title
		.setView(ll)																					// set the view to the linear layout
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {								// when the user clicks ok
			public void onClick(DialogInterface dialog, int whichButton) {
				String text = input.getText().toString().trim().toString();								// get the input
				mCurrent = pDb.GetParentByMessage(text);												// change current if there is one
				
				if(mCurrent == null){																	// if there isnt one already
					mCurrent = pDb.InsertMessage(text);													// insert it 
					HomeScreenActivity.adapter.changeCursor(pDb.GetAllParentMessages());				// requery the listview
				}
				messageList.setVisibility(View.GONE);													// set the listview to gone
				changeCurrent();																		// change the current applicationw ide
				pDb.Cleanup();																			// clean up the parent database
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {						// to just cancel the dialog
			public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
				messageList.setVisibility(View.GONE);													// just set the listview to gone
			}
		}).create().show();																				// show the dialog
    }
    
    /*	editTextDialog
     * 		shows the dialog for editing the current message
     */
    private void editTextDialog(){
		LinearLayout ll = new LinearLayout(HomeScreenActivity.this);									// layout for the dialog
		final EditText input = new EditText(HomeScreenActivity.this);									// edit text for editing the message
		input.setText(mCurrent.text);																	// set the text if there is one
		ll.setOrientation(LinearLayout.VERTICAL);														// make the layout verticle
		input.setLines(2);																				// set it multiline
		input.setGravity(Gravity.TOP);																	// set the edit text cursor
		input.setHint("Start typing message...");														// and what is says before the user types
		ll.addView(input);																				// add it to the layout
		AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreenActivity.this);					// for building the dialog
		builder.setTitle("Edit Message")																// title for the dialog
		.setView(ll)																					// set the linear layout as the view
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {								// when the user okays it
			public void onClick(DialogInterface dialog, int whichButton) {				
				// update message
				String text = input.getText().toString().trim().toString();								// get a trimmed down version of the input
				if(text.length() > 0){																	// if there is a message
					if(pDb.ParentEditMessage(mCurrent.getID(), text))									// and it isn't already edited
						editCurrentMessage(text);														// edit the current message
				}
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {						// just to cancel the dialog
			public void onClick(DialogInterface dialog, int whichButton) {}
		}).create().show();																				// show the dialog
    	
    }

    
    /*	enableMessage
     * 		enables the message telling the whole application through the preferences
     * 		as well as through static functions in the other classes. does the logic
     * 		for turning the right ringer volume up and changing the color of the button
     */
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
    	
    	AudioManager audiomanage = (AudioManager)getSystemService(Context.AUDIO_SERVICE);				// get an audio manager for changing ringer volume
    	SharedPreferences.Editor editor = getSharedPreferences(PREFS, Activity.MODE_PRIVATE).edit();	// get the editer to edit the prefs
    	SharedPreferences prefs = getSharedPreferences(PREFS, Activity.MODE_PRIVATE);					// prefs that are already made
    	inputMessage.setTextColor(Color.WHITE);															// change the text color to make it more highlited
    	enableButton.setImageResource(R.drawable.enabled_message_selector);								// change the color of the selector
    	// enable listener
    	enableButton.setClickable(true);																// make it so it is clickable just incase
    	editor.putInt(DB_ID_KEY, mCurrent.getID());														// put the current ID in the prefs
    	editor.putInt(MESSAGE_ENABLED_KEY, MESSAGE_ENABLED);											// tell the preferenes that a message is enabled
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
    
    /*	disableMessage
     * 		disables the message telling the whole application through the preferences
     * 		as well as through static functions in the other classes. does the logic
     * 		for turning the right ringer volume up and changing the color of the button
     */
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
    	
    	SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);										// get the prefs
    	SharedPreferences.Editor editor = prefs.edit();																// and the editor
    	AudioManager audiomanage = (AudioManager)getSystemService(Context.AUDIO_SERVICE);							// get the audiomanager for changing the rigner
    	editor.putInt(MESSAGE_ENABLED_KEY, MESSAGE_DISABLED);														// tell the prefs that the message is disabled
    	inputMessage.setTextColor(Color.WHITE);																		// highlight the text
    	enableButton.setImageResource(R.drawable.disabled_button_selector);											// change the color of the icon
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
    	SharedPreferences.Editor editor = getSharedPreferences(PREFS, Activity.MODE_PRIVATE).edit();								// get the editor
    	editor.putInt(MESSAGE_ENABLED_KEY, NO_MESSAGE_SELECTED);																	// tell the prefs there is no message selected
    	inputMessage.setText(NO_MESSAGE);																							// change the input text
    	inputMessage.setTextColor(Color.GRAY);																						// make the text dull
    	enableButton.setImageResource(R.drawable.nothing_button_selector);															// change the icon
    	mCurrent = null;																											// set the current to null
    	enableButton.setClickable(false);																							// make the enable button not clickable
    	
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

    /*	editCurrentMessage
     * 		just when changing the current message it tells the rest of
     * 		the app that the message ahas changed
     */
    private void editCurrentMessage(String t){
    	mCurrent.setText(t);																										// change the current message text
    	inputMessage.setText(mCurrent.text);																						// change the input text
    	MessageActivity.changeMessage(mCurrent);																					// tell the application
    }
    
    /*	changeCurrent
     * 		changes the current message to a different already made message
     * 		if it is null it will just go to no message selected
     * 		it is overloaded because soemtimes you know it is going to be null
     */
    private void changeCurrent(long db_id){
    	Message temp = pDb.GetParentById((int) db_id);																				// get the new message
    	mCurrent = temp;																											// set mCurrent to temp
    	changeCurrent();																											// check for null
   	}
    private void changeCurrent(){
    	MessageActivity.changeMessage(mCurrent);																					// change message in activity
    	if(mCurrent == null){																										// if current is null
    		noMessage();																											// change it to no message selected
    	}
    	else{																														// if it is not null
    		inputMessage.setText(mCurrent.text);																					// set the input text
    		disableMessage();																										// make it the disable message
    	}
    	
    	// Updates the Widget's Textview
    	Context context = this;
    	AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
    	RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
    	ComponentName thisWidget = new ComponentName(context, Widget.class);

    	remoteViews.setTextViewText(R.id.widget_textview, mCurrent.text);
    	appWidgetManager.updateAppWidget(thisWidget, remoteViews);
    }
    
    
    /*	class MessageListCursorAdapter
     * 		controls the list that drops down underneath the textview at the top
     */
    private class MessageListCursorAdapter extends CursorAdapter {
    	private final int layout;
    	private final int textview_id;
		public MessageListCursorAdapter(Context ctx, int lout, Cursor c,
				String[] from, int[] to){
			super(ctx, c);
			
			layout = lout;
			textview_id = to[0];
		}
		// inflates view and sets tag
		@Override
		public View newView(Context ctx, Cursor cursor, ViewGroup parent){			
			ViewHolder holder = new ViewHolder();							
			View v = LayoutInflater.from(ctx).inflate(layout, null);															
			v.setTag(holder);
			return v;
			
		}
		
		/*	bindView
		 * 		sets the data and click listenerns of the list items
		 */ 		 
		@Override
		public void bindView(View v, Context context, Cursor cursor){
			final ViewHolder holder = (ViewHolder) v.getTag();																		// get the views associated witht his position
			TextView tv = (TextView) v.findViewById(textview_id);																	// get the textview in the layout
			tv.setText(cursor.getString(cursor.getColumnIndex(MESSAGE)));															// set the text view
			holder.text = tv;																										// set the holders textview
			holder.id = cursor.getLong(cursor.getColumnIndex(ID));																	// set the id to the db_id
			v.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {																						// when selecting a message
					if(layout == R.layout.input_message_list_item){																	// if its the right layout
						changeCurrent(holder.id);																					// change the current message
						messageList.setVisibility(View.GONE);																		// make it so the message list is gone
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