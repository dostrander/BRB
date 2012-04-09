package b.r.b;

import java.util.ArrayList;
import java.util.Stack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

public class MessageActivity extends Activity {
	private static final String TAG = "MessageActivity";
	private static final int STARTTIME_ID = 0;
	private static final int ENDTIME_ID = 1;
	
	
	private static Message mMessage;
	private ContactMessageListAdapter mAdapter;
	private ListView 	vContactMessageList;
	TextView 			vPriority;
	RadioButton 		vHiButton;
	RadioButton 		vLoButton;
	TableRow			vPriorityRow;
	
	static TextView 	vStartTime;
	static TextView 	vEndTime;

	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_view);

		mMessage = new Message("Test");
		vStartTime 	= (TextView) findViewById(R.id.starttime_text);
		vEndTime 	= (TextView) findViewById(R.id.endtime_text);
		vPriorityRow = (TableRow) findViewById(R.id.priority_row);
		vHiButton 	= (RadioButton) findViewById(R.id.high_priority_button);
		vLoButton 	= (RadioButton) findViewById(R.id.low_priority_button);
		vContactMessageList = (ListView) findViewById(R.id.contact_specific_message_list);
		mAdapter = new ContactMessageListAdapter(this);
		vContactMessageList.setAdapter(mAdapter);
		
		setDates();
		registerListeners();
		vPriorityRow.setVisibility(View.GONE);
	}
	
	private void registerListeners(){
		// TextViews
		vStartTime.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				
				final LinearLayout dialoglayout = setUpDialog();
	    		final DatePicker dp = (DatePicker) dialoglayout.findViewById(0);
	    		final TimePicker tp = (TimePicker) dialoglayout.findViewById(1);
	    		tp.setCurrentHour(mMessage.getsHour());
	    		tp.setCurrentMinute(mMessage.getsMinute());
	    		tp.setOnTimeChangedListener(new OnTimeChangedListener(){
	    			public void onTimeChanged(TimePicker view, int hourOfDay,
							int minute) {}});
				dp.init(mMessage.getsYear(), mMessage.getsMonth(), mMessage.getsDay(),
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
				final LinearLayout dialoglayout = setUpDialog();
	    		final DatePicker dp = (DatePicker) dialoglayout.findViewById(0);
	    		final TimePicker tp = (TimePicker) dialoglayout.findViewById(1);
	    		tp.setCurrentHour(mMessage.getfHour());
	    		tp.setCurrentMinute(mMessage.getfMinute());
	    		tp.setOnTimeChangedListener(new OnTimeChangedListener(){
	    			public void onTimeChanged(TimePicker view, int hourOfDay,
							int minute) {}});
				dp.init(mMessage.getfYear(), mMessage.getfMonth(), mMessage.getfDay(),
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
	}
	private static void setDates(){
		vStartTime.setText(mMessage.startDateToText());
		vEndTime.setText(mMessage.endDateToText());
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
	
	
	
	
	// ListView
	
	//private static ListArray<String> mContactSpecificMessages;
	
	private class ContactMessageListAdapter extends BaseAdapter implements OnClickListener, OnLongClickListener{
		private class ViewHolder{		// holder class for the view
			TextView text;
			Button add;
		}
		private LayoutInflater inflater;
		private Context context;
		MessageActivity messageActivity;;
	
		public ContactMessageListAdapter(Context ctx){
			inflater = LayoutInflater.from(ctx);
			context = ctx;
			messageActivity = (MessageActivity) ctx;
		}
		
		public View getView(int position, View convertView, ViewGroup parent){
        	ViewHolder holder;
        	
        	final String tempObject = getItem(position);
         
        	if (convertView == null) {
        		convertView = inflater.inflate(R.layout.message_item, null);
        		holder = new ViewHolder();
        		holder.text = (TextView) 	convertView.findViewById(R.id.contact_specific_message_text);
        		holder.add  = (Button)		convertView.findViewById(R.id.edit_specific_message_text);
        		holder.text.setTag(getItem(position));
        		convertView.setTag(holder);
        	} else{
        		holder = (ViewHolder) convertView.getTag();
        		holder.text.setTag(getItem(position));
        	}
        	convertView.findViewById(R.id.contact_specific_message_text).setOnClickListener(this);
        	convertView.findViewById(R.id.add_names_button).setOnClickListener(this);
         
        	
//    		holder.text = (TextView) convertView.findViewById(R.id.message_item_text);
//    		holder.select = (Button) convertView.findViewById(R.id.message_item_select_button);
//    		holder.edit = (Button) convertView.findViewById(R.id.message_item_edit_button);

    		if (tempObject.equals("")) {
    			holder.add.setVisibility(View.VISIBLE);
    		}
			return convertView;
		}

		public int getCount() {
			return mMessage.specificMessages.size();
		}

		public String getItem(int position) {
			// TODO Auto-generated method stub
			return mMessage.specificMessages.get(position);
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		
		
		
		// Click Listeners
		public boolean onLongClick(View arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		public void onClick(View v) {
			switch(v.getId()){
			case R.id.add_names_button:
				break;
			case R.id.contact_specific_message_text:
				AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);

				builder.setTitle("Edit Message Text");
				// Set an EditText view to get user input 
				final EditText input = new EditText(MessageActivity.this);
				input.setLines(2);
				input.setGravity(Gravity.TOP);
				builder.setView(input);

				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						
					}
				});

				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
				    // Canceled.
					}
				});

				AlertDialog alert = builder.create();
				alert.show();
				break;
				default:
					break;
			}
		}

	}

}
