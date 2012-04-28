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
	private final int PID_COLUMN = 1;
	private final int PARENT_ID_COLUMN = 4;
	private final int PCHILD_ID_COLUMN = 3;
	private final int CNUMBERS_COLUMN = 2;
	//initializing the local objects
	private logDB log;
	
	private StringArrayConverter strc = new StringArrayConverter();
	private Context context;
	
	public LogInteraction(Context ctx){
		context = ctx;
		log = new logDB(context);
		
	}
	
	public Message InsertLog(int pid, String time, String date, int ampm, int type, String msg, String num){
		SQLiteDatabase db = log.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(PARENT_ID, pid);
		values.put(TIME,time);
		values.put(DATE, date);
		values.put(AMPM,ampm);
		values.put(TYPE, type);
		values.put(RECEIVED_MESSAGE, msg);
		values.put(NUMBER,num);
		long id = db.insertOrThrow(LOG_TABLE, null, values);
		return new Message(msg,(int) id);
		
		
	}
	
	
	//overloading Parent edit message so you can edit by sending the old message, or the id, if you have it
	//returns true meant it worked, otherwise false, yo
		
	//deleting a log row
	public boolean DeleteLog(int id){
		SQLiteDatabase db = log.getWritableDatabase();
		
		return db.delete(LOG_TABLE, _ID + "=" + id, null) > 0;
	}
	//deleting a parent row
	public Cursor GetAllLogs(){
		SQLiteDatabase db = log.getReadableDatabase();
		
		return db.query(LOG_TABLE, new String[]{PARENT_ID,TIME,DATE,AMPM,TYPE
				,RECEIVED_MESSAGE,NUMBER}, null,null,null,null,null);
		
	}
	//return log based on parent id
	public Cursor SearchLogByParentId(int id){
		SQLiteDatabase db = log.getReadableDatabase();
		
		return db.query(LOG_TABLE, new String[]{ID,PARENT_ID,TIME,DATE,AMPM,TYPE
				,RECEIVED_MESSAGE,NUMBER},PARENT_ID+"=?"
				, new String[]{String.valueOf(id)}, null, null, null);
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
	    if (log != null){
	    	log.close();
	    }
	}
}
