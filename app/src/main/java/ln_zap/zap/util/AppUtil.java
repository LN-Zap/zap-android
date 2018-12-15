package ln_zap.zap.util;

import android.content.Context;
import android.content.Intent;

import ln_zap.zap.PinEntryActivity;


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
		Intent intent = new Intent(context, PinEntryActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		context.startActivity(intent);
	}

}
