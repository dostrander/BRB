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
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

public class Widget extends AppWidgetProvider {
	public static String ACTION_WIDGET_TEXTVIEW = "ActionTextViewWidget";
	public static String ACTION_WIDGET_RECEIVER = "ActionReceiverWidget";
	public static String NO_MESSAGE = "Enter Text Here...";
	
	// Variables
	IncomingListener listener;
	private Context context;
	// View
	RemoteViews remoteViews = new RemoteViews("b.r.b", R.layout.widget);
	RemoteViews remoteViewsA = new RemoteViews("b.r.b", R.layout.widget);
	
	ImageButton enableButton;
	TextView inputMessage;
	boolean widgetEnabled = false;
	SharedPreferences prefs;
	SharedPreferences.Editor editor;
	
    public void onUpdate(Context ctx, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	context = ctx;
    	prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    	editor = prefs.edit();
    	
    	Intent active = new Intent(context, Widget.class);
    	active.setAction(ACTION_WIDGET_RECEIVER);
    	
    	PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);
    	remoteViews.setOnClickPendingIntent(R.id.widget_button, actionPendingIntent);
    	
    	Intent textActive = new Intent(context, Widget.class);
    	textActive.setAction(ACTION_WIDGET_TEXTVIEW);
    	
    	PendingIntent textPendingIntent = PendingIntent.getBroadcast(context, 0, textActive, 0);
    	remoteViewsA.setOnClickPendingIntent(R.id.widget_textview, textPendingIntent);

    	appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    	appWidgetManager.updateAppWidget(appWidgetIds, remoteViewsA);
    }
   
    @Override
    public void onReceive(Context ctx, Intent intent) {
    	// v1.5 fix that doesn't call onDelete Action
    	context = ctx;
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
    		else if (intent.getAction().equals(ACTION_WIDGET_TEXTVIEW)) {
    			String msg2 = "TextView Selected";
    			
    			Toast.makeText(context, msg2, Toast.LENGTH_SHORT).show();

    		}
    		super.onReceive(context, intent);
    	}
    }
}
