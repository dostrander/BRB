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
//			January 23rd, 2012			  //
//////////////////////////////////////////*/

package b.r.b;

import java.util.ArrayList;

import static b.r.b.Constants.*;
import static b.r.b.DatabaseInteraction.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
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
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
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
	private final String NO_MESSAGE = "Click to Edit Message";
	private int DB_ID = -1;
	private static Message mCurrent;

	// Variables
	IncomingListener listener;
	
	// Views
	ImageButton enableButton;
	ImageButton listButton;
	Button selectButton;
	static TextView inputMessage;
	private static ListView messageList;
	//private static AutoCompleteArrayAdapter adapter;
	private static MessageListCursorAdapter adapter;
	boolean enabled;
	TextView header;
	TabHost mTabHost;
	TabWidget mTabWidget;
	static DatabaseInteraction db;
	
	
	

	ListView messageListView;
	//View createMessageView;
	

	
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
        db = new DatabaseInteraction(this);
        View theader = ((LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.input_message_list_item, null, false);
        theader.setBackgroundColor(Color.DKGRAY);
        header = (TextView) theader.findViewById(R.id.input_message_list_item);
        header.setTextColor(Color.WHITE);
        header.setText("Create New Message");
        messageList.addHeaderView(header);
        adapter = new MessageListCursorAdapter(this,R.layout.input_message_list_item,db.GetAllParentMessages(),
        		new String[]{MESSAGE},new int[]{R.id.input_message_list_item});
        messageList.setAdapter(adapter);
        
        

        
        messageList.setVisibility(View.GONE);
        registerListeners();
    }
    
    @Override
    public void onStart(){
    	super.onStart();
    	Log.d(TAG,"in onStart");
    	Log.d(TAG,String.valueOf(isEnabled()));
    	switch(isEnabled()){
    	case MESSAGE_ENABLED: enableMessage(); break;
    	case MESSAGE_DISABLED: disableMessage(); break;
    	case NO_MESSAGE_SELECTED: noMessage(); break;
    	}

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
    	SharedPreferences prefs = getSharedPreferences(PREFS,MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
    	if(isEnabled() != MESSAGE_ENABLED)
    		editor.putInt(MESSAGE_ENABLED_KEY, NO_MESSAGE_SELECTED);
    	else editor.putInt(MESSAGE_ENABLED_KEY, MESSAGE_ENABLED);
    	editor.commit();
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	Log.d(TAG,"in onDestroy");
    		
    	
    }
    
    public int isEnabled(){
    	SharedPreferences prefs = getSharedPreferences(PREFS,MODE_PRIVATE);
    	return prefs.getInt(MESSAGE_ENABLED_KEY, NO_MESSAGE_SELECTED);
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
    	messageList.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

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
    }
    
    private void createNewMessageDialog(){
		// Set an EditText view to get user input
		int myDialogColor = Color.rgb(33, 66, 99);
		LinearLayout ll = new LinearLayout(HomeScreenActivity.this);
		ll.setOrientation(LinearLayout.VERTICAL);
		final EditText input = new EditText(HomeScreenActivity.this);
		final ListView lv = new ListView(HomeScreenActivity.this);
		Cursor c = db.GetAllParentMessages();
		ArrayList<String> temp = new ArrayList<String>();
		if(c.moveToFirst()){
			do temp.add(c.getString(c.getColumnIndex(MESSAGE))); 
			while(c.moveToNext());
		}
		String[] messages = temp.toArray(new String[]{});
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(HomeScreenActivity.this,
				R.layout.dialog_message_item,R.id.textView1, messages);
		//final MessageListCursorAdapter adapter = new MessageListCursorAdapter(this,R.layout.dialog_message_item,db.GetAllParentMessages(),
        	//	new String[]{MESSAGE},new int[]{R.id.textView1});
		input.setLines(2);
		input.setGravity(Gravity.TOP);
		input.setHint("Start typing message...");
		input.addTextChangedListener(new TextWatcher(){
			public void afterTextChanged(Editable e) {}
			public void beforeTextChanged(CharSequence s, int arg1,
					int arg2, int arg3) {}
			public void onTextChanged(CharSequence s, int start,
					int before, int count) {adapter.getFilter().filter(s);}	});
		ll.setBackgroundColor(myDialogColor);
		lv.setBackgroundColor(myDialogColor);
		lv.setCacheColorHint(myDialogColor);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> adap, View v,
					int position, long id) {
				input.setText(adapter.getItem(position));
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
				String text = input.getText().toString().trim().toString();
				mCurrent = db.GetParentByMessage(text);
				if(mCurrent == null){
					Log.d(TAG,"new message");
					mCurrent = db.InsertMessage(text, new String[]{});
					HomeScreenActivity.adapter.changeCursor(db.GetAllParentMessages());
					//adapter.notifyDataSetChanged();
				}
				messageList.setVisibility(View.GONE);
				changeCurrent();
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
				messageList.setVisibility(View.GONE);
			}
		}).create().show();
    }
    
    private void editTextDialog(){
		int myDialogColor = Color.rgb(33, 66, 99);
		LinearLayout ll = new LinearLayout(HomeScreenActivity.this);
		final EditText input = new EditText(HomeScreenActivity.this);
		ll.setOrientation(LinearLayout.VERTICAL);
		input.setLines(2);
		input.setGravity(Gravity.TOP);
		input.setHint("Start typing message...");
		ll.addView(input);
		AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreenActivity.this);
		builder.setTitle("Edit Message")
		.setView(ll)
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
//				String text = input.getText().toString().trim().toString();
//				mCurrent = db.getParentByMessage(text);
//				if(mCurrent == null){
//					Log.d(TAG,"new message");
//					mCurrent = db.InsertMessage(text, new String[]{});
//					HomeScreenActivity.adapter.changeCursor(db.GetAllParentMessages());
//				}
				
				// update message
				changeCurrent();
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {}
		}).create().show();
    	
    }

    
    private void enableMessage(){
    	Log.d(TAG,"here");
    	Log.d(TAG,"here");
    	Log.d(TAG,"here");
    	Log.d(TAG,"here");

    	AudioManager audiomanage = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
    	SharedPreferences.Editor editor = getSharedPreferences(PREFS, Activity.MODE_PRIVATE).edit();
    	inputMessage.setTextColor(Color.WHITE);
    	enableButton.setImageResource(R.drawable.enabled_message_selector);
    	enabled = true;
    	// enable listener
    	enableButton.setClickable(true);
    	editor.putInt(MESSAGE_ENABLED_KEY, MESSAGE_ENABLED);
    	editor.putInt("ringer_mode", audiomanage.getRingerMode());
    	audiomanage.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    	editor.commit();
    }
    
    private void disableMessage(){
    	SharedPreferences prefs = getSharedPreferences(PREFS, Activity.MODE_PRIVATE);
    	SharedPreferences.Editor editor = prefs.edit();
    	AudioManager audiomanage = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
    	editor.putInt(MESSAGE_ENABLED_KEY, MESSAGE_DISABLED);
    	inputMessage.setTextColor(Color.WHITE);
    	enableButton.setImageResource(R.drawable.disabled_button_selector);
    	enabled = false;
    	// disable listener
    	audiomanage.setRingerMode(prefs.getInt("ringer_mode",AudioManager.RINGER_MODE_NORMAL));
    	enableButton.setClickable(true);
    	editor.commit();
    }
    
    private void noMessage(){
    	SharedPreferences.Editor editor = getSharedPreferences(PREFS, Activity.MODE_PRIVATE).edit();
    	editor.putInt(MESSAGE_ENABLED_KEY, NO_MESSAGE_SELECTED);
    	inputMessage.setText(NO_MESSAGE);
    	inputMessage.setTextColor(Color.GRAY);
    	enableButton.setImageResource(R.drawable.nothing_button_selector);
    	enabled = false;
    	DB_ID = -1;
    	mCurrent = null;
    	enableButton.setClickable(false);
    	editor.commit();
    }
    
    public void changeCurrent(long db_id){
    	Log.d(TAG,"in changeCurrent");
    	Message temp = db.GetParentById(String.valueOf(db_id));
    	mCurrent = temp;
    	changeCurrent();
   	}
    
    public void changeCurrent(){
    	if(mCurrent == null)
    		noMessage();
    	else{
    		inputMessage.setText(mCurrent.text);
    		disableMessage();
    	}
    }
    private class MessageListCursorAdapter extends CursorAdapter {
    	private final int layout;
    	private final int textview_id;
		public MessageListCursorAdapter(Context ctx, int lout, Cursor c,
				String[] from, int[] to){
			super(ctx, c);
			layout = lout;
			textview_id = to[0];
		}
		
		@Override
		public View newView(Context ctx, Cursor cursor, ViewGroup parent){
			ViewHolder holder = new ViewHolder();
			View v = LayoutInflater.from(ctx).inflate(layout, null);
			v.setTag(holder);
			
			return v;
			
		}
		
		@Override
		public void bindView(View v, Context context, Cursor cursor){
			final ViewHolder holder = (ViewHolder) v.getTag();
			TextView tv = (TextView) v.findViewById(textview_id);
			tv.setText(cursor.getString(cursor.getColumnIndex(MESSAGE)));			
			holder.text = tv;
			holder.id = cursor.getLong(cursor.getColumnIndex(ID));
			v.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					Log.d(TAG,String.valueOf(holder.id));
					if(layout == R.layout.input_message_list_item){
						changeCurrent(holder.id);
						messageList.setVisibility(View.GONE);
					}
				}
			});
		}
    	
    	public class ViewHolder{
    		TextView text;
    		long id;
    	}
    	
    }
}