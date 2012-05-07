/* * * * * * * * * * * * * * * * * * * * * * * 
 * BRB-Android
 * AlarmReceiver.java
 * 
 * Created: 2012
 * 
 * Evan Dodge, Derek Ostrander, Max Shwenk
 * Jason Mather, Stuart Lang, Will Stahl
 * 
 * * * * * * * * * * * * * * * * * * * * * * */

package b.r.b;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class IncomingListener extends BroadcastReceiver {
	private final String 		TAG 			= "IncomingListener";
	private final String 		INCOMING_TEXT 	= "android.provider.Telephony.SMS_RECEIVED";
	private final String		PHONE_STATE		= "android.intent.action.PHONE_STATE";
    private final int CALL = 0;
	private final int TEXT = 1;
	
	
	private TelephonyManager	telephonyManager;
	public 	Message 			currentMessage;		
	
	public static LogInteraction lDb;
	
	/*	onReceive
	 * 		Handles if there is an incoming text or phone state
	 * 		If it is an Incoming Text
	 * 			Unwrap the message
	 * 			Check the validity of the number to text back
	 * 				If valid send a text back
	 * 			Add it to the response log
	 * 
	 * 		Else if it is a Phone State
	 * 			Check the validity of the number to text back
	 * 				If valid send a text back
	 * 			Add it to the response log
	 * 			
	 */			

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG,"in onReceive");
		Log.d(TAG, intent.getAction().toString());
		
		lDb = new LogInteraction(context);
		
		// If its a text then handle a text
		if(intent.getAction().equals(INCOMING_TEXT))							// If it is an incoming text
			handleText(intent,context);		
		// If its a call handle a call
		else if(intent.getAction().equals(PHONE_STATE))							// If it is a call
			handleCall(context,intent);											// handle it
	}
	
	
	/*	handleText
	 * 
	 */
	private void handleText(Intent intent,Context context){
		Log.d(TAG,"in incomingText");
		
		if (!Settings.HandleTexts()) return;
		
		Bundle bundle = intent.getExtras();
		if (bundle != null)	{
			Object[] pdus 				= (Object[]) bundle.get("pdus");	// Get the incoming text/s			
			SmsMessage incomingMessage 	= null;								
			for (int i=0; i<pdus.length; i++){								// For how many texts are coming in
				incomingMessage 		= SmsMessage						// Get the SmsMessage
											.createFromPdu((byte[])pdus[i]);
				Log.d(TAG,"From: " 
						+ incomingMessage.getOriginatingAddress());
				Log.d(TAG,"says: " 
						+ incomingMessage.getMessageBody().toString());
				addToResponseLog(											// Add info to response log
						incomingMessage.getOriginatingAddress(),			// Incoming Number
						incomingMessage.getMessageBody().toString());		// Message
			
				if(isValidNumber(											// Check if Valid Number
						incomingMessage.getOriginatingAddress().toString()))
					sendBackText(											// Tell the Message to handle sending
						incomingMessage.getOriginatingAddress().toString(),		// a text back
						context);
			}	
		}
	}
	/*	sendBackText
	 * 		tell the currentMessage to handle sending the text
	 * 		out to the incomingNumber
	 */
	
	
	private void sendBackText(String incNum,Context context){
		Log.d(TAG,"in sendBackText");
		Message current = HomeScreenActivity.mCurrent;
		if(current != null)
			current.sendSMS(incNum, context);
	}
	
	
	/*	handleCall
	 * 
	 */
	
	private void handleCall(final Context context,Intent intent){
		
		if (!Settings.HandleCalls()) return;
		
		// ---Handle Phone Call---
		telephonyManager	= (TelephonyManager)context.getSystemService(		// Make Telephony manager
								Context.TELEPHONY_SERVICE);							// to listen for PhoneStates
		PhoneStateListener callListener = new PhoneStateListener(){				// Create new PhoneStateListener
			@Override
			public void onCallStateChanged(int state, String incomingNumber){	// Handle Incoming PhoneState 
				Log.d(TAG,"in onCallStateChanged");
				if(state == TelephonyManager.CALL_STATE_RINGING){				// If ringing
					Log.d( TAG,"in RINGING");
					Log.d(TAG, incomingNumber);
					// * Check if a valid number * 
					
					// * Check if more than 1 caller *
					// * End call or turn off volume or at least stop incoming call
					//		Screen from showing up*
					addToResponseLog(incomingNumber,null);						// Add a call entry to the 
																					// response log 
																					// (null indicates call)
					if(isValidNumber(incomingNumber))							// Check the validity of the number
						sendBackText(incomingNumber,context);					// Tell the Message to handle sending
																					// a text back 
				} else Log.d(TAG,String.valueOf(state));
			}
		};
		telephonyManager.listen(callListener,									// Start Listening
				PhoneStateListener.LISTEN_CALL_STATE);				
	}	// ---End Handling PhoneState---

		

	
	/*	addToResponseLog
	 * 		Send an entry to the response log
	 * 		(Probably going to change/get rid of)
	 */
	private void addToResponseLog(String incNum, String msg){
		
		/* * * * * * * * * * * * * * * * * * *
		 * Add a call to the response log
		 * * * * * * * * * * * * * * * * * * */
		if(msg == null){ // If there is no message (It's a call)
			Log.d(TAG,"adding call to response log");
			
			// Create a new calendar to use for the current time and date
			Calendar cal = Calendar.getInstance();
			
			if(cal.get(Calendar.MINUTE) < 10) { // This handles times where the minutes is single digits
				// Add the call to the log
				currentMessage = lDb.InsertLog(HomeScreenActivity.mCurrent.getID(), 
						String.valueOf(cal.get(Calendar.HOUR))+":0"+String.valueOf(cal.get(Calendar.MINUTE)),
						String.valueOf(cal.get(Calendar.MONTH))+"/"+String.valueOf(cal.get(Calendar.DAY_OF_MONTH)), 
						cal.get(Calendar.AM_PM), CALL, "", HomeScreenActivity.mCurrent.getMessageText(), incNum);
			}
			else {
				// Add the call to the log
				currentMessage = lDb.InsertLog(HomeScreenActivity.mCurrent.getID(), 
						String.valueOf(cal.get(Calendar.HOUR))+":"+String.valueOf(cal.get(Calendar.MINUTE)),
						String.valueOf(cal.get(Calendar.MONTH))+"/"+String.valueOf(cal.get(Calendar.DAY_OF_MONTH)), 
						cal.get(Calendar.AM_PM), CALL, "", HomeScreenActivity.mCurrent.getMessageText(), incNum);
			}
		}
		/* * * * * * * * * * * * * * * * * * *
		 * Add a text to the response log
		 * * * * * * * * * * * * * * * * * * */
		else{
			Log.d(TAG,"adding text to response log");
			
			// Create a new calendar to use for the current time and date
			Calendar cal = Calendar.getInstance();

			if(cal.get(Calendar.MINUTE) < 10) { // This handles times where the minutes is single digits
				// Add the text to the log
				currentMessage = lDb.InsertLog(HomeScreenActivity.mCurrent.getID(), 
						String.valueOf(cal.get(Calendar.HOUR))+":0"+String.valueOf(cal.get(Calendar.MINUTE)),
						String.valueOf(cal.get(Calendar.MONTH))+"/"+String.valueOf(cal.get(Calendar.DAY_OF_MONTH)), 
						cal.get(Calendar.AM_PM), TEXT, msg, HomeScreenActivity.mCurrent.getMessageText(), incNum);
			}
			else {
				// Add the text to the log
				currentMessage = lDb.InsertLog(HomeScreenActivity.mCurrent.getID(), 
						String.valueOf(cal.get(Calendar.HOUR))+":"+String.valueOf(cal.get(Calendar.MINUTE)),
						String.valueOf(cal.get(Calendar.MONTH))+"/"+String.valueOf(cal.get(Calendar.DAY_OF_MONTH)), 
						cal.get(Calendar.AM_PM), TEXT, msg, HomeScreenActivity.mCurrent.getMessageText(), incNum);
			}
		}		
	}
	
	/*	isValidNumber
	 * 		Makes sure it passes various tests
	 * 		so it can be able to be texted back
	 * 			If it is 1800 number
	 * 			If it is an international call
	 * 			If it is a small lengthed number
	 * 			
	 */
	
	private boolean isValidNumber(String incNum){
		if((incNum.length() < 6) ||												// Check length
				(incNum.substring(0, incNum.length()/2).contains("1800")))		// Check if 1800 number
			return false;
		else return true;
	}
	
	
}

