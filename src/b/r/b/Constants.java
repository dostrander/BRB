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

	public static final String AUTHORITY = "b.r.b";
	public static final Uri CONTENT_URI_PARENT = Uri.parse("content://"
		         + AUTHORITY + "/" + PARENT_TABLE);
	public static final Uri CONTENT_URI_CHILD = Uri.parse("content://"
	         + AUTHORITY + "/" + CHILD_TABLE);

	// Columns in the Players database
	public static final String ID = "id";
	public static final String MESSAGE = "message";
	public static final String CHILD_IDS = "child_ids";

	public static final String RESPONSE_LOG_IDS = "response_log_ids";
	public static final String USES = "uses";
   
}
