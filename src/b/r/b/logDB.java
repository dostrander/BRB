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
	private static int DATABASE_VERSION = 3;
	//SQL statement we will run onCreate
	private static final String DATABASE_CREATE_TABLE =
			   " CREATE TABLE " + LOG_TABLE +
			   " ("+_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + PARENT_ID 
			   + " INTEGER NOT NULL, " + TIME + " TEXT NOT NULL, " + DATE 
			   + " TEXT NOT NULL, " + AMPM + " INTEGER NOT NULL, " + TYPE 
			   + " INTEGER NOT NULL, " + RECEIVED_MESSAGE + " TEXT NOT NULL, " 
			   + SENT_MESSAGE + " TEXT NOT NULL, " +NUMBER + " TEXT NOT NULL);";
	
	public logDB(Context ctx) {
	     super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
	  }


	@Override
	public void onCreate(SQLiteDatabase db) {
		//create the log database
		db.execSQL(DATABASE_CREATE_TABLE);
		
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//onUpgrade, get rid of an old version of the database if it exists and then create the new one
		int currentVersion = oldVersion;
		while(currentVersion < newVersion){
			currentVersion++;
			DATABASE_VERSION++;
			db.execSQL("DROP TABLE IF EXISTS " +CHILD_TABLE);
		    onCreate(db);
		}
	}

	
	
}

