package ln_zap.zap.util;

import android.content.Context;

import com.jakewharton.processphoenix.ProcessPhoenix;



public class AppUtil {

	private static AppUtil instance = null;
	private static Context context = null;



    private AppUtil() { ; }

	public static AppUtil getInstance(Context ctx) {
		
		context = ctx;
		
		if(instance == null) {
			instance = new AppUtil();
		}
		
		return instance;
	}


	public void restartApp() {
		ProcessPhoenix.triggerRebirth(context);
	}

}
