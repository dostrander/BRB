package b.r.b;


import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class Settings {
	
	private static final String PREFS_NAME = "BRBPrefsFile";

	private static HashMap<String,Integer> _themes;
	
	private static Activity _home;
	
	private static View _settings;
	
	private static int _theme;
	public static int Theme() {
		return _theme;
	}
	
	private static void SetTheme(String theme_request) {
		for (String theme_string : _themes.keySet()) {
			if (theme_string == theme_request) {
			  int old_theme = _theme;
			  _theme = _themes.get(theme_string);
			  if (old_theme == _theme) { break; }
			  SharedPreferences settings = _home.getSharedPreferences(PREFS_NAME, 0);
		      SharedPreferences.Editor editor = settings.edit();
		      editor.putInt("theme", _theme);
	
		      // Commit the edits!
		      editor.commit();
	
				_home.finish();
				_home.startActivity(new Intent(_home, _home.getClass()));
			}
		}
	}
	
	static public void Init(Activity home) {
		_home = home;
		SharedPreferences settings = home.getSharedPreferences(PREFS_NAME, 0);
		_theme = settings.getInt("theme", R.style.greyTheme);

//		_themes = new HashMap<String,Integer>();
//		
//		_themes.put("Grey", R.style.greyTheme);
//		_themes.put("Blue", R.style.blueTheme);
//		
//		String[] aStrings = new String[_themes.keySet().size()];
//		aStrings = _themes.keySet().toArray(aStrings);
		
//		_settings = LayoutInflater.from(_home).inflate(R.layout.settings_view, null);
//		
//		Spinner style_selecter = (Spinner) _settings.findViewById(R.id.style_selector);
//		
//		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(_home,
//				android.R.layout.simple_spinner_dropdown_item, aStrings);
//		
//		style_selecter.setAdapter(dataAdapter);
//		
//		style_selecter.setOnItemSelectedListener(new OnItemSelectedListener(){
//			public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
//				SetTheme(parent.getItemAtPosition(pos).toString());
//			}
//
//			public void onNothingSelected(AdapterView<?> arg0) {
//			}
//		});
	}

}
