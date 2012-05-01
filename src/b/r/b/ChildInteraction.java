//BRB This class written by Jason Mather on 4/28/2012

// Project: Derek Ostrander, Stu Lang, Will Stahl, Evan Dodge, Jason Mather
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




public class ChildInteraction extends Activity{
	//this is just a fancy way of saying the _id is found in column 1 and the 
	//numbers can be found in column 2
	private final int PID_COLUMN = 1;
	
	private final int CNUMBERS_COLUMN = 2;
	//initializing the local objects
	private logDB log;
	private parentDB parent;
	private childDB child;
	
	//private StringArrayConverter strc = new StringArrayConverter();
	private Context context;
	private boolean editSuccess;
	private boolean deleteSuccess;
	public ChildInteraction(Context ctx){
		context = ctx;
		//Since this is the childInteraction, we deal only with the child dB
		child = new childDB(context);
		
	}
	
	//This is the first of the overloaded ChildEditMessage methods
	//This version takes in the old message, AND the parent id, and replaces it with
	//the new message.
	public boolean ChildEditMessage(String oldMessage, int pid, String newMessage){
		//we need both a readable and a writable for this function
		SQLiteDatabase dbr = child.getReadableDatabase();
		SQLiteDatabase dbw = child.getWritableDatabase();
		
		//c now contains the row/s which have a message AND parent id which match the 
		//ones passed to the function
		Cursor c = dbr.query(CHILD_TABLE, new String[] {ID,NUMBER,MESSAGE,PARENT_ID} 
				,MESSAGE+"=?"+" AND " + PARENT_ID + "=?"
				, new String[]{oldMessage,String.valueOf(pid)}, null, null, null);
		//although confusing, this just means get from column 1
		//need to grab the other columns locally so we can put them in again for safety
		int cid = c.getInt(PID_COLUMN);
		
		String stringPid = String.valueOf(pid);
		int number = GetNumberFromChild(cid);
		String stringNumbers = String.valueOf(number);
		c.close();
		ContentValues values = new ContentValues();
		//get the row all ready for insert
		values.put(ID, cid);
		values.put(NUMBER, stringNumbers);
		values.put(MESSAGE,newMessage);
		values.put(PARENT_ID,stringPid);
		//did it work?
		editSuccess = dbw.update(CHILD_TABLE, values, null, null) > 0;
		//clean up
		dbr.close();
		dbw.close();
		return editSuccess;
	}
	
	//This version just takes in the old message and replaces it
	public boolean ChildEditMessage(String oldMessage, String newMessage){
		//We need both a readable and a writable database for this function
		SQLiteDatabase dbr = child.getReadableDatabase();
		SQLiteDatabase dbw = child.getWritableDatabase();

		//c now contains the row/s which have a message which matches the 
		//one passed to the function
		Cursor c = dbr.query(CHILD_TABLE, new String[] {ID,NUMBER,MESSAGE,PARENT_ID}, MESSAGE+"=?"
				, new String[]{oldMessage}, null, null, null);
		int cid = c.getInt(PID_COLUMN);
		int pid = c.getInt(c.getColumnIndex(PARENT_ID));
		String stringPid = String.valueOf(pid);
		int number = GetNumberFromChild(cid);
		String stringNumbers = String.valueOf(number);
		c.close();
		ContentValues values = new ContentValues();
		//prepare the row for insertion
		values.put(ID, cid);
		values.put(NUMBER, stringNumbers);
		values.put(MESSAGE,newMessage);
		values.put(PARENT_ID,stringPid);
		//did it work?
		editSuccess = dbw.update(CHILD_TABLE, values, null, null) > 0;
		//cleanup
		dbr.close();
		dbw.close();
		return editSuccess;
	}
	
	
	//This version just replaces the message by the id
	public boolean ChildEditMessage(int cid, String newMessage){
		SQLiteDatabase dbr = child.getReadableDatabase();
		SQLiteDatabase dbw = child.getWritableDatabase();
		//here c has the row which had the id passed to the function
		Cursor c = dbr.query(CHILD_TABLE, new String[] {ID,NUMBER,MESSAGE,PARENT_ID}, ID+"=?"
				, new String[]{String.valueOf(cid)}, null, null, null);
		
		int pid = c.getInt(c.getColumnIndex(PARENT_ID));
		String stringPid = String.valueOf(pid);
		c.close();
		
		int number = GetNumberFromChild(cid);
		String stringNumbers = String.valueOf(number);
		ContentValues values = new ContentValues();
		//preparing the row for insertion
		values.put(ID, cid);
		values.put(MESSAGE,newMessage);
		values.put(NUMBER, stringNumbers);
		values.put(PARENT_ID,stringPid);
		//did it work?
		editSuccess = dbw.update(CHILD_TABLE, values, null, null) > 0;
		//cleanup
		dbr.close();
		dbw.close();
		return editSuccess;
	}
	
	
	//For deletes, returns true if it worked, returns false if it doesn't
	//deleting a child row
	public boolean DeleteChild(String num, String message){
		SQLiteDatabase db = child.getWritableDatabase();
		deleteSuccess = db.delete(CHILD_TABLE, NUMBER + "=" + num + 
				" AND " + MESSAGE + "=" + message,  null) > 0;
		db.close();
		return deleteSuccess;
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
		
		return db.query(CHILD_TABLE, new String[] {ID,NUMBER,MESSAGE,PARENT_ID},PARENT_ID+"=?"
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
		c.close();
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

