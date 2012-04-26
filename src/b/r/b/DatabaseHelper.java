package b.r.b;

import android.app.Application;
//This class lets us handle more than one database interaction at once
public class DatabaseHelper extends Application{

	private static DatabaseInteraction dbInt;
	
	@Override
	public void onCreate(){
		super.onCreate();
		dbInt = new DatabaseInteraction(this);
		
	}
	public static DatabaseInteraction getDb(){
		return dbInt;
	}
	
	@Override
	public void onTerminate(){
		super.onTerminate();
		dbInt.CleanupChild();
		dbInt.CleanupLog();
		dbInt.CleanupParent();
	}
}	
