package zapsolutions.zap.customView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.transition.TransitionManager;
import com.google.android.material.tabs.TabLayout;
import zapsolutions.zap.R;
import zapsolutions.zap.util.OnSingleClickListener;
import zapsolutions.zap.util.PrefsUtil;

public class OnChainFeeView extends ConstraintLayout {

    public interface FeeTierChangedListener {
        void onFeeTierChanged(OnChainFeeTier onChainFeeTier);
    }

    public enum OnChainFeeTier {
        FAST,
        MEDIUM,
        SLOW;

        public int getTitle() {
            switch (this) {
                case FAST: return R.string.fee_tier_fast_title;
                case MEDIUM: return R.string.fee_tier_medium_title;
                case SLOW: return R.string.fee_tier_slow_title;
                default: return R.string.fee_tier_fast_title;
            }
        }

        public int getDescription() {
            switch (this) {
                case FAST: return R.string.fee_tier_fast_description;
                case MEDIUM: return R.string.fee_tier_medium_description;
                case SLOW: return R.string.fee_tier_slow_description;
                default: return R.string.fee_tier_fast_description;
            }
        }

        /**
         * In the future a user should be able to set
         * those values from the settings.
         */
        public int getConfirmationBlockTarget() {
            switch (this) {
                case FAST: return 1 ; // 10 Minutes
                case MEDIUM: return 6 * 6; // 6 Hours
                case SLOW: return 6 * 24; // 24 Hours
                default: return 1 ; // 10 Minutes
            }
        }

        public static OnChainFeeTier parseFromString(String enumAsString) {
            try {
                return valueOf(enumAsString);
            } catch (Exception ex) {
                return FAST;
            }
        }
    }

    private TextView mTvSendFeeAmount;
    private TextView mTvSendFeeSpeed;
    private TabLayout mTabLayoutSendFeeSpeed;
    private TextView mTvSendFeeDuration;
    private ImageView mFeeArrowUnitImage;
    private ConstraintLayout mClSendFeeAmountLayout;
    private ConstraintLayout mClSendFeeDurationLayout;
    private FeeTierChangedListener mFeeTierChangedListener;
    private OnChainFeeView.OnChainFeeTier mOnChainFeeTier;

    public OnChainFeeView(Context context) {
        super(context);
        init();
    }

    public OnChainFeeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OnChainFeeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View view = inflate(getContext(), R.layout.view_onchain_fee, this);

        mTvSendFeeAmount = view.findViewById(R.id.sendFeeOnChainAmount);
        mTvSendFeeSpeed = view.findViewById(R.id.sendFeeSpeed);
        mTabLayoutSendFeeSpeed = view.findViewById(R.id.feeSpeedTabLayout);
        mTvSendFeeDuration = view.findViewById(R.id.feeDurationText);
        mClSendFeeAmountLayout = view.findViewById(R.id.sendFeeOnChainAmountLayout);
        mFeeArrowUnitImage = view.findViewById(R.id.feeArrowUnitImage);

        mClSendFeeDurationLayout = view.findViewById(R.id.feeDurationLayout);

        // Set tier from shared preferences
        setFeeTier(OnChainFeeTier.parseFromString(PrefsUtil.getOnChainFeeTier()));
        mTabLayoutSendFeeSpeed.getTabAt( mOnChainFeeTier.ordinal()).select();

        // Toggle tier settings view on amount click
        mClSendFeeAmountLayout.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                boolean isFeeDurationVisible = mClSendFeeDurationLayout.getVisibility() == View.VISIBLE;
                toggleFeeTierView(isFeeDurationVisible);
            }
        });

        // Set initial block target time
        setBlockTargetTime(mOnChainFeeTier.getConfirmationBlockTarget());

        // Listen for tier change by user
        mTabLayoutSendFeeSpeed.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
              if(tab.getText() != null) {
                  if (tab.getText().equals(getResources().getString(OnChainFeeTier.SLOW.getTitle()))) {
                      setFeeTier(OnChainFeeTier.SLOW);
                  } else if (tab.getText().equals(getResources().getString(OnChainFeeTier.MEDIUM.getTitle()))) {
                      setFeeTier(OnChainFeeTier.MEDIUM);
                  } else if (tab.getText().equals(getResources().getString(OnChainFeeTier.FAST.getTitle()))) {
                      setFeeTier(OnChainFeeTier.FAST);
                  }
              }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public OnChainFeeTier getFeeTier() {
        return mOnChainFeeTier;
    }

    public void setFeeTierChangedListener(FeeTierChangedListener feeTierChangedListener) {
        mFeeTierChangedListener = feeTierChangedListener;
    }

    public void onFeeSuccess(String amount) {
        mTvSendFeeAmount.setText(amount);
    }

    public void onFeeFailure() {
        mTvSendFeeAmount.setText(R.string.fee_not_available);
    }

    /**
     * Set current fee tier and notify listeners
     */
    private void setFeeTier(OnChainFeeTier feeTier) {
        mOnChainFeeTier = feeTier;
        mTvSendFeeSpeed.setText(feeTier.getDescription());
        setBlockTargetTime(feeTier.getConfirmationBlockTarget());

        // Notify listener about changed tier
        if (mFeeTierChangedListener != null) {
            mFeeTierChangedListener.onFeeTierChanged(feeTier);
        }

        // Update choice to shared preferences
        PrefsUtil.edit().putString(PrefsUtil.ON_CHAIN_FEE_TIER,feeTier.name()).apply();
    }

    /**
     * Show or hide tabs to choose fee tier
     */
    private void toggleFeeTierView(boolean hide) {
        TransitionManager.beginDelayedTransition((ViewGroup) getRootView());
        mFeeArrowUnitImage.setImageResource(hide ? R.drawable.ic_arrow_down_24dp : R.drawable.ic_arrow_up_24dp);
        mClSendFeeDurationLayout.setVisibility(hide ? View.GONE : View.VISIBLE);
    }

    /**
     * Show estimated time of settlement
     */
    private void setBlockTargetTime(int blockTarget) {
        int minutes = blockTarget * 10;

        if(minutes < 60) {
            String quantityString = getResources().getQuantityString(R.plurals.duration_minute, minutes);
            mTvSendFeeDuration.setText(getContext().getString(R.string.fee_estimated_duration,minutes, quantityString));
        } else if( minutes < 60 * 24) {
            int hours = minutes / 60;
            String quantityString = getResources().getQuantityString(R.plurals.duration_hour, hours);
            mTvSendFeeDuration.setText(getContext().getString(R.string.fee_estimated_duration, hours, quantityString));
        } else {
            int days = minutes / 60 / 24;
            String quantityString = getResources().getQuantityString(R.plurals.duration_day, days);
            mTvSendFeeDuration.setText(getContext().getString(R.string.fee_estimated_duration, days, quantityString));
        }
    }
}
