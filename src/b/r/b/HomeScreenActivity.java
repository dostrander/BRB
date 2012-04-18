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


import android.app.AlertDialog;
import android.app.TabActivity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;											// Logs
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


/*	HomeScreenActivity
 * 		Starts the application bringing up the main screen	
 */

public class HomeScreenActivity extends TabActivity {
    // Convenience Variables
	private final String TAG = "HomeScreenActivity";
	private final String MESSAGE = "message";
	private final String LOG = "log";
	private int DB_ID = -1;
	
	private Message mCurrent;

	// Variables
	IncomingListener listener;
	
	// Views
	ImageButton enableButton;
	ImageButton listButton;
	Button selectButton;
	static TextView inputMessage;
	private static ListView messageList;
	private static AutoCompleteArrayAdapter adapter;
	boolean enabled;
	TextView header;
	TabHost mTabHost;
	TabWidget mTabWidget;
	
	
	

	ListView messageListView;
	//View createMessageView;
	
	// Temp
	int message_count = 5;
	
	static final String[] MESSAGES = new String[] {
		"I'm at class", "Playing Soccer", "At the Dentist", "Too Drunk to talk",
		"At work", "Call you back when I get a chance", "Well I rather not talk to you" 
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
        
        // Set TabHost
        mTabHost 	= getTabHost();
        mTabHost.addTab(mTabHost.newTabSpec(MESSAGE).
        		setIndicator("Message",getResources().getDrawable(R.drawable.message_tab_selector)).
        		setContent(new Intent(this,MessageActivity.class)));
        mTabHost.addTab(mTabHost.newTabSpec(LOG).
        		setIndicator("Log",getResources().getDrawable(R.drawable.log_tab_selector)).
        		setContent(new Intent(this,LogActivity.class)));
        mTabHost.setCurrentTab(0);        
        
        // Find Views
        enableButton = 		(ImageButton) findViewById(R.id.enable_away_button);
        listButton	 =	 	(ImageButton) findViewById(R.id.show_list_button);
        messageList  = 		(ListView) findViewById(R.id.auto_complete_list);
        inputMessage = 		(TextView) findViewById(R.id.message_input);
        
        View theader = ((LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.input_message_list_item, null, false);
        theader.setBackgroundColor(Color.DKGRAY);
        header = (TextView) theader.findViewById(R.id.input_message_list_item);
        header.setTextColor(Color.WHITE);
        header.setText("Create New Message");
        messageList.addHeaderView(header);

        // Set adapter
        adapter = new AutoCompleteArrayAdapter(this, MESSAGES);
        messageList.setAdapter(adapter);
        
        
        noMessage();
        
        messageList.setVisibility(View.GONE);
        registerListeners();
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
    
    private void registerListeners(){
    	Log.d(TAG,"in registerListener");
    	// Filter for CustomAutoComplete
    	
    	inputMessage.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(mCurrent == null)
					createNewMessageDialog();
				else editTextDialog();
			}
    	});
		
    	enableButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(!enabled) enableMessage();
				else disableMessage();
			}
    	});
    	listButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(messageList.getVisibility() == View.GONE){
					messageList.setVisibility(View.VISIBLE);
					messageList.bringToFront();
				}
				else messageList.setVisibility(View.GONE);
					
			}
    	});
    	
    	header.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				createNewMessageDialog();				
			}
    	});
    	
//        inputMessage.addTextChangedListener(new TextWatcher(){
//    		public void afterTextChanged(Editable e) {
//    			inputMessage.setEnabled(true);
//    		}
//
//    		public void beforeTextChanged(CharSequence s, int start,
//    				int count, int after) {
//    			inputMessage.setEnabled(false);
//    		}
//
//    		public void onTextChanged(CharSequence s, int start, int count,
//    				int after) {
//    			Log.d(TAG,String.valueOf(s.length()));
//    			if(s.length() > 1) showAutoComplete();
//    			else hideAutoComplete();
//    			adapter.getFilter().filter(s);
//    		}
//        });
    }
    
    private void createNewMessageDialog(){
		// Set an EditText view to get user input
		int myDialogColor = Color.rgb(33, 66, 99);
		LinearLayout ll = new LinearLayout(HomeScreenActivity.this);
		ll.setOrientation(LinearLayout.VERTICAL);
		final EditText input = new EditText(HomeScreenActivity.this);
		final ListView lv = new ListView(HomeScreenActivity.this);
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(HomeScreenActivity.this,
				R.layout.dialog_message_item,R.id.textView1, MESSAGES);
		input.setLines(2);
		input.setGravity(Gravity.TOP);
		input.setHint("Start typing message...");
		input.addTextChangedListener(new TextWatcher(){
			public void afterTextChanged(Editable e) {
				
			}

			public void beforeTextChanged(CharSequence s, int arg1,
					int arg2, int arg3) {
			}

			public void onTextChanged(CharSequence s, int start,
					int before, int count) {
				adapter.getFilter().filter(s);
			}
			
		});
		ll.setBackgroundColor(myDialogColor);
		lv.setBackgroundColor(myDialogColor);
		lv.setCacheColorHint(myDialogColor);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> adap, View v,
					int position, long id) {
				input.setText(MESSAGES[position]);
				input.setSelection(input.getText().length());
			}
		});
		ll.addView(input);
		ll.addView(lv);
		AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreenActivity.this);
		builder.setTitle("Create New Message")
		.setView(ll)
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				inputMessage.setText(input.getText().toString());
				disableMessage(); // make it so the light is red
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
			}
		}).create().show();
    }
    
    private void editTextDialog(){
    	
    }
    
    private void enableMessage(){
    	enableButton.setImageResource(R.drawable.enabled_message_selector);
    	enabled = true;
    	// startListener
    	
    }
    
    private void disableMessage(){
    	enableButton.setImageResource(R.drawable.disabled_button_selector);
    	enabled = false;
    	// disableListener
    }
    
    private void noMessage(){
    	enableButton.setImageResource(R.drawable.nothing_button_selector);
    	enabled = false;
    	DB_ID = -1;
    	mCurrent = null;
    }
    
    
    public void getMessageFromDB(String text){
    	// get Message from db
    	Log.d(TAG,text);
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
					messageList.setVisibility(View.GONE);
					disableMessage(); // make button red
//					editing = true;
//					hideAutoComplete();
				}
			});
			
			return convertView;
		}
    	
    }
    
    
}