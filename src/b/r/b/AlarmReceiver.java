/* * * * * * * * * * * * * * * * * * * * * * * 
 * BRB-Android
 * AlarmReceiver.java
 * 
 * Created: 2012
 * 
 * Evan Dodge, Derek Ostrander, Max Shwenk
 * Jason Mather, Stuart Lang, Will Stahl
 * 
 * * * * * * * * * * * * * * * * * * * * * * */

package b.r.b;

import static b.r.b.Constants.MESSAGE_DISABLED;
import static b.r.b.Constants.MESSAGE_ENABLED_KEY;
import static b.r.b.Constants.PREFS;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		
		editor.putInt(MESSAGE_ENABLED_KEY, MESSAGE_DISABLED);
		editor.commit();
	
		// Update the widget
    	AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
    	RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
    	ComponentName thisWidget = new ComponentName(context, Widget.class);
    	remoteViews.setImageViewResource(R.id.widget_button, R.drawable.enabled_message_selector);
    	appWidgetManager.updateAppWidget(thisWidget, remoteViews);
		
		Toast.makeText(context, "BRB Disabled From Preset End Time", Toast.LENGTH_LONG).show();
	}
}