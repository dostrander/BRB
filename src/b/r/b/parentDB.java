/* BRB
 * This class written by Jason Mather 2/25/2012
 * 
 * Project by: Evan Dodge, Stuart Lang, Jason Mather, Derek Ostrander, Max Schwenk, and Will Stahl 
 * 
 */
package b.r.b;

import static android.provider.BaseColumns._ID;
import android.content.Context;
import static b.r.b.Constants.*;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
public class parentDB extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "parent.db";
	private static int DATABASE_VERSION = 2;
	//This is the SQL statement to create the table for later
	private static final String DATABASE_CREATE_TABLE =
			   " CREATE TABLE " + PARENT_TABLE +
			   " ("+_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + MESSAGE +" TEXT NOT NULL);";
	/*
	private static final String DATABASE_CREATE_TABLE_2 =
			   " CREATE TABLE " + CHILD_TABLE +
			   " ("+_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT, " +ID + " INTEGER NOT NULL, "+ MESSAGE +" TEXT NOT NULL);";
	*/
	
	public parentDB(Context ctx) {
	     super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
	  }


	@Override
	public void onCreate(SQLiteDatabase db) {
		//create the table outlined above
		db.execSQL(DATABASE_CREATE_TABLE);
		
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		int currentVersion = oldVersion;
		while(currentVersion < newVersion){
			currentVersion++;
			DATABASE_VERSION++;
			db.execSQL("DROP TABLE IF EXISTS " +CHILD_TABLE);
		    onCreate(db);
		}
	}

	
	
}
