package zapsolutions.zap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.biometric.BiometricPrompt;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.io.BaseEncoding;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import at.favre.lib.armadillo.Armadillo;
import at.favre.lib.armadillo.PBKDF2KeyStretcher;
import zapsolutions.zap.baseClasses.App;
import zapsolutions.zap.baseClasses.BaseAppCompatActivity;
import zapsolutions.zap.connection.manageWalletConfigs.WalletConfigsManager;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.RefConstants;
import zapsolutions.zap.util.ScrambledNumpad;
import zapsolutions.zap.util.TimeOutUtil;
import zapsolutions.zap.util.UtilFunctions;


public class PinEntryActivity extends BaseAppCompatActivity {

    private int mPinLength = 0;

    private ImageButton mBtnPinConfirm;
    private ImageButton mBtnPinBack;
    private ImageButton mBtnBiometrics;
    private ImageView[] mPinHints = new ImageView[10];
    private Button[] mBtnNumpad = new Button[10];

    private BiometricPrompt mBiometricPrompt;
    private BiometricPrompt.PromptInfo mPromptInfo;

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

        mPinLength = PrefsUtil.getPrefs().getInt(PrefsUtil.PIN_LENGTH, RefConstants.PIN_MIN_LENGTH);

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
        mBtnBiometrics = findViewById(R.id.pinBiometrics);


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

        // ToDo: Remove if nobody has the old version installed.
        int ver = PrefsUtil.getPrefs().getInt(PrefsUtil.SETTINGS_VER, 0);

        // Make biometrics Button visible if enabled.
        if (PrefsUtil.isBiometricEnabled() && ver >= 16) {
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

                PrefsUtil.edit().putBoolean(PrefsUtil.BIOMETRICS_PREFERRED, true).apply();

                TimeOutUtil.getInstance().restartTimer();

                PrefsUtil.edit().putInt("numPINFails", 0).apply();

                Intent intent = new Intent(PinEntryActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);

                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                } else {
                    // This has to happen on the UI thread. Only this thread can change the recycler view.
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(PinEntryActivity.this, errString, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

        });


        // Call BiometricsPrompt on click on fingerprint symbol
        mBtnBiometrics.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBiometricPrompt.authenticate(mPromptInfo);
            }
        });


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

        // If the user closed and restarted the app he still has to wait until the PIN input delay is over.
        if (mNumFails >= RefConstants.PIN_MAX_FAILS) {

            long timeDiff = System.currentTimeMillis() - PrefsUtil.getPrefs().getLong("failedLoginTimestamp", 0L);

            if (timeDiff < RefConstants.PIN_DELAY_TIME * 1000) {

                for (Button btn : mBtnNumpad) {
                    btn.setEnabled(false);
                    btn.setAlpha(0.3f);
                }

                String message = getResources().getString(R.string.pin_entered_wrong_wait, String.valueOf((int) ((RefConstants.PIN_DELAY_TIME * 1000 - timeDiff) / 1000)));
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
                }, RefConstants.PIN_DELAY_TIME * 1000 - timeDiff);
            }
        }

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
        boolean correct = PrefsUtil.getPrefs().getString(PrefsUtil.PIN_HASH, "").equals(hashedInput);
        if (correct) {
            App.getAppContext().inMemoryPin = userEnteredPin;
            TimeOutUtil.getInstance().restartTimer();

            PrefsUtil.edit().putInt("numPINFails", 0)
                    .putBoolean(PrefsUtil.BIOMETRICS_PREFERRED, false).apply();



            // ToDo: Remove if nobody has the old version installed.
            int ver = PrefsUtil.getPrefs().getInt(PrefsUtil.SETTINGS_VER, 0);

            // Switch the way how connection data is stored
            if (ver < 16){

                boolean success = false;
                // Read the old connection settings
                App ctx = App.getAppContext();

                SharedPreferences prefsRemote = Armadillo.create(ctx, "prefs_remote")
                        .encryptionFingerprint(ctx)
                        .keyStretchingFunction(new PBKDF2KeyStretcher(RefConstants.NUM_HASH_ITERATIONS, null))
                        .password(ctx.inMemoryPin.toCharArray())
                        .contentKeyDigest(UtilFunctions.getZapsalt().getBytes())
                        .build();

                String connectionInfoCombined = prefsRemote.getString("remote_combined", "");
                String[] connectionInfo = connectionInfoCombined.split(";");


                // Save the old configuration in the new format
                String macaroon = connectionInfo[3];
                if (!(connectionInfo[2].equals("NO_CERT") || connectionInfo[2].equals("null"))) {
                    // No BTC pay, we now have to encode the macaroon in base16
                    byte[] macaroonBytes = BaseEncoding.base64Url().decode(connectionInfo[3]);
                    macaroon = BaseEncoding.base16().encode(macaroonBytes);
                }

                WalletConfigsManager walletConfigsManager = WalletConfigsManager.getInstance();
                try {
                    walletConfigsManager.saveWalletConfig(WalletConfigsManager.DEFAULT_WALLET_NAME,"remote", connectionInfo[0],
                            Integer.parseInt(connectionInfo[1]),connectionInfo[2], macaroon);
                    success = true;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (CertificateException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (UnrecoverableEntryException e) {
                    e.printStackTrace();
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (NoSuchProviderException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                }


                // Cleanup and set new settings version
                if (success) {
                    // Clear the old settings
                    prefsRemote.edit().clear().commit();
                    // Set new settings version
                    PrefsUtil.edit().putInt(PrefsUtil.SETTINGS_VER, RefConstants.CURRENT_SETTINGS_VER).apply();
                } else {
                    // Clear all
                    PrefsUtil.edit().clear().commit();
                    prefsRemote.edit().clear().commit();
                    // Set new settings version
                    PrefsUtil.edit().putInt(PrefsUtil.SETTINGS_VER, RefConstants.CURRENT_SETTINGS_VER).apply();
                }
            }
            // ToDo: End



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
                mVibrator.vibrate(RefConstants.VIBRATE_LONG);
            }

            // clear the user input
            mUserInput.setLength(0);
            displayUserInput();


            if (mNumFails >= RefConstants.PIN_MAX_FAILS) {
                for (Button btn : mBtnNumpad) {
                    btn.setEnabled(false);
                    btn.setAlpha(0.3f);
                }
                String message = getResources().getString(R.string.pin_entered_wrong_wait, String.valueOf(RefConstants.PIN_DELAY_TIME));
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                // Save timestamp. This way the delay can also be forced upon app restart.
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
                Toast.makeText(this, R.string.pin_entered_wrong, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Show biometric prompt if preferred
        if (PrefsUtil.isBiometricPreferred() && PrefsUtil.isBiometricEnabled()) {
            mBiometricPrompt.authenticate(mPromptInfo);
        }
    }
}
