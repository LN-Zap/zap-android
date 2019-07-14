package zapsolutions.zap.walletManagement;

import android.os.Bundle;
import android.view.View;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.BaseAppCompatActivity;

public class ManageWalletsActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_wallets);

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
