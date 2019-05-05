package ln_zap.zap;

import androidx.preference.PreferenceManager;

import ln_zap.zap.baseClasses.BaseAppCompatActivity;
import ln_zap.zap.interfaces.UserGuardianInterface;

import ln_zap.zap.util.MonetaryUtil;
import ln_zap.zap.util.UserGuardian;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ClipboardManager;

import com.google.common.net.UrlEscapers;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import net.glxn.qrgen.android.QRCode;


public class GeneratedRequestActivity extends BaseAppCompatActivity implements UserGuardianInterface {

    private UserGuardian mUG;
    private String mDataToEncode;
    private boolean mOnChain;
    private String mAddress;
    private String mMemo;
    private String mAmount;
    private String mLnInvoice;
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Receive data from last activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mOnChain = extras.getBoolean("onChain");
            mAddress = extras.getString("address");
            mAmount = extras.getString("amount");
            mMemo = extras.getString("memo");
            mLnInvoice = extras.getString("lnInvoice");
        }

        setContentView(R.layout.activity_generate_request);
        mUG = new UserGuardian(this, this);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(GeneratedRequestActivity.this);

        if (mOnChain) {
            // Show "On Chain" at top
            ImageView ivTypeIcon = findViewById(R.id.requestTypeIcon);
            ivTypeIcon.setImageResource(R.drawable.ic_onchain_black_24dp);
            TextView tvTypeText = findViewById(R.id.requestTypeText);
            tvTypeText.setText(R.string.onChain);
        }


        if (mOnChain) {

            // Generate on-chain request data to encode

            mDataToEncode = "bitcoin:" + mAddress;
            mMemo = UrlEscapers.urlPathSegmentEscaper().escape(mMemo);

            // Convert the value to the expected format for onChain invoices.
            mAmount = MonetaryUtil.getInstance().convertPrimaryToBitcoin(mAmount);

            // Append amount and memo to the invoice
            if (mAmount != null)
                if (!(mAmount.isEmpty() || mAmount.equals("0")))
                    mDataToEncode = appendParameter(mDataToEncode, "amount", mAmount);
            if (mMemo != null)
                if (!mMemo.isEmpty())
                    mDataToEncode = appendParameter(mDataToEncode, "message", mMemo);
        } else {
            // Generate lightning request data to encode
            mDataToEncode = "lightning:" + mLnInvoice;
        }


        // Generate "QR-Code"
        Bitmap bmpQRCode = QRCode
                .from(mDataToEncode)
                .withSize(750, 750)
                .withErrorCorrection(ErrorCorrectionLevel.L)
                .bitmap();
        ImageView ivQRCode = findViewById(R.id.requestQRCode);
        ivQRCode.setImageBitmap(bmpQRCode);

        // Action when long clicked on "QR-Code"
        ivQRCode.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder adb = new AlertDialog.Builder(GeneratedRequestActivity.this)
                        .setTitle(R.string.details)
                        .setMessage(mDataToEncode)
                        .setCancelable(true)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        });
                Dialog dlg = adb.create();
                // Apply FLAG_SECURE to dialog to prevent screen recording
                if (PreferenceManager.getDefaultSharedPreferences(GeneratedRequestActivity.this).getBoolean("preventScreenRecording", true)) {
                    dlg.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
                }
                dlg.show();
                return false;
            }
        });


        // Action when clicked on "share"
        View btnShare = findViewById(R.id.shareBtn);
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, mDataToEncode);
                shareIntent.setType("text/plain");
                String title = getResources().getString(R.string.shareDialogTitle);
                startActivity(Intent.createChooser(shareIntent, title));
            }
        });

        // Action when clicked on "details"
        Button btnDetails = findViewById(R.id.requestDetailsButton);
        btnDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder adb = new AlertDialog.Builder(GeneratedRequestActivity.this)
                        .setTitle(R.string.details)
                        .setMessage(mDataToEncode)
                        .setCancelable(true)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        });
                Dialog dlg = adb.create();
                // Apply FLAG_SECURE to dialog to prevent screen recording
                if (PreferenceManager.getDefaultSharedPreferences(GeneratedRequestActivity.this).getBoolean("preventScreenRecording", true)) {
                    dlg.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
                }
                dlg.show();
            }
        });

        // Action when clicked on "copy"
        View btnCopyLink = findViewById(R.id.copyBtn);
        btnCopyLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnChain) {
                    mUG.securityCopyToClipboard(mDataToEncode, 0);
                } else {
                    mUG.securityCopyToClipboard(mDataToEncode, 1);
                }
            }
        });

    }

    private String appendParameter(String base, String name, String value) {
        if (!base.contains("?"))
            return base + "?" + name + "=" + value;
        else
            return base + "&" + name + "=" + value;
    }

    @Override
    public void guardianDialogConfirmed(String DialogName) {
        switch (DialogName) {
            case UserGuardian.COPY_TO_CLIPBOARD:
                copyToClipboard();
                break;
        }
    }

    private void copyToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Address", mDataToEncode);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
    }
}
