package b.r.b;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ListenerService extends Service {

	@Override
	public void onCreate(){
		
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		
		return 1;
		
	}
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onDestroy(){
		
	}

}
