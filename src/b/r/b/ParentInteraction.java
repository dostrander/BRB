//BRB Written by: Jason Mather on 4/28/12

//project: Derek Ostrander, Stu Lang, Will Stahl, Evan Dodge, Jason Mather
//This class allows us to store and retrieve data to and from the database in an
//easier manner
package b.r.b;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import static b.r.b.Constants.*;



public class ParentInteraction extends Activity{
	private static final String TAG = "ParentInteraction";
	//Constants used to access a column using the cursor getString() method later
	private final int PID_COLUMN = 1;
	private final int PARENT_ID_COLUMN = 4;
	private final int PCHILD_ID_COLUMN = 3;
	private final int CNUMBERS_COLUMN = 2;
	//initializing the local objects
	private logDB log;
	private parentDB parent;
	private childDB child;
	private boolean editSuccess;
	private boolean deleteSuccess;
	//private StringArrayConverter strc = new StringArrayConverter();
	private Context context;
	
	public ParentInteraction(Context ctx){
		context = ctx;
		//we only need the parent since we have 3 different interaction classes; one for each dB
		parent = new parentDB(context);
		
		
	}
	
	
	
	
	//overloading Parent edit message so you can edit by sending the old message, or the id, if you have it
	//returns true means it worked, otherwise false
	public boolean ParentEditMessage(String oldMessage, String newMessage){
		//we need both a readable and a writable database for this function
		SQLiteDatabase dbr = parent.getReadableDatabase();
		SQLiteDatabase dbw = parent.getWritableDatabase();
		//get the rows which have a message which matches the one passed
		Cursor c = dbr.query(PARENT_TABLE, new String[] {ID,MESSAGE}, MESSAGE+"=?"
				, new String[]{oldMessage}, null, null, null);
		String pid = c.getString(PID_COLUMN); //grabbing the parent id locally
		c.close();//closing the cursor since we don't need it for anything else
		ContentValues values = new ContentValues(); //this is what we use to load the values
		values.put(ID, pid);//put the parent id we just grabbed
		values.put(MESSAGE,newMessage);//put the message that was passed 
		//did it work?
		editSuccess = dbw.update(PARENT_TABLE, values, null, null) > 0;//store the success of the update
		//cleanup
		dbr.close();
		dbw.close();
		return editSuccess;//return the success of the update
	}
	//second version of ParentEditMessage uses the id
	public boolean ParentEditMessage(int pid, String newMessage){
		Log.d("stuff",String.valueOf(pid));
		SQLiteDatabase db = parent.getWritableDatabase();//make a writable database
														 //don't need a readable database since we're editing by id
		String stringPid = "";//creating a variable to store the id as a string
		
		stringPid = String.valueOf(pid);//set the value of the string to the id we got
		
		//Don't need cursor, we have everything we need
		ContentValues values = new ContentValues();
		
		Log.d(TAG,"pid: " + stringPid);
		values.put(ID, stringPid);//put the pid in
		Log.d(TAG,"message " + newMessage);
		values.put(MESSAGE,newMessage);//put the new message we got
		//did it work?
		editSuccess = db.update(PARENT_TABLE, values, null, null) > 0;//store the success of the update call
		db.close();//close the dB
		
		return editSuccess;//return the success value of the update operation
	}
	
	
	//deleting a parent row by passing the message
	public boolean DeleteParent(String message){
		SQLiteDatabase db = parent.getWritableDatabase();//we only need to write
		deleteSuccess = db.delete(PARENT_TABLE, MESSAGE + "=" + message, null) > 0;//deletes any rows who's MESSAGE coloumn match the passed message
		db.close();//close the dB
		return deleteSuccess;//return the success value of the Delete operation
	}
	//to insert a message, you pass the string of the message
	public Message InsertMessage(String message){
		SQLiteDatabase db = parent.getWritableDatabase();//we only need a writable dB
		ContentValues values = new ContentValues();
		values.put(MESSAGE, message);//just put the message, id is auto incremented
		long id = db.insertOrThrow(PARENT_TABLE, null, values);//store the id of the insertOrThrow function, so if it works we will have the id
		db.close();
		//closes dB then returns the message and the id
		return new Message(message,(int) id);
	}
	
	
	
	//returns all the parent messages
	public Cursor GetAllParentMessages(){
		Log.d(TAG,"in GetAllParentMessages");
		SQLiteDatabase db = parent.getReadableDatabase();//just need a readable database
		//query arguments are null since we want them all
		return db.query(PARENT_TABLE, new String[]{ID,MESSAGE}, null, 
				null, null, null, null,null);//returns the cursor which contains all the rows
	}
	
	//to search by message just pass the message as a string
	public Cursor SearchParentByMessage(String message){
		Log.d(TAG,"in SearchParentByMessage");
		SQLiteDatabase db = parent.getReadableDatabase();//only need a readable for just returning message
		//returns the query directly
		return db.query(PARENT_TABLE, new String[] {ID,MESSAGE}, MESSAGE+"=?"
			, new String[]{message}, null, null, null);//return the cursor which has the row with the MESSAGE column which matches the one passes
	
	}
	
	//searches the child database for a parent and then grab its message 
		public Message GetParentByMessage(String message){
			Log.d(TAG,"in GetParentByMessage");
			ChildInteraction cDb = new ChildInteraction(context);//we need to search the child database
			Cursor c = SearchParentByMessage(message);//grab the cursor using the function right above
				
			if(c.moveToFirst()){//if its not empty then go to first one
				int id = c.getInt(c.getColumnIndex(ID));//grab the parent id
				Cursor cc = cDb.SearchChildByParentId(id);//search the child db by that parent id
				if(cc != null){//if it found a matching row
					Message m = new Message(c.getString(c.getColumnIndex(MESSAGE)),id,cc,context);//grab the message
					c.close();//cleanup 
					cDb.Cleanup();
					return m;//if we have a message, return it
				}
			}
			return null;//if it didn't exist, return null
		}
	
	//To search by ID (not sure why you would) just pass the id as an int
	public Cursor SearchParentById(int id){
		Log.d(TAG,"in SearchParentById");
		SQLiteDatabase db = parent.getReadableDatabase();//just need to read
		
		return db.query(PARENT_TABLE, new String[] {ID,MESSAGE},ID+"=?"
				, new String[]{String.valueOf(id)}, null, null, null);//return the cursor with any matching rows which had an ID which matched the one passed
	}
	//does the same as GetParentByMessage except with an id
	public Message GetParentById(int id){
		Log.d(TAG,"in GetParentById");
		ChildInteraction cDb = new ChildInteraction(context);//need the child db
		Cursor c = SearchParentById(id);//grab the cursor with the parents which match the id passed
		if(c.moveToFirst()){//if it has at least one entry
			Cursor cc = cDb.SearchChildByParentId(id);//grab a cursor with the child rows with the right parent id
			if(cc != null){//if at least one row exists
				//grab the message
				Message m = new Message(c.getString(c.getColumnIndex(MESSAGE)),id,cc,context);
				c.close();//cleanup
				cDb.Cleanup();
				return m;//return the message
			}
		}
		return null;//if the id didnt match, return null
	}
			
	
		
	
	//cleanup functions
	public void CleanupParent(){
		parent.close();
	}
	
	public void Cleanup(){
		if (parent != null) {
	        parent.close();
	    }
	    
	   	    
	}
	
	/*private void CleanupAll(){
		log.close();
		parent.close();
		child.close();
	}*/
	//make sure we close any open databases ondestroy
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    if (parent != null) {
	        parent.close();
	    }
	    if (child != null) {
	        child.close();
	    }
	    if (log != null){
	    	log.close();
	    }
	}
}

