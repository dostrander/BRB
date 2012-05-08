/* * * * * * * * * * * * * * * * * * * * * * * 
 * BRB-Android
 * Widget.java
 * 
 * Created: 2012
 * 
 * Evan Dodge, Derek Ostrander, Max Shwenk
 * Jason Mather, Stuart Lang, Will Stahl
 * 
 * * * * * * * * * * * * * * * * * * * * * * */

/* Color Scheme
 * Button:
 * 	- Grey = No Message
 * 	- Red = Disabled
 * 	- Green = Enabled
 */

package b.r.b;

import static b.r.b.Constants.*;
import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class Widget extends AppWidgetProvider {
	// Names for the actions so we can identify which button was called
	public static String ACTION_WIDGET_TEXTVIEW = "ActionTextViewWidget";
	public static String ACTION_WIDGET_ENABLE_BUTTON = "ActionEnableButtonWidget";
	public static String ACTION_WIDGET_LEFT_ARROW = "ActionLeftArrowWidget";
	public static String ACTION_WIDGET_RIGHT_ARROW = "ActionRightArrowWidget";
	public static String ACTION_WIDGET_ICON = "ActionIconWidget";
	
	// Variables
	private Context context; // Context for the Widget
	public static ParentInteraction pDb; // Widget Database Interaction
	
	// Remote View - Set to the widget layout
	RemoteViews remoteViews = new RemoteViews("b.r.b", R.layout.widget);
	
	// Database Variables
	int db_id; // The database ID - updated through preferences
	Cursor cursor; // Cursor for dealing with the Database
	
	// Shared Preferences Variables
	SharedPreferences prefs;
	SharedPreferences.Editor editor;
	
	// Log tag
	private static final String TAG = "Widget";
	
	
	/*  onUpdate
	 *	Called on a millisecond delay defined in /res/xml/widget_provider.xml
	 *	Sends broadcasts to buttons to update the widget when click listeners for
	 *	the BRB icon, left, right, and enable buttons. 
	 */
	@Override
    public void onUpdate(Context ctx, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.d(TAG, "onUpdate");

		context = ctx; 	// Set the global context equal to the context passed
    				   	// to onUpdate
    	
    	// Preferences Variables Initialized
    	prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    	editor = prefs.edit();
    	
    	// Database variables
    	db_id = prefs.getInt(DB_ID_KEY, -1); // Get the database ID from sharedpreferences
    	pDb = new ParentInteraction(context);
    	cursor = pDb.GetAllParentMessages();  
    	
    	// Make intent and pending intent for on receive
    	Intent active = new Intent(context, Widget.class);
    	PendingIntent actionPendingIntent;
    	
    	// Enable Button
    	active.setAction(ACTION_WIDGET_ENABLE_BUTTON);
    	actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);
    	remoteViews.setOnClickPendingIntent(R.id.widget_button, actionPendingIntent);
    	
    	// Left Arrow
    	active.setAction(ACTION_WIDGET_LEFT_ARROW);
    	actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);
    	remoteViews.setOnClickPendingIntent(R.id.widget_left_button, actionPendingIntent);
    	
    	// Right Arrow
    	active.setAction(ACTION_WIDGET_RIGHT_ARROW);
    	actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);
    	remoteViews.setOnClickPendingIntent(R.id.widget_right_button, actionPendingIntent);
    	
    	// Icon
    	active.setAction(ACTION_WIDGET_ICON);
    	actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);
    	remoteViews.setOnClickPendingIntent(R.id.widget_icon, actionPendingIntent);
    	
    	// Update everything to match the app when the widget is created
    	if(db_id != -1 && prefs.getInt(DB_ID_KEY, -1) == MESSAGE_DISABLED) {
    		cursor.moveToPosition(db_id-1);
    		remoteViews.setTextViewText(R.id.widget_textview,cursor.getString(cursor.getColumnIndex(MESSAGE)));
    	}
    	if(prefs.getInt(DB_ID_KEY, -1) == MESSAGE_ENABLED)
    		remoteViews.setImageViewResource(R.id.widget_button, R.drawable.disabled_button_selector);
    	else if (prefs.getInt(DB_ID_KEY, -1) == MESSAGE_DISABLED && db_id != -1)
    		remoteViews.setImageViewResource(R.id.widget_button, R.drawable.enabled_message_selector);
    	cursor.close();
    	pDb.Cleanup();
    	// Update using appWidgetManager
    	appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }
   
	
	/*  onReceive
	 * 
	 */
	
	
    @Override
    public void onReceive(Context ctx, Intent intent) {
    	Log.d(TAG, "onRecieve");
    	
    	context = ctx; // Set the context
    	
    	// Initialize the database stuff
    	pDb = new ParentInteraction(context);
    	cursor = pDb.GetAllParentMessages();
    	
    	// Get sharedpreferences and the editor ready
    	prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    	editor = prefs.edit();
    	
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * A workaround that makes it so that we call onDeleted() from here so that 
 * the widget doesn't prematurely lock the database.
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    	final String action = intent.getAction();
    	if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
    		final int appWidgetId = intent.getExtras().getInt(
    				AppWidgetManager.EXTRA_APPWIDGET_ID,
    				AppWidgetManager.INVALID_APPWIDGET_ID);
    		if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
    			this.onDeleted(context, new int[] { appWidgetId });
    		}
    	}
    	else {

    		// If the enable button is clicked
    		if (intent.getAction().equals(ACTION_WIDGET_ENABLE_BUTTON)) {
    			SharedPreferences prefs = context.getSharedPreferences(PREFS, Activity.MODE_PRIVATE);
    	    	SharedPreferences.Editor editor = prefs.edit();
    	    	
    	    	// Get the enabled key
    	    	int enableStatus = prefs.getInt(MESSAGE_ENABLED_KEY, NO_MESSAGE_SELECTED);
    	    	
    	    	/* * * * * * * * * * * * * * * * * * * * * * * *
    	    	 * Enabling the widget
    	    	 * * * * * * * * * * * * * * * * * * * * * * * */
    	    	if (enableStatus == MESSAGE_DISABLED)	// Since the status is disabled, clicking the button would
    	    	{										// result in the app enabling
    	    		// Set the icon image and then update the app
    	    		remoteViews.setImageViewResource(R.id.widget_button, R.drawable.enabled_message_selector);
    	    		ComponentName cn = new ComponentName(context,Widget.class);
    	    		AppWidgetManager.getInstance(context).updateAppWidget(cn, remoteViews);
    	    		
    	    		// Silence Ringer
    	    		AudioManager audiomanage = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    	    		editor.putInt("ringer_mode", audiomanage.getRingerMode());
    	        	audiomanage.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    	    		
    	        	// Make a toast that informs the user that BRB is enabled
    	    		Toast.makeText(context, "BRB Enabled", Toast.LENGTH_SHORT).show();
    	    		
    	    		// Set the sharedpreferences
    	    		editor.putInt(MESSAGE_ENABLED_KEY, MESSAGE_ENABLED);
    	    		editor.commit();
    	    	}
    	    	
    	    	/* * * * * * * * * * * * * * * * * * * * * * * *
    	    	 * Disabling the widget
    	    	 * * * * * * * * * * * * * * * * * * * * * * * */
    	    	else if (enableStatus == MESSAGE_ENABLED) 	// Since the status is enabled, clicking the button would
    	    	{										  	// result in the app disabling
    	    		// Set the icon image and then update the app
    	    		remoteViews.setImageViewResource(R.id.widget_button, R.drawable.disabled_button_selector);
    	    		ComponentName cn = new ComponentName(context,Widget.class);
    	    		AppWidgetManager.getInstance(context).updateAppWidget(cn, remoteViews);
    	    		
    	    		// Reset ringer
    	    		AudioManager audiomanage = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    	    		audiomanage.setRingerMode(prefs.getInt("ringer_mode",AudioManager.RINGER_MODE_NORMAL));
    	    		
    	    		// Make a toast that informs the user that BRB is disabled
    	    		Toast.makeText(context, "BRB Disabled", Toast.LENGTH_SHORT).show();
    	    		
    	    		// Set the sharedpreferences
    	    		editor.putInt(MESSAGE_ENABLED_KEY, MESSAGE_DISABLED);
    	    		editor.commit();
    	    	}
    	    	else{
    	    		// There shouldn't be an else, but who knows really...
    	    	}
    		}
    		
    		/* * * * * * * * * * * * * * * * * * * * * * * *
	    	 * Left Arrow Clicked
	    	 * * * * * * * * * * * * * * * * * * * * * * * */
    		else if (intent.getAction().equals(ACTION_WIDGET_LEFT_ARROW)) {
    			
    			// Find out if the message is enabled
    			int enableStatus = prefs.getInt(MESSAGE_ENABLED_KEY, NO_MESSAGE_SELECTED);
    			
    			// If there are messages in the Database and Message is not enabled
    			if(cursor.getCount() > 0 && enableStatus != MESSAGE_ENABLED)
    			{
    				db_id = prefs.getInt(DB_ID_KEY, -1); // Get the database ID from sharedPreferences
    				db_id--;	// Decrement the ID because we are going to the left
    		
    				if(db_id < 0) // If the dbID is less than zero than set it to the last spot in the DB
    					db_id = cursor.getCount() - 1;
    			
    				// Move the cursor into position and then set the textview to that message
    				cursor.moveToPosition(db_id);
    				remoteViews.setTextViewText(R.id.widget_textview,cursor.getString(cursor.getColumnIndex(MESSAGE)));

    				// Set the image of the enable button
    				remoteViews.setImageViewResource(R.id.widget_button, R.drawable.disabled_button_selector);
    	    		
    				// Save the preferences
    				editor.putInt(MESSAGE_ENABLED_KEY, MESSAGE_DISABLED);
    				editor.putInt(DB_ID_KEY, db_id);
    				editor.commit();
    			
    				// Update the widget
    				ComponentName comn = new ComponentName(context,Widget.class);
    				AppWidgetManager.getInstance(context).updateAppWidget(comn, remoteViews);
    			}
    		}
    		
    		/* * * * * * * * * * * * * * * * * * * * * * * *
	    	 * Right Arrow Clicked
	    	 * * * * * * * * * * * * * * * * * * * * * * * */
    		else if (intent.getAction().equals(ACTION_WIDGET_RIGHT_ARROW)){
    			
    			// Find out if the message is enabled
    			int enableStatus = prefs.getInt(MESSAGE_ENABLED_KEY, NO_MESSAGE_SELECTED);
    			
    			// If there are messages in the Database and Message is not enabled
    			if(cursor.getCount() > 0 && enableStatus != MESSAGE_ENABLED)
    			{
    				db_id = prefs.getInt(DB_ID_KEY, -1);	// Get the database ID from sharedPreferences
    				db_id++;	// Increment the ID because we are going to the left

    				if(db_id >= cursor.getCount()) // If the dbID is higher than the last spot in the DB, set it to zero
    					db_id = 0;
    			
    				// Move the cursor into position and then set the textview to that message
    				cursor.moveToPosition(db_id);
    				remoteViews.setTextViewText(R.id.widget_textview,cursor.getString(cursor.getColumnIndex(MESSAGE)));
    			
    				// Set the image of the enable button
    				remoteViews.setImageViewResource(R.id.widget_button, R.drawable.disabled_button_selector);
    	    		
    				// Save the preferences
    				editor.putInt(MESSAGE_ENABLED_KEY, MESSAGE_DISABLED);
    				editor.putInt(DB_ID_KEY, db_id);
    				editor.commit();
    			
    				// Update the widget
    				ComponentName comn = new ComponentName(context,Widget.class);
    				AppWidgetManager.getInstance(context).updateAppWidget(comn, remoteViews);
    			}
    		}
    		
    		/* * * * * * * * * * * * * * * * * * * * * * * *
	    	 * Icon Clicked
	    	 * * * * * * * * * * * * * * * * * * * * * * * */
    		else if (intent.getAction().equals(ACTION_WIDGET_ICON)){
    			
    			// Create a new package manager for launching an activity
    			PackageManager pm = context.getPackageManager();
    			
    			// Launch the main BRB App
    			try {
    			    String packageName = "b.r.b";
    			    Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
    			    context.startActivity(launchIntent);

    			}
    			catch (Exception e1) {
    			}
    		}
		   
    		super.onReceive(context, intent);
    	}
    	 pDb.Cleanup();
    	 cursor.close();
    }
    
    
	/*  onDeleted
	 * 	When one or more instances of the widget have been deleted 
	 * 	from the user's homescreen
	 * 
	 */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
    	cursor.close();
    	pDb.Cleanup();
    	super.onDeleted(context, appWidgetIds);
        Log.d(TAG, "onDeleted");
    }
    
    
	/*  onEnabled
	 * When the widget is created on the user's homescreen, follow the
	 * 	default procedure for the app widget provider class we're extending.
	 */
    @Override
    public void onEnabled(Context context) {
    	super.onEnabled(context);
        Log.d(TAG, "onEnabled");
        
    }
    
    
	/*  onDisabled
	 *  When all instances of the widget have been deleted from the homescreen.
	 */
    @Override
    public void onDisabled(Context context) {
    	cursor.close();
    	pDb.Cleanup();
    	super.onDisabled(context);
        Log.d(TAG, "onDiasbled");
    }
}
