package b.r.b;

import static b.r.b.Constants.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import b.r.b.Message.ChildMessage;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

public class MessageActivity extends Activity {
	// Convience
	private static final String TAG = "MessageActivity";
	private static final String HEADER = "THISISTHEHEADER";
	private static final int 	STARTTIME_ID = 0;
	private static final int 	ENDTIME_ID = 1;
	private static final int 	PICK_CONTACT_ID = 5;
	
	// Variables
	private static Message 						mMessage;
	private static TextView 					vStartTime;
	private static TextView 					vEndTime;
	private static ContactMessageListAdapter 	mAdapter;
	private ListView 					vContactMessageList;
	private TextView 					vPriority;
	private RadioButton					vHiButton;
	private RadioButton 				vLoButton;
	private TableRow					vPriorityRow;
	private View						header;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_view);
		
		vStartTime 			= (TextView) 	findViewById(R.id.starttime_text);
		vEndTime 			= (TextView) 	findViewById(R.id.endtime_text);
		vPriorityRow		= (TableRow) 	findViewById(R.id.priority_row);
		vHiButton 			= (RadioButton) findViewById(R.id.high_priority_button);
		vLoButton 			= (RadioButton) findViewById(R.id.low_priority_button);
		vContactMessageList = (ListView) 	findViewById(R.id.contact_specific_message_list);
//		
//		header 				= ((LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE))
//					.inflate(R.layout.message_item, null, false);
		header				= 				findViewById(R.id.header);
		
		
		mAdapter 			= new ContactMessageListAdapter(this);
		//vContactMessageList.addHeaderView(header);
		vContactMessageList.setAdapter(mAdapter);
		vContactMessageList.setLongClickable(true);

		setDates();
		registerListeners();
		
		vPriorityRow.setVisibility(View.GONE);
	}
	private boolean contains(String t){
		if(mMessage == null) return false;
		else return mMessage.childContainsMessage(t);
	}
	private boolean containsNumber(String num){
		if(mMessage == null) return false;
		else return mMessage.headerContainsNumber(num);
	}
//	public ChildInteraction getDatabase(){
//		return homeActivity.pDb;
//	}
	private void registerListeners(){
		// TextViews
		vStartTime.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				Calendar cal;
				final LinearLayout dialoglayout = setUpDialog();
	    		final DatePicker dp = (DatePicker) dialoglayout.findViewById(0);
	    		final TimePicker tp = (TimePicker) dialoglayout.findViewById(1);
	    		if(mMessage != null)
	    			cal = mMessage.getStartTime();
	    		else cal = Calendar.getInstance();
	    		tp.setCurrentHour(cal.get(Calendar.HOUR));
	    		tp.setCurrentMinute(cal.get(Calendar.MINUTE));
	    		tp.setOnTimeChangedListener(new OnTimeChangedListener(){
	    			public void onTimeChanged(TimePicker view, int hourOfDay,
							int minute) {}});
				dp.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
						new OnDateChangedListener(){
							public void onDateChanged(DatePicker view,
									int year, int monthOfYear, int dayOfMonth) {}});
				
				AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
				builder.setView(dialoglayout)
						.setTitle("Select Start Time")
				        .setCancelable(false) 
				        .setPositiveButton("Set", new AlertDialog.OnClickListener() {
				    	public void onClick(DialogInterface dialog, int which){
				    		mMessage.setStartDate(dp.getMonth(), dp.getDayOfMonth(), dp.getYear(),
				    				tp.getCurrentHour(), tp.getCurrentMinute());
				    		setDates();
						}
				})	.setNegativeButton("Cancel", new AlertDialog.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
				}
			});
		vEndTime.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				Calendar cal;
				final LinearLayout dialoglayout = setUpDialog();
	    		final DatePicker dp = (DatePicker) dialoglayout.findViewById(0);
	    		final TimePicker tp = (TimePicker) dialoglayout.findViewById(1);
	    		if(mMessage != null)
	    			cal = mMessage.getEndTime();
	    		else cal = Calendar.getInstance();
	    		tp.setCurrentHour(cal.get(Calendar.HOUR));
	    		tp.setCurrentMinute(cal.get(Calendar.MINUTE));
	    		tp.setOnTimeChangedListener(new OnTimeChangedListener(){
	    			public void onTimeChanged(TimePicker view, int hourOfDay,
							int minute) {}});
				dp.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
						new OnDateChangedListener(){
							public void onDateChanged(DatePicker view,
									int year, int monthOfYear, int dayOfMonth) {}});
				AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
				builder.setView(dialoglayout)
						.setTitle("Select End Time")
				        .setCancelable(false) 
				        .setPositiveButton("Set", new AlertDialog.OnClickListener() {
				    	public void onClick(DialogInterface dialog, int which){
				    		mMessage.setEndDate(dp.getMonth(), dp.getDayOfMonth(), dp.getYear(),
				    				tp.getCurrentHour(), tp.getCurrentMinute());
				    		Log.d(TAG,String.valueOf(tp.getCurrentHour()));
				    		Log.d(TAG,String.valueOf(tp.getCurrentMinute()));
				    		setDates();
						}
				})	.setNegativeButton("Cancel", new AlertDialog.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {}});
				AlertDialog alert = builder.create();
				alert.show();
				}
			});
		
		// Radio Buttons
		vHiButton.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// set priority on message
				vLoButton.setChecked(!isChecked);
			}
		});
		vLoButton.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// set priority on message
				vHiButton.setChecked(!isChecked);
			}
		});
		
		
		header.findViewById(R.id.add_names_button).setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
//				Intent intent = new Intent(Intent.ACTION_PICK,ContactsContract.Contacts.CONTENT_URI);
//				position_edited = -1;
//				startActivityForResult(intent,PICK_CONTACT_ID);
				//mMessage.header.
				contactPickerDialog(mMessage.getHeaderText());
			}
			
		});
		final TextView tv = (TextView) header.findViewById(R.id.contact_specific_message_text);
		final TextView nv  = (TextView) header.findViewById(R.id.names);
		nv.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
			}
		});

		tv.setOnClickListener(new OnClickListener(){
			public void onClick(final View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);

				builder.setTitle("Edit Message Text");
				// Set an EditText view to get user input 
				final EditText input = new EditText(MessageActivity.this);
				if(tv.getText().toString() != CLICK_TO_EDIT)
					input.setText(tv.getText().toString());
				input.setLines(2);
				input.setGravity(Gravity.TOP);
				builder.setView(input);

				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
							((TextView) v).setText(input.getText().toString().trim().toString());
							mMessage.setHeaderText(input.getText().toString().trim().toString());
					}
				});

				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
				    //  Canceled.
					}
				});

				AlertDialog alert = builder.create();
				alert.show();
			}
    	});
		header.findViewById(R.id.add_message_button).setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(!tv.getText().toString().equals(CLICK_TO_EDIT) && 
						!contains(tv.getText().toString()) &&
						tv.getText().toString().trim().length() > 0){
					if(mMessage == null){
						popToast("Please select a message prior to doing this action");
					}else if(mMessage.headerNumbersSize () < 1) 
						popToast("Please Add Contacts First");
					else if (mMessage.checkHeaderForDupNumbers())
						popToast("You have conflicting contacts in this contact specific Message. Please Check the contacts and try adding again");
					else{
						mMessage.addHeaderToChild(MessageActivity.this);
						mAdapter.notifyDataSetChanged();
						setViewToHeader();
					}
				}
			}
		});

		nv.setMovementMethod(new ScrollingMovementMethod());
		setViewToHeader();
	}
	
	public void setViewToHeader(){
		String t,n;
		if(mMessage != null){
			t = mMessage.getHeaderText();
			n = mMessage.getHeaderNames();
		}else {
			t = CLICK_TO_EDIT;
			n = CLICK_TO_ADD_NAMES;
		}
		((TextView)header.findViewById(R.id.contact_specific_message_text)).setText(t);
		((TextView)header.findViewById(R.id.names)).setText(n);
	}
	public void insertChild(String num, String text, long p_id){
		//HomeScreenActivity
	}
	
	public void contactPickerDialog(String text){
		// Set Cursor
	    Cursor cur = MessageActivity.this.getContentResolver().query(
	            ContactsContract.Data.CONTENT_URI,
	            new String[]{ContactsContract.Data._ID,ContactsContract.Data.DISPLAY_NAME, ContactsContract.Data.DATA1},
	            ContactsContract.Data.MIMETYPE + " = '" +   ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'",
	            null,
	            ContactsContract.Data.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
	    
	    // Get Views
		final ListView lv = new ListView(MessageActivity.this);
        final PickContactsAdapter adap = new PickContactsAdapter(MessageActivity.this, cur,text);
//		final EditText input = new EditText(MessageActivity.this);
		LinearLayout ll = new LinearLayout(MessageActivity.this);
		// Set Up Views
		ll.setOrientation(LinearLayout.VERTICAL);
//		ll.addView(input);
		ll.addView(lv);
//		input.setLines(1);
//		input.setGravity(Gravity.TOP);
//		input.setHint("Filter Contacts");
//		input.addTextChangedListener(new TextWatcher(){
//			public void afterTextChanged(Editable e) {}
//			public void beforeTextChanged(CharSequence s, int arg1,
//					int arg2, int arg3) {}
//			public void onTextChanged(CharSequence s, int start,
//					int before, int count) {adap.getFilter().filter(input.getText().toString());}});
		lv.setAdapter(adap);
		
		// Build Dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
		builder.setTitle("Select Contacts")
		.setView(ll)
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				mMessage.addHeaderContacts(MessageActivity.this, adap.getChecked());
				((TextView) header.findViewById(R.id.names)).setText(mMessage.getHeaderNames());
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		}).create().show();

	}
    public void popToast(String t){
    	Toast.makeText(this, t, Toast.LENGTH_LONG).show();
    }
	private static void setDates(){
		if(mMessage == null){
			vStartTime.setText(NO_END);
			vEndTime.setText(NO_END);
		} else {
			vStartTime.setText(mMessage.startDateToText());
			vEndTime.setText(mMessage.endDateToText());
		}
	}
	private LinearLayout setUpDialog(){
		LinearLayout dialoglayout =  new LinearLayout(MessageActivity.this);
		DatePicker dp  = new DatePicker(MessageActivity.this);
		TimePicker tp  = new TimePicker(MessageActivity.this);
		dp.setPadding(10, 10, 10, 10);
		dp.setId(0);
		tp.setIs24HourView(false);
		tp.setPadding(30, 10, 10, 10);
		tp.setId(1);
		dialoglayout.addView(dp);
		dialoglayout.addView(tp);
		dialoglayout.setOrientation(LinearLayout.VERTICAL);
		return dialoglayout;
	}
	public static void changeMessage(Message current){
		mMessage = current;
		mAdapter.notifyDataSetChanged();
	}
	public static void noMessage(){
		mMessage = null;
	}
	private void longClickDialog(){
		final String[] items = new String[]{"Edit Contacts", "Edit Message", "Delete Message"};
		AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
		builder.setTitle("Edit Contact Specific Message")
		.setItems(items,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Toast.makeText(MessageActivity.this, items[which], Toast.LENGTH_LONG)
						.show(); 
						
					}
				});
		builder.create().show();
	}
	// ListV
	
	private class ContactMessageListAdapter extends BaseAdapter {
		private class ViewHolder{		// holder class for the view
			TextView text;
			TextView names;
			ImageView add;
			ImageView addNames;
		}
		private LayoutInflater inflater;
	
		public ContactMessageListAdapter(Context ctx){
			inflater = LayoutInflater.from(ctx);
		}
		
		public View getView(final int position, View convertView, ViewGroup parent){
        	final ViewHolder holder;
        	
        	final ChildMessage tempObject = getItem(position);
         
        	if (convertView == null) {
        		convertView = inflater.inflate(R.layout.message_item, null);
        		holder = new ViewHolder();
        		holder.text 	= (TextView) 	convertView.findViewById(R.id.contact_specific_message_text);
        		holder.names 	= (TextView)	convertView.findViewById(R.id.names);
        		holder.addNames = (ImageView) 	convertView.findViewById(R.id.add_names_button);
        		holder.add  	= (ImageView)	convertView.findViewById(R.id.add_message_button);
        		holder.add.setVisibility(View.GONE);
        		holder.text.setTag(getItem(position));
        		convertView.setTag(holder);
        	} else{
        		holder = (ViewHolder) convertView.getTag();
        		holder.text.setTag(getItem(position));
        	}
        	convertView.setOnLongClickListener(new OnLongClickListener(){
				public boolean onLongClick(View v) {
					longClickDialog();
					return true;

				}
        	});
        	holder.names.setOnClickListener(new OnClickListener(){
    				public void onClick(View v) {
    					AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);

    					builder.setTitle("Edit Message Text");
    					// Set an EditText view to get user input 
    					final EditText input = new EditText(MessageActivity.this);
    					input.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
    					builder.setView(input);
    					builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    						public void onClick(DialogInterface dialog, int whichButton) {
    							if(holder.text.getText().toString() != input.getText().toString())
    								holder.text.setText(input.getText().toString());
    						}
    					});

    					builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    						public void onClick(DialogInterface dialog, int whichButton) {
    					    // Canceled.
    						}
    					});

    					AlertDialog alert = builder.create();
    					alert.show();
    				}
            	});
        	holder.addNames.setOnClickListener(new OnClickListener(){
    				public void onClick(View v) {
    				}
            	});
//    		holder.text = (TextView) convertView.findViewById(R.id.message_item_text);
//    		holder.select = (Button) convertView.findViewById(R.id.message_item_select_button);
//    		holder.edit = (Button) convertView.findViewById(R.id.message_item_edit_button);
    		holder.text.setText(tempObject.text);
    		holder.names.setText(tempObject.namesText);
			return convertView;
		}

		public int getCount() {
			if(mMessage == null) return 0;
			else return mMessage.cMessages.size();
		}

		public ChildMessage getItem(int position) {
			// TODO Auto-generated method stub
			return mMessage.cMessages.get(position);
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}
	}		
	private class PickContactsAdapter extends CursorAdapter{
		class ViewHolder{
			TextView name;
			TextView number;
			CheckBox checked;
		}
		class Holder{
			public Holder(String n,boolean c){
				this.name = n;
				this.checked = c;
			}
			boolean checked;
			String name;
		}
		HashMap<String,Holder> numbers;
		Cursor cursor;
		public PickContactsAdapter(Context context, Cursor c, String text) {
			super(context, c);
			Message.ChildMessage cm = mMessage.getChild(text);
			Holder h;
			String key;
			cursor = c;
			numbers = new HashMap<String,Holder>();
			Log.d(TAG,"Cursor: "+String.valueOf(cursor.getCount()));
			if(cursor.moveToFirst())
				do {
					h =  new Holder(cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)),
							 containsNumber(cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA1))));
					key = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA1));
					numbers.put(key,h);
					cursor.moveToNext();
				}
				while(!cursor.isAfterLast());
		}
	
		@Override
		public View newView(Context context, Cursor c, ViewGroup parent) {
			final ViewHolder holder = new ViewHolder();
			View v = LayoutInflater.from(context).inflate(R.layout.pick_contact_item, null);
			v.setTag(holder);
			return v;
		}
		@Override
		public void bindView(View v, Context context, final Cursor c) {
			Log.d(TAG,"position: " + String.valueOf(cursor.getPosition()));
			final String key = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA1));
			final ViewHolder holder = (ViewHolder) v.getTag();
			Log.d(TAG, "contains: " + String.valueOf(numbers.containsKey(key)));
			Log.d(TAG, "checked: " + String.valueOf(numbers.get(key).checked));
			Log.d(TAG,String.valueOf(numbers.get(key).name));
			holder.name = (TextView) v.findViewById(R.id.contact_name);
			holder.number = (TextView) v.findViewById(R.id.contact_number);
			holder.checked = (CheckBox) v.findViewById(R.id.contact_checked);
			holder.number.setText(key);
			holder.name.setText(numbers.get(key).name);
			holder.checked.setChecked(numbers.get(key).checked);
			holder.checked.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					boolean checked = !numbers.get(key).checked;
					String n = numbers.get(key).name;
					numbers.put(key, new Holder(n,checked));
				}
			});
			v.setTag(holder);
		}
		
		public String[] getChecked(){
			Log.d(TAG,"in getChecked");
			ArrayList<String> nums = new ArrayList<String>();
			for(String n : numbers.keySet())
				if(numbers.get(n).checked)
					nums.add(n);
			return (String[]) nums.toArray(new String[]{});
		}

		
	}

}
