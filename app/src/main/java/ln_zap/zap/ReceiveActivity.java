package ln_zap.zap;

import ln_zap.zap.BaseClasses.BaseAppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class ReceiveActivity extends BaseAppCompatActivity {

    private LinearLayout LightningTab;
    private LinearLayout OnChainTab;
    private boolean onChain = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);


        // Action when clicked on "Lightning"
        LightningTab = findViewById(R.id.receiveLightningButton);
        LightningTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LightningTab.setAlpha(1);
                OnChainTab.setAlpha(0.2f);
                onChain = false;
            }
        });


        // Action when clicked on "On Chain"
        OnChainTab = findViewById(R.id.receiveOnChainButton);
        OnChainTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LightningTab.setAlpha(0.2f);
                OnChainTab.setAlpha(1);
                onChain = true;
            }
        });


        // Action when clicked on "Generate Request"
        Button btnGenerateRequest = findViewById(R.id.receiveGenerateRequest);
        btnGenerateRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReceiveActivity.this, GenerateRequestActivity.class);
                intent.putExtra("onChain", onChain);
                startActivity(intent);
            }
        });

    }
}
