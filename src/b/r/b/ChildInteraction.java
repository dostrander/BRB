//BRB This class written by Jason Mather on 4/28/2012

// Project: Derek Ostrander, Stu Lang, Will Stahl, Evan Dodge, Jason Mather
//This class allows us to store and retrieve data to and from the database in an
//easier manner
package b.r.b;

import static b.r.b.Constants.*;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;





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
		c.close();//close the cursor since we have the values we need locally now
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
		return editSuccess;//return success value of the update
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
		//grab all the other values
		int cid = c.getInt(PID_COLUMN);
		int pid = c.getInt(c.getColumnIndex(PARENT_ID));
		String stringPid = String.valueOf(pid);
		int number = GetNumberFromChild(cid);
		String stringNumbers = String.valueOf(number);
		c.close();//we can now close the cursor since we have all the values 
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
		return editSuccess;//return the success of the update
	}
	
	
	//This version just replaces the message by the id
	public boolean ChildEditMessage(int cid, String newMessage){
		Log.d("child interaction","cid: " + String.valueOf(cid) );
		//need both a readable and a writable
		SQLiteDatabase dbr = child.getReadableDatabase();
		SQLiteDatabase dbw = child.getWritableDatabase();
		//here c has the row which had the id passed to the function
		Cursor c = dbr.query(CHILD_TABLE, new String[] {ID,NUMBER,MESSAGE,PARENT_ID}, ID+"=?"
				, new String[]{String.valueOf(cid)}, null, null, null);
		
		c.moveToFirst();
		int pid = c.getInt(c.getColumnIndex(PARENT_ID));
		String stringPid = String.valueOf(pid);
		c.close();//we can close the cursor since we have all the values we need locally
		
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
		return editSuccess;//return the success value of the update
	}
	
	
	//For delete, returns true if it worked, returns false if it doesn't
	//deleting a child row
	//It takes in a string which represents the number (3155555555)
	//and the message ("I'll be back later")
	public boolean DeleteChild(String num, String message){
		//Just need to write
		SQLiteDatabase db = child.getWritableDatabase();
		
		//the boolean contains truth value of the success of the delete statement,
		//which is looking for a row with the correct number AND message and then
		//deleting the entry if it exists
		deleteSuccess = db.delete(CHILD_TABLE, NUMBER + "= ?" + 
				" AND " + MESSAGE + "= ?",  new String[]{num,message}) > 0;
		db.close();
		return deleteSuccess;
	}
	//In order to insert a message into the child database, you just pass a number,
	//the message, and the parent id
	public long InsertMessage(String number, String message, long pid){
		String p = String.valueOf(pid);//we need to make a string version
		SQLiteDatabase db = child.getWritableDatabase();//just need to write
		ContentValues values = new ContentValues();
		//prepare the row for insertion
		values.put(NUMBER, number);
		values.put(MESSAGE, message);
		values.put(PARENT_ID, p);
		return db.insertOrThrow(CHILD_TABLE, null, values);//return the id of the child message
	}
	
	
	
	//to SearchByMessage just pass the String of the message
	public Cursor SearchChildByMessage(String message){
		SQLiteDatabase db = child.getReadableDatabase();
		//returns cursor which contains the rows with a message which matches the one passed
		return db.query(CHILD_TABLE, new String[] {ID,NUMBER,MESSAGE,PARENT_ID}, MESSAGE+"=?"
				, new String[]{message}, null, null, null);
		
	}
	
	//To search by ID (not sure why you would) just pass the id as an int
	public Cursor SearchChildById(int cid){
		SQLiteDatabase db = child.getReadableDatabase();
		//returns cursor which contains the row with an id which matches the one passed

		return db.query(CHILD_TABLE, new String[] {ID,NUMBER,MESSAGE,PARENT_ID},ID+"=?"
				, new String[]{String.valueOf(cid)}, null, null, null);
	}
	
	//To search by Parent id just pass the id as an int
	public Cursor SearchChildByParentId(int pid){
		SQLiteDatabase db = child.getReadableDatabase();
		//returns cursor which contains the rows with a parent id which matches the one passed

		return db.query(CHILD_TABLE, new String[] {ID,NUMBER,MESSAGE,PARENT_ID},PARENT_ID+"=?"
				, new String[]{String.valueOf(pid)}, null, null, null);
	}
	
	
	//To search for a child by number just pass the number as an int
	public Cursor SearchChildByNumber(int number){
		SQLiteDatabase db = child.getReadableDatabase();
		
		return db.query(CHILD_TABLE, new String[] {ID,NUMBER,MESSAGE,PARENT_ID},NUMBER+"=?"
				, new String[]{String.valueOf(number)}, null, null, null);
	}
	
	
	//To get a child's number just pass the id
	public int GetNumberFromChild(int cid){
		SQLiteDatabase db = child.getReadableDatabase();//we need a readable database
		
		Cursor c = db.query(CHILD_TABLE, new String[]{ID}, ID+"=?", new String[]{String.valueOf(cid)}
		, null, null, null);//grab the row with the correct id
		
		c.moveToFirst();
		String a = c.getString(CNUMBERS_COLUMN);//grab the number from the number column
		c.close();//we can close since we no stored the value
		db.close();//since the cursor is no longer open we can close the dB
		int number = Integer.parseInt(a);
		
		return number;
		
	}
	
	//some cleanup functions
	public void CleanupChild(){
		child.close();
	}
	
	public void Cleanup(){
	    if (child != null) {
	        child.close();
	    }
	   	    
	}
	
	
	//for extra safety, onDestroy close any open databases
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

