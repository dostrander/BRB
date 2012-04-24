package b.r.b;

import static b.r.b.Constants.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Stack;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.FrameLayout;
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
	private static final String TAG = "MessageActivity";
	private static final int STARTTIME_ID = 0;
	private static final int ENDTIME_ID = 1;
	private static final int PICK_CONTACT_ID = 5;


	
	
	private static Message mMessage;
	private ContactMessageListAdapter mAdapter;
	private ListView 	vContactMessageList;
	TextView 			vPriority;
	RadioButton 		vHiButton;
	RadioButton 		vLoButton;
	TableRow			vPriorityRow;
	View				header;
	
	static TextView 	vStartTime;
	static TextView 	vEndTime;

	
	
	private int position_edited;
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_view);

		vStartTime 	= (TextView) findViewById(R.id.starttime_text);
		vEndTime 	= (TextView) findViewById(R.id.endtime_text);
		vPriorityRow = (TableRow) findViewById(R.id.priority_row);
		vHiButton 	= (RadioButton) findViewById(R.id.high_priority_button);
		vLoButton 	= (RadioButton) findViewById(R.id.low_priority_button);
		vContactMessageList = (ListView) findViewById(R.id.contact_specific_message_list);
		header = ((LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.message_item, null, false);

		
		mAdapter = new ContactMessageListAdapter(this);
		vContactMessageList.addHeaderView(header);
		vContactMessageList.setAdapter(mAdapter);
		vContactMessageList.setLongClickable(true);

		position_edited = -1;
		setDates();
		registerListeners();
		vPriorityRow.setVisibility(View.GONE);
	}
	private boolean contains(String t){
		if(mMessage == null) return false;
		else return mMessage.childContainsMessage(t);
	}
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
				contactPickerDialog();
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
				input.setLines(2);
				input.setGravity(Gravity.TOP);
				builder.setView(input);

				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
							((TextView) v).setText(input.getText().toString().trim().toString());
							mMessage.header.text = input.getText().toString().trim().toString();
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
		header.findViewById(R.id.add_message_button).setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(!tv.getText().toString().equals(CLICK_TO_EDIT) && 
						!contains(tv.getText().toString()) &&
						tv.getText().toString().trim().length() > 0){
					if(mMessage == null){
						popToast("Please select a message prior to doing this action");
					}else{
						mMessage.addContactSpecificMessage("NONE",tv.getText().toString());
						tv.setText(CLICK_TO_EDIT);
						mAdapter.notifyDataSetChanged();
					}
				}
			}
		});
		tv.setText(CLICK_TO_EDIT);
		nv.setText(CLICK_TO_ADD_NAMES);
	}
	
	public void contactPickerDialog(){
		LinearLayout ll = new LinearLayout(MessageActivity.this);
		ll.setOrientation(LinearLayout.VERTICAL);
//		final EditText input = new EditText(MessageActivity.this);
//		input.setLines(1);
//		input.setGravity(Gravity.TOP);
//		input.setHint("Start Typing to Search for Contact");
		
		final ListView lv = new ListView(MessageActivity.this);
		

        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String[] projection = new String[] {
        		ContactsContract.Contacts._ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.Contacts.DISPLAY_NAME
        };
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
		PickContactsAdapter adap = new PickContactsAdapter(MessageActivity.this,getContentResolver().query(uri, projection, null, null, null));
		AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
		lv.setAdapter(adap);
		builder.setTitle("Select Contacts")
		.setView(lv)
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
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
			ImageView add;
		}
		private LayoutInflater inflater;
		private Context context;
		MessageActivity messageActivity;;
	
		public ContactMessageListAdapter(Context ctx){
			inflater = LayoutInflater.from(ctx);
			context = ctx;
			messageActivity = (MessageActivity) ctx;
		}
		
		public View getView(final int position, View convertView, ViewGroup parent){
        	final ViewHolder holder;
        	
        	final String tempObject = getItem(position);
         
        	if (convertView == null) {
        		convertView = inflater.inflate(R.layout.message_item, null);
        		holder = new ViewHolder();
        		holder.text = (TextView) 	convertView.findViewById(R.id.contact_specific_message_text);
        		holder.add  = (ImageView)	convertView.findViewById(R.id.add_message_button);
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
        	convertView.findViewById(R.id.contact_specific_message_text).setOnClickListener(new OnClickListener(){
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
        	convertView.findViewById(R.id.add_names_button).setOnClickListener(new OnClickListener(){
    				public void onClick(View v) {

    					Intent intent = new Intent(Intent.ACTION_PICK,ContactsContract.Contacts.CONTENT_URI);
    					position_edited = position;
    					startActivityForResult(intent,PICK_CONTACT_ID);
    				}
            	});
//    		holder.text = (TextView) convertView.findViewById(R.id.message_item_text);
//    		holder.select = (Button) convertView.findViewById(R.id.message_item_select_button);
//    		holder.edit = (Button) convertView.findViewById(R.id.message_item_edit_button);
    		holder.text.setText(getItem(position));
			return convertView;
		}

		public int getCount() {
			if(mMessage == null) return 0;
			else return mMessage.cMessages.size();
		}

		public String getItem(int position) {
			// TODO Auto-generated method stub
			return mMessage.cMessages.get(position).text;
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
		ArrayList<String> numbers;
		Cursor cursor;
		boolean[] checked;
		public PickContactsAdapter(Context context, Cursor c) {
			super(context, c);
			cursor = c;
			checked = new boolean[c.getCount()];
			if(cursor.moveToFirst())
				do {
					checked[cursor.getPosition()] = contains(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
					cursor.moveToNext();
				}
				while(cursor.isAfterLast());
		}
	
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			final ViewHolder holder = new ViewHolder();
			View v = LayoutInflater.from(context).inflate(R.layout.pick_contact_item, null);
			v.setTag(holder);
			return v;
		}
//		ContactsContract.Contacts._ID,
//        ContactsContract.CommonDataKinds.Phone.NUMBER,
//        ContactsContract.Contacts.DISPLAY_NAME
		@Override
		public void bindView(View v, Context context, Cursor cursor) {
			final int position =  cursor.getPosition();
			final ViewHolder holder = (ViewHolder) v.getTag();
			holder.name = (TextView) v.findViewById(R.id.contact_name);
			holder.number = (TextView) v.findViewById(R.id.contact_number);
			holder.checked = (CheckBox) v.findViewById(R.id.contact_checked);
			holder.name.setText(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
			holder.number.setText(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
			holder.checked.setChecked(checked[position]);
			holder.checked.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {checked[position] = isChecked;}});
		}
		
		public String[] getChecked(){
			return (String[]) numbers.toArray(new String[]{});
		}

		
	}

}
