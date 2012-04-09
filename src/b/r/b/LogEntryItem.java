package b.r.b;

import java.util.Calendar;

public class LogEntryItem {
	private int CALL = 0, TEXT = 1;   
	
	Calendar cal;
	int type;
	String contact_number;
	String message;
	Boolean expanded;

public LogEntryItem (int month, int day, int year, int tempType, String num, String msg)
{
	//Calendar
	
	type = tempType;
	contact_number = num;
	message = msg;
	expanded = false;
}
}


