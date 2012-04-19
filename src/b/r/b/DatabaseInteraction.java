//BRB Derek Ostrander, Stu Lang, Will Stahl, Evan Dodge, Jason Mather
//This class allows us to store and retrieve data to and from the database in an
//easier manner
package b.r.b;

import android.app.ListActivity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import static b.r.b.Constants.*;

public class DatabaseInteraction extends ListActivity {
	//Constants used to access a column using the cursor getString() method later
	private final int ID_COLUMN = 1;
	private final int MESSAGE_COLUMN = 2;
	private final int CHILD_IDS_COLUMN = 3;
	//initializing the local objects
	private parentDB parent = new parentDB(this);
	private StringArrayConverter strc = new StringArrayConverter();
	
	//to insert a message, you pass the string of the message, and an
	//array of strings that contain all the numbers it is used for
	//If it is a universal message I think we should just use null
	
	public void InsertMessage(String message, String[] numbers){
		SQLiteDatabase db = parent.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(MESSAGE, message);
		values.put(CHILD_IDS, strc.convertArrayToString(numbers));
		//id is set to auto-increment so I don't pass it an id 
		//(check out parentDB.java)
		db.insertOrThrow(PARENT_TABLE, null, values);
	}
	
	//to SearchByMessage just pass the String of the message
	public Cursor SearchByMessage(String message){
		SQLiteDatabase db = parent.getReadableDatabase();
		
		return db.query(PARENT_TABLE, new String[] {ID,MESSAGE,CHILD_IDS}, MESSAGE+"=?"
				, new String[]{message}, null, null, null);
		
	}
	
	//To search by ID (not sure why you would) just pass the id as a string
	public Cursor SearchById(String id){
		SQLiteDatabase db = parent.getReadableDatabase();
		
		return db.query(PARENT_TABLE, new String[] {ID,MESSAGE,CHILD_IDS},ID+"=?"
				, new String[]{id}, null, null, null);
	}
	
	//To search by childID just pass the number
	//remember, c may be null so make sure you try catch when you call
	//also I'm not sure if this will return all the messages with the sent child ID
	//or just the last one
	public Cursor SearchByChildId(String number){
		SQLiteDatabase db = parent.getReadableDatabase();
		
		Cursor c = db.rawQuery(PARENT_TABLE, null);
		String s = c.getString(CHILD_IDS_COLUMN);
		String[] a = strc.convertStringToArray(s);
		String[] ids = new String[a.length];
		
		for(int i = 0; i < a.length; i ++){
			if(a[i].equals(number)){
				ids[i] = c.getString(ID_COLUMN);
				c = db.query(PARENT_TABLE, new String[] {ID,MESSAGE,CHILD_IDS},ID+"=?"
						, new String[]{ids[i]}, null,null,null);
			}
		}
		
		return c;
	}
	
}
