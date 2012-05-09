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


/*	LogActivity
 * 		Activity that handles the Log Tab
 * 		Sets up the list to view the information in the log database
 * 		
 * 		has ability to delete logs from the database
 * 		Extends ListActivity to have the ability to interface a list of data
 */


public class LogActivity extends ListActivity{
    private final int CALL = 0;
	private final int TEXT = 1;
	private final int AM = 0;
	private final int PM = 1;
	private static final String TAG = "LogActivity";
	private static final String NO_LOGS = "No Log Entries for this Message";
	private TextView noLogs;
	private LogInteraction lDb;
	private LogAdapter adapt;
	private AlertDialog.Builder alert;
	
	//Called when the Activity is created in the beginning
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.response_log);						//set what view to fill with this activity
		Log.d(TAG,"in onCreate");
		HomeScreenActivity.logStarted = true;						//let the homescreen activity know that the log has started
		setTheme(Settings.Theme());									//check the theam in the settings and make this activity look like that
		noLogs = (TextView) findViewById(R.id.no_logs_label);		//text view when there are no logs
		lDb = new LogInteraction(this);								//set up interation with the log database
		
		Cursor temp = lDb.GetAllLogs();								//add all the logs from the database to the temp cursor
		adapt = new  LogAdapter(this,temp);							//create a new LogAdapter
		getListView().setAdapter(adapt);							//set the listview adapter
		Log.d(TAG,"Count = "+ temp.getCount());
		checkForLogs(temp);											// check to see if there are logs
		temp.close();												//close the cursor, no longer needed
		
		// Flush out logs before the log history # of days
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -Settings.LogHistoryDays());
		lDb.DeleteLog(sdf.format(cal.getTime()));
	}
	//When the activity is resumed by the Android OS
	public void onResume()
	{
		super.onResume();
		refresh();   			//Make sure list is up to date
		
	}
	
	//onStop() When the activity is no longer Visible	
	public void onStop()
	{
		super.onStop();
		lDb.Cleanup();
		adapt.getCursor().close();  //Close the cursor for next time
	}
	
	//When the Activity starts again after losing visibility 
	@Override
	public void onStart()
	{
		super.onStart();
		
			Cursor temp = lDb.GetAllLogs();					//get all the logs since last time using the app
			adapt = new  LogAdapter(this,temp);				//recreate the log adapter
			getListView().setAdapter(adapt);				//set the adapter to the listview
			Log.d(TAG,"Count = "+ temp.getCount());			
			checkForLogs(temp);								//make sure all logs are in the database
			temp.close();									// cursor is no longer needed
		
		refresh();											//Make sure logs are up to date
	}
	
	

	
	public void setMessage(Message current){
		
		if(HomeScreenActivity.logStarted){
			if(HomeScreenActivity.mCurrent == null){
				Cursor temp = lDb.GetAllLogs();					//get all the logs since last time using the app
				adapt = new  LogAdapter(this,temp);				//recreate the log adapter
				getListView().setAdapter(adapt);				//set the adapter to the listview
				Log.d(TAG,"Count = "+ temp.getCount());			
				checkForLogs(temp);								//make sure all logs are in the database
				temp.close();									// cursor is no longer needed
			}else{
				Cursor temp = lDb.GetLogBySentMessage(HomeScreenActivity.mCurrent.text);		//display only logs dealing with the current message
				adapt = new LogAdapter(this,temp);
				getListView().setAdapter(adapt);
				Log.d(TAG,"Count = "+temp.getCount());
				checkForLogs(temp);
				temp.close();
			}
			refresh();		//make sure data is up to date
		}
		
	}
	
	//Fill log with test data
//	private void fillData(){
//		Log.d(TAG,"in fillData");
//		//for(int i = 0; i < 5; i++)
//			lDb.InsertLog(0, "2:30", "02/04", AM, CALL, "" , "idk", "518-813-6375");
//	
//	}
	
	//used to make sure the displayed list is current to the database
	public void refresh()
	{
		Log.d(TAG,"in refresh");
		
		
		Cursor c = lDb.GetAllLogs();
		adapt.changeCursor(c);
		checkForLogs(c);
		adapt.notifyDataSetChanged();
		adapt.expand = new boolean[c.getCount()];
	}
	
	//Pop up an alert box asking if the user is sure they want to delete the log
	public void popUP(String num, String time)
	{
		final String nUm = num;			//need to be final if used in the AlertDialog.Builder
		final String tIme = time;
		
		alert = new AlertDialog.Builder(this)
	    .setTitle("Delete entry")
	    .setMessage("Are you sure you want to delete this entry?")
	    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	        	lDb.DeleteLog(nUm, tIme);		//user said yes, delete the log
	        	refresh();						//refresh the list to represent the new log
	        }
	     })
	    .setNegativeButton("No", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // User clikced no, do nothing
	        }
	     });
		
		alert.show(); //make the pop up show
		
		
	}
	
	//Simple function to see if there are any logs in the database
	private void checkForLogs(Cursor c){
		if(c.getCount() > 0)
			noLogs.setVisibility(View.GONE);
		else noLogs.setVisibility(View.GONE);
	}
	
	/*	LogAdapter
	 * 		class that represents each item in the list
	 * 		Sets up the list to look the way we want it too
	 * 		
	 * 		link each item to the response log xml
	 * 		Extends CursorAdapter to have the ability to interface with a cursor
	 */
	public class LogAdapter extends CursorAdapter{
		private final static String MORE = "more";
		private final static String LESS = "less";
		
	    private Context context;
	    private ParentInteraction pDb;
	    private boolean[] expand;

	    //constructor for LogAdapter, give it the cursor to the database so it can populate the list
	    public LogAdapter (Context context, Cursor c) {
	        super(context, c);
	        this.context = context;
	        expand = new boolean[c.getCount()];
	        Log.d(TAG,"Count2 = "+c.getCount());
	        pDb = new ParentInteraction(LogActivity.this);
	    }

	    //Set up what each item of the list consists of, a generic layout of each list item
	    @Override
	    public View newView(Context context, Cursor cursor, ViewGroup parent) {
	    	ViewHolder holder;

	    	//inflator for if expanded or not
	        final LayoutInflater inflater = LayoutInflater.from(context);
	        View v = inflater.inflate(R.layout.log_entry_item,null);
	        
	        //set the parts of the viewholder to each part of the xml
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

	    
	    //Bind what each item in the list looks like and what info the views hold
	    @Override
	    public void bindView(View v, Context context, Cursor c) {
	    	final int position = c.getPosition();
	    	final ViewHolder holder = (ViewHolder) v.getTag();
	    	final AlertDialog.Builder alert;
	    	
	    	//set the views to the information from the database
	    	holder.date.setText(c.getString(c.getColumnIndex(DATE)));
	    	holder.time.setText(c.getString(c.getColumnIndex(TIME)));
	    	holder.number.setText(c.getString(c.getColumnIndex(NUMBER)));
	    	holder.response.setText(c.getString(c.getColumnIndex(SENT_MESSAGE)));
	    	
	    	//Image for call or text
	    	//does it show what message was recieved for texts and not calls
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
	    	
	    	
	    	
	    	//add the AM or PM
	    	if(c.getInt(c.getColumnIndex(AMPM)) == 1)
	    		holder.ampm.setText("PM");
	    	else
	    		holder.ampm.setText("AM");
	    
	    	//show as expanded or not
	    	if(expand[position]){
				Log.d(TAG,"expanded");
				holder.more.setText(LESS);
				holder.expansion.setVisibility(View.VISIBLE); //Show expanded section
			}
			else{
				Log.d(TAG,"not expanded");
				holder.more.setText(MORE);
				holder.expansion.setVisibility(View.GONE); //make expanded section not visible
			}

	        
	       
	    	//Set a click listener to the view the expands the log
	    	holder.more.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					Log.d(TAG,"in onClick more");
					expand[position] = !expand[position]; //set this item of the list to exxpanded
					notifyDataSetChanged();
				}
    			
    		});
	    	
	    	//Set a Long click (hold) listener to each item of the list
	    	v.setOnLongClickListener(new OnLongClickListener() {  
	    		public boolean onLongClick(View v) {
				Log.d(TAG,"in onLongClick");
				
				popUP((String)holder.number.getText(), (String)holder.time.getText()); //Ask to make sure they want to delete

				refresh();				//refresh the list
				
				return true;
			} });
	    }
	    
	    //Give a name to every view in the xml
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
