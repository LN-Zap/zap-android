package zapsolutions.zap.pin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import zapsolutions.zap.R;
import zapsolutions.zap.baseClasses.App;
import zapsolutions.zap.connection.manageWalletConfigs.Cryptography;
import zapsolutions.zap.util.BiometricUtil;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.RefConstants;
import zapsolutions.zap.util.ScrambledNumpad;
import zapsolutions.zap.util.UtilFunctions;


public class PinFragment extends Fragment {

    public static final int CREATE_MODE = 0;
    public static final int CONFIRM_MODE = 1;
    public static final int ENTER_MODE = 2;
    private static final String LOG_TAG = PinFragment.class.getName();
    private static final String ARG_MODE = "pinMode";
    private static final String ARG_PROMPT = "promptString";

    private int mPinLength = 0;

    private ImageButton mBtnPinConfirm;
    private Button mBtnPinRemove;
    private ImageButton mBtnPinBack;
    private ImageButton mBtnBiometrics;
    private ImageView[] mPinHints = new ImageView[10];
    private Button[] mBtnNumpad = new Button[10];

    private BiometricPrompt mBiometricPrompt;
    private BiometricPrompt.PromptInfo mPromptInfo;

    private TextView mTvPrompt;
    private String mPromptString;
    private ScrambledNumpad mNumpad;
    private StringBuilder mUserInput;
    private Vibrator mVibrator;
    private int mMode;
    private int mNumFails;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param mode   set the mode to either create, confirm or enter pin.
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

        mNumFails = PrefsUtil.getPrefs().getInt("numPINFails", 0);

        // Get PIN length
        String pinString = null;
        if (mMode == CONFIRM_MODE) {
            pinString = App.getAppContext().pinTemp;
        } else {
            pinString = App.getAppContext().inMemoryPin;
        }

        mPinLength = pinString != null ? pinString.length() : RefConstants.PIN_MIN_LENGTH;

        mUserInput = new StringBuilder();
        mNumpad = new ScrambledNumpad();
        mTvPrompt = view.findViewById(R.id.pinPrompt);
        mTvPrompt.setText(mPromptString);

        mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        // Do not scramble numpad when we want to create a PIN
        boolean scramble = false;

        // Only scramble if we are in enter mode
        if (mMode == ENTER_MODE) {
            scramble = PrefsUtil.getPrefs().getBoolean("scramblePin", true);
        }


        // Get numpad buttons and assign a number to each of them
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
        mBtnPinRemove = view.findViewById(R.id.pinRemove);
        mBtnPinBack = view.findViewById(R.id.pinBack);
        mBtnBiometrics = view.findViewById(R.id.pinBiometrics);

        // Get PIN hints
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


        // Make biometrics Button visible if supported.
        if (mMode == ENTER_MODE && PrefsUtil.isBiometricEnabled() && BiometricUtil.hardwareAvailable()) {
            mBtnBiometrics.setVisibility(View.VISIBLE);
        } else {
            mBtnBiometrics.setVisibility(View.GONE);
        }

        Executor executor = Executors.newSingleThreadExecutor();

        mPromptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getResources().getString(R.string.biometricPrompt_title))
                .setNegativeButtonText(getResources().getString(R.string.cancel))
                .build();


        mBiometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                // Go to next step
                if (mMode == ENTER_MODE) {

                    PrefsUtil.edit().putInt("numPINFails", 0)
                            .putBoolean(PrefsUtil.BIOMETRICS_PREFERRED, true).apply();

                    ((PinSetupActivity) getActivity()).correctPinEntered();
                }

            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);

                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                } else {
                    // This has to happen on the UI thread. Only this thread can change the recycler view.
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity(), errString, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

        });


        // Show biometric prompt if preferred
        if (mMode == ENTER_MODE && PrefsUtil.isBiometricEnabled() && BiometricUtil.hardwareAvailable()) {
            if (PrefsUtil.isBiometricPreferred()) {
                initBiometricPrompt();
            }
        }

        // Call BiometricsPrompt on click on fingerprint symbol
        mBtnBiometrics.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BiometricUtil.notSetup()) {
                    AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.biometricPrompt_title)
                            .setMessage(R.string.biometricNotSetup)
                            .setCancelable(true)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            });
                    Dialog dlg = adb.create();
                    // Apply FLAG_SECURE to dialog to prevent screen recording
                    if (PrefsUtil.preventScreenRecording()) {
                        dlg.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
                    }
                    dlg.show();
                } else {
                    mBiometricPrompt.authenticate(mPromptInfo);
                }
            }
        });


        // Set action for numpad buttons
        for (Button btn : mBtnNumpad) {
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // vibrate
                    if (PrefsUtil.getPrefs().getBoolean("hapticPin", true)) {
                        mVibrator.vibrate(RefConstants.VIBRATE_SHORT);
                    }
                    // Add input
                    mUserInput.append(((Button) v).getText().toString());
                    displayUserInput();

                    // Auto accept if PIN input length was reached
                    if (mUserInput.toString().length() == mPinLength && mMode != CREATE_MODE) {
                        Handler handler = new Handler();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                pinEntered();
                            }
                        });
                    }
                }
            });
        }

        // Set action on confirm button
        mBtnPinConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                createPin();
            }
        });

        mBtnPinRemove.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PrefsUtil.edit().remove(PrefsUtil.PIN_HASH).commit();
                try {
                    new Cryptography(getActivity()).removePinActiveKey();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                getActivity().finish();
            }
        });

        // Set action on back button (delete one digit)
        mBtnPinBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mUserInput.toString().length() > 0) {
                    mUserInput.deleteCharAt(mUserInput.length() - 1);
                    if (PrefsUtil.getPrefs().getBoolean("hapticPin", true)) {
                        mVibrator.vibrate(RefConstants.VIBRATE_SHORT);
                    }
                }
                displayUserInput();

            }
        });

        // set long click action on back button (delete all digits)
        mBtnPinBack.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mUserInput.toString().length() > 0) {
                    mUserInput.setLength(0);
                    if (PrefsUtil.getPrefs().getBoolean("hapticPin", true)) {
                        mVibrator.vibrate(RefConstants.VIBRATE_SHORT);
                    }
                }
                displayUserInput();
                return false;
            }
        });


        // If the user closed and restarted the activity he still has to wait until the PIN input delay is over.
        if (mNumFails >= RefConstants.PIN_MAX_FAILS) {

            long timeDiff = System.currentTimeMillis() - PrefsUtil.getPrefs().getLong("failedLoginTimestamp", 0L);

            if (timeDiff < RefConstants.PIN_DELAY_TIME * 1000) {

                for (Button btn : mBtnNumpad) {
                    btn.setEnabled(false);
                    btn.setAlpha(0.3f);
                }

                String message = getResources().getString(R.string.pin_entered_wrong_wait, String.valueOf((int) ((RefConstants.PIN_DELAY_TIME * 1000 - timeDiff) / 1000)));
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        for (Button btn : mBtnNumpad) {
                            btn.setEnabled(true);
                            btn.setAlpha(1f);
                        }
                    }
                }, RefConstants.PIN_DELAY_TIME * 1000 - timeDiff);
            }
        }


        return view;
    }

    private void displayUserInput() {
        // Correctly display number of hints as visual PIN representation
        if (mMode == CREATE_MODE) {
            // Show used PIN hints as inactive
            for (int i = 0; i < mUserInput.toString().length(); i++) {
                mPinHints[i].setVisibility(View.VISIBLE);
            }
            // Hide unused PIN hints
            for (int i = mUserInput.toString().length(); i < mPinHints.length; i++) {
                if (i == 0)
                    mPinHints[i].setVisibility(View.INVISIBLE);
                mPinHints[i].setVisibility(View.GONE);
            }
        } else {

            // Set entered PIN hints active
            for (int i = 0; i < mUserInput.toString().length(); i++) {
                mPinHints[i].setColorFilter(ContextCompat.getColor(getActivity(), R.color.white));
            }
            // Set not yet entered PIN hints inactive
            for (int i = mUserInput.toString().length(); i < mPinLength; i++) {
                mPinHints[i].setColorFilter(ContextCompat.getColor(getActivity(), R.color.invisibleGray));
            }
            // Hide unused PIN hints
            for (int i = mPinLength; i < mPinHints.length; i++) {
                mPinHints[i].setVisibility(View.GONE);
            }
        }


        // Disable numpad if max PIN length reached
        if (mUserInput.toString().length() >= RefConstants.PIN_MAX_LENGTH) {
            for (Button btn : mBtnNumpad) {
                btn.setEnabled(false);
                btn.setAlpha(0.3f);
            }
        } else {
            for (Button btn : mBtnNumpad) {
                btn.setEnabled(true);
                btn.setAlpha(1f);
            }
        }

        // Show confirm button when creating PIN
        if (mMode == CREATE_MODE) {
            // Show confirm button only if the PIN has a valid length.
            if (mUserInput.toString().length() >= RefConstants.PIN_MIN_LENGTH && mUserInput.toString().length() <= RefConstants.PIN_MAX_LENGTH) {
                mBtnPinConfirm.setVisibility(View.VISIBLE);
                mBtnPinRemove.setVisibility(View.INVISIBLE);
            } else {
                mBtnPinConfirm.setVisibility(View.INVISIBLE);
                if (PrefsUtil.isPinEnabled()) {
                    mBtnPinRemove.setVisibility(View.VISIBLE);
                } else {
                    mBtnPinRemove.setVisibility(View.INVISIBLE);
                }
            }
        } else {
            mBtnPinConfirm.setVisibility(View.INVISIBLE);
            mBtnPinRemove.setVisibility(View.INVISIBLE);
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

    public void pinEntered() {
        // Check if PIN was correct

        boolean correct;
        if (mMode == ENTER_MODE) {
            String userEnteredPin = mUserInput.toString();
            String hashedInput = UtilFunctions.pinHash(userEnteredPin);
            correct = PrefsUtil.getPrefs().getString(PrefsUtil.PIN_HASH, "").equals(hashedInput);
        } else if (mMode == CONFIRM_MODE) {
            correct = mUserInput.toString().equals(App.getAppContext().pinTemp);
        } else {
            correct = mUserInput.toString().equals(App.getAppContext().inMemoryPin);
        }

        if (correct) {
            // Go to next step
            if (mMode == ENTER_MODE) {

                PrefsUtil.edit().putInt("numPINFails", 0)
                        .putBoolean(PrefsUtil.BIOMETRICS_PREFERRED, false).apply();

                ((PinActivityInterface) getActivity()).correctPinEntered();
            } else if (mMode == CONFIRM_MODE) {
                ((PinSetupActivity) getActivity()).pinConfirmed(mUserInput.toString());
            }
        } else {
            if (mMode == ENTER_MODE) {
                mNumFails++;

                PrefsUtil.edit().putInt("numPINFails", mNumFails)
                        .putBoolean(PrefsUtil.BIOMETRICS_PREFERRED, false).apply();

                final Animation animShake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
                View view = getActivity().findViewById(R.id.pinInputLayout);
                view.startAnimation(animShake);

                if (PrefsUtil.getPrefs().getBoolean("hapticPin", true)) {
                    mVibrator.vibrate(200);
                }

                // Clear the user input
                mUserInput.setLength(0);
                displayUserInput();

                if (mNumFails >= RefConstants.PIN_MAX_FAILS) {
                    for (Button btn : mBtnNumpad) {
                        btn.setEnabled(false);
                        btn.setAlpha(0.3f);
                    }
                    String message = getResources().getString(R.string.pin_entered_wrong_wait, String.valueOf(RefConstants.PIN_DELAY_TIME));
                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();

                    // Save timestamp. This way the delay can also be forced upon restart of the activity.
                    PrefsUtil.edit().putLong("failedLoginTimestamp", System.currentTimeMillis()).apply();

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            for (Button btn : mBtnNumpad) {
                                btn.setEnabled(true);
                                btn.setAlpha(1f);
                            }
                        }
                    }, RefConstants.PIN_DELAY_TIME * 1000);
                } else {
                    // Show error
                    Toast.makeText(getActivity(), R.string.pin_entered_wrong, Toast.LENGTH_SHORT).show();
                }

            } else {
                // Show error
                Toast.makeText(getActivity(), R.string.pin_entered_wrong, Toast.LENGTH_SHORT).show();

                final Animation animShake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
                View view = getActivity().findViewById(R.id.pinInputLayout);
                view.startAnimation(animShake);

                if (PrefsUtil.getPrefs().getBoolean("hapticPin", true)) {
                    mVibrator.vibrate(RefConstants.VIBRATE_LONG);
                }

                // Clear the user input
                mUserInput.setLength(0);
                displayUserInput();
            }
        }
    }

    public void createPin() {
        // Go to next step
        ((PinSetupActivity) getActivity()).pinCreated(mUserInput.toString());
    }

    private void initBiometricPrompt() {
        mBiometricPrompt.cancelAuthentication();
        mBiometricPrompt.authenticate(mPromptInfo);
    }

}
