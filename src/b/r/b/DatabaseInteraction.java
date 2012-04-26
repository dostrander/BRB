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


public class DatabaseInteraction extends Activity{
	//Constants used to access a column using the cursor getString() method later
	private final int PID_COLUMN = 1;
	private final int PARENT_ID_COLUMN = 4;
	private final int PCHILD_ID_COLUMN = 3;
	private final int CNUMBERS_COLUMN = 2;
	//initializing the local objects
	private logDB log;
	private parentDB parent;
	private childDB child;
	private SQLiteDatabase ldbr;
	private SQLiteDatabase ldbw;
	private SQLiteDatabase cdbr;
	private SQLiteDatabase cdbw;
	private SQLiteDatabase pdbr;
	private SQLiteDatabase pdbw;
	private StringArrayConverter strc = new StringArrayConverter();
	private Context context;
	
	public DatabaseInteraction(Context ctx){
		context = ctx;
		log = new logDB(context);
		parent = new parentDB(context);
		child = new childDB(context);
		ldbr = log.getReadableDatabase();
		ldbw = log.getWritableDatabase();
		cdbr = child.getReadableDatabase();
		cdbw = child.getWritableDatabase();
		pdbr = parent.getReadableDatabase();
		pdbw = parent.getWritableDatabase();
	}
	
	public Message InsertLog(int pid, String time, String date, int ampm, int type, String msg, String num){
		//SQLiteDatabase db = log.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(PARENT_ID, pid);
		values.put(TIME,time);
		values.put(DATE, date);
		values.put(AMPM,ampm);
		values.put(TYPE, type);
		values.put(MESSAGE, msg);
		values.put(NUMBER,num);
		long id = ldbw.insertOrThrow(PARENT_TABLE, null, values);
		return new Message(msg,(int) id);
		
		
	}
	
	
	//overloading Parent edit message so you can edit by sending the old message, or the id, if you have it
	//returns true meant it worked, otherwise false, yo
	public boolean ParentEditMessage(String oldMessage, String newMessage){
		//SQLiteDatabase dbr = parent.getReadableDatabase();
		//SQLiteDatabase dbw = parent.getWritableDatabase();
		
		Cursor c = pdbr.query(PARENT_TABLE, new String[] {ID,MESSAGE}, MESSAGE+"=?"
				, new String[]{oldMessage}, null, null, null);
		String pid = c.getString(PID_COLUMN);
		
		ContentValues values = new ContentValues();
		values.put(ID, pid);
		values.put(MESSAGE,newMessage);
		//did it work?
		return pdbw.update(PARENT_TABLE, values, null, null) > 0;
	}
	
	public boolean ParentEditMessage(int pid, String newMessage){
		Log.d("stuff",String.valueOf(pid));
		//SQLiteDatabase db = parent.getWritableDatabase();
		String stringPid = "";
		// What are we doing here????
		stringPid = String.valueOf(pid);
		
		// END
		ContentValues values = new ContentValues();
		
		values.put(ID, stringPid);
		values.put(MESSAGE,newMessage);
		//did it work?
		return pdbw.update(PARENT_TABLE, values, null, null) > 0;
	}
	
	
	public boolean ChildEditMessage(String oldMessage, String newMessage){
		//SQLiteDatabase dbr = child.getReadableDatabase();
		//SQLiteDatabase dbw = child.getWritableDatabase();
		
		Cursor c = cdbr.query(CHILD_TABLE, new String[] {ID,NUMBER,MESSAGE,PARENT_ID}, MESSAGE+"=?"
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
		return cdbw.update(CHILD_TABLE, values, null, null) > 0;
	}
	
	

	public boolean ChildEditMessage(int cid, String newMessage){
		//SQLiteDatabase dbr = child.getReadableDatabase();
		//SQLiteDatabase dbw = child.getWritableDatabase();
		
		Cursor c = cdbr.query(CHILD_TABLE, new String[] {ID,NUMBER,MESSAGE,PARENT_ID}, ID+"=?"
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
		return cdbw.update(CHILD_TABLE, values, null, null) > 0;
	}
	
	
	//For deletes, returns true if it worked, returns false if it doesn't
	//deleting a child row
	public boolean DeleteChild(String num, String message){
		//SQLiteDatabase db = child.getWritableDatabase();
		
		return cdbw.delete(CHILD_TABLE, NUMBER + "=" + num + 
				" AND " + MESSAGE + "=" + message,  null) > 0;
	}
	
	//deleting a log row
	public boolean DeleteLog(int id){
		//SQLiteDatabase db = log.getWritableDatabase();
		
		return ldbw.delete(LOG_TABLE, _ID + "=" + id, null) > 0;
	}
	//deleting a parent row
	public boolean DeleteParent(String message){
		//SQLiteDatabase db = parent.getWritableDatabase();
		
		return pdbw.delete(PARENT_TABLE, MESSAGE + "=" + message, null) > 0;
	}
	//to insert a message, you pass the string of the message, and an
	//array of strings that contain all the numbers it is used for	
	//For inserting into the parent, pass an id, the message, and an array of possible child 
		//ids
	public Message InsertMessage(String message){
		//SQLiteDatabase db = parent.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(MESSAGE, message);
		long id = pdbw.insertOrThrow(PARENT_TABLE, null, values);
		return new Message(message,(int) id);
	}
	
	
	//For inserting into the child, pass the id, an array of the numbers, the message, and 
	//an array of the parent ids
	public Message InsertMessage(String number, String message, long pid){
		String p = String.valueOf(pid);
		//SQLiteDatabase db = child.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(NUMBER, number);
		values.put(MESSAGE, message);
		values.put(PARENT_ID, p);
		
		long id = cdbw.insertOrThrow(CHILD_TABLE, null, values);
		return new Message(message,(int) id);
	}
	
	
	
	//to SearchByMessage just pass the String of the message
	public Cursor SearchChildByMessage(String message){
		//SQLiteDatabase db = child.getReadableDatabase();
		
		return cdbr.query(CHILD_TABLE, new String[] {ID,NUMBER,MESSAGE,PARENT_ID}, MESSAGE+"=?"
				, new String[]{message}, null, null, null);
		
	}
	
	//To search by ID (not sure why you would) just pass the id as a string
	public Cursor SearchChildById(int cid){
		//SQLiteDatabase db = child.getReadableDatabase();
		
		return cdbr.query(CHILD_TABLE, new String[] {ID,NUMBER,MESSAGE,PARENT_ID},ID+"=?"
				, new String[]{String.valueOf(cid)}, null, null, null);
	}
	
	//To search by childID just pass the number
	//remember, c may be null so make sure you try catch when you call
	//also I'm not sure if this will return all the messages with the sent child ID
	//or just the last one
	public Cursor SearchChildByParentId(int pid){
		//SQLiteDatabase db = child.getReadableDatabase();
		
		return cdbr.query(CHILD_TABLE, new String[] {ID,NUMBER,MESSAGE,PARENT_ID},ID+"=?"
				, new String[]{String.valueOf(pid)}, null, null, null);
	}
	
	public Cursor SearchChildByNumber(int number){
		//SQLiteDatabase db = child.getReadableDatabase();
		
		return cdbr.query(CHILD_TABLE, new String[] {ID,NUMBER,MESSAGE,PARENT_ID},NUMBER+"=?"
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
	public Cursor GetAllParentMessages(){
		//SQLiteDatabase db = parent.getReadableDatabase();
		
		return pdbr.query(PARENT_TABLE, new String[]{ID,MESSAGE}, null, 
				null, null, null, null,null);
	}
	
	//returns all logs, might need this
	public Cursor GetAllLogs(){
		//SQLiteDatabase db = log.getReadableDatabase();
		
		return ldbr.query(LOG_TABLE, new String[]{ID,PARENT_ID,TIME,DATE,AMPM,TYPE
				,RECEIVED_MESSAGE,NUMBER}, null,null,null,null,null);
		
	}
	//return log based on parent id
	public Cursor SearchLogByParentId(int id){
		//SQLiteDatabase db = log.getReadableDatabase();
		
		return ldbr.query(LOG_TABLE, new String[]{ID,PARENT_ID,TIME,DATE,AMPM,TYPE
				,RECEIVED_MESSAGE,NUMBER},PARENT_ID+"=?"
				, new String[]{String.valueOf(id)}, null, null, null);
	}
	
	
	//to SearchByMessage just pass the String of the message
	public Cursor SearchParentByMessage(String message){
		//SQLiteDatabase db = parent.getReadableDatabase();
	
		return pdbr.query(PARENT_TABLE, new String[] {ID,MESSAGE}, MESSAGE+"=?"
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
		//SQLiteDatabase db = parent.getReadableDatabase();
		
		return pdbr.query(PARENT_TABLE, new String[] {ID,MESSAGE},ID+"=?"
				, new String[]{String.valueOf(id)}, null, null, null);
	}
		
	public Message GetParentById(int id){
		Cursor c = SearchParentById(id);
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
	
		
	public int GetNumberFromChild(int cid){
		//SQLiteDatabase db = child.getWritableDatabase();
		
		Cursor c = cdbw.query(CHILD_TABLE, new String[]{ID}, ID+"=?", new String[]{String.valueOf(cid)}
		, null, null, null);
		
		String a = c.getString(CNUMBERS_COLUMN);
		int number = Integer.parseInt(a);
		
		return number;
		
	}
	
	public void CleanupParent(){
		parent.close();
	}
	
	public void CleanupChild(){
		child.close();
	}
	
	public void CleanupLog(){
		log.close();
	}
	
	public void Cleanup(){
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
