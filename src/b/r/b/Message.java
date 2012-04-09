package b.r.b;

// Java Imports
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
	
	// Variables
	private Map<String,Integer>	specificNumbers;										// For contact specific Messages
	ArrayList<String>	specificMessages;
	public String 				text;													// Text of the message to be sent
	private boolean 			no_end;													// Tells whether there is an end or not
	public final int 			DB_ID;													// Database ID
	// start time
	private Calendar 			startTime;
	private Calendar			endTime;
	// finish time
		
	/*	Message Constructor
	 * 		sets the text, initialize contact specificMessages,
	 * 		tells whether or not it is a child and gets database id
	 */
	public Message(String t){
	 	Log.d(TAG,"in Message Constructor");
	 	startTime	= Calendar.getInstance();
	 	endTime 	= Calendar.getInstance(); 
		text 				= new String(t);											// Set text
		specificNumbers 	= new HashMap<String,Integer>();							// Initialize specific messages, key is 
																							// the number of the contact
		specificMessages	= new ArrayList<String>();
		specificMessages.add("");
		DB_ID 				= 0; 														// Query Database for ID
	}

	
	// setters
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
	

	
	
	/*	saveToDatabase
	 * 		check whether or not it is in the appropriate 
	 * 		database (child or parent) and if it is there
	 * 		update the entry if it is not just save the whole message
	 */
	public void saveToDatabase(){
		Log.d(TAG,"in saveToDatabase");
//		if (child == true)
//			// Check to see if there is an entry 
//			// in the child database
//			;
//		else
//			// Check to see if there is an entry
//			// in the parent database
//			;
	}
	
	/*	addContactSpecificMessage
	 * 		Adds a child Message to specificMessages
	 */
	public void addContactSpecificMessage(String number, String t){
		Log.d(TAG,"in addContactSpecificMessage");
		// Add to database
		int db_id = 0;
		specificNumbers.put(number, specificMessages.size());
		specificMessages.add(t);
																							// make it a child message
	}
	
	/*	sendSMS
	 * 		check whether or not there is a contact
	 * 		specific message to be sent, if not just send the text
	 * 		if there is tell that message to send the text
	 */
	public void sendSMS(String incomingNumber, Context context){
		Log.d(TAG,"in sendText");
		if(specificNumbers.containsKey(incomingNumber));								// If there is a key that matches
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