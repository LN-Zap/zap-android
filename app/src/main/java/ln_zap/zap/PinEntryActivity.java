package ln_zap.zap;

import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import androidx.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import ln_zap.zap.baseClasses.BaseActivity;
import ln_zap.zap.util.ScrambledNumpad;


public class PinEntryActivity extends BaseActivity {

    private static final int MIN_PIN_LENGTH = 5;
    private static final int MAX_PIN_LENGTH = 8;

    private Button mBtnNumpad1;
    private Button mBtnNumpad2;
    private Button mBtnNumpad3;
    private Button mBtnNumpad4;
    private Button mBtnNumpad5;
    private Button mBtnNumpad6;
    private Button mBtnNumpad7;
    private Button mBtnNumpad8;
    private Button mBtnNumpad9;
    private Button mBtnNumpad0;
    private ImageButton mBtnPinConfirm;
    private ImageButton mBtnPinBack;

    private TextView mTvPrompt;
    private TextView mTvUserInput;
    private ScrambledNumpad mNumpad;
    private StringBuilder mUserInput;
    private Vibrator mVibrator;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pin_input);


        mUserInput = new StringBuilder();
        mNumpad = new ScrambledNumpad();
        mTvUserInput = findViewById(R.id.pinUserInput);
        mTvUserInput.setText("");
        mTvPrompt = findViewById(R.id.pinPrompt);
        mTvPrompt.setText(R.string.pin_enter);

        mVibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);


        boolean scramble = PreferenceManager.getDefaultSharedPreferences(PinEntryActivity.this).getBoolean("scramblePin",true);


        // Define buttons

        mBtnNumpad1 = findViewById(R.id.pinNumpad1);
        mBtnNumpad1.setText(scramble ? Integer.toString(mNumpad.getNumpad().get(0).getValue()) : "1");
        mBtnNumpad2 = findViewById(R.id.pinNumpad2);
        mBtnNumpad2.setText(scramble ? Integer.toString(mNumpad.getNumpad().get(1).getValue()) : "2");
        mBtnNumpad3 = findViewById(R.id.pinNumpad3);
        mBtnNumpad3.setText(scramble ? Integer.toString(mNumpad.getNumpad().get(2).getValue()) : "3");
        mBtnNumpad4 = findViewById(R.id.pinNumpad4);
        mBtnNumpad4.setText(scramble ? Integer.toString(mNumpad.getNumpad().get(3).getValue()) : "4");
        mBtnNumpad5 = findViewById(R.id.pinNumpad5);
        mBtnNumpad5.setText(scramble ? Integer.toString(mNumpad.getNumpad().get(4).getValue()) : "5");
        mBtnNumpad6 = findViewById(R.id.pinNumpad6);
        mBtnNumpad6.setText(scramble ? Integer.toString(mNumpad.getNumpad().get(5).getValue()) : "6");
        mBtnNumpad7 = findViewById(R.id.pinNumpad7);
        mBtnNumpad7.setText(scramble ? Integer.toString(mNumpad.getNumpad().get(6).getValue()) : "7");
        mBtnNumpad8 = findViewById(R.id.pinNumpad8);
        mBtnNumpad8.setText(scramble ? Integer.toString(mNumpad.getNumpad().get(7).getValue()) : "8");
        mBtnNumpad9 = findViewById(R.id.pinNumpad9);
        mBtnNumpad9.setText(scramble ? Integer.toString(mNumpad.getNumpad().get(8).getValue()) : "9");
        mBtnNumpad0 = findViewById(R.id.pinNumpad0);
        mBtnNumpad0.setText(scramble ? Integer.toString(mNumpad.getNumpad().get(9).getValue()) : "0");
        mBtnPinConfirm = findViewById(R.id.pinConfirm);
        mBtnPinBack = findViewById(R.id.pinBack);

        // Set all layout element states to the current user input (empty right now)
        displayUserInput();



        mBtnPinConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // Placeholder. Replace this function later when the PIN functionality is really done.
                Intent intent = new Intent(PinEntryActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        mBtnPinBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mUserInput.toString().length() > 0) {
                    mUserInput.deleteCharAt(mUserInput.length() - 1);
                    if(PreferenceManager.getDefaultSharedPreferences(PinEntryActivity.this).getBoolean("hapticPin",true))    {
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
                    if(PreferenceManager.getDefaultSharedPreferences(PinEntryActivity.this).getBoolean("hapticPin",true))    {
                        mVibrator.vibrate(55);
                    }
                }
                displayUserInput();
                return false;
            }
        });

    }

    public void OnNumberPadClick(View view) {
        if(PreferenceManager.getDefaultSharedPreferences(PinEntryActivity.this).getBoolean("hapticPin",true))    {
            mVibrator.vibrate(55);
        }
        mUserInput.append(((Button) view).getText().toString());
        displayUserInput();
    }

    private void displayUserInput() {

        // Display the correct number of "*" as visual PIN representation
        mTvUserInput.setText("");

        for (int i = 0; i < mUserInput.toString().length(); i++) {
            mTvUserInput.append("*");
        }

        // Disable numpad if max PIN length reached
        if (mUserInput.toString().length() >= MAX_PIN_LENGTH){
            mBtnNumpad1.setEnabled(false);
            mBtnNumpad1.setAlpha(0.3f);
            mBtnNumpad2.setEnabled(false);
            mBtnNumpad2.setAlpha(0.3f);
            mBtnNumpad3.setEnabled(false);
            mBtnNumpad3.setAlpha(0.3f);
            mBtnNumpad4.setEnabled(false);
            mBtnNumpad4.setAlpha(0.3f);
            mBtnNumpad5.setEnabled(false);
            mBtnNumpad5.setAlpha(0.3f);
            mBtnNumpad6.setEnabled(false);
            mBtnNumpad6.setAlpha(0.3f);
            mBtnNumpad7.setEnabled(false);
            mBtnNumpad7.setAlpha(0.3f);
            mBtnNumpad8.setEnabled(false);
            mBtnNumpad8.setAlpha(0.3f);
            mBtnNumpad9.setEnabled(false);
            mBtnNumpad9.setAlpha(0.3f);
            mBtnNumpad0.setEnabled(false);
            mBtnNumpad0.setAlpha(0.3f);
        }
        else{
            mBtnNumpad1.setEnabled(true);
            mBtnNumpad1.setAlpha(1f);
            mBtnNumpad2.setEnabled(true);
            mBtnNumpad2.setAlpha(1f);
            mBtnNumpad3.setEnabled(true);
            mBtnNumpad3.setAlpha(1f);
            mBtnNumpad4.setEnabled(true);
            mBtnNumpad4.setAlpha(1f);
            mBtnNumpad5.setEnabled(true);
            mBtnNumpad5.setAlpha(1f);
            mBtnNumpad6.setEnabled(true);
            mBtnNumpad6.setAlpha(1f);
            mBtnNumpad7.setEnabled(true);
            mBtnNumpad7.setAlpha(1f);
            mBtnNumpad8.setEnabled(true);
            mBtnNumpad8.setAlpha(1f);
            mBtnNumpad9.setEnabled(true);
            mBtnNumpad9.setAlpha(1f);
            mBtnNumpad0.setEnabled(true);
            mBtnNumpad0.setAlpha(1f);
        }

        // Show confirm button only if the PIN has a valid length.
        if (mUserInput.toString().length() >= MIN_PIN_LENGTH && mUserInput.toString().length() <= MAX_PIN_LENGTH) {
            mBtnPinConfirm.setVisibility(View.VISIBLE);
        } else {
            mBtnPinConfirm.setVisibility(View.INVISIBLE);
        }

        // Disable back button if user input is empty.
        if (mUserInput.toString().length() > 0) {
            mBtnPinBack.setEnabled(true);
            mBtnPinBack.setAlpha(1f);
        } else {
            mBtnPinBack.setEnabled(false);
            mBtnPinBack.setAlpha(0.3f);
        }

    }


}
