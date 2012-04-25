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
public class logDB extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "log.db";
	private static final int DATABASE_VERSION = 2;
	private static final String DATABASE_CREATE_TABLE =
			   " CREATE TABLE " + LOG_TABLE +
			   " ("+_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + PARENT_ID 
			   + " INTEGER NOT NULL, " + TIME + " TEXT NOT NULL, " + DATE 
			   + " TEXT NOT NULL, " + AMPM + " INTEGER NOT NULL, " + TYPE 
			   + " INTEGER NOT NULL, " + RECEIVED_MESSAGE + " TEXT NOT NULL, " 
			   + NUMBER + " TEXT NOT NULL);";
	/*
	private static final String DATABASE_CREATE_TABLE_2 =
			   " CREATE TABLE " + CHILD_TABLE +
			   " ("+_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT, " +ID + " INTEGER NOT NULL, "+ MESSAGE +" TEXT NOT NULL);";
	*/
	public logDB(Context ctx) {
	     super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
	  }


	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(DATABASE_CREATE_TABLE);
		//db.execSQL(DATABASE_CREATE_TABLE_2);
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
		db.execSQL("DROP TABLE IF EXISTS " +LOG_TABLE);
	     onCreate(db);
	}

	
	
}

