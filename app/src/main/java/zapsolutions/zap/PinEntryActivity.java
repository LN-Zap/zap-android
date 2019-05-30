package zapsolutions.zap;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;

import androidx.core.content.ContextCompat;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import zapsolutions.zap.baseClasses.App;
import zapsolutions.zap.baseClasses.BaseAppCompatActivity;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.ScrambledNumpad;
import zapsolutions.zap.util.TimeOutUtil;
import zapsolutions.zap.util.UtilFunctions;


public class PinEntryActivity extends BaseAppCompatActivity {

    public static final int WAIT_TIME = 10;

    private int mPinLength = 0;

    private ImageButton mBtnPinConfirm;
    private ImageButton mBtnPinBack;
    private ImageView[] mPinHints = new ImageView[10];
    private Button[] mBtnNumpad = new Button[10];

    private TextView mTvPrompt;
    private ScrambledNumpad mNumpad;
    private StringBuilder mUserInput;
    private Vibrator mVibrator;
    private int mNumFails;
    private boolean mScramble;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pin_input);

        mUserInput = new StringBuilder();
        mNumpad = new ScrambledNumpad();
        mTvPrompt = findViewById(R.id.pinPrompt);
        mTvPrompt.setText(R.string.pin_enter);

        mVibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        mNumFails = PrefsUtil.getPrefs().getInt("numPINFails", 0);

        mPinLength = PrefsUtil.getPrefs().getInt(PrefsUtil.pin_length, 4);

        mScramble = PrefsUtil.getPrefs().getBoolean("scramblePin", true);


        // Define buttons

        mBtnNumpad[0] = findViewById(R.id.pinNumpad1);
        mBtnNumpad[0].setText(mScramble ? Integer.toString(mNumpad.getNumpad().get(0).getValue()) : "1");
        mBtnNumpad[1] = findViewById(R.id.pinNumpad2);
        mBtnNumpad[1].setText(mScramble ? Integer.toString(mNumpad.getNumpad().get(1).getValue()) : "2");
        mBtnNumpad[2] = findViewById(R.id.pinNumpad3);
        mBtnNumpad[2].setText(mScramble ? Integer.toString(mNumpad.getNumpad().get(2).getValue()) : "3");
        mBtnNumpad[3] = findViewById(R.id.pinNumpad4);
        mBtnNumpad[3].setText(mScramble ? Integer.toString(mNumpad.getNumpad().get(3).getValue()) : "4");
        mBtnNumpad[4] = findViewById(R.id.pinNumpad5);
        mBtnNumpad[4].setText(mScramble ? Integer.toString(mNumpad.getNumpad().get(4).getValue()) : "5");
        mBtnNumpad[5] = findViewById(R.id.pinNumpad6);
        mBtnNumpad[5].setText(mScramble ? Integer.toString(mNumpad.getNumpad().get(5).getValue()) : "6");
        mBtnNumpad[6] = findViewById(R.id.pinNumpad7);
        mBtnNumpad[6].setText(mScramble ? Integer.toString(mNumpad.getNumpad().get(6).getValue()) : "7");
        mBtnNumpad[7] = findViewById(R.id.pinNumpad8);
        mBtnNumpad[7].setText(mScramble ? Integer.toString(mNumpad.getNumpad().get(7).getValue()) : "8");
        mBtnNumpad[8] = findViewById(R.id.pinNumpad9);
        mBtnNumpad[8].setText(mScramble ? Integer.toString(mNumpad.getNumpad().get(8).getValue()) : "9");
        mBtnNumpad[9] = findViewById(R.id.pinNumpad0);
        mBtnNumpad[9].setText(mScramble ? Integer.toString(mNumpad.getNumpad().get(9).getValue()) : "0");

        mBtnPinConfirm = findViewById(R.id.pinConfirm);
        mBtnPinBack = findViewById(R.id.pinBack);


        // Get pin hints
        mPinHints[0] = findViewById(R.id.pinHint1);
        mPinHints[1] = findViewById(R.id.pinHint2);
        mPinHints[2] = findViewById(R.id.pinHint3);
        mPinHints[3] = findViewById(R.id.pinHint4);
        mPinHints[4] = findViewById(R.id.pinHint5);
        mPinHints[5] = findViewById(R.id.pinHint6);
        mPinHints[6] = findViewById(R.id.pinHint7);
        mPinHints[7] = findViewById(R.id.pinHint8);
        mPinHints[8] = findViewById(R.id.pinHint9);
        mPinHints[9] = findViewById(R.id.pinHint10);


        // Set all layout element states to the current user input (empty right now)
        displayUserInput();


        mBtnPinBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mUserInput.toString().length() > 0) {
                    mUserInput.deleteCharAt(mUserInput.length() - 1);
                    if (PrefsUtil.getPrefs().getBoolean("hapticPin", true)) {
                        mVibrator.vibrate(55);
                    }
                }
                displayUserInput();

            }
        });

        mBtnPinBack.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mUserInput.toString().length() > 0) {
                    mUserInput.setLength(0);
                    if (PrefsUtil.getPrefs().getBoolean("hapticPin", true)) {
                        mVibrator.vibrate(55);
                    }
                }
                displayUserInput();
                return false;
            }
        });

    }

    public void OnNumberPadClick(View view) {
        if (PrefsUtil.getPrefs().getBoolean("hapticPin", true)) {
            mVibrator.vibrate(55);
        }
        mUserInput.append(((Button) view).getText().toString());
        displayUserInput();

        if (mUserInput.toString().length() == mPinLength) {
            // We want to start the pin check after UI has updated, otherwise it doesn't look good.
            Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    pinEntered();
                }
            });
        }
    }

    private void displayUserInput() {

        // Correctly display number of hints as visual PIN representation

        // Mark with highlight color
        for (int i = 0; i < mUserInput.toString().length(); i++) {
            mPinHints[i].setColorFilter(ContextCompat.getColor(this, R.color.white));
        }
        // Set missing
        for (int i = mUserInput.toString().length(); i < mPinLength; i++) {
            mPinHints[i].setColorFilter(ContextCompat.getColor(this, R.color.invisibleGray));
        }
        // Hide not used PIN hints
        for (int i = mPinLength; i < mPinHints.length; i++) {
            mPinHints[i].setVisibility(View.GONE);
        }

        // Hide confirm button
        mBtnPinConfirm.setVisibility(View.INVISIBLE);

        // Disable back button if user input is empty.
        if (mUserInput.toString().length() > 0) {
            mBtnPinBack.setEnabled(true);
            mBtnPinBack.setAlpha(1f);
        } else {
            mBtnPinBack.setEnabled(false);
            mBtnPinBack.setAlpha(0.3f);
        }

    }

    public void pinEntered() {
        // Check if PIN was correct
        String userEnteredPin = mUserInput.toString();
        String hashedInput = UtilFunctions.pinHash(userEnteredPin);
        boolean correct = PrefsUtil.getPrefs().getString(PrefsUtil.pin_hash, "").equals(hashedInput);
        if (correct) {
            App.getAppContext().inMemoryPin = userEnteredPin;
            TimeOutUtil.getInstance().restartTimer();

            PrefsUtil.edit().putInt("numPINFails", 0).apply();

            Intent intent = new Intent(PinEntryActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            mNumFails++;

            PrefsUtil.edit().putInt("numPINFails", mNumFails).apply();

            final Animation animShake = AnimationUtils.loadAnimation(this, R.anim.shake);
            View view = findViewById(R.id.pinInputLayout);
            view.startAnimation(animShake);

            if (PrefsUtil.getPrefs().getBoolean("hapticPin", true)) {
                mVibrator.vibrate(200);
            }

            // clear the user input
            mUserInput.setLength(0);
            displayUserInput();


            if (mNumFails > 2) {
                for (Button btn : mBtnNumpad) {
                    btn.setEnabled(false);
                    btn.setAlpha(0.3f);
                }
                String message = getResources().getString(R.string.pin_entered_wrong_wait, String.valueOf(WAIT_TIME));
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        for (Button btn : mBtnNumpad) {
                            btn.setEnabled(true);
                            btn.setAlpha(1f);
                        }
                    }
                }, WAIT_TIME * 1000);
            } else {
                Toast.makeText(this, R.string.pin_entered_wrong, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
