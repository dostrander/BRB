/*//////////////////////////////////////////
//				BRB-Android 	     	  //
//				Created by:				  //
//			  Derek Ostrander			  //
//				 Max Shwenk				  //
//			    Stuart Lang				  //
//				Evan Dodge				  //
//			   Jason Mather				  //
//				Will Stahl				  //
//										  //
//				Created on:				  //
//			January 23rd, 2011			  //
//////////////////////////////////////////*/

package b.r.b;

import java.util.ArrayList;


import android.app.Activity;									
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
import android.os.Bundle;
//import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;											// Logs
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;


/*	HomeScreenActivity
 * 		Starts the application bringing up the main screen	
 */

public class HomeScreenActivity extends TabActivity {
	private final String TAG = "HomeScreenActivity";
	private final String MESSAGE = "message";
	private final String LOG = "log";
    // Convenience Variables
	
	// Variables
	IncomingListener listener;
	
	// Views
	Button enableButton;
	Button selectButton;
	static EditText inputMessage;
	private static ListView mAutoCompleteList;
	private static AutoCompleteArrayAdapter adapter;
	private TextWatcher filter;
	boolean enabled;
	TabHost mTabHost;
	TabWidget mTabWidget;
	
	
	

	ListView messageListView;
	//View createMessageView;
	
	// Temp
	int message_count = 5;
	static final String[] COUNTRIES = new String[] {
		  "Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra",
		  "Angola", "Anguilla", "Antarctica", "Antigua and Barbuda", "Argentina",
		  "Armenia", "Aruba", "Australia", "Austria", "Azerbaijan",
		  "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium",
		  "Belize", "Benin", "Bermuda", "Bhutan", "Bolivia",
		  "Bosnia and Herzegovina", "Botswana", "Bouvet Island", "Brazil", "British Indian Ocean Territory",
		  "British Virgin Islands", "Brunei", "Bulgaria", "Burkina Faso", "Burundi",
		  "Cote d'Ivoire", "Cambodia", "Cameroon", "Canada", "Cape Verde",
		  "Cayman Islands", "Central African Republic", "Chad", "Chile", "China",
		  "Christmas Island", "Cocos (Keeling) Islands", "Colombia", "Comoros", "Congo",
		  "Cook Islands", "Costa Rica", "Croatia", "Cuba", "Cyprus", "Czech Republic",
		  "Democratic Republic of the Congo", "Denmark", "Djibouti", "Dominica", "Dominican Republic",
		  "East Timor", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea",
		  "Estonia", "Ethiopia", "Faeroe Islands", "Falkland Islands", "Fiji", "Finland",
		  "Former Yugoslav Republic of Macedonia", "France", "French Guiana", "French Polynesia",
		  "French Southern Territories", "Gabon", "Georgia", "Germany", "Ghana", "Gibraltar",
		  "Greece", "Greenland", "Grenada", "Guadeloupe", "Guam", "Guatemala", "Guinea", "Guinea-Bissau",
		  "Guyana", "Haiti", "Heard Island and McDonald Islands", "Honduras", "Hong Kong", "Hungary",
		  "Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Israel", "Italy", "Jamaica",
		  "Japan", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Kuwait", "Kyrgyzstan", "Laos",
		  "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein", "Lithuania", "Luxembourg",
		  "Macau", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands",
		  "Martinique", "Mauritania", "Mauritius", "Mayotte", "Mexico", "Micronesia", "Moldova",
		  "Monaco", "Mongolia", "Montserrat", "Morocco", "Mozambique", "Myanmar", "Namibia",
		  "Nauru", "Nepal", "Netherlands", "Netherlands Antilles", "New Caledonia", "New Zealand",
		  "Nicaragua", "Niger", "Nigeria", "Niue", "Norfolk Island", "North Korea", "Northern Marianas",
		  "Norway", "Oman", "Pakistan", "Palau", "Panama", "Papua New Guinea", "Paraguay", "Peru",
		  "Philippines", "Pitcairn Islands", "Poland", "Portugal", "Puerto Rico", "Qatar",
		  "Reunion", "Romania", "Russia", "Rwanda", "Sqo Tome and Principe", "Saint Helena",
		  "Saint Kitts and Nevis", "Saint Lucia", "Saint Pierre and Miquelon",
		  "Saint Vincent and the Grenadines", "Samoa", "San Marino", "Saudi Arabia", "Senegal",
		  "Seychelles", "Sierra Leone", "Singapore", "Slovakia", "Slovenia", "Solomon Islands",
		  "Somalia", "South Africa", "South Georgia and the South Sandwich Islands", "South Korea",
		  "Spain", "Sri Lanka", "Sudan", "Suriname", "Svalbard and Jan Mayen", "Swaziland", "Sweden",
		  "Switzerland", "Syria", "Taiwan", "Tajikistan", "Tanzania", "Thailand", "The Bahamas",
		  "The Gambia", "Togo", "Tokelau", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey",
		  "Turkmenistan", "Turks and Caicos Islands", "Tuvalu", "Virgin Islands", "Uganda",
		  "Ukraine", "United Arab Emirates", "United Kingdom",
		  "United States", "United States Minor Outlying Islands", "Uruguay", "Uzbekistan",
		  "Vanuatu", "Vatican City", "Venezuela", "Vietnam", "Wallis and Futuna", "Western Sahara",
		  "Yemen", "Yugoslavia", "Zambia", "Zimbabwe"
		};
	
	// For ListView
	private static ArrayList<Message> messages = new ArrayList<Message>();
	
	/*	onCreate
	 * 		
	 */
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"in onCreate");
        setContentView(R.layout.main_screen);
        enabled = false;
        mTabHost 	= getTabHost();
        mTabWidget 	= getTabWidget();
        mTabHost.addTab(mTabHost.newTabSpec(MESSAGE).
        		setIndicator("Message",getResources().getDrawable(R.drawable.message_tab_selector)).
        		setContent(new Intent(this,MessageActivity.class)));
        mTabHost.addTab(mTabHost.newTabSpec(LOG).
        		setIndicator("Log",getResources().getDrawable(R.drawable.log_tab_selector)).
        		setContent(new Intent(this,LogActivity.class)));
        mTabHost.setCurrentTab(0);        
        
        // Find Views
//        enableButton = (Button) findViewById(R.id.enable_away_button);
//        selectButton = (Button) findViewById(R.id.select_message_button);
        mAutoCompleteList = (ListView) findViewById(R.id.auto_complete_list);
        inputMessage = (EditText) findViewById(R.id.message_input);
        adapter = new AutoCompleteArrayAdapter(this, COUNTRIES);
        mAutoCompleteList.setAdapter(adapter);
        adapter.setDropDownViewResource(R.id.message_input);
        
        filter = new TextWatcher(){
			public void afterTextChanged(Editable e) {
				inputMessage.setEnabled(true);
			}

			public void beforeTextChanged(CharSequence s, int start,
					int count, int after) {
				inputMessage.setEnabled(false);
			}

			public void onTextChanged(CharSequence s, int start, int count,
					int after) {
				adapter.clear();
				Log.d(TAG,"in onTextChanged");
				if(count > 2){
					for(String sc : COUNTRIES)
						if(sc.contains(s))
							adapter.add(sc);
					showAutoComplete();
				} else hideAutoComplete();
			}
        };
        

        showAutoComplete();
    }
    
    @Override
    public void onStart(){
    	super.onStart();
    	Log.d(TAG,"in onStart");
    	   	
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	Log.d(TAG,"in onResume");
    	fillData();
    	
    }
        
    @Override
    public void onPause(){
    	super.onPause();
    	Log.d(TAG,"in onPause");
    }
    
    @Override
    public void onStop(){
    	super.onStop();
    	Log.d(TAG, "in onStop");
    	
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	Log.d(TAG,"in onDestroy");
    	
    }
    
    
    public void popToast(String t){
    	Toast.makeText(this, t, Toast.LENGTH_LONG);
    }
    
    /*	registerClickListeners
     * 
     */
    
    private void registerClickListeners(){
    	Log.d(TAG,"in registerListener");
    	// Enable Button
//    	enableButton.setOnClickListener(new View.OnClickListener(){
//
//			public void onClick(View v) {
//		        getIntent().putExtra("currentMessageText", "hey");
//		        listener = new IncomingListener();
//			}
//    	});
//    	// Select Button
//    	selectButton.setOnClickListener(new Button.OnClickListener() {
//    		public void onClick(View v){
//
//    		}
//		});
    	// Input Message
//    	inputMessage.setOnClickListener(new EditText.OnClickListener(){
//			public void onClick(View v) {
//				if(inputMessage.getText().toString() == DEFAULT_TEXT)
//					inputMessage.setText("");
//				
//			}
//    		
//    	});
//    	inputMessage.setOnKeyListener(new OnKeyListener(){
//
//			public boolean onKey(View v, int keyCode, KeyEvent event) {
//				if(event.getAction() == KeyEvent.ACTION_DOWN){
//				if(inputMessage.getText().toString() == DEFAULT_TEXT){
//					inputMessage.setText("");
//					return true;}
//				else if(keyCode == KeyEvent.KEYCODE_BACK && inputMessage.getText().toString() == ""){
//					inputMessage.setText(DEFAULT_TEXT);
//					return true;}
//				}
//				return false;
//			}
//    		
//    	});
////    	inputMessage.setOnFocusChangeListener(new EditText.OnFocusChangeListener(){
//			public void onFocusChange(View v, boolean t) {
//				if(t == false && inputMessage.getText().toString() == "")
//					inputMessage.setText(DEFAULT_TEXT);
//					
//					
//			}
//    		
//    	});
    }
    
    
    private void fillData(){
    	// This is where we are going to get all of the messages from the database
    	if(message_count > 0)
    		for(int i = 0; i < message_count; i++)
    			messages.add(new Message("Random Message: " + String.valueOf(message_count)));
    	else{// else add a fake one telling them to add one
    		messages.add(
    				new Message("Stuff"));
    	}
    		
    }
    
    
    private void showAutoComplete(){
    	mAutoCompleteList.setVisibility(View.VISIBLE);
    	mAutoCompleteList.bringToFront();
    }
    
    private static void hideAutoComplete(){
    	mAutoCompleteList.setVisibility(View.GONE);
    }
    
    private class AutoCompleteArrayAdapter extends ArrayAdapter<String> {
    	private final Context context;
    	private final String[] messages;
    	
    	public class ViewHolder{
    		TextView text;
    	}

		public AutoCompleteArrayAdapter(Context ctx, String[] msgs){
			super(ctx, R.id.auto_complete_list, msgs);
			context = ctx;
			messages = msgs;
			setNotifyOnChange(true);
			}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent){
			ViewHolder holder;
			if(convertView == null){
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.input_message_list_item,null);
				holder = new ViewHolder();
				holder.text = (TextView) convertView.findViewById(R.id.input_message_list_item);
				convertView.setTag(holder);
			} else holder = (ViewHolder) convertView.getTag();
			
			holder.text.setText(getItem(position));
			convertView.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					inputMessage.setText(getItem(position));
					hideAutoComplete();
				}
			});
			
			return convertView;
		}
    	
    }
    
    
}