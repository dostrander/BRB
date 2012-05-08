package b.r.b;


import static b.r.b.Constants.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LogActivity extends ListActivity{
    private final int CALL = 0;
	private final int TEXT = 1;
	private final int AM = 0;
	private final int PM = 1;
	private static final String TAG = "LogActivity";
	private static final String NO_LOGS = "No Log Entries for this Message";
	private TextView noLogs;
	private static Message mCurrent;
	private LogInteraction lDb;
	private LogAdapter adapt;
	private AlertDialog.Builder alert;
	
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.response_log);
		Log.d(TAG,"in onCreate");
		setTheme(Settings.Theme());
		noLogs = (TextView) findViewById(R.id.no_logs_label);
		lDb = new LogInteraction(this);
		
		// Flush out logs before the log history # of days
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -Settings.LogHistoryDays());
		mCurrent = HomeScreenActivity.mCurrent;
		if(mCurrent == null){
			Cursor temp = lDb.GetAllLogs();
			adapt = new  LogAdapter(this,temp);
			getListView().setAdapter(adapt);
			Log.d(TAG,"Count = "+ temp.getCount());
			temp.close();
		}else{
			Cursor temp = lDb.GetLogBySentMessage(mCurrent.text);
			adapt = new LogAdapter(this,temp);
			getListView().setAdapter(adapt);
			Log.d(TAG,"Count = "+temp.getCount());
			temp.close();
		}
		refresh();

		
	}
	
	public void onResume()
	{
		super.onResume();
		refresh();
		
	}
	
	//onStop() When the activity is no longer Visible
	
	public void onStop()
	{
		super.onStop();
		mCurrent = null;
		//Close the cursor for next time
		adapt.getCursor().close();
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		
		mCurrent = HomeScreenActivity.mCurrent;
		if(mCurrent == null){
			Cursor temp = lDb.GetAllLogs();
			adapt = new  LogAdapter(this,temp);
			getListView().setAdapter(adapt);
			Log.d(TAG,"Count = "+ temp.getCount());
			checkForLogs(temp);
			temp.close();
		}else{
			Cursor temp = lDb.GetLogBySentMessage(mCurrent.text);
			adapt = new LogAdapter(this,temp);
			getListView().setAdapter(adapt);
			Log.d(TAG,"Count = "+temp.getCount());
			checkForLogs(temp);
			temp.close();
		}
		refresh();
	}
	
	

	
	public void setMessage(Message current){
		
		mCurrent = current;
		if(lDb != null){
			if(mCurrent.text == null){
				Cursor temp = lDb.GetAllLogs();
				adapt = new  LogAdapter(this,temp);
				getListView().setAdapter(adapt);
				Log.d(TAG,"Count = "+ temp.getCount());
				checkForLogs(temp);
				temp.close();
			}else{
				Cursor temp = lDb.GetLogBySentMessage(mCurrent.text);
				adapt = new LogAdapter(this,temp);
				getListView().setAdapter(adapt);
				Log.d(TAG,"Count = "+temp.getCount());
				checkForLogs(temp);
				temp.close();
			}
			refresh();
		}
		
		
	}
	
//	private void fillData(){
//		Log.d(TAG,"in fillData");
//		//for(int i = 0; i < 5; i++)
//			lDb.InsertLog(0, "2:30", "02/04", AM, CALL, "" , "idk", "518-813-6375");
//	
//	}
	
	public void refresh()
	{
		
		lDb.Cleanup();
		Cursor c = lDb.GetAllLogs();
		adapt.changeCursor(c);
		checkForLogs(c);
		adapt.notifyDataSetChanged();
		adapt.expand = new boolean[c.getCount()];
	}
	
	
	public void popUP(String num, String time)
	{
		final String nUm = num;
		final String tIme = time;
		
		alert = new AlertDialog.Builder(this)
	    .setTitle("Delete entry")
	    .setMessage("Are you sure you want to delete this entry?")
	    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	        	lDb.DeleteLog(nUm, tIme);
	        	refresh();
	        }
	     })
	    .setNegativeButton("No", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // do nothing
	        }
	     });
		
		alert.show();
		
		
	}
	
	private void checkForLogs(Cursor c){
		if(c.getCount() > 0)
			noLogs.setVisibility(View.VISIBLE);
		else noLogs.setVisibility(View.GONE);
	}
	
	
	public class LogAdapter extends CursorAdapter{
		private final static String MORE = "more";
		private final static String LESS = "less";
		
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
			holder.more = (TextView) v.findViewById(R.id.log_entry_more);
	        
			v.setTag(holder);

	        return v;
	    }

	    @Override
	    public void bindView(View v, Context context, Cursor c) {
	    	final int position = c.getPosition();
	    	final ViewHolder holder = (ViewHolder) v.getTag();
	    	final AlertDialog.Builder alert;
	    	
	    	
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
	    
	    	//Expanded or not
	    	if(expand[position]){
				Log.d(TAG,"expanded");
				holder.more.setText(LESS);
				holder.expansion.setVisibility(View.VISIBLE);
			}
			else{
				Log.d(TAG,"not expanded");
				holder.more.setText(MORE);
				holder.expansion.setVisibility(View.GONE);
			}

	        
	       
	    	
	    	holder.more.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					Log.d(TAG,"in onClick more");
					expand[position] = !expand[position];
					notifyDataSetChanged();
				}
    			
    		});
	    	
	    	v.setOnLongClickListener(new OnLongClickListener() {  
	    		public boolean onLongClick(View v) {
				Log.d(TAG,"in onLongClick");
				//boolean temp = false;
				
				
				//temp = lDb.DeleteLog((String)holder.number.getText(), (String)holder.time.getText());
				popUP((String)holder.number.getText(), (String)holder.time.getText());
				
				//notifyDataSetChanged();
				
				
				refresh();
				
				return true;
			} });
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
			TextView more;
			
		}

	}
}
