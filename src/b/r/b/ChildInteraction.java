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


public class ChildInteraction extends Activity{
	//Constants used to access a column using the cursor getString() method later
	private final int PID_COLUMN = 1;
	//private final int PARENT_ID_COLUMN = 4;
	//private final int PCHILD_ID_COLUMN = 3;
	private final int CNUMBERS_COLUMN = 2;
	//initializing the local objects
	private logDB log;
	private parentDB parent;
	private childDB child;
	
	//private StringArrayConverter strc = new StringArrayConverter();
	private Context context;
	
	public ChildInteraction(Context ctx){
		context = ctx;
		
		child = new childDB(context);
		
	}
	
	
	
	
	public boolean ChildEditMessage(String oldMessage, String newMessage){
		SQLiteDatabase dbr = child.getReadableDatabase();
		SQLiteDatabase dbw = child.getWritableDatabase();
		
		Cursor c = dbr.query(CHILD_TABLE, new String[] {ID,NUMBER,MESSAGE,PARENT_ID}, MESSAGE+"=?"
				, new String[]{oldMessage}, null, null, null);
		int cid = c.getInt(PID_COLUMN);
		int pid = c.getInt(c.getColumnIndex(PARENT_ID));
		String stringPid = String.valueOf(pid);
		int number = GetNumberFromChild(cid);
		String stringNumbers = String.valueOf(number);
		ContentValues values = new ContentValues();
		values.put(ID, cid);
		values.put(NUMBER, stringNumbers);
		values.put(MESSAGE,newMessage);
		values.put(PARENT_ID,stringPid);
		//did it work?
		return dbw.update(CHILD_TABLE, values, null, null) > 0;
	}
	
	

	public boolean ChildEditMessage(int cid, String newMessage){
		SQLiteDatabase dbr = child.getReadableDatabase();
		SQLiteDatabase dbw = child.getWritableDatabase();
		
		Cursor c = dbr.query(CHILD_TABLE, new String[] {ID,NUMBER,MESSAGE,PARENT_ID}, ID+"=?"
				, new String[]{String.valueOf(cid)}, null, null, null);
		
		int pid = c.getInt(c.getColumnIndex(PARENT_ID));
		String stringPid = String.valueOf(pid);
		
		int number = GetNumberFromChild(cid);
		String stringNumbers = String.valueOf(number);
		ContentValues values = new ContentValues();
		
		values.put(ID, cid);
		values.put(MESSAGE,newMessage);
		values.put(NUMBER, stringNumbers);
		values.put(PARENT_ID,stringPid);
		//did it work?
		return dbw.update(CHILD_TABLE, values, null, null) > 0;
	}
	
	
	//For deletes, returns true if it worked, returns false if it doesn't
	//deleting a child row
	public boolean DeleteChild(String num, String message){
		SQLiteDatabase db = child.getWritableDatabase();
		
		return db.delete(CHILD_TABLE, NUMBER + "=" + num + 
				" AND " + MESSAGE + "=" + message,  null) > 0;
	}
	
	public long InsertMessage(String number, String message, long pid){
		String p = String.valueOf(pid);
		SQLiteDatabase db = child.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(NUMBER, number);
		values.put(MESSAGE, message);
		values.put(PARENT_ID, p);
		return db.insertOrThrow(CHILD_TABLE, null, values);
	}
	
	
	
	//to SearchByMessage just pass the String of the message
	public Cursor SearchChildByMessage(String message){
		SQLiteDatabase db = child.getReadableDatabase();
		
		return db.query(CHILD_TABLE, new String[] {ID,NUMBER,MESSAGE,PARENT_ID}, MESSAGE+"=?"
				, new String[]{message}, null, null, null);
		
	}
	
	//To search by ID (not sure why you would) just pass the id as a string
	public Cursor SearchChildById(int cid){
		SQLiteDatabase db = child.getReadableDatabase();
		
		return db.query(CHILD_TABLE, new String[] {ID,NUMBER,MESSAGE,PARENT_ID},ID+"=?"
				, new String[]{String.valueOf(cid)}, null, null, null);
	}
	
	//To search by childID just pass the number
	//remember, c may be null so make sure you try catch when you call
	//also I'm not sure if this will return all the messages with the sent child ID
	//or just the last one
	public Cursor SearchChildByParentId(int pid){
		SQLiteDatabase db = child.getReadableDatabase();
		
		return db.query(CHILD_TABLE, new String[] {ID,NUMBER,MESSAGE,PARENT_ID},ID+"=?"
				, new String[]{String.valueOf(pid)}, null, null, null);
	}
	
	public Cursor SearchChildByNumber(int number){
		SQLiteDatabase db = child.getReadableDatabase();
		
		return db.query(CHILD_TABLE, new String[] {ID,NUMBER,MESSAGE,PARENT_ID},NUMBER+"=?"
				, new String[]{String.valueOf(number)}, null, null, null);
	}
	//Just send the parent id and you will get an array of the child ids back
	/*
	 public int[] GetChildIdsFromParent(String pid){
	 
		SQLiteDatabase db = parent.getWritableDatabase();
		
		Cursor c = db.query(PARENT_TABLE, new String[]{ID}, ID+"=?", new String[]{pid}, null, null, null);
		
		// HERE
		String a = c.getString(PARENT_ID_COLUMN);
		String[] b = strc.convertStringToArray(a);
		int[] cids = new int[b.length];
		for(int i = 0; i < cids.length; i++){
			cids[i] = Integer.parseInt(b[i]);
		}
		return cids;
	}
	*/
	public int GetNumberFromChild(int cid){
		SQLiteDatabase db = child.getWritableDatabase();
		
		Cursor c = db.query(CHILD_TABLE, new String[]{ID}, ID+"=?", new String[]{String.valueOf(cid)}
		, null, null, null);
		
		String a = c.getString(CNUMBERS_COLUMN);
		int number = Integer.parseInt(a);
		
		return number;
		
	}
	
	
	public void CleanupChild(){
		child.close();
	}
	
	public void Cleanup(){
	    if (child != null) {
	        child.close();
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

