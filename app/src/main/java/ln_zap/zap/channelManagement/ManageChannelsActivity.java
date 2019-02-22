package ln_zap.zap.channelManagement;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.View;

import ln_zap.zap.R;
import ln_zap.zap.baseClasses.BaseAppCompatActivity;

public class ManageChannelsActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_channels);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, R.string.coming_soon, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}
