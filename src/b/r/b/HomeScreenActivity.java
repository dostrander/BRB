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
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
import android.os.Bundle;
//import android.telephony.SmsMessage;
import android.util.Log;											// Logs
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
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
    // Convenience Variables
	
	// Variables
	IncomingListener listener;
	
	// Views
	Button enableButton;
	Button selectButton;
	EditText inputMessage;
	boolean enabled;
	TabHost mTabHost;
	TabWidget mTabWidget;
	
	
	
	//MessageListAdapter messageListAdapter;
	ListView messageListView;
	//View createMessageView;
	
	// Temp
	int message_count = 5;
	
	
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
        LogEntryItem log = new LogEntryItem(0,0,0,0,0,0,"5182312471","stuff");
        //Intent i = new Intent(this,MessageActivity.class);
        mTabHost 	= getTabHost();
        mTabWidget 	= getTabWidget();
        //mTabHost.addTab(mTabHost.newTabSpec("message").setIndicator("Message").setContent(R.id.textview1));
        //mTabHost.addTab(mTabHost.newTabSpec("message").setIndicator("Message").setContent(i));
        mTabHost.addTab(mTabHost.newTabSpec("message").setIndicator("Message").setContent(new Intent(this,MessageActivity.class)));
        mTabHost.addTab(mTabHost.newTabSpec("log").setIndicator("Log").setContent(new Intent(this,LogActivity.class)));
        
        //mTabHost.addTab(mTabHost.newTabSpec("log").setIndicator("Log").setContent(R.id.textview2));
        mTabHost.setCurrentTab(0);
        //mTabHost.set
        //spec = mTabHost.newTabSpec("messages").setIndicator("Messages").setContent(viewId)
        
        
        // Find Views
//        enableButton = (Button) findViewById(R.id.enable_away_button);
//        selectButton = (Button) findViewById(R.id.select_message_button);
        inputMessage = (EditText) findViewById(R.id.message_input);
        inputMessage.setText(log.findContact(this));
//        messageListView = (ListView) findViewById(R.id.message_list);
//        
//        messageListAdapter = new MessageListAdapter(this);
//        messageListView.setAdapter(messageListAdapter);
//        registerClickListeners();
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
    
    
    
    
    /*	Adapter Class for ListView
     * 
     */
//    private static class MessageListAdapter extends BaseAdapter {
//    	private class ViewHolder{		// holder class for the view
//    		TextView text;
//    		Button edit;
//    		Button select;
//    	}
//    	private LayoutInflater inflater;
//    	private Context context;
//    	HomeScreenActivity homeScreenActivity;
//    	
//    	public MessageListAdapter(Context ctx){
//    		inflater = LayoutInflater.from(ctx);
//    		context = ctx;
//    		homeScreenActivity = (HomeScreenActivity) ctx;
//    		
//    	}
//		public View getView(int position, View convertView, ViewGroup parent) {
//        	ViewHolder holder;
//        	
//        	final Message tempObject = (Message) messages.get(position);
//         
//        	if (convertView == null) {
//        		convertView = inflater.inflate(R.layout.message_item, null);
//        	}
//         
////    		holder = new ViewHolder();
////    		holder.text = (TextView) convertView.findViewById(R.id.message_item_text);
////    		holder.select = (Button) convertView.findViewById(R.id.message_item_select_button);
////    		holder.edit = (Button) convertView.findViewById(R.id.message_item_edit_button);
////
////    		if (tempObject.DB_ID == -1) {
////    			Button hideMiles = (Button) convertView.findViewById(R.id.message_item_edit_button);
////    			hideMiles.setVisibility(View.INVISIBLE);
////    			Button hidefees = (Button) convertView.findViewById(R.id.message_item_select_button);
////    			hidefees.setVisibility(View.INVISIBLE);
////    			holder.text.setText("No Messages created");
////    		}
//			return convertView;
//		}
//
//		public int getCount() {
//			return messages.size();
//		}
//
//		public Object getItem(int position) {
//			return messages.get(position);
//		}
//
//		public long getItemId(int position) {
//			return 0;
//		}
//
//    	
//    }
}