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
import android.database.Cursor;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

public class Widget extends AppWidgetProvider {
	public static String ACTION_WIDGET_TEXTVIEW = "ActionTextViewWidget";
	public static String ACTION_WIDGET_RECEIVER = "ActionReceiverWidget";
	public static String ACTION_WIDGET_LEFT = "ActionLeftWidget";
	public static String ACTION_WIDGET_RIGHT = "ActionRightWidget";
	
	// Variables
	private Context context;
	public static DatabaseInteraction db;
	
	
	// View
	RemoteViews remoteViews = new RemoteViews("b.r.b", R.layout.widget);
	RemoteViews remoteViewsA = new RemoteViews("b.r.b", R.layout.widget);
	RemoteViews remoteViewsLeft = new RemoteViews("b.r.b", R.layout.widget);
	RemoteViews remoteViewsRight = new RemoteViews("b.r.b", R.layout.widget);
	
	
	int db_id;
	ImageButton enableButton;
	Cursor cursor;
	TextView inputMessage;
	boolean widgetEnabled = false;
	SharedPreferences prefs;
	SharedPreferences.Editor editor;
	
    public void onUpdate(Context ctx, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	context = ctx;
    	prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    	db_id = prefs.getInt(DB_ID_KEY, -1);
    	editor = prefs.edit();
    	db = new DatabaseInteraction(context);
    	cursor = db.GetAllParentMessages();
    	
    	// Intent for enableButton
    	Intent active = new Intent(context, Widget.class);
    	active.setAction(ACTION_WIDGET_RECEIVER);
    	
    	PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);
    	remoteViews.setOnClickPendingIntent(R.id.widget_button, actionPendingIntent);
    	
    	// Intent for textView
    	Intent textActive = new Intent(context, Widget.class);
    	textActive.setAction(ACTION_WIDGET_TEXTVIEW);
    	
    	PendingIntent textPendingIntent = PendingIntent.getBroadcast(context, 0, textActive, 0);
    	remoteViewsA.setOnClickPendingIntent(R.id.widget_textview, textPendingIntent);
    	
    	// Intent for leftArrow
    	Intent activeLeft = new Intent(context, Widget.class);
    	activeLeft.setAction(ACTION_WIDGET_LEFT);
    	
    	PendingIntent leftPendingIntent = PendingIntent.getBroadcast(context, 0, activeLeft, 0);
    	remoteViewsLeft.setOnClickPendingIntent(R.id.widget_left_button, leftPendingIntent);
    	
    	// Intent for rightArrow
    	Intent activeRight = new Intent(context, Widget.class);
    	activeRight.setAction(ACTION_WIDGET_RIGHT);
    	
    	PendingIntent rightPendingIntent = PendingIntent.getBroadcast(context, 0, activeRight, 0);
    	remoteViewsRight.setOnClickPendingIntent(R.id.widget_right_button, rightPendingIntent);

    	appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    	appWidgetManager.updateAppWidget(appWidgetIds, remoteViewsA);
    	appWidgetManager.updateAppWidget(appWidgetIds, remoteViewsLeft);
    	appWidgetManager.updateAppWidget(appWidgetIds, remoteViewsRight);
    }
   
    @Override
    public void onReceive(Context ctx, Intent intent) {
    	// v1.5 fix that doesn't call onDelete Action
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
    		// check, if our Action was called
    		if (intent.getAction().equals(ACTION_WIDGET_RECEIVER)) {
    			SharedPreferences prefs = context.getSharedPreferences(PREFS, Activity.MODE_PRIVATE);
    	    	SharedPreferences.Editor editor = prefs.edit();
    	    	
    	    	int enableStatus = prefs.getInt(MESSAGE_ENABLED_KEY, NO_MESSAGE_SELECTED);
    	    	if (enableStatus == MESSAGE_ENABLED)
    	    	{
    	    		remoteViews.setImageViewResource(R.id.widget_button, R.drawable.disabled_button_selector);
    	    		ComponentName cn = new ComponentName(context,Widget.class);
    	    		AppWidgetManager.getInstance(context).updateAppWidget(cn, remoteViews);
    	    		
    	    		Toast.makeText(context, "BRB Enabled", Toast.LENGTH_SHORT).show();
    	    		editor.putInt(MESSAGE_ENABLED_KEY, MESSAGE_DISABLED);
    	    		editor.commit();
    	    	}
    	    	else if (enableStatus == MESSAGE_DISABLED)
    	    	{
    	    		remoteViews.setImageViewResource(R.id.widget_button, R.drawable.enabled_message_selector);
    	    		ComponentName cn2 = new ComponentName(context,Widget.class);
    	    		AppWidgetManager.getInstance(context).updateAppWidget(cn2, remoteViews);
    	    		
    	    		Toast.makeText(context, "BRB Disabled", Toast.LENGTH_SHORT).show();
    	    		editor.putInt(MESSAGE_ENABLED_KEY, MESSAGE_ENABLED);
    	    		editor.commit();
    	    	}
    	    	else
    	    	{
    	    		Toast.makeText(context, "No Message", Toast.LENGTH_SHORT).show();
    	    		editor.putInt(MESSAGE_ENABLED_KEY, MESSAGE_ENABLED);
    	    		editor.commit();
    	    	}
    		}
    		else if (intent.getAction().equals(ACTION_WIDGET_LEFT)) {
    			db_id = prefs.getInt(DB_ID_KEY, -1);
    			db_id--;
    		
    			if(db_id < 0)
    				db_id = cursor.getCount() - 1;
    			
        		cursor.moveToPosition(db_id);
        			//remoteViewsA.setTextViewText(R.id.widget_textview,"Left");
    			remoteViewsA.setTextViewText(R.id.widget_textview,cursor.getString(cursor.getColumnIndex(MESSAGE)));

    			editor.putInt(DB_ID_KEY, db_id);
	    		editor.commit();
    			
       			ComponentName comn = new ComponentName(context,Widget.class);
	    		AppWidgetManager.getInstance(context).updateAppWidget(comn, remoteViewsA);
    		}
    		else if (intent.getAction().equals(ACTION_WIDGET_RIGHT)){
    			db_id = prefs.getInt(DB_ID_KEY, -1);
    			db_id++;

    			if(db_id >= cursor.getCount())
    				db_id = 0;
    			
        		cursor.moveToPosition(db_id);
    			remoteViewsA.setTextViewText(R.id.widget_textview,cursor.getString(cursor.getColumnIndex(MESSAGE)));
    			
    			editor.putInt(DB_ID_KEY, db_id);
	    		editor.commit();
    			
    			ComponentName comn = new ComponentName(context,Widget.class);
	    		AppWidgetManager.getInstance(context).updateAppWidget(comn, remoteViewsA);
    		}
    		
    		super.onReceive(context, intent);
    	}
    }
}
