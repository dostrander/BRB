//BRB Derek Ostrander, Stu Lang, Will Stahl, Evan Dodge, Jason Mather
//This class allows us to store and retrieve data to and from the database in an
//easier manner
package b.r.b;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import static b.r.b.Constants.*;

public class DatabaseInteraction {
	//Constants used to access a column using the cursor getString() method later
	private final int PID_COLUMN = 1;
	private final int PARENT_IDS_COLUMN = 4;
	private final int PCHILD_IDS_COLUMN = 3;
	private final int CPARENT_IDS_COLUMN = 4;
	private final int CNUMBERS_COLUMN = 2;
	//initializing the local objects
	private parentDB parent;
	private childDB child;
	private StringArrayConverter strc = new StringArrayConverter();
	private Context context;
	
	public DatabaseInteraction(Context ctx){
		context = ctx;
		parent = new parentDB(context);
		child = new childDB(context);
	}
	
	//overloading Parent edit message so you can edit by sending the old message, or the id, if you have it
	//returns true meant it worked, otherwise false, yo
	public boolean ParentEditMessage(String oldMessage, String newMessage){
		SQLiteDatabase dbr = parent.getReadableDatabase();
		SQLiteDatabase dbw = parent.getWritableDatabase();
		
		Cursor c = dbr.query(PARENT_TABLE, new String[] {ID,MESSAGE,CHILD_IDS}, MESSAGE+"=?"
				, new String[]{oldMessage}, null, null, null);
		String pid = c.getString(PID_COLUMN);
		int[] cids = GetChildIdsFromParent(pid);
		String stringCids = "";
		for(int i = 0; i < cids.length; i++){
			stringCids = stringCids + cids[i];
			if( i != cids.length - 1){
				stringCids = stringCids + ",";
			}
		}
		ContentValues values = new ContentValues();
		values.put(ID, pid);
		values.put(MESSAGE,newMessage);
		values.put(CHILD_IDS,stringCids);
		//did it work?
		return dbw.update(PARENT_TABLE, values, null, null) > 0;
	}
	
	public boolean ParentEditMessage(int pid, String newMessage){
		SQLiteDatabase db = parent.getWritableDatabase();
		String stringPid = "";
		stringPid = stringPid + pid;
		int[] cids = GetChildIdsFromParent(stringPid);
		String stringCids = "";
		for(int i = 0; i < cids.length; i++){
			stringCids = stringCids + cids[i];
			if( i != cids.length - 1){
				stringCids = stringCids + ",";
			}
		}
		ContentValues values = new ContentValues();
		
		values.put(ID, stringPid);
		values.put(MESSAGE,newMessage);
		values.put(CHILD_IDS,stringCids);
		//did it work?
		return db.update(PARENT_TABLE, values, null, null) > 0;
	}
	
	
	public boolean ChildEditMessage(String oldMessage, String newMessage){
		SQLiteDatabase dbr = child.getReadableDatabase();
		SQLiteDatabase dbw = child.getWritableDatabase();
		
		Cursor c = dbr.query(CHILD_TABLE, new String[] {ID,MESSAGE,CHILD_IDS}, MESSAGE+"=?"
				, new String[]{oldMessage}, null, null, null);
		String cid = c.getString(PID_COLUMN);
		int[] pids = GetParentIdsFromChild(cid);
		String stringPids = "";
		for(int i = 0; i < pids.length; i++){
			stringPids = stringPids + pids[i];
			if( i != pids.length - 1){
				stringPids = stringPids + ",";
			}
		}
		int [] numbers = GetNumbersFromChild(cid);
		String stringNumbers = "";
		for(int i = 0; i < numbers.length; i++){
			stringNumbers = stringNumbers + numbers[i];
			if( i != numbers.length - 1){
				stringNumbers = stringNumbers + ",";
			}
		}
		ContentValues values = new ContentValues();
		values.put(ID, cid);
		values.put(NUMBERS, stringNumbers);
		values.put(MESSAGE,newMessage);
		values.put(PARENT_IDS,stringPids);
		//did it work?
		return dbw.update(CHILD_TABLE, values, null, null) > 0;
	}
	
	

	public boolean ChildEditMessage(int cid, String newMessage){
		SQLiteDatabase db = child.getWritableDatabase();
		String stringCid = "";
		stringCid = stringCid + cid;
		int[] pids = GetParentIdsFromChild(stringCid);
		String stringPids = "";
		for(int i = 0; i < pids.length; i++){
			stringPids = stringPids + pids[i];
			if( i != pids.length - 1){
				stringPids = stringPids + ",";
			}
		}
		int [] numbers = GetNumbersFromChild(stringCid);
		String stringNumbers = "";
		for(int i = 0; i < numbers.length; i++){
			stringNumbers = stringNumbers + numbers[i];
			if( i != numbers.length - 1){
				stringNumbers = stringNumbers + ",";
			}
		}
		ContentValues values = new ContentValues();
		
		values.put(ID, stringCid);
		values.put(MESSAGE,newMessage);
		values.put(NUMBERS, stringNumbers);
		values.put(PARENT_IDS,stringPids);
		//did it work?
		return db.update(CHILD_TABLE, values, null, null) > 0;
	}
	
	
	
	
	
	
	//to insert a message, you pass the string of the message, and an
	//array of strings that contain all the numbers it is used for	
	//For inserting into the parent, pass an id, the message, and an array of possible child 
		//ids
	public Message InsertMessage(String message, String[] cids){
		SQLiteDatabase db = parent.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(MESSAGE, message);
		values.put(CHILD_IDS, strc.convertArrayToString(cids));
		db.insertOrThrow(PARENT_TABLE, null, values);
		return new Message(message);
	}
	
	
	//For inserting into the child, pass the id, an array of the numbers, the message, and 
	//an array of the parent ids
	public void InsertMessage(String[] numbers, String message, int[] pids){
		String p = "";
		for(int i = 0; i < pids.length; i ++){
			p = p + pids[i];
			if(i != (pids.length - 1)){
				p = p + ",";
			}
			
		}
		SQLiteDatabase db = child.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(NUMBERS, strc.convertArrayToString(numbers));
		values.put(MESSAGE, message);
		values.put(PARENT_IDS, p);
		db.insertOrThrow(CHILD_TABLE, null, values);

	}
	
	
	
	//to SearchByMessage just pass the String of the message
	public Cursor SearchChildByMessage(String message){
		SQLiteDatabase db = child.getReadableDatabase();
		
		return db.query(CHILD_TABLE, new String[] {ID,NUMBERS,MESSAGE,PARENT_IDS}, MESSAGE+"=?"
				, new String[]{message}, null, null, null);
		
	}
	
	//To search by ID (not sure why you would) just pass the id as a string
	public Cursor SearchChildById(String id){
		SQLiteDatabase db = child.getReadableDatabase();
		
		return db.query(CHILD_TABLE, new String[] {ID,NUMBERS,MESSAGE,PARENT_IDS},ID+"=?"
				, new String[]{id}, null, null, null);
	}
	
	//To search by childID just pass the number
	//remember, c may be null so make sure you try catch when you call
	//also I'm not sure if this will return all the messages with the sent child ID
	//or just the last one
	public Cursor SearchChildByParentId(String pid){
		SQLiteDatabase db = child.getReadableDatabase();
		
		Cursor c = db.rawQuery(CHILD_TABLE, null);
		String s = c.getString(PARENT_IDS_COLUMN);
		String[] a = strc.convertStringToArray(s);
		String[] ids = new String[a.length];
		
		for(int i = 0; i < a.length; i ++){
			if(a[i].equals(pid)){
				ids[i] = c.getString(PID_COLUMN);
				c = db.query(PARENT_TABLE, new String[] {ID,MESSAGE,CHILD_IDS},ID+"=?"
						, new String[]{ids[i]}, null,null,null);
			}
		}
		
		return c;
	}
	//Just send the parent id and you will get an array of the child ids back
	public int[] GetChildIdsFromParent(String pid){
		SQLiteDatabase db = parent.getWritableDatabase();
		
		Cursor c = db.query(PARENT_TABLE, new String[]{ID}, ID+"=?", new String[]{pid}, null, null, null);
		
		String a = c.getString(PCHILD_IDS_COLUMN);
		String[] b = strc.convertStringToArray(a);
		int[] cids = new int[b.length];
		for(int i = 0; i < cids.length; i++){
			cids[i] = Integer.parseInt(b[i]);
		}
		return cids;
	}
	
	public Cursor GetAllParentMessages(){
		SQLiteDatabase db = parent.getReadableDatabase();
		
		return db.query(PARENT_TABLE, new String[]{ID,MESSAGE}, null, 
				null, null, null, null,null);
	}
	
	//to SearchByMessage just pass the String of the message
	public Cursor SearchParentByMessage(String message){
	SQLiteDatabase db = parent.getReadableDatabase();
	
	return db.query(PARENT_TABLE, new String[] {ID,MESSAGE,CHILD_IDS}, MESSAGE+"=?"
			, new String[]{message}, null, null, null);
	
	}
	public Message GetParentByMessage(String message){
		Cursor c = SearchParentByMessage(message);
		if(c.moveToFirst()){
			return new Message(c.getString(c.getColumnIndex(MESSAGE)));
		} else return null;
	}
	
		//To search by ID (not sure why you would) just pass the id as a string
	public Cursor SearchParentById(String id){
		SQLiteDatabase db = parent.getReadableDatabase();
		
		return db.query(PARENT_TABLE, new String[] {ID,MESSAGE,CHILD_IDS},ID+"=?"
				, new String[]{id}, null, null, null);
	}
		
	public Message GetParentById(String id){
		Cursor c = SearchParentById(id);
		if(c.moveToFirst()){
			return new Message(c.getString(c.getColumnIndex(MESSAGE)));
		} else return null;
	}
		//To search by childID just pass the number
		//remember, c may be null so make sure you try catch when you call
		//also I'm not sure if this will return all the messages with the sent child ID
		//or just the last one
	public Cursor SearchParentByChildId(String number){
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
	}
	
	public int[] GetParentIdsFromChild(String cid) {
		SQLiteDatabase db = child.getWritableDatabase();
		
		Cursor c = db.query(CHILD_TABLE, new String[]{ID}, ID+"=?", new String[]{cid}, null, null, null);
		
		String a = c.getString(CPARENT_IDS_COLUMN);
		String[] b = strc.convertStringToArray(a);
		int[] pids = new int[b.length];
		for(int i = 0; i < pids.length; i++){
			pids[i] = Integer.parseInt(b[i]);
		}
		return pids;
	}
	
	public int[] GetNumbersFromChild(String cid){
		SQLiteDatabase db = child.getWritableDatabase();
		
		Cursor c = db.query(CHILD_TABLE, new String[]{ID}, ID+"=?", new String[]{cid}, null, null, null);
		
		String a = c.getString(CNUMBERS_COLUMN);
		String[] b = strc.convertStringToArray(a);
		int[] numbers = new int[b.length];
		for(int i = 0; i < numbers.length; i++){
			numbers[i] = Integer.parseInt(b[i]);
		}
		return numbers;
	}
}
