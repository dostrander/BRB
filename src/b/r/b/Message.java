package b.r.b;

// Java Imports
import static b.r.b.Constants.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
// Android Imports
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;


/*	Message
 * 		Holds the text of the general message if it is a parent message 
 * 			or if it is a child it sends out the contact specific message
 * 		Holds the database id
 * 		Handles how to send text messages back, adding contact specific
 * 			messages, adding to databases, and changing the text
 */
public class Message{
	// Convenience 
	private final String TAG = "Message";												// For logs
	private static final String CLICK_TO_EDIT = "Click to Edit Text";					// Header default text
	private Context context;															// context of homescreen
	
	// Variables
	private int DB_ID;																	// id in database
	ChildMessage header;																// controls the header in MessageActivity
	public ArrayList<ChildMessage> cMessages;											// list of contact specific messages
	public String 				text;													// Text of the message to be sent
	// start time
	private Calendar 			startTime;												// NOT IMPLEMENTED BUT WOULD CAUSE ERRORS IF TAKEN OUT
	private Calendar			endTime;												// end time of the message

	
	/* 	ChildMessage
	 * 		Basically the contact specific messages, but that was too long of
	 * 		a name. 
	 * 		Contains text - to send to the appropriate contacts
	 * 		numbers - hashmap of numbers and there ids. Needs to check hashmap
	 * 			for number if its in there then send the text if not send the norm
	 * 		namesText - convenience for the view, just contains the names of the
	 * 			contacts, if they are in the your contacts list, if not just
	 * 			contains the number
	 */
	public class ChildMessage{ 
		//private final String TAG = "ChildMessage";				// for logs
		String text;											// text to send
		HashMap<String,Integer> numbers;						// numbers and db_ids
		String namesText;										// String to set in view
			
		// Constructors
			// For Header, initializes the numbers hashmap and sets the
			// strings that will be shown in the view to the header defaults
		ChildMessage(){
			text = CLICK_TO_EDIT;								// Set to Header default
			namesText = CLICK_TO_ADD_NAMES;						// Set to Header default
			numbers = new HashMap<String,Integer>();			// init numbers to none
		}
			// For NonHeader, initializes everything and sets the numbers
			// for already made ones
		ChildMessage(String t, int ids[], String nums[],Context ctx){				
			text = t;											// Set text for child
			numbers = new HashMap<String,Integer>();			// init numbers hashmap
			addNumbers(ids,nums,ctx);								
		}
		ChildMessage(String t, int id, String num,Context ctx){
			text = t;
			numbers = new HashMap<String,Integer>();
			numbers.put(num, id);
			numbersToString(ctx);
		}
		// Boolean Instructions
		public boolean containsNumber(String n){	// to see if the cMessage selected 
			return numbers.containsKey(n);}							// contains the number
		// Getters

		// Setters
		//	addNumbers
		//		 adds numbers to the child message
		public void addNumbers(int ids[], String nums[], Context c){
			for(int n =0; n < ids.length; n++)					// for all of the numbers
				numbers.put(nums[n],ids[n]);					// put the numbers in
			numbersToString(c);									// change the namesText
		}
		// Convenience 
		//	numbersToString
		//		changes the numbers to get the name from contacts api
		public void numbersToString(Context ctx){
			boolean first = true;								// if the first dont put a comma
			namesText = "";										// start of namestext
			for(String n : numbers.keySet()){					// for all the numbers
				String name = numberToString(n,ctx);			// get the contact
				if(name == null)								// if its null
					name = n;									// name is number
				if(!first)										// if it isnt the first
					namesText = namesText + "," + name;			// put a comma in between
				else{											// if it is the fist
					namesText = name;							// put it with no comma
					first = false;								// and say that its not the first
				}
			}
		}
		// editText
		//		edits childMessage text in both the DB and the datastructure
		public void editText(String t, Context ctx){
			ChildInteraction cDb = new ChildInteraction(ctx);	// For Writing to the database
			text = t;											// set the text
			for(String key : numbers.keySet())					// for all the numbers
				cDb.ChildEditMessage(numbers.get(key), text);	// edit the text in the db
			cDb.Cleanup();										// clean up the cursor
		}
		//	delete
		//		goes through the numbers and deletes every entry in numbers
		public void delete(Context ctx){
			ChildInteraction cDb = new ChildInteraction(ctx);	// for writing to the db
			for(String key : numbers.keySet())					// for all the numbers
				cDb.DeleteChild(key, text);						// delete the entry in the db
			cMessages.remove(this);								// remove this from cMessages
			cDb.Cleanup();										// cleanup
		}
		/*	numberToString
		 * 		takes the number in of a contact and returns the name of the contact
		 */
		private String numberToString(String num, Context ctx){
			Uri contactUri = Uri.withAppendedPath(				// URI for the ContactsContract
					ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(num));			
			Cursor cursor = ctx.getContentResolver().query(		// cursor for traversing
					contactUri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, 
					null, null, null);
			// if there is a cursor with soethign in it
			if(cursor.moveToFirst())
				if(!cursor.isAfterLast())
					return cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));	// retunr the name
			return null;
		}
	}// End of Child Class
	


		
	/*	Message Constructor
	 * 		sets the text, initialize contact specificMessages,
	 * 		tells whether or not it is a child and gets database id
	 */
	public Message(String t,int db_id){
		Log.d(TAG,"new Message Constructor");
	 	startTime	= Calendar.getInstance();
	 	endTime 	= Calendar.getInstance(); 
	 	header = new ChildMessage();
		text 				= new String(t);											// Set text
		cMessages			= new ArrayList<ChildMessage>();
		DB_ID = db_id;
	}
	public Message(String t, int dbid, Cursor c,Context ctx){
		Log.d(TAG,"old Message Contructor");
		context = ctx;
	 	startTime	= Calendar.getInstance();
	 	endTime 	= Calendar.getInstance();
	 	header = new ChildMessage();
		text 				= new String(t);											// Set text
		cMessages			= new ArrayList<ChildMessage>();
		DB_ID = dbid;
		
		String tt;
		String num; 
		int id;
		ChildMessage cmm;
		
		Log.d(TAG, "cursor length: " + String.valueOf(c.getCount()));
		if(c.moveToFirst())
			do{
				Log.d(TAG,"here");
				tt 	= c.getString(c.getColumnIndex(MESSAGE));
				num = c.getString(c.getColumnIndex(NUMBER));
				id 	= (int) c.getLong(c.getColumnIndex(ID));
				cmm = getChild(tt);
				if(cmm != null){
					getChild(tt).numbers.put(num,id);
					Log.d(TAG,"containts message");
				}else cMessages.add(new ChildMessage(tt,id,num,ctx));
				c.moveToNext();
			}while(!c.isAfterLast());
		for(ChildMessage cm: cMessages)
			cm.numbersToString(ctx);
		c.close();
	}
	
	// setters
	public boolean childContainsMessage(String t){
		if(cMessages.size() > 0)
			for(ChildMessage n : cMessages)
				if(n.text == t)
					return true;
		return false;
	}
	public int getID(){return DB_ID;}
	public String getStringID(){return String.valueOf(DB_ID);}
	public void setText(String t){text = t;}
	public void setStartDate(int m, int d, int y, int h, int mi){
		startTime.set(y,m,d,h,mi);
		checkDates();
	}
	public void setEndDate(int m, int d, int y, int h, int mi){
		endTime.set(y,m,d,h,mi);
		SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    	SharedPreferences.Editor editor = prefs.edit();
    	
    	editor.putInt("END_TIME_YEAR", y);
    	editor.putInt("END_TIME_MONTH", m);
    	editor.putInt("END_TIME_DAY", d);
    	editor.putInt("END_TIME_HOUR", h);
    	editor.putInt("END_TIME_MIN", mi);
    	editor.commit();
    	
		checkDates();
	}
	// getters
	public Calendar getEndTime(){
		return endTime;
	}
	public Calendar getStartTime(){
		return startTime;
	}
	public int getsYear(){ return startTime.get(Calendar.YEAR);}
	public int getfYear(){ return endTime.get(Calendar.YEAR);}
	public int getsMonth(){ return startTime.get(Calendar.MONTH);}
	public int getfMonth(){ return endTime.get(Calendar.MONTH);}
	public int getsDay(){ return startTime.get(Calendar.DAY_OF_MONTH);}
	public int getfDay(){ return endTime.get(Calendar.DAY_OF_MONTH);}
	public int getsHour(){ return startTime.get(Calendar.HOUR_OF_DAY);}
	public int getfHour(){ return endTime.get(Calendar.HOUR_OF_DAY);}
	public int getsMinute(){ return startTime.get(Calendar.MINUTE);}
	public int getfMinute(){ return endTime.get(Calendar.MINUTE);}
	public String getMessageText(){return text;}
	public String startDateToText(){
		DateFormat format = new SimpleDateFormat("MM/dd/yy hh:mm aa");
		return format.format(startTime.getTime()).toString();
	}
	public String endDateToText(){
		DateFormat format = new SimpleDateFormat("MM/dd/yy hh:mm aa");
		return format.format(endTime.getTime()).toString();
	}
	
	private void checkDates(){
		if(startTime.after(endTime)){
			Log.d(TAG,"after");
			if(endTime.get(Calendar.HOUR_OF_DAY) < startTime.get(Calendar.HOUR_OF_DAY) ||	// if the hour is larger
					(endTime.get(Calendar.HOUR_OF_DAY) == startTime.get(Calendar.HOUR_OF_DAY) && // or the hour is the same
					 endTime.get(Calendar.MINUTE) < startTime.get(Calendar.MINUTE)))			// and minute is larger
				endTime.set(startTime.get(Calendar.YEAR),										// set all categories 
					startTime.get(Calendar.MONTH), 
					startTime.get(Calendar.DAY_OF_MONTH),
					startTime.get(Calendar.HOUR),
					startTime.get(Calendar.MINUTE));
			else endTime.set(startTime.get(Calendar.YEAR),
					startTime.get(Calendar.MONTH), 
					startTime.get(Calendar.DAY_OF_MONTH));
		}
	}
	

	
	// MESSAGE FUNCTIONS
	
	// Getters
		// For Header
	public ChildMessage getHeader(){return header;}						// get the header variable
	public String getHeaderNames(){return header.namesText;}			// get header nameText
	public String getHeaderText(){return header.text;}					// get header text
	public int headerNumbersSize(){return header.numbers.size();}		// get size of contact in header
	
		// For nonHeaders 
	public ChildMessage getChild(String t){								// get the specific header
		for(ChildMessage c : cMessages){								// for all cMessages
			if(c.text.equals(t))										// if the texts equals t
				return c;												// return it
		}
		return null;													// else return null
	}

	// Setters
		// For Headers
	/*	addheaderToChild
	 * 		adds the header ChildMessage to the database then adds
	 * 		it to the list of ChildMessages
	 */
	public void addHeaderToChild(Context ctx){
		ChildInteraction cDb = new ChildInteraction(ctx);
		for(String key : header.numbers.keySet())
			if(header.numbers.get(key) < 0)
				header.numbers.put(key, (int)cDb.InsertMessage(key, header.text, DB_ID));
		header.numbersToString(ctx);
		cDb.Cleanup();
		cMessages.add(header);
		clearHeader();
	}
	// put the header back to no data in it
	private void clearHeader(){
		header = new ChildMessage();
	}
	public void setHeaderText(String t){header.text = t;}
	// add a contact to the header then change the namesText
	public void addHeaderContacts(Context ctx,String[] nums){
		header.numbers.clear();
		for(String n : nums)
			addContactHeader(n,ctx);
		header.numbersToString(ctx);
	}
	// helper function for adding contacts to the header
	private void addContactHeader(String n,Context ctx){
		header.numbers.put(n, -1);
		header.numbersToString(ctx);
	}

	
	// Boolean Operations
		// Header
	public boolean checkHeaderForDupNumbers(){
		for(String num : header.numbers.keySet())
			if(isDuplicateNumber(num))
				return true;
		return false;
	}	
		// nonHeader
	// if there is a duplicate number
	public boolean isDuplicateNumber(String t){
		for(ChildMessage c : cMessages)
			if(c.containsNumber(t))
				return true;
		return false;
	}
	// if a header contains a certain fil
	public boolean headerContainsNumber(String num){
		return header.containsNumber(num);}
	
	// get the text for a certain contact (for sending)
	public String getContactText(String num){
		for(ChildMessage c : cMessages)
			for(String k : c.numbers.keySet())
				if(trimNumber(num).equals(trimNumber(k))){
					Log.d(TAG,"number : "+ num);
					Log.d(TAG,"text: " + c.text);
					return c.text;
				} else{ Log.d(TAG,"number: "+ num); Log.d(TAG,"k " + k);}
		return null;
	}
	
	
	// trims number of hyphens and beginning 1's 
	private String trimNumber(String num){
		String incomingNumber = num;
		incomingNumber = incomingNumber.replaceAll("-", "" );
		if(incomingNumber.startsWith("1"))
			incomingNumber = incomingNumber.replaceFirst("1", "");
		Log.d(TAG,"trimnum: " + num );
		Log.d(TAG,"trimNumber: " + incomingNumber);
		return incomingNumber;
	}
	
	/*	sendSMS
	 * 		check whether or not there is a contact
	 * 		specific message to be sent, if not just send the text
	 * 		if there is tell that message to send the text
	 */
	public void sendSMS(String incomingNumber, Context context){
		String t = getContactText(incomingNumber);
		if(t != null){
			Log.d(TAG,"Contact Specific");
			send(incomingNumber,context,t);
		}
		else{																			// If not
			Log.d(TAG,"in sendSMS non Contact Specific");
			send(incomingNumber,context);
		}
	}
	private void send(String incomingNumber,Context context, String stext){
		Log.d(TAG,"in contact specific");
        Log.d(incomingNumber,stext);
        String 			SENT 				= "SMS_SENT";
        String 			DELIVERED 			= "SMS_DELIVERED";
        PendingIntent 	sentIntent			= PendingIntent.getBroadcast(context, 0,	// Set up sent Pending Intent
        										new Intent(SENT), 0);
        PendingIntent 	deliveryIntent	= PendingIntent.getBroadcast(context, 0,		// Set up delivery Pending Intent
        										new Intent(DELIVERED), 0);
        SmsManager		smsManager			= SmsManager.getDefault();					// Get SmsManager
        
		  try {
	        smsManager.sendTextMessage(														// Send the text
        			incomingNumber, null, stext, sentIntent, deliveryIntent);
			Toast.makeText(context, "SMS Sent!",
						Toast.LENGTH_LONG).show();
		  } catch (Exception e) {
			Toast.makeText(context,
				"SMS faild, please try again later!",
				Toast.LENGTH_LONG).show();
			e.printStackTrace();
		  }
	}
	private void send(String incomingNumber,Context context){
        String 			SENT 				= "SMS_SENT";
        String 			DELIVERED 			= "SMS_DELIVERED";
        PendingIntent 	sentIntent			= PendingIntent.getBroadcast(context, 0,	// Set up sent Pending Intent
        										new Intent(SENT), 0);
        PendingIntent 	deliveryIntent	= PendingIntent.getBroadcast(context, 0,		// Set up delivery Pending Intent
        										new Intent(DELIVERED), 0);
        SmsManager		smsManager			= SmsManager.getDefault();					// Get SmsManager
        try {
        	smsManager.sendTextMessage(														// Send the text
        			incomingNumber, null, text, sentIntent, deliveryIntent);
        	Toast.makeText(context, "SMS Sent!",
        			Toast.LENGTH_LONG).show();
        } catch (Exception e) {
        	Toast.makeText(context,
				"SMS faild, please try again later!",
				Toast.LENGTH_LONG).show();
        	e.printStackTrace();
        }
		  
		  
		  ////
		  

	}
}