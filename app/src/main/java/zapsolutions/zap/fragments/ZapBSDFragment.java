package zapsolutions.zap.fragments;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import zapsolutions.zap.R;
import zapsolutions.zap.util.PrefsUtil;

public class ZapBSDFragment extends RxBSDFragment {

    BottomSheetBehavior<FrameLayout> mBottomSheetBehavior;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FrameLayout bottomSheet = getDialog().findViewById(R.id.design_bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        // We do not want the bottom sheet dialog to cover the whole screen
        mBottomSheetBehavior.setPeekHeight((int) (metrics.heightPixels * 0.9));

        // Set scroll box max height in relation to screen height
        // Only for layouts with ScrollableBottomSheet
        try {
            ConstraintLayout csLayout = bottomSheet.findViewById(R.id.scrollableBsdRoot);
            ConstraintSet csRoot = new ConstraintSet();
            csRoot.clone(csLayout);
            csRoot.constrainMaxHeight(R.id.content, (int) (metrics.heightPixels * 0.7));
            csRoot.applyTo(csLayout);
        } catch (Exception ignored) {
        }

        // Apply FLAG_SECURE to dialog to prevent screen recording
        if (PrefsUtil.isScreenRecordingPrevented()) {
            getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    @Override
    public int getTheme() {
        return R.style.ZapBottomSheetDialogTheme;
    }
}
