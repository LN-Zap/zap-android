package zapsolutions.zap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import zapsolutions.zap.baseClasses.BaseAppCompatActivity;

public class WelcomeNewUserActivity extends BaseAppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome_new_user);

        Button startBtn = findViewById(R.id.welcomeStartButton);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go to wallet in demo mode.
                Intent intent = new Intent(WelcomeNewUserActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

    }
}
