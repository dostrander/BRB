/*//////////////////////////////////////////
//				BRB-Android 	     	  //
//				Created by:				  //
//			  Derek Ostrander			  //
//				 Max Shwenk				  //
//			    Stuart Lang				  //
//				Evan Dodge				  //
//			   Jason Mather				  //
//				Will Stahl				  //
//										  //
//				Created on:				  //
//			January 23rd, 2011			  //
//////////////////////////////////////////*/

package b.r.b;

import android.app.Activity;									
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
import android.os.Bundle;
//import android.telephony.SmsMessage;
import android.util.Log;											// Logs
import android.widget.Toast;


/*	HomeScreenActivity
 * 		Starts the application bringing up the main screen	
 */

public class HomeScreenActivity extends Activity {
	private final String TAG = "HomeScreenActivity";
    // Convenience Variables
	
	// Variables
	IncomingListener listener;
	
	
	/*	onCreate
	 * 		
	 */
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"in onCreate");
    }
    
    @Override
    public void onStart(){
    	super.onStart();
    	Log.d(TAG,"in onStart");
        setContentView(R.layout.main);
        //listener = new Listener(new Message("Hey, I'm Derek!",false),this);
        //smsListener 	= new SmsListener();
        //callListener 	= new CallListener();
        getIntent().putExtra("currentMessageText", "hey");
        listener = new IncomingListener();
    	   	
    }
        
    @Override
    public void onPause(){
    	super.onPause();
    	Log.d(TAG,"in onPause");
    }
    
    @Override
    public void onStop(){
    	super.onStop();
    	Log.d(TAG, "in onStop");
    	
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	Log.d(TAG,"in onDestroy");
    	
    }
    
    
    public void popToast(String t){
    	Toast.makeText(this, t, Toast.LENGTH_LONG);

    }
    
    
    
}