package zapsolutions.zap.customView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import zapsolutions.zap.R;

public class BSDResultView extends ConstraintLayout {

    private TextView mHeading;
    private ImageView mTypeIcon;
    private TextView mDetails;
    private LinearLayout mResultContent;
    private OnOkListener mOnOkListener;


    public BSDResultView(Context context) {
        super(context);
        init();
    }

    public BSDResultView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BSDResultView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View view = inflate(getContext(), R.layout.bsd_result, this);

        mHeading = view.findViewById(R.id.resultHeading);
        mTypeIcon = view.findViewById(R.id.resultTypeIcon);
        mDetails = view.findViewById(R.id.resultDetails);
        mResultContent = view.findViewById(R.id.resultContent);

        // Ok Button
        Button okButton = findViewById(R.id.okButton);
        okButton.setOnClickListener(view1 -> {
            if (mOnOkListener != null)
                mOnOkListener.onOk();
        });
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (mResultContent == null) {
            super.addView(child, index, params);
        } else {
            //Forward these calls to the content view
            mResultContent.addView(child, index, params);
        }
    }

    public void setHeading(String text, boolean success) {
        mHeading.setText(text);
        if (success) {
            mHeading.setTextColor(getResources().getColor(R.color.superGreen));
        } else {
            mHeading.setTextColor(getResources().getColor(R.color.superRed));
        }
    }

    public void setHeading(int resID, boolean success) {
        mHeading.setText(resID);
        if (success) {
            mHeading.setTextColor(getResources().getColor(R.color.superGreen));
        } else {
            mHeading.setTextColor(getResources().getColor(R.color.superRed));
        }
    }

    public void setTypeIcon(int resID) {
        mTypeIcon.setImageResource(resID);
    }

    public void setTypeIconVisibility(boolean visible) {
        mTypeIcon.setVisibility(visible ? VISIBLE : GONE);
    }

    public void setDetailsText(int resID) {
        mDetails.setText(resID);
    }

    public void setDetailsText(String text) {
        mDetails.setText(text);
    }

    public void setDetailsVisibility(boolean visible) {
        mDetails.setVisibility(visible ? VISIBLE : GONE);
    }

    public void setOnOkListener(OnOkListener listener) {
        mOnOkListener = listener;
    }

    public interface OnOkListener {
        void onOk();
    }
}
