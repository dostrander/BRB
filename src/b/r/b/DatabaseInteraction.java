//BRB Derek Ostrander, Stu Lang, Will Stahl, Evan Dodge, Jason Mather
//This class allows us to store and retrieve data to and from the database in an
//easier manner
package b.r.b;

import android.app.ListActivity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import static b.r.b.Constants.*;

public class DatabaseInteraction extends ListActivity {
	
	private parentDB parent = new parentDB(this);
	private StringArrayConverter strc = new StringArrayConverter();
	
	public void InsertMessage(String message, String[] numbers){
		SQLiteDatabase db = parent.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(MESSAGE, message);
		values.put(CHILD_IDS, strc.convertArrayToString(numbers));
		db.insertOrThrow(PARENT_TABLE, null, values);
	}
	
}
