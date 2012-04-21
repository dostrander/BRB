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
	
	//to insert a message, you pass the string of the message, and an
	//array of strings that contain all the numbers it is used for
	
	
	//For inserting into the parent, pass an id, the message, and an array of possible child 
	//ids
	public void InsertMessage(String id, String message, String[] cids){
		SQLiteDatabase db = parent.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(_ID, id);
		values.put(MESSAGE, message);
		values.put(CHILD_IDS, strc.convertArrayToString(cids));
		
		db.insertOrThrow(PARENT_TABLE, id, values);
	}
	
	
	//For inserting into the child, pass the id, an arry of the numbers, the message, and 
	//an array of the parent ids
	public void InsertMessage(String id, String[] numbers, String message, int[] pids){
		String p = "";
		for(int i = 0; i < pids.length; i ++){
			p = p + pids[i];
			if(i != (pids.length - 1)){
				p = p + ",";
			}
			
		}
		SQLiteDatabase db = child.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(_ID, id);
		values.put(NUMBERS, strc.convertArrayToString(numbers));
		values.put(MESSAGE, message);
		values.put(PARENT_IDS, p);
		db.insertOrThrow(CHILD_TABLE, id, values);
		
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
		
		//To search by ID (not sure why you would) just pass the id as a string
		public Cursor SearchParentById(String id){
			SQLiteDatabase db = parent.getReadableDatabase();
			
			return db.query(PARENT_TABLE, new String[] {ID,MESSAGE,CHILD_IDS},ID+"=?"
					, new String[]{id}, null, null, null);
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
}
