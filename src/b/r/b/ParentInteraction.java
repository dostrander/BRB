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


public class ParentInteraction extends Activity{
	//Constants used to access a column using the cursor getString() method later
	private final int PID_COLUMN = 1;
	private final int PARENT_ID_COLUMN = 4;
	private final int PCHILD_ID_COLUMN = 3;
	private final int CNUMBERS_COLUMN = 2;
	//initializing the local objects
	private logDB log;
	private parentDB parent;
	private childDB child;
	
	//private StringArrayConverter strc = new StringArrayConverter();
	private Context context;
	
	public ParentInteraction(Context ctx){
		context = ctx;
		
		parent = new parentDB(context);
		
		
	}
	
	
	
	
	//overloading Parent edit message so you can edit by sending the old message, or the id, if you have it
	//returns true meant it worked, otherwise false, yo
	public boolean ParentEditMessage(String oldMessage, String newMessage){
		SQLiteDatabase dbr = parent.getReadableDatabase();
		SQLiteDatabase dbw = parent.getWritableDatabase();
		
		Cursor c = dbr.query(PARENT_TABLE, new String[] {ID,MESSAGE}, MESSAGE+"=?"
				, new String[]{oldMessage}, null, null, null);
		String pid = c.getString(PID_COLUMN);
		
		ContentValues values = new ContentValues();
		values.put(ID, pid);
		values.put(MESSAGE,newMessage);
		//did it work?
		return dbw.update(PARENT_TABLE, values, null, null) > 0;
	}
	
	public boolean ParentEditMessage(int pid, String newMessage){
		Log.d("stuff",String.valueOf(pid));
		SQLiteDatabase db = parent.getWritableDatabase();
		String stringPid = "";
		
		stringPid = String.valueOf(pid);
		
		// END
		ContentValues values = new ContentValues();
		
		values.put(ID, stringPid);
		values.put(MESSAGE,newMessage);
		//did it work?
		return db.update(PARENT_TABLE, values, null, null) > 0;
	}
	
	
	//deleting a parent row
	public boolean DeleteParent(String message){
		SQLiteDatabase db = parent.getWritableDatabase();
		
		return db.delete(PARENT_TABLE, MESSAGE + "=" + message, null) > 0;
	}
	//to insert a message, you pass the string of the message, and an
	//array of strings that contain all the numbers it is used for	
	//For inserting into the parent, pass an id, the message, and an array of possible child 
		//ids
	public Message InsertMessage(String message){
		SQLiteDatabase db = parent.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(MESSAGE, message);
		long id = db.insertOrThrow(PARENT_TABLE, null, values);
		return new Message(message,(int) id);
	}
	
	
	//For inserting into the child, pass the id, an array of the numbers, the message, and 
	//an array of the parent ids
	
	public Cursor GetAllParentMessages(){
		SQLiteDatabase db = parent.getReadableDatabase();
		
		return db.query(PARENT_TABLE, new String[]{ID,MESSAGE}, null, 
				null, null, null, null,null);
	}
	public Cursor SearchParentByMessage(String message){
		SQLiteDatabase db = parent.getReadableDatabase();
	
		return db.query(PARENT_TABLE, new String[] {ID,MESSAGE}, MESSAGE+"=?"
			, new String[]{message}, null, null, null);
	
	}
	public Message GetParentByMessage(String message){
		Cursor c = SearchParentByMessage(message);
		if(c.moveToFirst()){
			return new Message(c.getString(c.getColumnIndex(MESSAGE)),c.getInt(c.getColumnIndex(ID)));
		} else return null;
	}
	
		//To search by ID (not sure why you would) just pass the id as a string
	public Cursor SearchParentById(int id){
		SQLiteDatabase db = parent.getReadableDatabase();
		
		return db.query(PARENT_TABLE, new String[] {ID,MESSAGE},ID+"=?"
				, new String[]{String.valueOf(id)}, null, null, null);
	}
		
	public Message GetParentById(int id){
		Cursor c = SearchParentById(id);
		Log.d("DatabaseInteraction", String.valueOf(c.getCount()));
		if(c.moveToFirst()){
			return new Message(c.getString(c.getColumnIndex(MESSAGE)),Integer.valueOf(id));
		} else return null;
	}
		//To search by childID just pass the number
		//remember, c may be null so make sure you try catch when you call
		//also I'm not sure if this will return all the messages with the sent child ID
		//or just the last one
	/*public Cursor SearchParentByChildId(String number){
		SQLiteDatabase db = parent.getReadableDatabase();
		
		Cursor c = db.rawQuery(PARENT_TABLE, null);
		String s = c.getString(PCHILD_IDS_COLUMN);
		String[] a = strc.convertStringToArray(s);
		String[] ids = new String[a.length];
		
		for(int i = 0; i < a.length; i ++){
			if(a[i].equals(number)){
				ids[i] = c.getString(PID_COLUMN);
				c = db.query(PARENT_TABLE, new String[] {ID,MESSAGE,CHILD_IDS},ID+"=?"
						, new String[]{ids[i]}, null,null,null);
			}
		}
		
		return c;
	}*/
	
		
	
	
	public void CleanupParent(){
		parent.close();
	}
	
	public void Cleanup(){
		if (parent != null) {
	        parent.close();
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

