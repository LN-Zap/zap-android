package ln_zap.zap;

import android.content.Intent;
import android.os.Bundle;


import androidx.preference.PreferenceManager;
import ln_zap.zap.baseClasses.BaseAppCompatActivity;

public class LandingActivity extends BaseAppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isWalletSetup = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("isWalletSetup",false);

        if (isWalletSetup){
            // Go to PIN entry screen
            Intent intent = new Intent(this, PinEntryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else{
            // Go to welcome screen
            Intent intent = new Intent(this, WelcomeNewUserActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        }
    }
}
