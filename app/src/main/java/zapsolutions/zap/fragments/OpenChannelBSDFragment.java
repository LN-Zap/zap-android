package zapsolutions.zap.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import zapsolutions.zap.R;
import zapsolutions.zap.lightning.LightningNodeUri;
import zapsolutions.zap.util.PrefsUtil;

public class OpenChannelBSDFragment extends BottomSheetDialogFragment {

    public static final String TAG = OpenChannelBSDFragment.class.getName();
    public static final String ARGS_NODE_URI = "NODE_URI";

    private View mNumpad;
    private EditText mEtAmount;
    private TextView mTvNodeAlias;
    private LightningNodeUri mLightningNodeUri;
    private Button mOpenChannelButton;
    private View mLocalAmountsInputView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bsd_open_channel, container);

        // Apply FLAG_SECURE to dialog to prevent screen recording
        if (PrefsUtil.preventScreenRecording()) {
            getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        mNumpad = view.findViewById(R.id.Numpad);
        mEtAmount = view.findViewById(R.id.localAmount);
        mTvNodeAlias = view.findViewById(R.id.nodeAliasText);
        mLocalAmountsInputView = view.findViewById(R.id.localAmountInputsView);
        mOpenChannelButton = view.findViewById(R.id.openChannelButton);

        // temporary coming soon
        mOpenChannelButton.setText(R.string.coming_soon);
        mLocalAmountsInputView.setEnabled(false);

        mNumpad.setVisibility(View.VISIBLE);
        mOpenChannelButton.setEnabled(false);
        mOpenChannelButton.setTextColor(getResources().getColor(R.color.gray));

        if (getArguments() != null) {
            mLightningNodeUri = (LightningNodeUri) getArguments().getSerializable(ARGS_NODE_URI);
            mTvNodeAlias.setText(mLightningNodeUri.getHost() + " (" + mLightningNodeUri.getPubKey() + ")");
        }

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            // We have to call this delayed, as otherwise it will still bring up the softKeyboard
            mEtAmount.requestFocus();
        }, 200);

        // deactivate default keyboard for number input.
        mEtAmount.setShowSoftInputOnFocus(false);

        // Action when clicked on "x" (close) button
        ImageButton btnCloseBSD = view.findViewById(R.id.closeButton);
        btnCloseBSD.setOnClickListener(v -> dismiss());

        // redraw layout when height is available, otherwise it won't be shown completely
        getDialog().setOnShowListener(dialog -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = d.findViewById(R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                CoordinatorLayout coordinatorLayout = (CoordinatorLayout) bottomSheet.getParent();
                BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
                bottomSheetBehavior.setPeekHeight(bottomSheet.getHeight());
                coordinatorLayout.getParent().requestLayout();
            }
        });

        return view;
    }

    @Override
    public int getTheme() {
        return R.style.ZapBottomSheetDialogTheme;
    }
}
