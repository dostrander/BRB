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
	
	@Override
	public void onTerminate(){
		super.onTerminate();
		dbInt.Cleanup();
	}
	
	public static DatabaseInteraction getDatabaseInteraction(){
		return dbInt;
	}
}
