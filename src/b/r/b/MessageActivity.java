package b.r.b;

import static b.r.b.Constants.*;
import b.r.b.Message.ChildMessage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

public class MessageActivity extends Activity {
	// Convinience 
	private static final String TAG = "MessageActivity";										// For Logs
	
	// Variables
	private static ContactMessageListAdapter 	mAdapter;										// Adapter for controller listView for Contact Specific Messages
	private static Message 						mMessage;										// Current Message enabled, null if disabled
	// Views
	private static TextView 					vEndTime;										// Text of End Time of Current Message
	private ListView 							vContactMessageList;							// ListView of Contact Specific Messages
	private View								header;											// Header on top of listview that adds new Contact Specific Messages
	
	
	/*	onCreate
	 * 		Gets all views and registers clickListeners
	 * 		also sets dates
	 */
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setTheme(Settings.Theme());																// Set theme decided in SettingsActivity
		setContentView(R.layout.message_view);													// Set layout of the Activity
		
		// Find Views
		vEndTime 			= (TextView) 	findViewById(R.id.endtime_text);		
		vContactMessageList = (ListView) 	findViewById(R.id.contact_specific_message_list);
		header				= 				findViewById(R.id.header);
		
		// Set Adapter for Contact Specific Message ListView
		mAdapter 			= new ContactMessageListAdapter(this);
		vContactMessageList.setAdapter(mAdapter);
		vContactMessageList.setLongClickable(true);												// Set LongClickable for edit/deletion
		
		setDates();																				// Set Dates of EndTime Text
		registerListeners();																	// Register Click Listeners
		
	}
	/*	registerListeners
	 * 		registers onClick Listeners for the various views in the layout
	 */
	private void registerListeners(){
		// 	End Time TextView onClick
		// 		Create a dialog with date and time picker to change the 
		// 		EndTime in the current Message
		vEndTime.setOnClickListener(new OnClickListener(){										// Click Listener for EndTime
			public void onClick(View v) {
				Calendar cal;
				final LinearLayout dialoglayout = setUpDialog();								// Linear Layout for container of the new dialog
	    		final DatePicker dp = (DatePicker) dialoglayout.findViewById(0);				// Get DatePicker
	    		final TimePicker tp = (TimePicker) dialoglayout.findViewById(1);				// Get TimePicker
	    		if(mMessage != null)															// If there is a current Message
	    			cal = mMessage.getEndTime();													// get the End Time
	    		else cal = Calendar.getInstance();												// else get the current time
	    		tp.setCurrentHour(cal.get(Calendar.HOUR));										// set time picker hour
	    		tp.setCurrentMinute(cal.get(Calendar.MINUTE));									// set time picker minute
				dp.init(cal.get(Calendar.YEAR), 												// set date picker year
						cal.get(Calendar.MONTH), 												// set date picker month
						cal.get(Calendar.DAY_OF_MONTH),											// set date picker day
						new OnDateChangedListener(){											// Fake Date Changed Listener 
							public void onDateChanged(DatePicker view,								// (There is no reason for this but it is just
									int year, int monthOfYear, int dayOfMonth) {}});				//  a way to initialize it with the correct date)
				
				// Build Dialog for EndTime Date/Time Picker
				AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);	// Builder for Dialog
				builder.setView(dialoglayout)													// Set View
						.setTitle("Select End Time")											// Set title of Dialog
				        .setCancelable(false) 													// Make it so they can not use the back button
				        .setPositiveButton("Set", new AlertDialog.OnClickListener() {			// OnClick to actually set the time/date
				    	public void onClick(DialogInterface dialog, int which){
				    		if(mMessage != null)												// If there is a message 
				    			mMessage.setEndDate(dp.getMonth(), dp.getDayOfMonth(),			// Set the current Message Date
				    					dp.getYear(),
				    					tp.getCurrentHour(), tp.getCurrentMinute());			// Set the current Message Time
				    		setDates();															// Set the textView
						}
				})	.setNegativeButton("Cancel", new AlertDialog.OnClickListener(){				// Set an Empty Negative button to be able to 
					public void onClick(DialogInterface dialog, int which) {}});					// get out of the dialog without making changes
				AlertDialog alert = builder.create();											// Create the dialog from the builder
				alert.show();																	// And show it
				}
			});
		
		// -- Header Clickables --
		//	Header add Names
		//		Add contacts to contact to the header to allow for contact specific
		//		Messages to be added to the Message
		header.findViewById(R.id.add_names_button).setOnClickListener(new OnClickListener(){	// Find the View
			public void onClick(View v) {
				if(mMessage != null)															// if the message is not null
					contactPickerDialog(mMessage.getHeaderText());								// open up a contactPickerDialog
			}
		});
		// TextViews for reuse
		final TextView tv = (TextView) header.findViewById(R.id.contact_specific_message_text);	// Find TextView for the message text
		final TextView nv  = (TextView) header.findViewById(R.id.names);						// Find TextView for the contacts names
		// 	Header show contact names
		//		If there are names in there allow them to be edited
		//		if there is a message and no names bring up the dialog
		//		else popToast to tell them to select a message
		nv.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(mMessage == null)															// If Current Message is null 
					popToast("To add contact specific messages select a message");				// poptoast telling them to select a message
				else if(nv.getText().toString().equals(CLICK_TO_ADD_NAMES))						// if there aren't any contacts added
					contactPickerDialog(mMessage.getHeaderText());								// bring up the dialog for new contacts
//				else editContactsDialog(); EDIT CONTACTS
			}
		});
		// 	Header show message text
		//		If there is a message selected then show a new dialog
		//		else tell them to select a message
		tv.setOnClickListener(new OnClickListener(){
			public void onClick(final View v) {
				if(mMessage == null)															// If Current Message is null
					popToast("To add contact specific messages select a message");				// poptoast telling them to select a message
				else{																			// else build a dialog
					// Build Edit Contact Specific Message Dialog
					AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);// Get the builder
					builder.setTitle("Edit Message Text");										// Set the title of the Dialog
					final EditText input = new EditText(MessageActivity.this);					// EditView to allow user to edit the message
					if(tv.getText().toString() != CLICK_TO_EDIT)								// If the user has changed the text
						input.setText(tv.getText().toString());									// put that text into the EditText
					input.setLines(2);															// Amount of lines for 2
					input.setGravity(Gravity.TOP);												// So the cursor of the textview is at the top left
					builder.setView(input);														// add the view to the dialog

					builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {		// When The Text is set
						public void onClick(DialogInterface dialog, int whichButton) {
								((TextView) v).setText(											// Set the textView to the new text
										input.getText().toString().trim().toString());			
								mMessage.setHeaderText(											// Set the Current Message header text
										input.getText().toString().trim().toString());
						}
					});
					builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {}});		// Just Cancel the dialog
					AlertDialog alert = builder.create();										// Create the dialog from the builder
					alert.show();																// and show it
				}
			}
    	});
		//	Header add header button
		//		If the there is a message selected, contacts in the Contact Specific Message
		//		and the text has been edited allow the header to be added to the list
		//		of contact specific messages for that Parent Message
		header.findViewById(R.id.add_message_button).setOnClickListener(new OnClickListener(){	// Find the button
			public void onClick(View v) {
				if(!tv.getText().toString().equals(CLICK_TO_EDIT) &&							// If it has been edited 
						!contains(tv.getText().toString()) &&									// if there is no other texts like it in the
																									// contact specific messages for that message
						tv.getText().toString().trim().length() > 0){							// if there is actually text in the Contact Specifc Message
					if(mMessage == null){														// If there isn't a message selected
						popToast("Please select a message prior to doing this action");			// tell them to select one
					}else if(mMessage.headerNumbersSize () < 1)									// If there are no contacts in it 
						popToast("Please Add Contacts First");									// tell them to put some in first
					else if (mMessage.checkHeaderForDupNumbers())								// If there are duplicate numbers
						popToast("You have conflicting contacts in this contact specific " +	// Tell them to edit the contacts
								"Message. Please Check the contacts and try adding again");		
					else{																		// else add it to the list
						mMessage.addHeaderToChild(MessageActivity.this);						// add header to child in Current Message
						mAdapter.notifyDataSetChanged();										// update contact specific message listview
						setViewToHeader();														// Set The header View to what it is supposed to be
					}
				}
			}
		});

		nv.setMovementMethod(new ScrollingMovementMethod());									// Allow the names textview to be scrollable
		setViewToHeader();																		// Set The header View to what it is supposed to be
	}
	
	// --- View Setters ---
	
	/*	setViewToHeader
	 * 		If there is a message selected then set the header view to the currentMessages
	 * 		underlying header object, 
	 * 		If there is not a message selected just put the textViews to the defaults
	 */
	public void setViewToHeader(){
		String t,n;																				// Variables for text and names
		if(mMessage != null){																	// If Message is selected
			t = mMessage.getHeaderText();														// set t to the Message Header's text 
			n = mMessage.getHeaderNames();														// set n to the Message Header's names
		}else {																					// if there is no message selected
			t = CLICK_TO_EDIT;																	// set t to default 
			n = CLICK_TO_ADD_NAMES;																// set n to defaults
		}
		((TextView)header.findViewById(R.id.contact_specific_message_text)).setText(t);			// set the message text to the view
		((TextView)header.findViewById(R.id.names)).setText(n);									// set the names to the view
	}
	/*	setDates
	 * 		If there is a message selected set the EndTime to that text
	 * 		else just set it to the default NO+END 
	 */
	private static void setDates(){
		if(mMessage == null){																	// If a message is not selected
			vEndTime.setText(NO_END);															// just set the endtime to default
		} else {																				// if there is one selected
			vEndTime.setText(mMessage.endDateToText());											// set it the current message's end time
		}
	}
	
	// --- Message Setters ---
	
	/*	changeMessage
	 * 		changes current Message and tells the Contact Specific Message ListView
	 * 		to update to the new list
	 */
	public static void changeMessage(Message current){
		mMessage = current;																		// Change the current Message
		mAdapter.notifyDataSetChanged();														// Update listView
	}
	
	/*	noMessage
	 * 		set the Current Message to null
	 */
	public static void noMessage(){ mMessage = null; }	
	
	// --- Boolean Operations ---
	
	/*	contains
	 * 		checks to see if the Current Message has a certain text as a child message if it
	 * 		does it returns true, if it doesn't or the Current Message is null it returns false
	 */
	private boolean contains(String t){
		if(mMessage == null) return false;														// If there is no message just return false					
		else return mMessage.childContainsMessage(t);											// else see if it contains that message text
	}
	/*	containsNumber
	 * 		checks to see if the Current Message header has a specific number, if it does it returns
	 * 		true, if it doesn't or the Current Message is null it returns false.
	 */
	private boolean containsNumber(String num){
		if(mMessage == null) return false;														// if there is no message selected just return false
		else return mMessage.headerContainsNumber(num);											// else check to see if it contains that number already
	}

	
	
	// Dialog/Toast Making Functions
	/*	popToast
	 * 		Convenience for just throwing up a toast to the screen
	 */
    private void popToast(String t){Toast.makeText(this, t, Toast.LENGTH_LONG).show();}
    
    /*	longClickDialog
     * 		throw up the dialog for delete the message, editing the contacts or 
     * 		editing the message
     */
	private void longClickDialog(){
		final String[] items = new String[]{"Edit Contacts", "Edit Message", "Delete Message"};	// The three categories
		AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);			// get the builder
		builder.setTitle("Edit Contact Specific Message")										// set the title
		.setItems(items,																		// set the items
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {					// When the item is clicked
						
					}
				});
		builder.create().show();																// create and show the dialog
	}
	
	/*	contactPickerDialog
	 * 		Gets a Cursor (which allows for traversing through the DB) from the contacts api
	 * 		to get a list of contacts for the user to pick and add them to the contact specific
	 * 		message contacts
	 */
	public void contactPickerDialog(String text){
	    final Cursor cur = MessageActivity.this.getContentResolver().query(						// Query the contacts api for a cursor
	            ContactsContract.Data.CONTENT_URI,												// Contacts URI
	            new String[]{ContactsContract.Data._ID											// Have the cursor contain the ID
	            		,ContactsContract.Data.DISPLAY_NAME,									//		the Name of the contact
	            		ContactsContract.Data.DATA1},											// 		and the number of the person
	            ContactsContract.Data.MIMETYPE + " = '" +										// Checker to see if it is a phone number
	            				ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'",	// 		(aka not email)
	            null,																			// No Args for the last statement
	            ContactsContract.Data.DISPLAY_NAME + " COLLATE LOCALIZED ASC");					// In ASC order
	    
	    // Get Views
		final ListView lv = new ListView(MessageActivity.this);									// Get New ListView for showing contacts
        final PickContactsAdapter adap = new PickContactsAdapter(MessageActivity.this,cur);		// Create new adapter for picking contacts
		LinearLayout ll = new LinearLayout(MessageActivity.this);								// Put it all in a linear layout holder
		// Set Up Views
		ll.setOrientation(LinearLayout.VERTICAL);												// Set linear layout orientation to Vertical
		ll.addView(lv);																			// add the listview
		lv.setAdapter(adap);																	// set the adapter for the listview
		
		// Build Dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);			// Get the builder for the dialog
		builder.setTitle("Select Contacts")														// set the title
		.setView(ll)																			// set the view for the dialog
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {						// When the user is done
			public void onClick(DialogInterface dialog, int whichButton) {
				mMessage.addHeaderContacts(MessageActivity.this, adap.getChecked());			// Add the contacts from the headerview to the message header
				((TextView) header.findViewById(R.id.names)).setText(mMessage.getHeaderNames());// Set the header names textview to the contacts actual names
				cur.close();																	// Close the cursor to avoid random errors
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {				// When the user just wants to cancel
			public void onClick(DialogInterface dialog, int whichButton) {
				cur.close();																	// close the cursor
			}
		}).create().show();																		// create and show the dialog
	}
	
	/*	setUpDialog
	 * 		sets up the Linear layout for inside the dialog
	 */
	private LinearLayout setUpDialog(){
		LinearLayout dialoglayout 	=  new LinearLayout(MessageActivity.this);					// Create a linear layout for the dialog
		DatePicker dp  				= new DatePicker(MessageActivity.this);						// get date picker
		TimePicker tp  				= new TimePicker(MessageActivity.this);						// get time picker
		dp.setPadding(10, 10, 10, 10);															// set some space around the datepicker
		dp.setId(0);																			// set the ID for getting later
		tp.setIs24HourView(false);																// make it so it is AM/PM view
		tp.setPadding(30, 10, 10, 10);															// set some space around the timepicker
		tp.setId(1);																			// set the ID for getting later
		dialoglayout.addView(dp);																// add date picker to linear layout
		dialoglayout.addView(tp);																// add time picker to linear layout
		dialoglayout.setOrientation(LinearLayout.VERTICAL);										// set linear layout to vertical orientation
		return dialoglayout;																	// return the linear layout
	}

	
	/*	class ContactMessageListAdapter
	 * 		sets up and controls the listView for the Contact Specific Messages
	 */		
	private class ContactMessageListAdapter extends BaseAdapter {
		// Holder class for easy accessing of the items in the listview
		private class ViewHolder{
			TextView text;																		// Message TextView
			TextView names;																		// Message names of Contacts TextView
			ImageView addNames;																	// Add contacts button
		}
		private LayoutInflater inflater;														// Inflator for inflating views
		// Constructor, just create a layout inflator so we can use it later
		public ContactMessageListAdapter(Context ctx){inflater = LayoutInflater.from(ctx);}
		
		/*	getView
		 * 		gets the next view coming in the list view
		 * 		if there is not a view inflated already inflate one and add the views
		 * 		to the ViewHolder object for easier access later
		 * 		then set the data inside the views and the click listeners
		 */
		public View getView(final int position, View convertView, ViewGroup parent){
        	final ViewHolder holder;															// Temp viewHolder object
        	final ChildMessage tempObject = getItem(position);									// Get the child message associated with that position
        	if (convertView == null) {															// If the view is not inflated
        		convertView = inflater.inflate(R.layout.message_item, null);					// inflate it
        		holder = new ViewHolder();														// set the viewHolder object
        		// Find the Views
        		holder.text 	= (TextView) 	convertView.findViewById(						// Find the view for the message text
        				R.id.contact_specific_message_text);
        		holder.names 	= (TextView)	convertView.findViewById(R.id.names);			// find the view for the contacts names
        		holder.addNames = (ImageView) 	convertView.findViewById(R.id.add_names_button);// find the button for adding the contacts
        		holder.text.setTag(getItem(position));											// set the text
        		convertView.setTag(holder);														// set a tag to the holder so we can get it later
        	} else{
        		holder = (ViewHolder) convertView.getTag();										// set the view holder (since it wasn't set yet)
        		holder.text.setTag(tempObject);													// set the text
        	}
        	
        	// Set Click Listeners
        	
        	// Long of the whole ListItem 
        	convertView.setOnLongClickListener(new OnLongClickListener(){						// On Long Click of an Item
				public boolean onLongClick(View v) {	
					longClickDialog();															// show the longClickDialog
					return true;																// tell it that it actually longClicked

				}
        	});
        	// 	Click of the message text textview
        	//		build a dialog to edit the text
        	holder.text.setOnClickListener(new OnClickListener(){
    				public void onClick(View v) {
    					AlertDialog.Builder builder = new AlertDialog.Builder(					// Get a builder for making the dialog
    							MessageActivity.this);
    					builder.setTitle("Edit Message Text");									// set the title
    					final EditText input = new EditText(MessageActivity.this);				// Edit Text for editing the text
    					input.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);				// set the inputType
    					builder.setView(input);													// set the dialog view
    					builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {	// when the user is finished
    						public void onClick(DialogInterface dialog, int whichButton) {
    							if(holder.text.getText().toString() 							// if the holder text
    									!= input.getText().toString()){							// does not equal the input text (if it has been changed)
    								holder.text.setText(input.getText().toString());			// set the holder text
    								getItem(position).text = input.getText().toString();		// set the contact specific messages text in the message
    							}
    						}
    					});
    					
    					builder.setNegativeButton("Cancel",										// when the user wants to cancel 
    							new DialogInterface.OnClickListener() {
    						public void onClick(DialogInterface dialog, int whichButton) {}
    					});
    					AlertDialog alert = builder.create();									// create the dialog
    					alert.show();															// and show it
    				}
            	});
        	
        	// 	Click the add contacts button
        	//		brings up the Contact Picker dialog with the correct contacts
        	//		checked
        	holder.addNames.setOnClickListener(new OnClickListener(){
    				public void onClick(View v) {
    				}
            	});
        	// set data
    		holder.text.setText(tempObject.text);												// set the text of the message of the list item 
    		holder.names.setText(tempObject.namesText);											// set the text of the names of the list item
			return convertView;																	// return the view
		}
		/*	getCount
		 * 		if there is no message selected return 0
		 * 		else just return the size of the ChildMessages
		 * 			(Have to check for null because the listView calls this automatically)
		 */
		public int getCount() {
			if(mMessage == null) return 0;														// if null return 0
			else return mMessage.cMessages.size();												// else return the size
		}
		
		/*	getItem
		 * 		Just gets the Item at the certain position
		 * 			only called by me so we don't have to catch an exception
		 */
		public ChildMessage getItem(int position) {
			return mMessage.cMessages.get(position);
		}
		// NOT called by me, needed to build the custom Adapter
		public long getItemId(int position) { return 0;}
	}		
	/*	class PickContactsAdapter
	 * 		gets Cursor holding all of the and sets it up in the HashMap for easier access
	 * 			(THIS COULD BE A BASEADAPTER INSTEAD OF CURSOR ADAPTER HOWEVER
	 * 			 I TRIED TO CHANGE IT TO ONE AND IT WASN'T WORKING RIGHT SO JUST DUE TO
	 * 			 TIME I AM GOING TO LEAVE THIS A CURSORADAPTER)
	 */
	private class PickContactsAdapter extends CursorAdapter{
		// ViewHolder for easy access of certain items in the list item
		class ViewHolder{
			TextView name;																		// For Contact name
			TextView number;																	// For the contacts phone number
			CheckBox checked;																	// To see if you want it in the contact specific message
		}
		// Data Holder for holding the name and if it is checked
		class Holder{
			public Holder(String n,boolean c){name = n; checked = c;}							// Constructor
			boolean checked;																	// underlying boolean to tell if it is checked or not
			String name;																		// underlying string for the name of the contact
		}
		HashMap<String,Holder> numbers;															// Hashmap, the number as key, dataholder as object
		Cursor cursor;																			// Cursor for Contacts api
		
		// 	Constructor
		//		gets the cursor and traverses through it, adding it to the hashmap
		public PickContactsAdapter(Context context, Cursor c){
			super(context, c);
			Holder h;																			// temp data object
			String key;																			// temp key
			cursor = c;																			// set cursor
			numbers = new HashMap<String,Holder>();												// initialize hashmap
			if(cursor.moveToFirst())															// move to first, if there isn't any or an error just dont do it
				do {																			
					h =  new Holder(cursor.getString(											// create new data holder objuect
							cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)),			// with the display name
							 containsNumber(cursor.getString(									// and if it is already in the contact specific messages or not
									 cursor.getColumnIndex(ContactsContract.Data.DATA1))));
					key = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA1));	// Set the key as the contacts number
					numbers.put(key,h);															// add it to the hashmap
					cursor.moveToNext();														// go to the next item in the cursor
				}
				while(!cursor.isAfterLast());													// if there is nothing left in the cursor just quit
		}
	
		
		/*	newView
		 * 		inflate layout for the view and set the tag of the view to a new viewholder object
		 */
		@Override
		public View newView(Context context, Cursor c, ViewGroup parent) {
			final ViewHolder holder = new ViewHolder();											// set new viewholder object
			View v = LayoutInflater.from(context).inflate(R.layout.pick_contact_item, null);	// inflate layout
			v.setTag(holder);																	// set tag for future use
			return v;
		}
		
		/*	bindView
		 * 		gets Views and puts them to the holder object and sets the data
		 * 		inside the view to the correct data
		 */
		@Override
		public void bindView(View v, Context context, final Cursor c) {
			final String key = c.getString(cursor.getColumnIndex(ContactsContract.Data.DATA1));	// Get the number
			final ViewHolder holder = (ViewHolder) v.getTag();									// get the view holder
			holder.name 	= (TextView) v.findViewById(R.id.contact_name);						// find the contact name textview	
			holder.number 	= (TextView) v.findViewById(R.id.contact_number);					// find the contact number textview
			holder.checked 	= (CheckBox) v.findViewById(R.id.contact_checked);					// find the checkbox
			holder.number.setText(key);															// set the number textview
			holder.name.setText(numbers.get(key).name);											// set the name textview
			holder.checked.setChecked(numbers.get(key).checked);								// set if it is selected or not
			holder.checked.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					boolean checked = !numbers.get(key).checked;								// switch if it is checked or not
					String n = numbers.get(key).name;											// get the name of the person checked or not
					numbers.put(key, new Holder(n,checked));									// set in the hashmap the new data
				}
			});
			v.setTag(holder);																	// set the tag for future reuse
		}
		/*	getChecked
		 * 		goes through the hashmap and gets the names that were checked
		 * 		once it finds all of them it tells the user how many contacts were added
		 * 		and returns the numbers
		 */
		public String[] getChecked(){
			ArrayList<String> nums = new ArrayList<String>();									// List of numbers
			int count = 0;																		// count for showing the user, init to 0
			for(String n : numbers.keySet())													// For all of the numbers
				if(numbers.get(n).checked){														// if it is checked
					nums.add(n);																// add it to the list of numbers
					count++;																	// up the count
				}	
			popToast("You have added " + String.valueOf(count) + " contacts.");					// tell the user how many are now in there
			return (String[]) nums.toArray(new String[]{});										// return the numbers
		}

	}

}
