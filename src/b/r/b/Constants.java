/* BRB
 * This class written by Jason Mather 2/25/2012
 * 
 * Project by: Evan Dodge, Stuart Lang, Jason Mather, Derek Ostrander, Max Schwenk, and Will Stahl 
 * 
 */
package b.r.b;

import android.net.Uri;
import android.provider.BaseColumns;

public interface Constants extends BaseColumns {
	
	public static final String PARENT_TABLE = "parent";	
	public static final String CHILD_TABLE = "child";
	public static final String LOG_TABLE = "log";

	public static final String AUTHORITY = "b.r.b";
	public static final Uri CONTENT_URI_PARENT = Uri.parse("content://"
		         + AUTHORITY + "/" + PARENT_TABLE);
	public static final Uri CONTENT_URI_CHILD = Uri.parse("content://"
	         + AUTHORITY + "/" + CHILD_TABLE);

	
	//These are constants used to make the SQL statements in childDB, parentDB, and
		//logDB easier to read
	public static final String TIME = "time";
	public static final String DATE = "date";
	public static final String AMPM = "ampm";
	public static final String TYPE = "type";
	public static final String RECEIVED_MESSAGE = "received_message";
	public static final String SENT_MESSAGE = "sent_message";
	public static final String ID = "_id";
	public static final String MESSAGE = "message";
	public static final String PARENT_ID = "parent_id";
	public static final String NUMBER = "number";
	public static final String RESPONSE_LOG_IDS = "response_log_ids";
	public static final String USES = "uses";
	
	// Prefs keywords
	public static final String PREFS = "BRB_PREFERENCES"; // for accessing preferences
	public static final String DB_ID_KEY = "db_id";       // get enabled db_id
	
	public static final String MESSAGE_ENABLED_KEY = "message_enabled"; // get if the message is enabled
	public static final int MESSAGE_ENABLED = 0;					// message enabled int
	public static final int MESSAGE_DISABLED = 1;					// message disbled int
	public static final int NO_MESSAGE_SELECTED = 2;				// no message selected int
	
	//volume levels to be set when BRB wakes up from alarm
	
	public static final String SOUND_PREF_KEY = "sound_pref";
	public static final int LOW_VOLUME=0;
	public static final int MEDIUM_VOLUME=0;
	public static final int HIGH_VOLUME=0;
	public static final int VIBRATE=0;
	public static final int SILENT=0;
	public static final int PREVIOUS_VOLUME=0;
	
	// end prefs keywords
	
	public static final String CLICK_TO_EDIT = "Click to Edit Text";
	public static final String CLICK_TO_ADD_NAMES = "Click to add Names...";
	
	public static final String NO_END = "No end";
   
}
