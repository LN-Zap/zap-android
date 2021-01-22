package zapsolutions.zap.customView;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import zapsolutions.zap.R;

public class BSDProgressView extends ConstraintLayout {


    private ImageView mProgressTypeIcon;
    private ImageView mProgressResultIcon;
    private MotionLayout mMotionLayout;


    public BSDProgressView(Context context) {
        super(context);
        init();
    }

    public BSDProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BSDProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View view = inflate(getContext(), R.layout.view_bsd_progress, this);

        mProgressTypeIcon = view.findViewById(R.id.progressTypeIcon);
        mProgressResultIcon = view.findViewById(R.id.progressResultIcon);
        mMotionLayout = view.findViewById(R.id.progressMotionLayout);

    }

    public void startSpinning() {
        mMotionLayout.setTransition(R.id.startSpinningTransition);
        mMotionLayout.transitionToEnd();
    }

    public void spinningFinished(boolean success) {
        if (success) {
            mProgressResultIcon.setImageResource(R.drawable.ic_check_circle_black_60dp);
            mProgressResultIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.superGreen)));
        } else {
            mProgressResultIcon.setImageResource(R.drawable.ic_failed_circle_black_60dp);
            mProgressResultIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.superRed)));
        }
        mMotionLayout.setTransition(R.id.endSpinningTransition);
        mMotionLayout.transitionToEnd();
    }

    public void setProgressTypeIcon(int resID) {
        mProgressTypeIcon.setImageResource(resID);
    }

    public void setProgressTypeIconVisibility(boolean visible) {
        mProgressTypeIcon.setVisibility(visible ? VISIBLE : GONE);
    }
}
