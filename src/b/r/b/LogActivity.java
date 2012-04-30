package b.r.b;

import java.util.ArrayList;

import static b.r.b.Constants.*;
import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class LogActivity extends ListActivity{
    private final int CALL = 0;
	private final int TEXT = 1;
	private final int AM = 0;
	private final int PM = 1;
	private static final String TAG = "LogActivity";
	private static final String NO_LOGS = "No Log Entries for this Message";
	private Message mCurrent;
	private LogInteraction lDb;
	
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		Log.d(TAG,"in onCreate");
//		setListAdapter(new LogAdapter(this));
		lDb = new LogInteraction(this);
		fillData();
		Cursor temp = lDb.GetAllLogs();
		getListView().setAdapter(new LogAdapter(this,lDb.GetAllLogs()));
		Log.d(TAG,"Count = "+temp.getCount());
		mCurrent = null;

	}
	
	public void setMessage(Message current){ mCurrent = current;}
	
	private void fillData(){
		Log.d(TAG,"in fillData");
		//for(int i = 0; i < 5; i++)
			//lDb.InsertLog(i, "2:30", "02/04", 1, CALL, "idk", "518-813-6375");
		
//		if(mCurrent == null){
//			mLogItems.add(new LogEntryItem("","",0,0,NO_LOGS,"-1"));
//		}else {
//			
//			
//			mLogItems.add(new LogEntryItem("","",0,0,NO_LOGS,"-1"));
//			//tempCursor.moveToNext();
//		}
	}
	
	
	
	public class LogAdapter extends CursorAdapter{

	    private Context context;
	    private ParentInteraction pDb;
	    private boolean[] expand;


	    public LogAdapter (Context context, Cursor c) {
	        super(context, c);
	        this.context = context;
	        expand = new boolean[c.getCount()];
	        Log.d(TAG,"Count2 = "+c.getCount());
	        pDb = new ParentInteraction(LogActivity.this);
	    }

	    @Override
	    public View newView(Context context, Cursor cursor, ViewGroup parent) {

	    	ViewHolder holder;

	        final LayoutInflater inflater = LayoutInflater.from(context);
	        View v = inflater.inflate(R.layout.log_entry_item,null);
	        
	        holder = new ViewHolder();
			holder.date = (TextView) v.findViewById(R.id.log_entry_date);
			holder.time = (TextView) v.findViewById(R.id.log_entry_time);
			holder.number = (TextView) v.findViewById(R.id.log_entry_contact);
			holder.expanded = (TextView) v.findViewById(R.id.log_entry_more);
			holder.ampm = (TextView) v.findViewById(R.id.log_entry_time_ampm);
			holder.response_label = (TextView) v.findViewById(R.id.log_entry_response_label);
			holder.time_label = (TextView) v.findViewById(R.id.log_entry_time_label);
			holder.response = (TextView) v.findViewById(R.id.log_entry_response);
			holder.type = (ImageView) v.findViewById(R.id.log_entry_picture);
			holder.expansion = (RelativeLayout) v.findViewById(R.id.log_entry_expansion);
			holder.received = (TextView) v.findViewById(R.id.log_entry_received);
			holder.received_label = (TextView) v.findViewById(R.id.log_entry_received_label);
	        
			v.setTag(holder);

	        return v;
	    }

	    @Override
	    public void bindView(View v, Context context, Cursor c) {
	    	
	    	final int position = c.getPosition();
	    	ViewHolder holder = (ViewHolder) v.getTag();
	    	
	    	
	    	holder.date.setText(c.getString(c.getColumnIndex(DATE)));
	    	holder.time.setText(c.getString(c.getColumnIndex(TIME)));
	    	holder.number.setText(c.getString(c.getColumnIndex(NUMBER)));
	    	holder.response.setText(c.getString(c.getColumnIndex(SENT_MESSAGE)));
	    	
	    	//Image for call or text
	    	if(c.getInt(c.getColumnIndex(TYPE)) == CALL){
	    		holder.type.setImageResource(R.drawable.phone_symbol);
	    		holder.received.setVisibility(View.GONE);
	    		holder.received_label.setVisibility(View.GONE);
	    	}
	    	else{
	    		holder.type.setImageResource(R.drawable.message_symbol);
	    		holder.received.setVisibility(View.VISIBLE);
	    		holder.received_label.setVisibility(View.VISIBLE);
	    		holder.received.setText(c.getString(c.getColumnIndex(RECEIVED_MESSAGE)));
	    	}
	    	
	    	
	    	//AMPM
	    	if(c.getInt(c.getColumnIndex(AMPM)) == 1)
	    		holder.ampm.setText("PM");
	    	else
	    		holder.ampm.setText("AM");
	    
	    	
	    	if(expand[position]){
				Log.d(TAG,"expanded");
				holder.expansion.setVisibility(View.VISIBLE);
			}
			else{
				Log.d(TAG,"not expanded");
				holder.expansion.setVisibility(View.GONE);
			}

	        
	       
	    	
	    	v.findViewById(R.id.log_entry_more).setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					Log.d(TAG,"in onClick more");
					expand[position] = !expand[position];
					if(expand[position])
						((TextView) v).setText("less");
					else 
						((TextView) v).setText("more");
					notifyDataSetChanged();
				}
    			
    		});
	    	
	    	
	    	
	    }
	    
	    class ViewHolder{
			ImageView type;
			TextView date;
			TextView number;
			TextView expanded;
			RelativeLayout expansion;
			TextView time;
			TextView ampm;
			TextView response;
			TextView time_label;
			TextView response_label;
			TextView received;
			TextView received_label;
			
		}

	}
}
