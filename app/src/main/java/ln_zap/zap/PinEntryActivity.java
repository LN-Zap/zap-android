package ln_zap.zap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import ln_zap.zap.util.ScrambledNumpad;


public class PinEntryActivity extends Activity {

    private static final int MIN_PIN_LENGTH = 5;
    private static final int MAX_PIN_LENGTH = 8;

    private Button btnNumpad1;
    private Button btnNumpad2;
    private Button btnNumpad3;
    private Button btnNumpad4;
    private Button btnNumpad5;
    private Button btnNumpad6;
    private Button btnNumpad7;
    private Button btnNumpad8;
    private Button btnNumpad9;
    private Button btnNumpad0;
    private ImageButton btnPinConfirm;
    private ImageButton btnPinBack;

    private TextView tvPrompt;
    private TextView tvUserInput;
    private ScrambledNumpad numpad;
    private StringBuilder userInput;
    private Vibrator vibrator;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pin_input);

         /*Secure against screenshots and automated screen recording. Keep in mind that this does not prevent popups and other
           dialogues to be secured as well. Luckily we do not have some of these right now. If there are added some later,
           extra security measures have to be considered.*/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        userInput = new StringBuilder();
        numpad = new ScrambledNumpad();
        tvUserInput = findViewById(R.id.pinUserInput);
        tvUserInput.setText("");
        tvPrompt = findViewById(R.id.pinPrompt);

        vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);


        boolean scramble = PreferenceManager.getDefaultSharedPreferences(PinEntryActivity.this).getBoolean("scramblePin",false);


        // Define buttons

        btnNumpad1 = findViewById(R.id.pinNumpad1);
        btnNumpad1.setText(scramble ? Integer.toString(numpad.getNumpad().get(0).getValue()) : "1");
        btnNumpad2 = findViewById(R.id.pinNumpad2);
        btnNumpad2.setText(scramble ? Integer.toString(numpad.getNumpad().get(1).getValue()) : "2");
        btnNumpad3 = findViewById(R.id.pinNumpad3);
        btnNumpad3.setText(scramble ? Integer.toString(numpad.getNumpad().get(2).getValue()) : "3");
        btnNumpad4 = findViewById(R.id.pinNumpad4);
        btnNumpad4.setText(scramble ? Integer.toString(numpad.getNumpad().get(3).getValue()) : "4");
        btnNumpad5 = findViewById(R.id.pinNumpad5);
        btnNumpad5.setText(scramble ? Integer.toString(numpad.getNumpad().get(4).getValue()) : "5");
        btnNumpad6 = findViewById(R.id.pinNumpad6);
        btnNumpad6.setText(scramble ? Integer.toString(numpad.getNumpad().get(5).getValue()) : "6");
        btnNumpad7 = findViewById(R.id.pinNumpad7);
        btnNumpad7.setText(scramble ? Integer.toString(numpad.getNumpad().get(6).getValue()) : "7");
        btnNumpad8 = findViewById(R.id.pinNumpad8);
        btnNumpad8.setText(scramble ? Integer.toString(numpad.getNumpad().get(7).getValue()) : "8");
        btnNumpad9 = findViewById(R.id.pinNumpad9);
        btnNumpad9.setText(scramble ? Integer.toString(numpad.getNumpad().get(8).getValue()) : "9");
        btnNumpad0 = findViewById(R.id.pinNumpad0);
        btnNumpad0.setText(scramble ? Integer.toString(numpad.getNumpad().get(9).getValue()) : "0");
        btnPinConfirm = findViewById(R.id.pinConfirm);
        btnPinBack = findViewById(R.id.pinBack);

        // Set all layout element states to the current user input (empty right now)
        displayUserInput();



        btnPinConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // Placeholder. Replace this function later when the PIN functionality is really done.
                Intent intent = new Intent(PinEntryActivity.this, MainActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        btnPinBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if (userInput.toString().length() > 0) {
                    userInput.deleteCharAt(userInput.length() - 1);
                    if(PreferenceManager.getDefaultSharedPreferences(PinEntryActivity.this).getBoolean("hapticPin",false) == true)    {
                        vibrator.vibrate(55);
                    }
                }
                displayUserInput();

            }
        });

        btnPinBack.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (userInput.toString().length() > 0) {
                    userInput.setLength(0);
                    if(PreferenceManager.getDefaultSharedPreferences(PinEntryActivity.this).getBoolean("hapticPin",false) == true)    {
                        vibrator.vibrate(55);
                    }
                }
                displayUserInput();
                return false;
            }
        });

    }

    public void OnNumberPadClick(View view) {
        if(PreferenceManager.getDefaultSharedPreferences(PinEntryActivity.this).getBoolean("hapticPin",false) == true)    {
            vibrator.vibrate(55);
        }
        userInput.append(((Button) view).getText().toString());
        displayUserInput();
    }

    private void displayUserInput() {

        // Display the correct number of "*" as visual PIN representation
        tvUserInput.setText("");

        for (int i = 0; i < userInput.toString().length(); i++) {
            tvUserInput.append("*");
        }

        // Disable numpad if max PIN length reached
        if (userInput.toString().length() >= MAX_PIN_LENGTH){
            btnNumpad1.setEnabled(false);
            btnNumpad1.setAlpha(0.3f);
            btnNumpad2.setEnabled(false);
            btnNumpad2.setAlpha(0.3f);
            btnNumpad3.setEnabled(false);
            btnNumpad3.setAlpha(0.3f);
            btnNumpad4.setEnabled(false);
            btnNumpad4.setAlpha(0.3f);
            btnNumpad5.setEnabled(false);
            btnNumpad5.setAlpha(0.3f);
            btnNumpad6.setEnabled(false);
            btnNumpad6.setAlpha(0.3f);
            btnNumpad7.setEnabled(false);
            btnNumpad7.setAlpha(0.3f);
            btnNumpad8.setEnabled(false);
            btnNumpad8.setAlpha(0.3f);
            btnNumpad9.setEnabled(false);
            btnNumpad9.setAlpha(0.3f);
            btnNumpad0.setEnabled(false);
            btnNumpad0.setAlpha(0.3f);
        }
        else{
            btnNumpad1.setEnabled(true);
            btnNumpad1.setAlpha(1f);
            btnNumpad2.setEnabled(true);
            btnNumpad2.setAlpha(1f);
            btnNumpad3.setEnabled(true);
            btnNumpad3.setAlpha(1f);
            btnNumpad4.setEnabled(true);
            btnNumpad4.setAlpha(1f);
            btnNumpad5.setEnabled(true);
            btnNumpad5.setAlpha(1f);
            btnNumpad6.setEnabled(true);
            btnNumpad6.setAlpha(1f);
            btnNumpad7.setEnabled(true);
            btnNumpad7.setAlpha(1f);
            btnNumpad8.setEnabled(true);
            btnNumpad8.setAlpha(1f);
            btnNumpad9.setEnabled(true);
            btnNumpad9.setAlpha(1f);
            btnNumpad0.setEnabled(true);
            btnNumpad0.setAlpha(1f);
        }

        // Show confirm button only if the PIN has a valid length.
        if (userInput.toString().length() >= MIN_PIN_LENGTH && userInput.toString().length() <= MAX_PIN_LENGTH) {
            btnPinConfirm.setVisibility(View.VISIBLE);
        } else {
            btnPinConfirm.setVisibility(View.INVISIBLE);
        }

        // Disable back button if user input is empty.
        if (userInput.toString().length() > 0) {
            btnPinBack.setEnabled(true);
            btnPinBack.setAlpha(1f);
        } else {
            btnPinBack.setEnabled(false);
            btnPinBack.setAlpha(0.3f);
        }

    }


}
