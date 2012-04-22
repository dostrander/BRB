package b.r.b;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

public class Widget extends AppWidgetProvider {
	public static String ACTION_WIDGET_RECEIVER = "ActionReceiverWidget";
	
	// Variables
	IncomingListener listener;

	// Views
	ImageButton widgetEnableButton;
	static TextView widgetInputMessage;
	boolean widgetEnabled = false;
	
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	
    	RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

    	Intent active = new Intent(context, Widget.class);
    	
    	active.setAction(ACTION_WIDGET_RECEIVER);

    	active.putExtra("msg", "BRB Enabled");
    	
    	PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);

    	remoteViews.setOnClickPendingIntent(R.id.widget_button, actionPendingIntent);

    	appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
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
    			
    			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    			
    		}
    		super.onReceive(context, intent);
    	}
    }
}
