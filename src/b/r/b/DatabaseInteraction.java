//BRB Derek Ostrander, Stu Lang, Will Stahl, Evan Dodge, Jason Mather
//This class allows us to store and retrieve data to and from the database in an
//easier manner
package b.r.b;

import android.app.ListActivity;
import android.database.sqlite.SQLiteDatabase;


public class DatabaseInteraction extends ListActivity {
	
	private parentDB parent = new parentDB(this);
	
	public void InsertMessage(String[] message, String[] numbers){
		SQLiteDatabase db = parent.getWritableDatabase();
	}
	
}
