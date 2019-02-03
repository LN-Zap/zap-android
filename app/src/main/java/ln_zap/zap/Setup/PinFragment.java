package ln_zap.zap.Setup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import ln_zap.zap.R;
import ln_zap.zap.util.ScrambledNumpad;


public class PinFragment extends Fragment {

    private static final String LOG_TAG = "PIN Fragment";

    public static final int CREATE_MODE = 0;
    public static final int CONFIRM_MODE = 1;
    public static final int ENTER_MODE = 2;

    private static final String ARG_MODE = "pinMode";
    private static final String ARG_PROMPT = "promptString";

    private static final int MIN_PIN_LENGTH = 4;
    private static final int MAX_PIN_LENGTH = 10;

    private int mPinLength = 0;

    private ImageButton mBtnPinConfirm;
    private ImageButton mBtnPinBack;
    private ImageView[] mPinHints = new ImageView[10];
    private Button[] mBtnNumpad = new Button[10];

    private TextView mTvPrompt;
    private String mPromptString;
    private ScrambledNumpad mNumpad;
    private StringBuilder mUserInput;
    private Vibrator mVibrator;
    private int mMode;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param mode set the mode to either create, confirm or enter pin.
     * @param prompt Short text to describe what is happening.
     * @return A new instance of fragment PinFragment.
     */
    public static PinFragment newInstance(int mode, String prompt) {
        PinFragment fragment = new PinFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MODE, mode);
        args.putString(ARG_PROMPT, prompt);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMode = getArguments().getInt(ARG_MODE);
            mPromptString = getArguments().getString(ARG_PROMPT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.pin_input, container, false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mPinLength = prefs.getInt("pin_length",4);

        mUserInput = new StringBuilder();
        mNumpad = new ScrambledNumpad();
        mTvPrompt = view.findViewById(R.id.pinPrompt);
        mTvPrompt.setText(mPromptString);


        mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        boolean scramble = false;

        if (mMode == ENTER_MODE){
            scramble = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("scramblePin",true);
        }


        // Define buttons

        mBtnNumpad[0] = view.findViewById(R.id.pinNumpad1);
        mBtnNumpad[0].setText(scramble ? Integer.toString(mNumpad.getNumpad().get(0).getValue()) : "1");
        mBtnNumpad[1] = view.findViewById(R.id.pinNumpad2);
        mBtnNumpad[1].setText(scramble ? Integer.toString(mNumpad.getNumpad().get(1).getValue()) : "2");
        mBtnNumpad[2] = view.findViewById(R.id.pinNumpad3);
        mBtnNumpad[2].setText(scramble ? Integer.toString(mNumpad.getNumpad().get(2).getValue()) : "3");
        mBtnNumpad[3] = view.findViewById(R.id.pinNumpad4);
        mBtnNumpad[3].setText(scramble ? Integer.toString(mNumpad.getNumpad().get(3).getValue()) : "4");
        mBtnNumpad[4] = view.findViewById(R.id.pinNumpad5);
        mBtnNumpad[4].setText(scramble ? Integer.toString(mNumpad.getNumpad().get(4).getValue()) : "5");
        mBtnNumpad[5] = view.findViewById(R.id.pinNumpad6);
        mBtnNumpad[5].setText(scramble ? Integer.toString(mNumpad.getNumpad().get(5).getValue()) : "6");
        mBtnNumpad[6] = view.findViewById(R.id.pinNumpad7);
        mBtnNumpad[6].setText(scramble ? Integer.toString(mNumpad.getNumpad().get(6).getValue()) : "7");
        mBtnNumpad[7] = view.findViewById(R.id.pinNumpad8);
        mBtnNumpad[7].setText(scramble ? Integer.toString(mNumpad.getNumpad().get(7).getValue()) : "8");
        mBtnNumpad[8] = view.findViewById(R.id.pinNumpad9);
        mBtnNumpad[8].setText(scramble ? Integer.toString(mNumpad.getNumpad().get(8).getValue()) : "9");
        mBtnNumpad[9] = view.findViewById(R.id.pinNumpad0);
        mBtnNumpad[9].setText(scramble ? Integer.toString(mNumpad.getNumpad().get(9).getValue()) : "0");


        mBtnPinConfirm = view.findViewById(R.id.pinConfirm);
        mBtnPinBack = view.findViewById(R.id.pinBack);

        // Set OnClickListeners for numpad buttons
        for (Button btn : mBtnNumpad){
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("hapticPin",true))    {
                        mVibrator.vibrate(55);
                    }
                    mUserInput.append(((Button) v).getText().toString());
                    displayUserInput();

                    if (mUserInput.toString().length() == mPinLength && mMode != CREATE_MODE){
                        pinEntered();
                    }
                }
            });
        }


        // Get pin hints
        mPinHints[0] = view.findViewById(R.id.pinHint1);
        mPinHints[1] = view.findViewById(R.id.pinHint2);
        mPinHints[2] = view.findViewById(R.id.pinHint3);
        mPinHints[3] = view.findViewById(R.id.pinHint4);
        mPinHints[4] = view.findViewById(R.id.pinHint5);
        mPinHints[5] = view.findViewById(R.id.pinHint6);
        mPinHints[6] = view.findViewById(R.id.pinHint7);
        mPinHints[7] = view.findViewById(R.id.pinHint8);
        mPinHints[8] = view.findViewById(R.id.pinHint9);
        mPinHints[9] = view.findViewById(R.id.pinHint10);


        // Set all layout element states to the current user input (empty right now)
        displayUserInput();


        mBtnPinConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                    createPin();
            }
        });

        mBtnPinBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mUserInput.toString().length() > 0) {
                    mUserInput.deleteCharAt(mUserInput.length() - 1);
                    if(PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("hapticPin",true))    {
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
                    if(PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("hapticPin",true))    {
                        mVibrator.vibrate(55);
                    }
                }
                displayUserInput();
                return false;
            }
        });
        return view;
    }

    private void displayUserInput() {

        // Correctly display number of hints as visual PIN representation
        if(mMode == CREATE_MODE) {
            // Show used PIN hints as inactive
            for (int i = 0; i < mUserInput.toString().length(); i++) {
                mPinHints[i].setVisibility(View.VISIBLE);
            }
            // Hide not used PIN hints
            for (int i = mUserInput.toString().length(); i < mPinHints.length; i++) {
                if(i==0)
                    mPinHints[i].setVisibility(View.INVISIBLE);
                mPinHints[i].setVisibility(View.GONE);
            }
        }
        else{

            // Set used PIN hints active
            for (int i = 0; i < mUserInput.toString().length(); i++) {
                mPinHints[i].setColorFilter(ContextCompat.getColor(getActivity(), R.color.lightningOrange));
            }
            // Set unused PIN hints inactive
            for (int i = mUserInput.toString().length(); i < mPinLength; i++) {
                mPinHints[i].setColorFilter(ContextCompat.getColor(getActivity(), R.color.invisibleGray));
            }
            // Hide not used PIN hints
            for (int i = mPinLength; i < mPinHints.length; i++) {
                mPinHints[i].setVisibility(View.GONE);
            }
        }


        // Disable numpad if max PIN length reached
        if (mUserInput.toString().length() >= MAX_PIN_LENGTH){
            for (Button btn : mBtnNumpad){
                btn.setEnabled(false);
                btn.setAlpha(0.3f);
            }
        }
        else{
            for (Button btn : mBtnNumpad){
                btn.setEnabled(true);
                btn.setAlpha(1f);
            }
        }

        // Show confirm button when creating PIN
        if(mMode == CREATE_MODE) {
            // Show confirm button only if the PIN has a valid length.
            if (mUserInput.toString().length() >= MIN_PIN_LENGTH && mUserInput.toString().length() <= MAX_PIN_LENGTH) {
                mBtnPinConfirm.setVisibility(View.VISIBLE);
            } else {
                mBtnPinConfirm.setVisibility(View.INVISIBLE);
            }
        }
        else{
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

    public void pinEntered(){

        // Check if PIN was correct
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean correct = prefs.getString("pin_UNSECURE", "").equals(mUserInput.toString());
        if (correct) {
            // Go to next Step
            if (mMode == ENTER_MODE){
                ((SetupActivity) getActivity()).correctPinEntered();
            }
            else if (mMode == CONFIRM_MODE){
                ((SetupActivity) getActivity()).pinConfirmed();
            }
        }
        else{
            Toast.makeText(getActivity(), R.string.pin_entered_wrong, Toast.LENGTH_SHORT).show();

            final Animation animShake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
            View view = getActivity().findViewById(R.id.pinInputLayout);
            view.startAnimation(animShake);

            // clear the user input
            mUserInput.setLength(0);
            displayUserInput();
        }
    }

    public void createPin(){

        // save the created PIN in Shared preferences UNSECURE!!!
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("pin_UNSECURE", mUserInput.toString());
        editor.putInt("pin_length", mUserInput.toString().length());
        editor.putBoolean("isWalletSetup", true);
        editor.apply();

        // Go to next Step
        ((SetupActivity) getActivity()).pinCreated();
    }

}
