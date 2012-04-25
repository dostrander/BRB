/*********************************************
 * BRB-Android
 * Widget.java
 * 
 * Created: 2012
 * 
 * Evan Dodge, Derek Ostrander, Max Shwenk
 * Jason Mather, Stuart Lang, Will Stahl
 * 
 *********************************************/

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
	public static DatabaseInteraction db; // Widget Database Interaction
	
	// Views
	RemoteViews remoteViews = new RemoteViews("b.r.b", R.layout.widget);
	
	// Database Variables
	int db_id;
	Cursor cursor;
	
	// Shared Preferences Variables
	SharedPreferences prefs;
	SharedPreferences.Editor editor;
	
	// Log tag
	private static final String TAG = "Widget";
	
	
	/*  onUpdate
	 *
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
    	db_id = prefs.getInt(DB_ID_KEY, -1);
    	db = new DatabaseInteraction(context);
    	cursor = db.GetAllParentMessages();   	
    	
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
    	
    	// Update using appWidgetManager
    	appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }
   
	
	/*  onReceive
	 * 
	 */
    @Override
    public void onReceive(Context ctx, Intent intent) {
    	Log.d(TAG, "onRecieve");
    	
    	context = ctx;
    	db = new DatabaseInteraction(context);
    	cursor = db.GetAllParentMessages();
    	
    	prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    	editor = prefs.edit();
    	
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
    			// Enable button clicked
    		if (intent.getAction().equals(ACTION_WIDGET_ENABLE_BUTTON)) {
    			SharedPreferences prefs = context.getSharedPreferences(PREFS, Activity.MODE_PRIVATE);
    	    	SharedPreferences.Editor editor = prefs.edit();
    	    	
    	    	int enableStatus = prefs.getInt(MESSAGE_ENABLED_KEY, NO_MESSAGE_SELECTED);
    	    	
    	    	if (enableStatus == MESSAGE_DISABLED)
    	    	{
    	    		remoteViews.setImageViewResource(R.id.widget_button, R.drawable.enabled_message_selector);
    	    		ComponentName cn = new ComponentName(context,Widget.class);
    	    		AppWidgetManager.getInstance(context).updateAppWidget(cn, remoteViews);
    	    		
    	    		Toast.makeText(context, "BRB Enabled", Toast.LENGTH_SHORT).show();
    	    		editor.putInt(MESSAGE_ENABLED_KEY, MESSAGE_ENABLED);
    	    		editor.commit();
    	    	}
    	    	else if (enableStatus == MESSAGE_ENABLED)
    	    	{
    	    		remoteViews.setImageViewResource(R.id.widget_button, R.drawable.disabled_button_selector);
    	    		ComponentName cn2 = new ComponentName(context,Widget.class);
    	    		AppWidgetManager.getInstance(context).updateAppWidget(cn2, remoteViews);
    	    		
    	    		Toast.makeText(context, "BRB Disabled", Toast.LENGTH_SHORT).show();
    	    		editor.putInt(MESSAGE_ENABLED_KEY, MESSAGE_DISABLED);
    	    		editor.commit();
    	    	}
    	    	else
    	    	{
    	    		Toast.makeText(context, "No Message", Toast.LENGTH_SHORT).show();
    	    		editor.putInt(MESSAGE_ENABLED_KEY, MESSAGE_ENABLED);
    	    		editor.commit();
    	    	}
    		}
    		
    		// If the Left Arrow is clicked
    		else if (intent.getAction().equals(ACTION_WIDGET_LEFT_ARROW)) {
    			
    			int enableStatus = prefs.getInt(MESSAGE_ENABLED_KEY, NO_MESSAGE_SELECTED);
    			
    			if(cursor.getCount() > 0 && enableStatus != MESSAGE_ENABLED)
    			{
    				db_id = prefs.getInt(DB_ID_KEY, -1);
    				db_id--;
    		
    				if(db_id < 0)
    					db_id = cursor.getCount() - 1;
    			
    				cursor.moveToPosition(db_id);
    				remoteViews.setTextViewText(R.id.widget_textview,cursor.getString(cursor.getColumnIndex(MESSAGE)));

    				editor.putInt(DB_ID_KEY, db_id);
    				editor.commit();
    			
    				ComponentName comn = new ComponentName(context,Widget.class);
    				AppWidgetManager.getInstance(context).updateAppWidget(comn, remoteViews);
    			}
    		}
    		
    		// If the Right Arrow is clicked
    		else if (intent.getAction().equals(ACTION_WIDGET_RIGHT_ARROW)){
    			int enableStatus = prefs.getInt(MESSAGE_ENABLED_KEY, NO_MESSAGE_SELECTED);
    			
    			if(cursor.getCount() > 0 && enableStatus != MESSAGE_ENABLED)
    			{
    				db_id = prefs.getInt(DB_ID_KEY, -1);
    				db_id++;

    				if(db_id >= cursor.getCount())
    					db_id = 0;
    			
    				cursor.moveToPosition(db_id);
    				remoteViews.setTextViewText(R.id.widget_textview,cursor.getString(cursor.getColumnIndex(MESSAGE)));
    			
    				editor.putInt(DB_ID_KEY, db_id);
    				editor.commit();
    			
    				ComponentName comn = new ComponentName(context,Widget.class);
    				AppWidgetManager.getInstance(context).updateAppWidget(comn, remoteViews);
    			}
    		}
    		
    		// If the Icon is clicked on
    		else if (intent.getAction().equals(ACTION_WIDGET_ICON)){
    			PackageManager pm = context.getPackageManager();
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
    }
    
    
	/*  onDeleted
	 * 
	 */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
    	super.onDeleted(context, appWidgetIds);
        Log.d(TAG, "onDeleted");
    }
    
    
	/*  onEnabled
	 * 
	 */
    @Override
    public void onEnabled(Context context) {
    	super.onEnabled(context);
        Log.d(TAG, "onEnabled");
    }
    
    
	/*  onDisabled
	 * 
	 */
    @Override
    public void onDisabled(Context context) {
    	super.onDisabled(context);
        Log.d(TAG, "onDiasbled");
    }
}
