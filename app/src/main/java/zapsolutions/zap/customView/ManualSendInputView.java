package zapsolutions.zap.customView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.github.lightningnetwork.lnd.lnrpc.PayReq;

import java.net.URL;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import zapsolutions.zap.R;
import zapsolutions.zap.connection.RemoteConfiguration;
import zapsolutions.zap.lightning.LightningNodeUri;
import zapsolutions.zap.lnurl.channel.LnUrlChannelResponse;
import zapsolutions.zap.lnurl.channel.LnUrlHostedChannelResponse;
import zapsolutions.zap.lnurl.pay.LnUrlPayResponse;
import zapsolutions.zap.lnurl.withdraw.LnUrlWithdrawResponse;
import zapsolutions.zap.util.BitcoinStringAnalyzer;
import zapsolutions.zap.util.RefConstants;

public class ManualSendInputView extends ConstraintLayout {

    private Button mBtnContinue;
    private EditText mEditText;
    private ProgressBar mSpinner;
    private OnResultListener mListener;
    private CompositeDisposable mCompositeDisposable;
    private String mData;

    public ManualSendInputView(Context context) {
        super(context);
        init();
    }

    public ManualSendInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ManualSendInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        View view = inflate(getContext(), R.layout.view_manual_send_input, this);

        mEditText = view.findViewById(R.id.sendInput);
        mBtnContinue = view.findViewById(R.id.continueButton);
        mSpinner = view.findViewById(R.id.spinner);
    }

    public void setupView(CompositeDisposable cd) {
        mCompositeDisposable = cd;
        mBtnContinue.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mData = mEditText.getText().toString();
                mSpinner.setVisibility(VISIBLE);
                mBtnContinue.setVisibility(INVISIBLE);
                /* We are not allowed to access LNURL links twice.
                Therefore we first have to check if it is a LNURL and then hand over to the HomeActivity.
                Executing the rest twice doesn't harm anyone.
                 */
                if (BitcoinStringAnalyzer.isLnUrl(mData)) {
                    mListener.onValid(mData);
                }
                BitcoinStringAnalyzer.analyze(getContext(), mCompositeDisposable, mData, new BitcoinStringAnalyzer.OnDataDecodedListener() {
                    @Override
                    public void onValidLightningInvoice(PayReq paymentRequest, String invoice) {
                        mListener.onValid(mData);
                    }

                    @Override
                    public void onValidBitcoinInvoice(String address, long amount, String message, String lightningInvoice) {
                        mListener.onValid(mData);
                    }

                    @Override
                    public void onValidLnUrlWithdraw(LnUrlWithdrawResponse withdrawResponse) {
                        invalidInput();
                    }

                    @Override
                    public void onValidLnUrlChannel(LnUrlChannelResponse channelResponse) {
                        invalidInput();
                    }

                    @Override
                    public void onValidLnUrlHostedChannel(LnUrlHostedChannelResponse hostedChannelResponse) {
                        invalidInput();
                    }

                    @Override
                    public void onValidLnUrlPay(LnUrlPayResponse payResponse) {
                        mListener.onValid(mData);
                    }

                    @Override
                    public void onValidLnUrlAuth(URL url) {
                        invalidInput();
                    }

                    @Override
                    public void onValidInternetIdentifier(LnUrlPayResponse payResponse) {
                        mListener.onValid(mData);
                    }

                    @Override
                    public void onValidLndConnectString(RemoteConfiguration remoteConfiguration) {
                        invalidInput();
                    }

                    @Override
                    public void onValidBTCPayConnectData(RemoteConfiguration remoteConfiguration) {
                        invalidInput();
                    }

                    @Override
                    public void onValidNodeUri(LightningNodeUri nodeUri) {
                        mListener.onValid(mData);
                    }

                    @Override
                    public void onError(String error, int duration) {
                        errorReadingData(error, duration);
                    }

                    @Override
                    public void onNoReadableData() {
                        errorReadingData(getContext().getString(R.string.string_analyzer_unrecognized_data), RefConstants.ERROR_DURATION_SHORT);
                    }
                });
            }
        });
    }

    public void setOnResultListener(OnResultListener listener) {
        mListener = listener;
    }

    public interface OnResultListener {
        void onValid(String data);

        void onError(String error, int duration);
    }

    private void errorReadingData(String error, int duration) {
        mListener.onError(error, duration);
        mBtnContinue.setVisibility(VISIBLE);
        mSpinner.setVisibility(INVISIBLE);
    }

    private void invalidInput() {
        mListener.onError(getContext().getString(R.string.error_only_payment_data_allowed), RefConstants.ERROR_DURATION_MEDIUM);
        mBtnContinue.setVisibility(VISIBLE);
        mSpinner.setVisibility(INVISIBLE);
    }
}
