/* * * * * * * * * * * * * * * * * * * * * * * 
 * BRB-Android
 * SettingsActivity.java
 * 		- Activity where user configurable settings are changed
 * 
 * Created: 2012
 * Author: Will Stahl
 * 
 * Evan Dodge, Derek Ostrander, Max Shwenk
 * Jason Mather, Stuart Lang, Will Stahl
 * 
 * * * * * * * * * * * * * * * * * * * * * * */

package b.r.b;

import static b.r.b.Constants.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.DatePicker.OnDateChangedListener;

public class SettingsActivity extends Activity {
	private static final String TAG = "SettingsActivity";
	private HashMap<String,Integer> _themes;
	private List<String> _theme_names;
	private static final String PREFS_NAME = "BRBPrefsFile";
	Spinner _style_selecter;
	private static int _enabled_ringer_pref;
	private static SharedPreferences.Editor _editor;
	private static SharedPreferences _settings;
	private static String enabled_volume_choice="";
	private static String disabled_volume_choice="";
	private static int style_selected=0;
	RadioGroup volumeGroup;
	RadioGroup styleGroup;
	
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		Log.d(TAG,"in onCreate");
		
		setTheme(Settings.Theme());
		style_selected = Settings.Theme();
		setContentView(R.layout.settings_view);
		
		_themes = new HashMap<String,Integer>();
		
		for (Field f : R.style.class.getFields()) {
			try {
				String name = f.getName();
				if (!name.startsWith("Theme_")) {continue;}
				name = name.substring(6);
				int value = f.getInt(null);
				_themes.put(name, value);
			} catch (Exception e) { }
			
		}
		//
		_settings = SettingsActivity.this.getSharedPreferences(PREFS, MODE_PRIVATE);
		String enabled_vol_str,disabled_vol_str;
		enabled_vol_str = _settings.getString(ENABLED_VOL_TXT, "Default");
		disabled_vol_str = _settings.getString(DISABLED_VOL_TXT, "Default");

		
		
		
		  _theme_names = new ArrayList<String>(_themes.keySet());
		
		// Make sure the initial states are correct
		((TextView)findViewById(R.id.on_enable_volume_edit)).setText(enabled_vol_str);
		((TextView)findViewById(R.id.on_enable_volume_edit)).setText(disabled_vol_str);
		((CheckBox)findViewById(R.id.calls_enabled_checkbox)).setChecked(Settings.HandleCalls());
		((CheckBox)findViewById(R.id.texts_enabled_checkbox)).setChecked(Settings.HandleTexts());
		((EditText)findViewById(R.id.log_history_edit_number)).setText(
				Integer.toString(Settings.LogHistoryDays()));
		
		// Set the apply button's actions
		((Button)findViewById(R.id.apply_button)).setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				
				// Apply settings changes when apply button is hit
				Settings.SetHandleCalls(
						((CheckBox)findViewById(R.id.calls_enabled_checkbox)).isChecked());
				Settings.SetHandleTexts(
						((CheckBox)findViewById(R.id.texts_enabled_checkbox)).isChecked());
				
				// Here we capture try and get a value for log history, and if we catch a proper
				// int value we clamp it and assign it to the log history setting. Regardless
				// at the end we assign the log history setting value back to the GUI text field.
				EditText logField = (EditText)findViewById(R.id.log_history_edit_number);
				try {
					int result = Integer.parseInt(logField.getText().toString());
					if (result < 0) result = 0;
					if (result > 999) result = 999;
					Settings.SetLogHistoryDays(result);
				} catch (NumberFormatException nfe) {}
				logField.setText(Integer.toString(Settings.LogHistoryDays()));
				
				// Set Theme last because it essentially restarts the application
				Settings.SetTheme(style_selected);
			}
		});
		
		TextView styleSelector = (TextView)findViewById(R.id.style_selector);
		styleSelector.setText(Settings.ThemeName());
		//set listenner for style selector
		styleSelector.setOnClickListener(new OnClickListener(){										// Click Listener for EndTime
			public void onClick(View v) {
				//create a linear layout setup by setUpStyleDialog
				final LinearLayout dialoglayout = setUpStyleDialog();								// Linear Layout for container of the new dialog
	    		
				AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);	// Builder for Dialog
				builder.setView(dialoglayout)													// Set View
						.setTitle("Select Style")											// Set title of Dialog
				        .setCancelable(false) 													// Make it so they can not use the back button
				        .setPositiveButton("Set", new AlertDialog.OnClickListener() {			// OnClick to actually set the time/date
				    	public void onClick(DialogInterface dialog, int which){
				    		// Set the textView
				    		int choice = styleGroup.getCheckedRadioButtonId();
				    		if(choice==0) {
				        		((TextView)findViewById(R.id.style_selector)).setText("Night");
				        		style_selected = R.style.Theme_Night;
				    		} else {
				        		((TextView)findViewById(R.id.style_selector)).setText("Default");
				        		style_selected = R.style.Theme_Default;
				    		}
				    		
						}
				})	.setNegativeButton("Cancel", new AlertDialog.OnClickListener(){				// Set an Empty Negative button to be able to 
					public void onClick(DialogInterface dialog, int which) {}});					// get out of the dialog without making changes
				AlertDialog alert = builder.create();											// Create the dialog from the builder
				alert.show();																	// And show it
				}
			});
		((TextView)findViewById(R.id.on_enable_volume_edit)).setOnClickListener(new OnClickListener(){										// Click Listener for EndTime
			public void onClick(View v) {
				
				final LinearLayout dialoglayout = setUpVolumeDialog();								// Linear Layout for container of the new dialog
	    		
				AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);	// Builder for Dialog
				builder.setView(dialoglayout)													// Set View
						.setTitle("Select Enabled Ringer Mode")											// Set title of Dialog
				        .setCancelable(false) 													// Make it so they can not use the back button
				        .setPositiveButton("Set", new AlertDialog.OnClickListener() {			// OnClick to actually set the time/date
				    	public void onClick(DialogInterface dialog, int which){
				    		// Set the textView
				    		int choice = volumeGroup.getCheckedRadioButtonId();
				    		_settings = SettingsActivity.this.getSharedPreferences(PREFS, MODE_PRIVATE);
				    		_editor = _settings.edit();
				    		_editor.putInt(ENABLED_VOL,choice);
				    		
				    		_editor.commit();
				        	AudioManager audiomanage = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
				    		int volumePref = _settings.getInt(ENABLED_VOL, SILENT);
				        	_editor.putInt(PREVIOUS_RINGER_MODE,audiomanage.getRingerMode());
				        	_editor.putInt(PREVIOUS_VOL, audiomanage.getStreamVolume(AudioManager.STREAM_RING));
				        	switch(volumePref){
				        	case SILENT:
				        		//
				        		((TextView)findViewById(R.id.on_enable_volume_edit)).setText("Silent");
				        		_editor.putString(ENABLED_VOL_TXT, "Silent");
				        		audiomanage.setRingerMode(AudioManager.RINGER_MODE_SILENT);
				        		break;
				        	case VIBRATE:
				        		//
				        		((TextView)findViewById(R.id.on_enable_volume_edit)).setText("Vibrate");
				        		_editor.putString(ENABLED_VOL_TXT, "Vibrate");
				        		audiomanage.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
				        		break;
				        	case LOW_VOLUME:
				        		//
				        		((TextView)findViewById(R.id.on_enable_volume_edit)).setText("Low Vol.");
				        		_editor.putString(ENABLED_VOL_TXT, "Low Vol.");
				        		audiomanage.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				            	audiomanage.setStreamVolume(AudioManager.STREAM_RING, audiomanage.getStreamMaxVolume(AudioManager.STREAM_RING)/4, 0);
				        		break;
				        	case MEDIUM_VOLUME:
				        		//
				        		((TextView)findViewById(R.id.on_enable_volume_edit)).setText("Med. Vol.");
				        		_editor.putString(ENABLED_VOL_TXT, "Med. Vol.");
				        		audiomanage.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				            	audiomanage.setStreamVolume(AudioManager.STREAM_RING, audiomanage.getStreamMaxVolume(AudioManager.STREAM_RING)/2, 0);
				        		break;
				        	case HIGH_VOLUME:
				        		//
				        		((TextView)findViewById(R.id.on_enable_volume_edit)).setText("High Vol.");
				        		_editor.putString(ENABLED_VOL_TXT, "High Vol.");
				        		audiomanage.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				            	audiomanage.setStreamVolume(AudioManager.STREAM_RING, audiomanage.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
				        		break;
				        	case MAINTAIN:
				        		//do nothing
				        		((TextView)findViewById(R.id.on_enable_volume_edit)).setText("Maintain");
				        		_editor.putString(ENABLED_VOL_TXT, "Maintain");

				        		break;
				        	}
				    		
						}
				})	.setNegativeButton("Cancel", new AlertDialog.OnClickListener(){				// Set an Empty Negative button to be able to 
					public void onClick(DialogInterface dialog, int which) {}});					// get out of the dialog without making changes
				AlertDialog alert = builder.create();											// Create the dialog from the builder
				alert.show();																	// And show it
				}
			});
		((TextView)findViewById(R.id.on_disabled_volume_edit)).setOnClickListener(new OnClickListener(){										// Click Listener for EndTime
			public void onClick(View v) {
				
				final LinearLayout dialoglayout = setUpVolumeDialog();								// Linear Layout for container of the new dialog
	    		
				AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);	// Builder for Dialog
				builder.setView(dialoglayout)													// Set View
						.setTitle("Select Disabled Ringer Mode")											// Set title of Dialog
				        .setCancelable(false) 													// Make it so they can not use the back button
				        .setPositiveButton("Set", new AlertDialog.OnClickListener() {			// OnClick to actually set the time/date
				    	public void onClick(DialogInterface dialog, int which){
				    		// Set the textView
				    		int choice = volumeGroup.getCheckedRadioButtonId();
				    		_settings = SettingsActivity.this.getSharedPreferences(PREFS, MODE_PRIVATE);
				    		_editor = _settings.edit();
				    		_editor.putInt(DISABLED_VOL,choice);
				    		_editor.commit();
				        	AudioManager audiomanage = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
				    		
				    		switch(choice){
				        	case SILENT:
				        		//
				        		((TextView)findViewById(R.id.on_disabled_volume_edit)).setText("Silent");
					    		_editor.putString(DISABLED_VOL_TXT, "Silent");
				        		audiomanage.setRingerMode(AudioManager.RINGER_MODE_SILENT);
				        		break;
				        	case VIBRATE:
				        		//
				        		((TextView)findViewById(R.id.on_disabled_volume_edit)).setText("Vibrate");
				        		_editor.putString(DISABLED_VOL_TXT, "Vibrate");
				        		audiomanage.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
				        		break;
				        	case LOW_VOLUME:
				        		//
				        		((TextView)findViewById(R.id.on_disabled_volume_edit)).setText("Low Vol.");
				        		_editor.putString(DISABLED_VOL_TXT, "Low Vol.");
				        		audiomanage.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				            	audiomanage.setStreamVolume(AudioManager.STREAM_RING, audiomanage.getStreamMaxVolume(AudioManager.STREAM_RING)/4, 0);
				        		break;
				        	case MEDIUM_VOLUME:
				        		//
				        		((TextView)findViewById(R.id.on_disabled_volume_edit)).setText("Med. Vol.");
				        		_editor.putString(DISABLED_VOL_TXT, "Med. Vol.");
				        		audiomanage.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				            	audiomanage.setStreamVolume(AudioManager.STREAM_RING, audiomanage.getStreamMaxVolume(AudioManager.STREAM_RING)/2, 0);
				        		break;
				        	case HIGH_VOLUME:
				        		//
				        		((TextView)findViewById(R.id.on_disabled_volume_edit)).setText("High Vol.");
				        		_editor.putString(DISABLED_VOL_TXT, "High Vol.");
				        		audiomanage.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				            	audiomanage.setStreamVolume(AudioManager.STREAM_RING, audiomanage.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
				        		break;
				        	case MAINTAIN:
				        		((TextView)findViewById(R.id.on_disabled_volume_edit)).setText("Maintain");
				        		_editor.putString(DISABLED_VOL_TXT, "Maintain");
				        		int previousVolume = _settings.getInt(PREVIOUS_VOL, 0);
				        		int previousRingMode = _settings.getInt(PREVIOUS_RINGER_MODE,audiomanage.getRingerMode());
				        		audiomanage.setStreamVolume(AudioManager.STREAM_RING,previousVolume, 0);
				        		audiomanage.setRingerMode(previousRingMode);
				        		//do nothing
				        		break;
				        	}
						}
				})	.setNegativeButton("Cancel", new AlertDialog.OnClickListener(){				// Set an Empty Negative button to be able to 
					public void onClick(DialogInterface dialog, int which) {}});					// get out of the dialog without making changes
				AlertDialog alert = builder.create();											// Create the dialog from the builder
				alert.show();																	// And show it
				}
			});
		
	}
	private LinearLayout setUpVolumeDialog(){
		LinearLayout dialoglayout 			= new LinearLayout(SettingsActivity.this);					// Create a linear layout for the dialog
		RadioButton vibrate  				= new RadioButton(SettingsActivity.this);						// get date picker
		RadioButton silent  				= new RadioButton(SettingsActivity.this);
		RadioButton low  					= new RadioButton(SettingsActivity.this);
		RadioButton medium  				= new RadioButton(SettingsActivity.this);
		RadioButton high  					= new RadioButton(SettingsActivity.this);
		RadioButton maintain  				= new RadioButton(SettingsActivity.this);
		volumeGroup 								= new RadioGroup(SettingsActivity.this);

		vibrate.setId(1);
		silent.setId(0);
		low.setId(2);
		medium.setId(3);
		high.setId(4);
		maintain.setId(5);
		
		vibrate.setText("Vibrate");
		silent.setText("Silent");
		low.setText("Low");
		medium.setText("Medium");
		high.setText("High");
		maintain.setText("Maintain current");
		volumeGroup.addView(vibrate);
		volumeGroup.addView(silent);
		volumeGroup.addView(low);
		volumeGroup.addView(medium);
		volumeGroup.addView(high);
		volumeGroup.addView(maintain);
											// set the ID for getting later
		dialoglayout.addView(volumeGroup);
		
															// add time picker to linear layout
		dialoglayout.setOrientation(LinearLayout.VERTICAL);										// set linear layout to vertical orientation
		return dialoglayout;																	// return the linear layout
	}
	private LinearLayout setUpStyleDialog(){
		//default
		//night
		LinearLayout dialoglayout 							= new LinearLayout(SettingsActivity.this);					// Create a linear layout for the dialog
		RadioButton defaultStyle  							= new RadioButton(SettingsActivity.this);						// get date picker
		RadioButton nightStyle	  							= new RadioButton(SettingsActivity.this);
		styleGroup 											= new RadioGroup(SettingsActivity.this);

		defaultStyle.setId(1);
		nightStyle.setId(0);
		
		defaultStyle.setText("Default");
		nightStyle.setText("Night");
		
		styleGroup.addView(defaultStyle);
		styleGroup.addView(nightStyle);
		
		// set the ID for getting later
		dialoglayout.addView(styleGroup);
		
															// add time picker to linear layout
		dialoglayout.setOrientation(LinearLayout.VERTICAL);										// set linear layout to vertical orientation
		return dialoglayout;																	// return the linear layout
	}
}
