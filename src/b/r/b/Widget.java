package b.r.b;

import static b.r.b.Constants.*;
import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.util.Log;
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
	// Views
	
	ImageButton enableButton;
	TextView inputMessage;
	boolean widgetEnabled = false;
	
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	this.context = context;
    	RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
    	RemoteViews remoteViewsA = new RemoteViews(context.getPackageName(), R.layout.widget);

    	Intent active = new Intent(context, Widget.class);
    	active.setAction(ACTION_WIDGET_RECEIVER);

    	active.putExtra("msg", "BRB Enabled");
    	
    	PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);
    	remoteViews.setOnClickPendingIntent(R.id.widget_button, actionPendingIntent);
    	
    	
    	Intent textActive = new Intent(context, Widget.class);
    	textActive.setAction(ACTION_WIDGET_TEXTVIEW);
    	
    	textActive.putExtra("msg2", "Text Clicked");
    	
    	PendingIntent textPendingIntent = PendingIntent.getBroadcast(context, 0, textActive, 0);
    	remoteViewsA.setOnClickPendingIntent(R.id.widget_textview, textPendingIntent);

    	appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    	appWidgetManager.updateAppWidget(appWidgetIds, remoteViewsA);
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
    	// v1.5 fix that doesn't call onDelete Action
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
    			String msg = "null";
    			try {
    				msg = intent.getStringExtra("msg");
    			} 
    			catch (NullPointerException e) {
    				Log.e("Error", "msg = null");
    			}
    			SharedPreferences prefs = context.getSharedPreferences(PREFS, Activity.MODE_PRIVATE);
    	    	SharedPreferences.Editor editor = prefs.edit();
    	    	
    	    	
    			
    			
    			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    			
    		}
    		else if (intent.getAction().equals(ACTION_WIDGET_TEXTVIEW)) {
    			String msg2 = "null";
    			
    			try {
    				msg2 = intent.getStringExtra("msg2");
    			} 
    			catch (NullPointerException e) {
    				Log.e("Error", "msg = null");
    			}
    			
    			
    			Toast.makeText(context, msg2, Toast.LENGTH_SHORT).show();

    		}
    		super.onReceive(context, intent);
    	}
    }
    
    public int isEnabled(){
    	SharedPreferences prefs = context.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
    	return prefs.getInt(MESSAGE_ENABLED_KEY, NO_MESSAGE_SELECTED);
    }
    
    private void enableMessage() {
    	AudioManager audiomanage = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    	SharedPreferences.Editor editor = context.getSharedPreferences(PREFS, Activity.MODE_PRIVATE).edit();
    	inputMessage.setTextColor(Color.WHITE);
    	enableButton.setImageResource(R.drawable.enabled_message_selector);
    	// enable listener
    	enableButton.setClickable(true);
    	editor.putInt(MESSAGE_ENABLED_KEY, MESSAGE_ENABLED);
    	editor.putInt("ringer_mode", audiomanage.getRingerMode());
    	audiomanage.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    	editor.commit();
    }
    
    private void disableMessage() {
    	SharedPreferences prefs = context.getSharedPreferences(PREFS, Activity.MODE_PRIVATE);
    	SharedPreferences.Editor editor = prefs.edit();
    	AudioManager audiomanage = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    	editor.putInt(MESSAGE_ENABLED_KEY, MESSAGE_DISABLED);
    	inputMessage.setTextColor(Color.WHITE);
    	enableButton.setImageResource(R.drawable.disabled_button_selector);
    	// disable listener
    	audiomanage.setRingerMode(prefs.getInt("ringer_mode",AudioManager.RINGER_MODE_NORMAL));
    	enableButton.setClickable(true);
    	editor.commit();
    }
    
    private void noMessage() {
    	SharedPreferences.Editor editor = context.getSharedPreferences(PREFS, Activity.MODE_PRIVATE).edit();
    	editor.putInt(MESSAGE_ENABLED_KEY, NO_MESSAGE_SELECTED);
    	inputMessage.setText(NO_MESSAGE);
    	inputMessage.setTextColor(Color.GRAY);
    	enableButton.setImageResource(R.drawable.nothing_button_selector);
    	enableButton.setClickable(false);
    	editor.commit();
    }
}
