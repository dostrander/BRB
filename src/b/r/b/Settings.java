/* * * * * * * * * * * * * * * * * * * * * * * 
 * BRB-Android
 * Settings.java
 * 		- Servers as a static accessor for user configurable settings
 * 
 * Created: 2012
 * Author: Will Stahl
 * 
 * Evan Dodge, Derek Ostrander, Max Shwenk
 * Jason Mather, Stuart Lang, Will Stahl
 * 
 * * * * * * * * * * * * * * * * * * * * * * */

package b.r.b;


import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

public class Settings {
	
	private static final String PREFS_NAME = "BRBPrefsFile";
	public static final boolean DEBUG_MODE = !System.getProperty("java.vm.info", "").contains("sharing");
	private static Activity _home;
	private static SharedPreferences _settings;
	private static SharedPreferences.Editor _editor;
	
	// The number of days that logs are kept
	private static int _logHistoryDays = 10;
	public static int LogHistoryDays() {
		return _logHistoryDays;
	}
	public static void SetLogHistoryDays(int value) {
		_logHistoryDays = value;
		_editor.putInt("log_history_days", _logHistoryDays);
	}
	
	// Whether phone calls should be handled
	private static Boolean _handleCalls = true;
	public static Boolean HandleCalls() {
		return _handleCalls;
	}
	public static void SetHandleCalls(Boolean value) {
		_handleCalls = value;
		_editor.putBoolean("handle_calls", _handleCalls);
	}
	
	// Whether texts should be handled
	private static Boolean _handleTexts = true;
	public static Boolean HandleTexts() {
		return _handleTexts;
	}
	public static void SetHandleTexts(Boolean value) {
		_handleTexts = value;
		_editor.putBoolean("handle_texts", _handleTexts);
	}
	
	// The name of the currently enabled theme
	private static String _themeName;
	public static String ThemeName() {
		return _themeName;
	}
	
	private static int _theme;
	public static int Theme() {
		return _theme;
	}
	
	public static void SetTheme(int input) {
		int id = _theme;
		String name = "";
		
		// Look for a theme in the style resources that corresponds with the given id
		for (Field f : R.style.class.getDeclaredFields()) {
			try {
				// Make sure input matches field's value
				if (f.getInt(null) != input) continue;
				
				name = f.getName();
				
				// Make sure given resource is a Theme
				if (!name.startsWith("Theme_")) break;
				
				// Get name part
				name = name.substring(6); 
				id = input;
				break;
			} catch (Exception e) {}
		}
		
		// Make sure we found a theme which needs to be assigned
		if (_theme == id) return;
			
		_theme = id;
		_themeName = name;
		
		_editor.putString("theme", name);
		_editor.commit();

		// The home activity needs to be restarted to set the theme
		_home.finish();
		_home.startActivity(new Intent(_home, _home.getClass()));
	}
	
	static public void Init(Activity home) {
		// Get homescreen activity and settings editor
		_home = home;
		_settings = home.getSharedPreferences(PREFS_NAME, 0);
		_editor = _settings.edit();
		
//		if (DEBUG_MODE) _editor.clear(); // Uncomment if you want settings to clear when running in debug mode 

		_editor.commit();
		
		_handleCalls = _settings.getBoolean("handle_calls", true);
		_handleTexts = _settings.getBoolean("handle_texts", true);
		_logHistoryDays = _settings.getInt("log_history_days", 10);
		
		// Try to get name of saved theme, otherwise use default
		_themeName = _settings.getString("theme", "Default");
		_theme = R.style.Theme_Default;
		try {
			// Try to find the theme in the style resources that corresponds with the given name (using reflection).
			// The "Theme_" prefix needs to be attached because the Themes were saved with the format "Theme.<Name>"
			_theme = R.style.class.getField("Theme_" + _themeName).getInt(null);
		} catch (Exception e) {}
		
		// If we had a problem finding the saved theme, use and save the default theme as current
		if (_theme == R.style.Theme_Default) {
			_themeName = "Default";
		}
		
		// Write settings
		_editor.putBoolean("handle_calls", _handleCalls);
		_editor.putBoolean("handle_texts", _handleTexts);
		_editor.putInt("log_history_days", _logHistoryDays);
		_editor.putString("theme", _themeName);
		
		// Commit settings
		_editor.commit();
	}
	
	

}
