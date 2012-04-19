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
	private final int ID_COLUMN = 1;
	private final int MESSAGE_COLUMN = 2;
	private final int CHILD_IDS_COLUMN = 3;
	
	private parentDB parent = new parentDB(this);
	private StringArrayConverter strc = new StringArrayConverter();
	
	public void InsertMessage(String message, String[] numbers){
		SQLiteDatabase db = parent.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(MESSAGE, message);
		values.put(CHILD_IDS, strc.convertArrayToString(numbers));
		db.insertOrThrow(PARENT_TABLE, null, values);
	}
	
	public Cursor SearchByMessage(String message){
		SQLiteDatabase db = parent.getReadableDatabase();
		
		return db.query(PARENT_TABLE, new String[] {ID,MESSAGE,CHILD_IDS}, MESSAGE+"=?"
				, new String[]{message}, null, null, null);
		
	}
	
	public Cursor SearchById(String id){
		SQLiteDatabase db = parent.getReadableDatabase();
		
		return db.query(PARENT_TABLE, new String[] {ID,MESSAGE,CHILD_IDS},ID+"=?"
				, new String[]{id}, null, null, null);
	}
	
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
