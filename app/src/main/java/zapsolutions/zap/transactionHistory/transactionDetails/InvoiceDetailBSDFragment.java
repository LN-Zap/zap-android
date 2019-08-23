package zapsolutions.zap.transactionHistory.transactionDetails;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.github.lightningnetwork.lnd.lnrpc.Invoice;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import net.glxn.qrgen.android.QRCode;
import zapsolutions.zap.R;
import zapsolutions.zap.util.MonetaryUtil;
import zapsolutions.zap.util.Wallet;
import zapsolutions.zap.util.ZapLog;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InvoiceDetailBSDFragment extends BottomSheetDialogFragment {

    public static final String TAG = InvoiceDetailBSDFragment.class.getName();
    public static final String ARGS_TRANSACTION = "TRANSACTION";

    private TextView mTransactionDescription;
    private TextView mAmountLabel;
    private TextView mAmount;
    private TextView mMemoLabel;
    private TextView mMemo;
    private TextView mDateLabel;
    private TextView mDate;
    private TextView mExpiryLabel;
    private TextView mExpiry;
    private ImageView mQRCodeView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bsd_invoice_detail, container);

        mTransactionDescription = view.findViewById(R.id.transactionDescription);
        mAmountLabel = view.findViewById(R.id.amountLabel);
        mAmount = view.findViewById(R.id.amount);
        mMemoLabel = view.findViewById(R.id.memoLabel);
        mMemo = view.findViewById(R.id.memo);
        mDateLabel = view.findViewById(R.id.dateLabel);
        mDate = view.findViewById(R.id.date);
        mExpiryLabel = view.findViewById(R.id.expiryLabel);
        mExpiry = view.findViewById(R.id.expiry);
        mQRCodeView = view.findViewById(R.id.requestQRCode);

        ImageButton closeButton = view.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(view1 -> dismiss());

        if (getArguments() != null) {
            ByteString transactionString = (ByteString) getArguments().getSerializable(ARGS_TRANSACTION);
            try {
                bindInvoice(transactionString);
            } catch (InvalidProtocolBufferException exception) {
                ZapLog.debug(TAG, "Failed to parse channel.");
                dismiss();
            } catch (NullPointerException npException) {
                ZapLog.debug(TAG, "Failed to parse channel.");
                dismiss();
            }
        }

        return view;
    }


    private void bindInvoice(ByteString transactionString) throws InvalidProtocolBufferException {

        Invoice invoice = Invoice.parseFrom(transactionString);


        String amountLabel = getActivity().getResources().getString(R.string.amount) + ":";
        mAmountLabel.setText(amountLabel);
        String memoLabel = getActivity().getResources().getString(R.string.memo) + ":";
        mMemoLabel.setText(memoLabel);
        String dateLabel = getActivity().getResources().getString(R.string.date) + ":";
        mDateLabel.setText(dateLabel);
        String expiryLabel = getActivity().getResources().getString(R.string.expiry) + ":";
        mExpiryLabel.setText(expiryLabel);

        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, getActivity().getResources().getConfiguration().locale);
        String formattedDate = df.format(new Date(invoice.getCreationDate() * 1000L));

        DateFormat tf = DateFormat.getTimeInstance(DateFormat.MEDIUM, getActivity().getResources().getConfiguration().locale);
        String formattedTime = tf.format(new Date(invoice.getCreationDate() * 1000L));
        mDate.setText(formattedDate + ", " + formattedTime);

        if (invoice.getMemo().isEmpty()) {
            mMemo.setVisibility(View.GONE);
            mMemoLabel.setVisibility(View.GONE);
        } else {
            mMemo.setText(invoice.getMemo());
        }


        Long amt = invoice.getValue();
        Long amtPayed = invoice.getAmtPaidSat();

        if (amt.equals(0L)) {
            // if no specific value was requested
            if (!amtPayed.equals(0L)) {
                // The invoice has been payed
                bindPayedInvoice(invoice);
            } else {
                // The invoice has not been payed yet
                if (Wallet.getInstance().isInvoiceExpired(invoice)) {
                    bindExpiredInvoice(invoice);
                } else {
                    // The invoice has not yet expired
                    bindOpenInvoice(invoice);
                }
            }
        } else {
            // if a specific value was requested
            if (amtPayed.equals(amt)) {
                // The invoice has been payed
                bindPayedInvoice(invoice);
            } else {
                // The invoice has not been payed yet
                if (Wallet.getInstance().isInvoiceExpired(invoice)) {
                    // The invoice has expired
                    bindExpiredInvoice(invoice);
                } else {
                    // The invoice has not yet expired
                    bindOpenInvoice(invoice);
                }
            }
        }
    }

    private void bindOpenInvoice(Invoice invoice) {
        mTransactionDescription.setText(R.string.invoice_detail);

        mAmount.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(invoice.getValue()));

        // Generate "QR-Code"
        Bitmap bmpQRCode = QRCode
                .from(invoice.getPaymentRequest())
                .withSize(500, 500)
                .withErrorCorrection(ErrorCorrectionLevel.L)
                .bitmap();
        mQRCodeView.setImageBitmap(bmpQRCode);

        ScheduledExecutorService expiryUpdateSchedule =
                Executors.newSingleThreadScheduledExecutor();

        expiryUpdateSchedule.scheduleAtFixedRate
                (new Runnable() {
                    public void run() {
                        long timeLeft = (invoice.getCreationDate() + invoice.getExpiry()) - (System.currentTimeMillis() / 1000);
                        String expiryText = formattedDuration(timeLeft) + " " + getActivity().getResources().getString(R.string.remaining);

                        mExpiry.setText(expiryText);
                    }
                }, 0, 1, TimeUnit.SECONDS);
    }

    private void bindPayedInvoice(Invoice invoice) {
        mTransactionDescription.setText(R.string.transaction_detail);
        mExpiryLabel.setVisibility(View.GONE);
        mExpiry.setVisibility(View.GONE);
        mAmount.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(invoice.getAmtPaidSat()));
        mQRCodeView.setVisibility(View.GONE);
    }

    private void bindExpiredInvoice(Invoice invoice) {
        mTransactionDescription.setText(R.string.invoice_detail);
        mAmount.setText(MonetaryUtil.getInstance().getPrimaryDisplayAmountAndUnit(invoice.getValue()));
        mExpiry.setText(R.string.expired);
        mQRCodeView.setVisibility(View.GONE);
    }

    /**
     * Returns a nicely formatted duration.
     *
     * @param duration in seconds
     * @return
     */
    private String formattedDuration(long duration) {
        String formattedString = "";

        int hours = (int) duration / 3600;
        String hoursString = getActivity().getResources().getQuantityString(R.plurals.duration_hour, hours, hours);
        int days = (int) duration / 86400;
        String daysString = getActivity().getResources().getQuantityString(R.plurals.duration_day, days, days);
        int minutes = (int) (duration % 3600) / 60;
        String minutesUnit = getActivity().getResources().getString(R.string.duration_minute_short);
        String secondsUnit = getActivity().getResources().getString(R.string.duration_second_short);


        if (duration < 3600) {
            formattedString = String.format("%02d %s, %02d %s", (duration % 3600) / 60, minutesUnit, (duration % 60), secondsUnit);
        } else if (duration < 86400) {
            formattedString = hoursString;
        } else {
            formattedString = daysString;
        }

        return formattedString;
    }

    @Override
    public int getTheme() {
        return R.style.ZapBottomSheetDialogTheme;
    }
}
