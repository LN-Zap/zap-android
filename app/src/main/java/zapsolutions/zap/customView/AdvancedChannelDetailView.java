package zapsolutions.zap.customView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.transition.TransitionManager;

import zapsolutions.zap.R;
import zapsolutions.zap.util.OnSingleClickListener;

public class AdvancedChannelDetailView extends ConstraintLayout {

    private TextView mTvDetailLabel;
    private TextView mTvDetailValue;
    private TextView mTvDetailExplanation;
    private ImageView mExpandArrowImage;
    private View mVBasicDetails;
    private ClickableConstraintLayoutGroup mGroupExpandedContent;


    public AdvancedChannelDetailView(Context context) {
        super(context);
        init();
    }

    public AdvancedChannelDetailView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AdvancedChannelDetailView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View view = inflate(getContext(), R.layout.view_advanced_channel_detail, this);

        mTvDetailLabel = view.findViewById(R.id.detailLabel);
        mTvDetailValue = view.findViewById(R.id.detailValue);
        mTvDetailExplanation = view.findViewById(R.id.detailExplanation);
        mExpandArrowImage = view.findViewById(R.id.feeArrowUnitImage);

        mVBasicDetails = view.findViewById(R.id.basicDetails);
        mGroupExpandedContent = view.findViewById(R.id.expandedContent);

        mVBasicDetails.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                boolean isExpanded = mGroupExpandedContent.getVisibility() == View.VISIBLE;
                toggleExpandState(isExpanded);
            }
        });

        mGroupExpandedContent.setOnAllClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                boolean isExpanded = mGroupExpandedContent.getVisibility() == View.VISIBLE;
                toggleExpandState(isExpanded);
            }
        });
    }

    public void setContent(int label, String value, int explanation) {
        mTvDetailLabel.setText(getContext().getResources().getString(label));
        mTvDetailValue.setText(value);
        mTvDetailExplanation.setText(getContext().getResources().getString(explanation));
    }

    public void setValue(String value) {
        mTvDetailValue.setText(value);
    }

    /**
     * Show or hide expanded content
     */
    private void toggleExpandState(boolean hide) {
        TransitionManager.beginDelayedTransition((ViewGroup) getRootView());
        mExpandArrowImage.setImageResource(hide ? R.drawable.ic_arrow_down_24dp : R.drawable.ic_arrow_up_24dp);
        mGroupExpandedContent.setVisibility(hide ? View.GONE : View.VISIBLE);
    }
}
