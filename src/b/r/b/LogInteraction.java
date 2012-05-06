//BRB Written by Jason Mather on 4/28/12

//project: Derek Ostrander, Stu Lang, Will Stahl, Evan Dodge, Jason Mather
//This class allows us to store and retrieve data to and from the database in an
//easier manner
package b.r.b;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import static b.r.b.Constants.*;




public class LogInteraction extends Activity{
	//Constants used to access a column using the cursor getString() method later
	//private final int PID_COLUMN = 1;
	//private final int PARENT_ID_COLUMN = 4;
	//private final int PCHILD_ID_COLUMN = 3;
	//private final int CNUMBERS_COLUMN = 2;
	//initializing the local objects
	private logDB log;
	private parentDB parent;
	private childDB child;
	//private StringArrayConverter strc = new StringArrayConverter();
	private Context context;
	private boolean deleteSuccess;
	public LogInteraction(Context ctx){
		context = ctx;
		log = new logDB(context);
		
	}
	
	//To insert a log pass the parent id, time, date, ampm (1 for pm, 0 for am), type, recieved message, sent message, and number
	public Message InsertLog(int pid, String time, String date, int ampm, int type, String r_msg, String s_msg, String num){
		SQLiteDatabase db = log.getWritableDatabase();//just need a writable dB
		ContentValues values = new ContentValues();
		//prepare the row for insertion
		values.put(PARENT_ID, pid);
		values.put(TIME,time);
		values.put(DATE, date);
		values.put(AMPM,ampm);
		values.put(TYPE, type);
		values.put(RECEIVED_MESSAGE, r_msg);
		values.put(SENT_MESSAGE, s_msg);
		values.put(NUMBER,num);
		//try to insert the values, return the received message and the id if it worked
		long id = db.insertOrThrow(LOG_TABLE, null, values);
		
		//returns the message and the id 
		return new Message(r_msg,(int) id);
		
		
	}
	
	
	//deleting a log row
	public boolean DeleteLog(int id){
		SQLiteDatabase db = log.getWritableDatabase();
		//store the success of the delete
		deleteSuccess = db.delete(LOG_TABLE, ID + "=" + id, null) > 0;
		//cleanup
		db.close();
		return deleteSuccess;
	}
	/*This version of DeleteLog checks by number and time*/
	public boolean DeleteLog(String num,String time){
		SQLiteDatabase db = log.getWritableDatabase();//just need a writeable dB
		deleteSuccess = db.delete(LOG_TABLE, NUMBER + "= ? AND " +
		TIME + "= ?", new String[] {num,time}) >0;//if true, then the delete worked, if false, it didn't
		db.close();//we can now close the dB
		return deleteSuccess;//return the success value
	}
	
	//return ALL THE LOGS
	public Cursor GetAllLogs(){
		SQLiteDatabase db = log.getReadableDatabase();
		//cutting out the middle man, just return the query for speed
		return db.query(LOG_TABLE, new String[]{ID,TIME,DATE,AMPM,TYPE
				,RECEIVED_MESSAGE,SENT_MESSAGE,NUMBER}, null,null,null,null,null);
		
	}
	//return log based on parent id
	public Cursor SearchLogByParentId(int pid){
		SQLiteDatabase db = log.getReadableDatabase();
		//returns the rows which have the right parent id
		return db.query(LOG_TABLE, new String[]{ID,TIME,DATE,AMPM,TYPE
				,RECEIVED_MESSAGE,SENT_MESSAGE,NUMBER},PARENT_ID+"=?"
				, new String[]{String.valueOf(pid)}, null, null, null);
	}
	
	
	//some maintenance functions we may need
	public void CleanupLog(){
		log.close();
	}
	
	public void Cleanup(){
	    if (log != null){
	    	log.close();
	    }
	   	    
	}
	
	/*private void CleanupAll(){
		log.close();
		parent.close();
		child.close();
	}*/
	//make sure we close any open database onDestroy
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    if (parent != null) {
	        parent.close();
	    }
	    if (child != null) {
	        child.close();
	    }
	    if (log != null){
	    	log.close();
	    }
	}
}
