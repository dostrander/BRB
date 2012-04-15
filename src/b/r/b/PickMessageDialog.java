package b.r.b;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class PickMessageDialog extends Dialog {
	HomeScreenActivity activity;
	Context context;
	ListView messageList;
	EditText inputMessage;
	String[] messages;

	protected PickMessageDialog(Context ctx) {
		super(ctx);
		messageList = (ListView) findViewById(R.id.dialog_message_list);
		inputMessage = (EditText) findViewById(R.id.dialog_input_message);
		activity = (HomeScreenActivity) ctx;
		context = ctx;
		messages = activity.MESSAGES;
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(ctx,
				R.id.input_message_list_item,messages);
		messageList.setAdapter(adapter);
		messageList.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				activity.getMessageFromDB(messages[position]);
			}
		});
      inputMessage.addTextChangedListener(new TextWatcher(){
  		public void beforeTextChanged(CharSequence s, int start,
  				int count, int after) {
  			inputMessage.setEnabled(false);
  		}
		public void afterTextChanged(Editable e){
			inputMessage.setEnabled(true);
		}
  		public void onTextChanged(CharSequence s, int start, int count,
  				int after) {
  			adapter.getFilter().filter(s);
  		}
      });
	}
}
