package zapsolutions.zap;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import zapsolutions.zap.baseClasses.BaseAppCompatActivity;
import zapsolutions.zap.customView.IdentitySwitchView;
import zapsolutions.zap.customView.UserAvatarView;
import zapsolutions.zap.util.ClipBoardUtil;
import zapsolutions.zap.util.HelpDialogUtil;
import zapsolutions.zap.util.UserGuardian;
import zapsolutions.zap.util.Wallet;

public class IdentityActivity extends BaseAppCompatActivity {

    private Button mBtnHint;
    private UserAvatarView mUserAvatarView;
    private BottomNavigationView mBottomButtons;
    private IdentitySwitchView mIdentitySwitchView;
    private TextView mTvPublicKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identity);

        mUserAvatarView = findViewById(R.id.userAvatarView);
        mIdentitySwitchView = findViewById(R.id.identityTypeSwitcher);
        mBottomButtons = findViewById(R.id.bottomButtons);
        mBtnHint = findViewById(R.id.hintButton);
        mTvPublicKey = findViewById(R.id.publicKey);

        mUserAvatarView.setupWithNodeUris(Wallet.getInstance().getNodeUris(), true);

        if (mUserAvatarView.hasTorAndPublicIdentity()) {
            mIdentitySwitchView.setVisibility(View.VISIBLE);
            mIdentitySwitchView.setIdentityTypeChangedListener(new IdentitySwitchView.IdentityTypeChangedListener() {
                @Override
                public void onIdentityTypeChanged(IdentitySwitchView.IdentityType identityType) {
                    switch (identityType) {
                        case TOR:
                            mUserAvatarView.showTorIdentity();
                            break;
                        case PUBLIC:
                            mUserAvatarView.showClearNetIdentity();
                            break;
                        default:
                            mUserAvatarView.showTorIdentity();
                    }
                }
            });
        } else {
            mIdentitySwitchView.setVisibility(View.GONE);
        }

        mUserAvatarView.setOnStateChangedListener(new UserAvatarView.OnStateChangedListener() {
            @Override
            public void onReveal() {
                mTvPublicKey.setVisibility(View.VISIBLE);
                mBtnHint.setVisibility(View.GONE);
            }

            @Override
            public void onHide() {
                mTvPublicKey.setVisibility(View.GONE);
                mBtnHint.setVisibility(View.VISIBLE);
            }
        });


        mBottomButtons.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                switch (id) {
                    case R.id.action_share:
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_TEXT, mUserAvatarView.getCurrentNodeIdentity().getAsString());
                        shareIntent.setType("text/plain");
                        String title = getResources().getString(R.string.shareDialogTitle);
                        startActivity(Intent.createChooser(shareIntent, title));
                        break;
                    case R.id.action_copy:
                        // Ask user to confirm risks about clipboard manipulation

                        new UserGuardian(IdentityActivity.this, () -> {
                            // Copy data to clipboard
                            ClipBoardUtil.copyToClipboard(getApplicationContext(), "NodePubKey", mUserAvatarView.getCurrentNodeIdentity().getAsString());
                        }).securityCopyToClipboard(mUserAvatarView.getCurrentNodeIdentity().getAsString(), UserGuardian.CLIPBOARD_DATA_TYPE_NODE_URI);
                        break;
                }
                // Return false as we actually don't want to select it.
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.help_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        if (id == R.id.helpButton) {
            HelpDialogUtil.showDialog(IdentityActivity.this, R.string.identity_explanation);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}