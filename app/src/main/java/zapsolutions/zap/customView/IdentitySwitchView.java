package zapsolutions.zap.customView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.tabs.TabLayout;

import zapsolutions.zap.R;

public class IdentitySwitchView extends ConstraintLayout {

    private TabLayout mTabLayoutSendFeeSpeed;
    private IdentityTypeChangedListener mIdentityTypeChangedListener;
    private IdentityType mIdentityType;

    public IdentitySwitchView(Context context) {
        super(context);
        init();
    }

    public IdentitySwitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public IdentitySwitchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View view = inflate(getContext(), R.layout.view_identity_switch, this);

        mTabLayoutSendFeeSpeed = view.findViewById(R.id.identitySwitchTabLayout);

        // Set tor as standard
        setIdentityType(IdentityType.TOR);
        mTabLayoutSendFeeSpeed.getTabAt(mIdentityType.ordinal()).select();


        // Listen for identity type change by user
        mTabLayoutSendFeeSpeed.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText() != null) {
                    if (tab.getText().equals(getResources().getString(IdentityType.TOR.getTitle()))) {
                        setIdentityType(IdentityType.TOR);
                    } else if (tab.getText().equals(getResources().getString(IdentityType.PUBLIC.getTitle()))) {
                        setIdentityType(IdentityType.PUBLIC);
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

    public IdentityType getIdentityType() {
        return mIdentityType;
    }

    /**
     * Set current identity type and notify listeners
     */
    private void setIdentityType(IdentityType identityType) {
        mIdentityType = identityType;

        // Notify listener about changed type
        if (mIdentityTypeChangedListener != null) {
            mIdentityTypeChangedListener.onIdentityTypeChanged(identityType);
        }
    }

    public void setIdentityTypeChangedListener(IdentityTypeChangedListener identityTypeChangedListener) {
        mIdentityTypeChangedListener = identityTypeChangedListener;
    }

    public enum IdentityType {
        TOR,
        PUBLIC;

        public static IdentityType parseFromString(String enumAsString) {
            try {
                return valueOf(enumAsString);
            } catch (Exception ex) {
                return TOR;
            }
        }

        public int getTitle() {
            switch (this) {
                case TOR:
                    return R.string.identity_switch_tor;
                case PUBLIC:
                    return R.string.identity_switch_public;
                default:
                    return R.string.identity_switch_tor;
            }
        }
    }

    public interface IdentityTypeChangedListener {
        void onIdentityTypeChanged(IdentityType identityType);
    }
}
