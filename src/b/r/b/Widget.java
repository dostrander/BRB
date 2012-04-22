package b.r.b;

import android.appwidget.AppWidgetProvider;
import android.widget.ImageButton;
import android.widget.TextView;

public class Widget extends AppWidgetProvider {
	private final String TAG = "Widget";
	private final String MESSAGE = "message";
	private final String NO_MESSAGE = "Click to Edit Message";
	
	// Variables
	IncomingListener listener;

	// Views
	ImageButton enableButton;
	static TextView inputMessage;
	boolean enabled;
	
}
