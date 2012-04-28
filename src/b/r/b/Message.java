package b.r.b;

// Java Imports
import static b.r.b.Constants.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
// Android Imports
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
	private final String TAG = "Message";
	private static final String CLICK_TO_EDIT = "Click to Edit Text";
	
	// Variables
	private int DB_ID;
	
	
	
	
	
	private ChildMessage header;
	ArrayList<ChildMessage> cMessages;
	public String 				text;													// Text of the message to be sent
	private boolean 			no_end;													// Tells whether there is an end or not
	// start time
	private Calendar 			startTime;
	private Calendar			endTime;
	// finish time
	
	
	public class ChildMessage{
		String text;
		HashMap<String,Integer> numbers;
//		ArrayList<Integer> ids;
//		ArrayList<String> numbers;
		String namesText;
		ChildMessage(){
			text = CLICK_TO_EDIT;
			namesText = CLICK_TO_ADD_NAMES;
//			ids = new ArrayList<Integer>();
//			numbers = new ArrayList<String>();
			numbers = new HashMap<String,Integer>();
		}
		ChildMessage(String t, int i, String num){
			text = t;
//			ids = new ArrayList<Integer>();
//			numbers = new ArrayList<String>();
			numbers = new HashMap<String,Integer>();
			addNumbers(i,num);
			
		}
		public boolean containsNumber(String n){
			return numbers.containsKey(n);
//			for(String num : numbers.keySet())
//				if(n.equals(num))
//					return true;
//			return false;
		}
		public void addNumbers(int i, String num){
//			ids.add(i);
//			numbers.add(num);
			numbers.put(num, i);
		}
		public void numbersToString(Context ctx){
			boolean first = true;
			namesText = "";
			for(String n : numbers.keySet()){
				String name = numberToString(n,ctx);
				if(!first)
					namesText = namesText + "," + name;
				else{
					namesText = name;
					first = false;
				}
			}
		}

		private String numberToString(String num, Context ctx){
			Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(num));
			Cursor cursor = ctx.getContentResolver().query(contactUri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, 
					null, null, null);
			if(cursor.moveToFirst())
				if(!cursor.isAfterLast())
					return cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
			return null;
		}
	}
	
	public boolean headerContainsNumber(String num){
		return header.containsNumber(num);
	}
	public ChildMessage getHeader(){
		return header;
	}
	public String getHeaderNames(){
		return header.namesText;
	}
	public String getHeaderText(){
		return header.text;
	}
	public void addNewChildMessage(Context ctx){
		
		DatabaseInteraction db = ((MessageActivity)ctx).getDatabase();
		for(String key : header.numbers.keySet())
			if(header.numbers.get(key) < 0)
				db.InsertMessage(key, header.text, DB_ID);
		cMessages.add(header);
		clearHeader();
	}
	private void clearHeader(){
		header = new ChildMessage();
//		header.ids.clear();
//		header.numbers.clear();
//		header.text = CLICK_TO_EDIT;
//		header.namesText = CLICK_TO_ADD_NAMES;
	}
	public int headerNumbersSize(){return header.numbers.size();}
	public void setHeaderText(String t){header.text = t;}
	public void addHeaderContacts(Context ctx,String[] nums){
		for(String n : nums)
			addContactHeader(n);
		header.numbersToString(ctx);
	}
	private void addContactHeader(String n){
		header.addNumbers(-1, n);
	}

	public boolean checkHeaderForDupNumbers(){
		for(String num : header.numbers.keySet())
			if(isDuplicateNumber(num))
				return true;
		return false;
	}
	public boolean isDuplicateNumber(String t){
		for(ChildMessage c : cMessages)
			if(c.containsNumber(t))
				return true;
		return false;
	}
	private void addChildMessages(Cursor c){
		String text;
		int id;
		String num;

		//if(c.moveToFirst())
			//do{
				
		
	}
	public ChildMessage getChild(String t){
		for(ChildMessage c : cMessages)
			if(c.text.equals(t))
				return c;
		return header;
	}

		
	/*	Message Constructor
	 * 		sets the text, initialize contact specificMessages,
	 * 		tells whether or not it is a child and gets database id
	 */
	public Message(String t,int db_id){
	 	Log.d(TAG,"in Message Constructor");
	 	startTime	= Calendar.getInstance();
	 	endTime 	= Calendar.getInstance(); 
	 	header = new ChildMessage();
		text 				= new String(t);											// Set text
		cMessages			= new ArrayList<ChildMessage>();
		DB_ID = db_id;
	}
	public Message(String t, int[] cids, int dbid){
	 	Log.d(TAG,"in Message Constructor");
	 	startTime	= Calendar.getInstance();
	 	endTime 	= Calendar.getInstance(); 
		text 				= new String(t);											// Set text
		cMessages			= new ArrayList<ChildMessage>();
					
		getChildrens(cids);// get chidren from the db
		// the number of the contact
		DB_ID = dbid;
	}
	
	// setters
	private void getChildrens(int[] cids){
		
	}
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
		Log.d(TAG,"start" +String.valueOf(startTime.get(Calendar.YEAR)) + " " + 
				String.valueOf(startTime.get(Calendar.MONTH)) + " " +
				String.valueOf(startTime.get(Calendar.DAY_OF_MONTH)) + " " +
				String.valueOf(startTime.get(Calendar.HOUR)) + " " +
				String.valueOf(startTime.get(Calendar.MINUTE)));
		Log.d(TAG,"end" +String.valueOf(endTime.get(Calendar.YEAR)) + " " + 
				String.valueOf(endTime.get(Calendar.MONTH)) + " " +
				String.valueOf(endTime.get(Calendar.DAY_OF_MONTH)) + " " +
				String.valueOf(endTime.get(Calendar.HOUR)) + " " +
				String.valueOf(endTime.get(Calendar.MINUTE)));
	}
	
	
	/*	addContactSpecificMessage
	 * 		Adds a child Message to specificMessages
	 */
	public void addContactSpecificMessage(String number, String t){
		Log.d(TAG,"in addContactSpecificMessage");
		// Add to database
		int db_id = 0;
		//specificNumbers.put(number, specificMessages.size());
		//specificMessages.add(t);
																							// make it a child message
	}
	
	/*	sendSMS
	 * 		check whether or not there is a contact
	 * 		specific message to be sent, if not just send the text
	 * 		if there is tell that message to send the text
	 */
	public void sendSMS(String incomingNumber, Context context){
		Log.d(TAG,"in sendText");
		//if(specificNumbers.containsKey(incomingNumber));								// If there is a key that matches
		if(true);
//			specificMessages.get(incomingNumber).sendSMS(incomingNumber,context);		// Tell that message to send it
		
		else{																			// If not
			SmsManager smsManager = SmsManager.getDefault();							// Get the SmsManager
			
			// if we want to track whether or not it was sent we need to change this 
			//smsManager.sendTextMessage(incomingNumber, 									// And send the text message
										//null, incomingNumber, null, null);
			send(incomingNumber,context);
			
			
		}
	}
	
	private void send(String incomingNumber,Context context){
        String 			SENT 				= "SENT_INTENT";							// Name of the sent intent
        String 			DELIVERED 			= "DELIVERED_INTENT";						// Name of the delivered intent
        PendingIntent 	sentIntent			= PendingIntent.getBroadcast(context, 0,	// Set up sent Pending Intent
        										new Intent(SENT), 0);
        PendingIntent 	deliveryIntent	= PendingIntent.getBroadcast(context, 0,		// Set up delivery Pending Intent
        										new Intent(DELIVERED), 0);
        SmsManager		smsManager			= SmsManager.getDefault();					// Get SmsManager
        
        //---When the SMS has been send---
        context.registerReceiver(new BroadcastReceiver(){								// Register Receiver from main context
        	@Override
            public void onReceive(Context c, Intent i) {								// When the sent signal is received
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:											// If result was sent
                        Log.d(TAG,"sent");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:						// If result failed to send
                        Log.d(TAG,"Generic Failure");
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:							// If there is no service
                        Log.d(TAG,"No Service");
                        break;	
                    case SmsManager.RESULT_ERROR_NULL_PDU:								// If there is a null pdu
                    	Log.d(TAG,"Null PDU");
                    	break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:								// If radio is off
                    	Log.d(TAG,"Radio off?");
                        break;
                }
            }
        }, new IntentFilter(SENT));														// Name the intent SENT
 
        //---when the SMS has been delivered---
        context.registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context c, Intent i) {								// When delivered signal received
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:											// If result was delivered
                    	Log.d(TAG,"SMS Delivered");
                        break;
                    case Activity.RESULT_CANCELED:										// If result was not delivered
                    	Log.d(TAG,"SMS Canceled");
                        break;                        
                }
            }
        }, new IntentFilter(DELIVERED));												// Name the intent DELIVERED
        
        smsManager.sendTextMessage(														// Send the text
        			incomingNumber, null, text, sentIntent, deliveryIntent);
	}
}