package b.r.b;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;

public class LogEntryItem {
	private int CALL = 0, TEXT = 1;   
	private static final String TAG = "LogEntryItem";
	//Calendar cal;
	int type;
	String time;
	String date;
	int ampm;
	String contact_number;
	String response;
	//String messageSent;
	Boolean expanded;

public LogEntryItem (String tempTime, String tempDate, int tempAmpm, int tempType, String rsp, String num)
{
	//Log.d(TAG, response);
	time = tempTime;
	date = tempDate;
	ampm = tempAmpm;
	type = tempType;
	contact_number = num;
	response = rsp;
	expanded = false;
}

public String getDate()
{
	//DateFormat format = new SimpleDateFormat("MM/dd");
	//return format.format(cal.getTime()).toString();
	return date;
}
public String getTime()
{
	//DateFormat format = new SimpleDateFormat("hh:mm"); //kk sets hour of the day e.g. 24 hours
	//return format.format(cal.getTime()).toString();
	return time;
}
public String getAMPM()
{
	//DateFormat format = new SimpleDateFormat("aa");
	//return format.format(cal.getTime()).toString();
	
	//AMPM is set as either 0 or 1, 0 is for AM, 1 for PM
	String retAmpm;
	
	if(ampm == 0)
		retAmpm = "AM";
	else
		retAmpm = "PM";
	
	return retAmpm;
}
public String findContact(Context ctx, String num)
{
	Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(num));
	Cursor cursor = ctx.getContentResolver().query(contactUri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
	if(cursor.moveToFirst())
	if(!cursor.isAfterLast())
		return cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
	return num;

}
public void setExpanded(){expanded = !expanded;}


public String getResponse()
{
	return response;
}



}




