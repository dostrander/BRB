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
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class SettingsActivity extends Activity {
	private static final String TAG = "SettingsActivity";
	private HashMap<String,Integer> _themes;
	private List<String> _theme_names;
	Spinner _style_selecter;
	
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		Log.d(TAG,"in onCreate");
		
		setTheme(Settings.Theme());
		
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
		
		_theme_names = new ArrayList<String>(_themes.keySet());
		
		_style_selecter = (Spinner) findViewById(R.id.style_selector);
		
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, _theme_names);
		
		_style_selecter.setAdapter(dataAdapter);
		
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		_style_selecter.setSelection(_theme_names.indexOf(Settings.ThemeName()));
		
//		_style_selecter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
//			public void onItemSelected(AdapterView adapter, View v, int i, long lng) {}
//			public void onNothingSelected(AdapterView arg0) {}
//			});
		
		// Make sure the initial states are correct
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
				Settings.SetLogHistoryDays(Integer.parseInt(
						((EditText)findViewById(R.id.log_history_edit_number)).getText().toString()));
				// Set Theme last because it essentially restarts the application
				Settings.SetTheme(_themes.get(_theme_names.get(
						_style_selecter.getSelectedItemPosition())));
			}
		});
	}
}
