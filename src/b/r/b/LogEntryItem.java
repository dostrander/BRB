package b.r.b;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;

public class LogEntryItem {
	private int CALL = 0, TEXT = 1;   
	private static final String TAG = "LogEntryItem";
	Calendar cal;
	int type;
	String contact_number;
	String response;
	Boolean expanded;

// hour should be given in 24 hour(0-23) time!!!
public LogEntryItem (int month, int day, int year, int hour, int minutes, int tempType, String num, String rsp)
{
	//Calendar
	cal = Calendar.getInstance();
	cal.set(year, month, day, hour, minutes);
	//Log.d(TAG, response);
	type = tempType;
	contact_number = num;
	response = rsp;
	expanded = false;
}

public String getDate()
{
	DateFormat format = new SimpleDateFormat("MM/dd");
	return format.format(cal.getTime()).toString();
}
public String getTime()
{
	DateFormat format = new SimpleDateFormat("hh:mm");
	return format.format(cal.getTime()).toString();
}
public String getAMPM()
{
	DateFormat format = new SimpleDateFormat("aa");
	return format.format(cal.getTime()).toString();
}
public String findContact(String phoneNumber,Context ctx)
{
	Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
	Cursor resolver = ctx.getContentResolver().query(uri, new String[]{PhoneLookup.DISPLAY_NAME},null,null,null);
	return(resolver.getString(0));
}


}




