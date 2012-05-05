/* BRB
 * This class written by Jason Mather 2/25/2012
 * 
 * Project by: Evan Dodge, Stuart Lang, Jason Mather, Derek Ostrander, Max Schwenk, and Will Stahl 
 * 
 */
package b.r.b;
//imports
import static android.provider.BaseColumns._ID;
import android.content.Context;
import static b.r.b.Constants.*;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
public class childDB extends SQLiteOpenHelper {
	//SQL statement we run when the Database is created
	private static final String DATABASE_NAME = "child.db";
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_CREATE_TABLE =
			   " CREATE TABLE " + CHILD_TABLE +
			   " ("+_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + NUMBER +" TEXT NOT NULL, " 
			   + MESSAGE + " TEXT NOT NULL, " + PARENT_ID + " INTEGER NOT NULL);";
	/*
	private static final String DATABASE_CREATE_TABLE_2 =
			   " CREATE TABLE " + CHILD_TABLE +
			   " ("+_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT, " +ID + " INTEGER NOT NULL, "+ MESSAGE +" TEXT NOT NULL);";
	*/
	public childDB(Context ctx) {
	     super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
	  }


	@Override
	public void onCreate(SQLiteDatabase db) {
		//This runs the create table SQL statement onCreate
		db.execSQL(DATABASE_CREATE_TABLE);
		
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		//This runs an SQL statement which drops any existing child table if we are
		//making a new one, and replaces it with the new one
		db.execSQL("DROP TABLE IF EXISTS " +CHILD_TABLE);
	     onCreate(db);
	}

	
	
}
