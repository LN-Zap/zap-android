package ln_zap.zap.util;

import android.content.Context;

import com.jakewharton.processphoenix.ProcessPhoenix;



public class AppUtil {

	private static AppUtil mInstance = null;
	private static Context mContext = null;



    private AppUtil() { ; }

	public static AppUtil getInstance(Context ctx) {
		
		mContext = ctx;
		
		if(mInstance == null) {
			mInstance = new AppUtil();
		}
		
		return mInstance;
	}


	public void restartApp() {
		ProcessPhoenix.triggerRebirth(mContext);
	}

}
