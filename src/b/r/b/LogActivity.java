package b.r.b;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LogActivity extends ListActivity{
	private static final String TAG = "LogActivity";
	private static final String NO_LOGS = "No Log Entries for this Message";
	
	private static ArrayList<LogEntryItem> mLogItems;
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		Log.d(TAG,"in onCreate");
		mLogItems = new ArrayList<LogEntryItem>();
		setListAdapter(new LogAdapter(this));
		fillData();
		
	}
	
	private void fillData(){
		Log.d(TAG,"in fillData");
		if(true){
			
			mLogItems.add(new LogEntryItem(0,0,0,0,0,0,"-1",NO_LOGS));
		}else {
			
		}
	}
	
	
	

	private class LogAdapter extends BaseAdapter{
		private final int CALL = 0;
		private final int TEXT = 1;
		private LayoutInflater inflater;
		private Context context;
		private LogActivity logActivity; 
		
		public LogAdapter(Context ctx){
			inflater = LayoutInflater.from(ctx);
			context = ctx;
			logActivity = (LogActivity) ctx;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			final LogEntryItem tempObject = getItem(position);
			Log.d(TAG,"in getView");

			if(convertView == null){
				convertView = inflater.inflate(R.layout.log_entry_item,null);
				holder = new ViewHolder();
				holder.date = (TextView) convertView.findViewById(R.id.log_entry_date);
				holder.time = (TextView) convertView.findViewById(R.id.log_entry_time);
				holder.number = (TextView) convertView.findViewById(R.id.log_entry_contact);
				holder.expanded = (TextView) convertView.findViewById(R.id.log_entry_more);
				holder.ampm = (TextView) convertView.findViewById(R.id.log_entry_time_ampm);
				holder.response_label = (TextView) convertView.findViewById(R.id.log_entry_response_label);
				holder.time_label = (TextView) convertView.findViewById(R.id.log_entry_time_label);
				holder.response = (TextView) convertView.findViewById(R.id.log_entry_response);
				holder.type = (ImageView) convertView.findViewById(R.id.log_entry_picture);
				holder.expansion = (RelativeLayout) convertView.findViewById(R.id.log_entry_expansion);
//				if(tempObject.message == NO_LOGS){
//					holder.response.setText(tempObject.message);
//					Log.d("here","here");
////					holder.date.setVisibility(View.INVISIBLE);
////					holder.time.setVisibility(View.INVISIBLE);
////					holder.number.setVisibility(View.INVISIBLE);
////					holder.expanded.setVisibility(View.INVISIBLE);
////					holder.ampm.setVisibility(View.INVISIBLE);
////					holder.response_label.setVisibility(View.INVISIBLE);
////					holder.time_label.setVisibility(View.INVISIBLE);
//////					holder.response.setVisibility(View.INVISIBLE);
////					holder.type.setVisibility(View.INVISIBLE);
//
//				}

//				holder.date.setText
//				holder.time
//				holder.number
//				holder.expanded
//				holder.ampm
//				holder.response_label
//				holder.time_label
//				holder.response
//				holder.type
				convertView.setTag(holder);
			}else {
				holder = (ViewHolder) convertView.getTag();
			}
			if(tempObject.expanded){
				Log.d(TAG,"expanded");
				convertView.findViewById(R.id.log_entry_expansion).setVisibility(View.VISIBLE);
			}
			else{
				Log.d(TAG,"not expanded");
				convertView.findViewById(R.id.log_entry_expansion).setVisibility(View.GONE);
			}

    		convertView.setOnLongClickListener(new OnLongClickListener() {
				public boolean onLongClick(View v) {
					if (tempObject.response != NO_LOGS)
						;

					return false;
				}
			});
    		convertView.findViewById(R.id.log_entry_more).setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					Log.d(TAG,"in onClick more");
					getItem(position).setExpanded();
					notifyDataSetChanged();
				}
    			
    		});
         

			return convertView;
		}

		public int getCount() {
			return mLogItems.size();
		}

		public LogEntryItem getItem(int position) {
			return mLogItems.get(position);
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}
		
		class ViewHolder{
			ImageView type;
			TextView date;
			TextView number;
			TextView expanded;
			RelativeLayout expansion;
			TextView time;
			TextView ampm;
			TextView response;
			TextView time_label;
			TextView response_label;
			
		}
		
	}
	
	
	
	
	
	

    /**
     * We will use a SpeechView to display each speech. It's just a LinearLayout
     * with two text fields.
     *
     */
    private class ExtendView extends LinearLayout {
		TextView time;
		TextView ampm;
		TextView response;
		TextView time_label;
		TextView response_label;
        public ExtendView(Context context, String title, String dialogue, boolean expanded) {
            super(context);

            this.setOrientation(VERTICAL);

            // Here we build the child views in code. They could also have
            // been specified in an XML file.

            mTitle = new TextView(context);
            mTitle.setText(title);
            addView(mTitle, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            mDialogue = new TextView(context);
            mDialogue.setText(dialogue);
            addView(mDialogue, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            mDialogue.setVisibility(expanded ? VISIBLE : GONE);
        }

        /**
         * Convenience method to set the title of a SpeechView
         */
        public void setTitle(String title) {
            mTitle.setText(title);
        }

        /**
         * Convenience method to set the dialogue of a SpeechView
         */
        public void setDialogue(String words) {
            mDialogue.setText(words);
        }

        /**
         * Convenience method to expand or hide the dialogue
         */
        public void setExpanded(boolean expanded) {
            mDialogue.setVisibility(expanded ? VISIBLE : GONE);
        }

        private TextView mTitle;
        private TextView mDialogue;
    }

}
