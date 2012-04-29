//BRB Derek Ostrander, Stu Lang, Will Stahl, Evan Dodge, Jason Mather
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

//TODO delete
// log by id
// parent by message
// child by message and number


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
	private StringArrayConverter strc = new StringArrayConverter();
	private Context context;
	private boolean deleteSuccess;
	public LogInteraction(Context ctx){
		context = ctx;
		log = new logDB(context);
		
	}
	
	public Message InsertLog(int pid, String time, String date, int ampm, int type, String r_msg, String s_msg, String num){
		SQLiteDatabase db = log.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(PARENT_ID, pid);
		values.put(TIME,time);
		values.put(DATE, date);
		values.put(AMPM,ampm);
		values.put(TYPE, type);
		values.put(RECEIVED_MESSAGE, r_msg);
		values.put(SENT_MESSAGE, s_msg);
		values.put(NUMBER,num);
		long id = db.insertOrThrow(LOG_TABLE, null, values);
		return new Message(r_msg,(int) id);
		
		
	}
	
	
	//overloading Parent edit message so you can edit by sending the old message, or the id, if you have it
	//returns true meant it worked, otherwise false, yo
		
	//deleting a log row
	public boolean DeleteLog(int id){
		SQLiteDatabase db = log.getWritableDatabase();
		deleteSuccess = db.delete(LOG_TABLE, ID + "=" + id, null) > 0;
		db.close();
		return deleteSuccess;
	}
	//deleting a parent row
	public Cursor GetAllLogs(){
		SQLiteDatabase db = log.getReadableDatabase();
		
		return db.query(LOG_TABLE, new String[]{ID,TIME,DATE,AMPM,TYPE
				,RECEIVED_MESSAGE,SENT_MESSAGE,NUMBER}, null,null,null,null,null);
		
	}
	//return log based on parent id
	public Cursor SearchLogByParentId(int pid){
		SQLiteDatabase db = log.getReadableDatabase();
		
		return db.query(LOG_TABLE, new String[]{ID,TIME,DATE,AMPM,TYPE
				,RECEIVED_MESSAGE,SENT_MESSAGE,NUMBER},PARENT_ID+"=?"
				, new String[]{String.valueOf(pid)}, null, null, null);
	}
	
	
	
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
