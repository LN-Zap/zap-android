package zapsolutions.zap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.github.lightningnetwork.lnd.lnrpc.Invoice;
import com.google.common.net.UrlEscapers;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import net.glxn.qrgen.android.QRCode;

import zapsolutions.zap.baseClasses.BaseAppCompatActivity;
import zapsolutions.zap.interfaces.UserGuardianInterface;
import zapsolutions.zap.util.ClipBoardUtil;
import zapsolutions.zap.util.MonetaryUtil;
import zapsolutions.zap.util.PrefsUtil;
import zapsolutions.zap.util.InvoiceUtil;
import zapsolutions.zap.util.UserGuardian;
import zapsolutions.zap.util.Wallet;


public class GeneratedRequestActivity extends BaseAppCompatActivity implements UserGuardianInterface, Wallet.InvoiceSubscriptionListener {

    private static final String LOG_TAG = GeneratedRequestActivity.class.getName();

    private UserGuardian mUG;
    private String mDataToEncode;
    private boolean mOnChain;
    private String mAddress;
    private String mMemo;
    private String mAmount;
    private String mLnInvoice;
    private ConstraintLayout mClRequestView;
    private ConstraintLayout mClPaymentReceivedView;
    private TextView mFinishedAmount;
    private long mLnInvoiceAddIndex;

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
            mLnInvoiceAddIndex = extras.getLong("lnInvoiceAddIndex");
        }

        setContentView(R.layout.activity_generate_request);
        mUG = new UserGuardian(this, this);


        // Register listeners
        Wallet.getInstance().registerInvoiceSubscriptionListener(this);

        mClRequestView = findViewById(R.id.requestView);
        mClPaymentReceivedView = findViewById(R.id.paymentReceivedView);
        mFinishedAmount = findViewById(R.id.finishedText2);
        mClPaymentReceivedView.setVisibility(View.GONE);


        if (mOnChain) {
            // Show "On Chain" at top
            ImageView ivTypeIcon = findViewById(R.id.requestTypeIcon);
            ivTypeIcon.setImageResource(R.drawable.ic_onchain_black_24dp);
            TextView tvTypeText = findViewById(R.id.requestTypeText);
            tvTypeText.setText(R.string.onChain);

            // Set the icon for the request payed screen
            ImageView ivTypeIcon2 = findViewById(R.id.finishedPaymentTypeIcon);
            ivTypeIcon2.setImageResource(R.drawable.ic_onchain_black_24dp);


            // Generate on-chain request data to encode

            mDataToEncode = InvoiceUtil.generateBitcoinUri(mAddress);
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
            mDataToEncode = InvoiceUtil.generateLightningUri(mLnInvoice);
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
                if (PrefsUtil.preventScreenRecording()) {
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
                if (PrefsUtil.preventScreenRecording()) {
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

        // Action when clicked on "ok" Button
        Button btnOk = findViewById(R.id.okButton);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
                ClipBoardUtil.copyToClipboard(getApplicationContext(), "Address", mDataToEncode);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unregister listeners
        Wallet.getInstance().unregisterInvoiceSubscriptionListener(this);
    }

    @Override
    public void onNewInvoiceAdded(Invoice invoice) {

    }

    @Override
    public void onExistingInvoiceUpdated(Invoice invoice) {

        // This has to happen on the UI thread. Only this thread can change the UI.
        runOnUiThread(new Runnable() {
            public void run() {
                // Check if the invoice was payed
                if (Wallet.getInstance().isInvoicePayed(invoice)) {
                    // The updated invoice is payed, now check if it is the invoice whe currently have opened.
                    if (invoice.getAddIndex() == mLnInvoiceAddIndex) {

                        // It was payed, show success screen

                        mFinishedAmount.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(invoice.getAmtPaidSat()));
                        mClPaymentReceivedView.setVisibility(View.VISIBLE);
                        mClRequestView.setVisibility(View.GONE);

                    }
                }
            }
        });

    }
}
