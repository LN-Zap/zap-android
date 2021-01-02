package zapsolutions.zap.customView;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import androidx.constraintlayout.utils.widget.ImageFilterView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import net.glxn.qrgen.android.QRCode;

import zapsolutions.zap.R;
import zapsolutions.zap.lightning.LightningNodeUri;

public class UserAvatarView extends ConstraintLayout {

    private ImageView mIvQRCode;
    private ImageFilterView mIvUserAvatar;
    private LightningNodeUri[] mNodeUris;
    private OnStateChangedListener mListener;
    private int mCurrentUriId = 0;
    private boolean mIsQRCodeIncluded;

    public UserAvatarView(Context context) {
        super(context);
        init();
    }

    public UserAvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UserAvatarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        View view = inflate(getContext(), R.layout.user_avatar_view, this);

        mIvQRCode = findViewById(R.id.qrCode);
        mIvUserAvatar = findViewById(R.id.userAvatar);

        showAvatar();
    }

    public void setupWithNodeUri(LightningNodeUri nodeUri, boolean includeQRCode) {
        LightningNodeUri[] tempNodeUris = new LightningNodeUri[1];
        tempNodeUris[0] = nodeUri;

        setupWithNodeUris(tempNodeUris, includeQRCode);
    }

    public void setupWithNodeUris(LightningNodeUri[] nodeUris, boolean includeQRCode) {
        reset();
        mNodeUris = nodeUris;
        mIsQRCodeIncluded = includeQRCode;

        /*
        if (mIsQRCodeIncluded) {
            mIvQRCode.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAvatar();
                    ((MotionLayout) findViewById(R.id.userAvatarMotionLayout)).transitionToStart();
                    if (mListener != null) {
                        mListener.onHide();
                    }
                }
            });

            mIvUserAvatar.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showQRCode();
                    ((MotionLayout) findViewById(R.id.userAvatarMotionLayout)).transitionToEnd();
                    if (mListener != null) {
                        mListener.onReveal();
                    }
                }
            });
        }
        */

        showAvatar();
        showIdentity(true);
    }

    public void showIdentity(boolean tor) {
        if (mNodeUris != null) {
            if (mNodeUris.length > 1) {
                for (int i = 0; i < mNodeUris.length; i++) {
                    if (mNodeUris[i].getHost() != null) {
                        if (mNodeUris[i].isTorUri() == tor) {
                            showIdentity(i);
                            return;
                        }
                    }
                }
            }
            showIdentity(0);
        }
    }

    private void showIdentity(int id) {
        if (mNodeUris != null) {
            mCurrentUriId = Math.min(mNodeUris.length, id);

            if (mNodeUris[mCurrentUriId] != null) {
                if (mIsQRCodeIncluded) {
                    // Generate "QR-Code"
                    Bitmap bmpQRCode = QRCode
                            .from(mNodeUris[mCurrentUriId].getAsString())
                            .withSize(750, 750)
                            .withErrorCorrection(ErrorCorrectionLevel.L)
                            .bitmap();
                    mIvQRCode.setImageBitmap(bmpQRCode);
                }

                // Load user Avatar
                /*
                Glide.with(getContext())
                        .setDefaultRequestOptions(new RequestOptions().timeout(15000))
                        .load(UserAvatarUtil.getAvatarUrl(mNodeUris[mCurrentUriId].getPubKey()))
                        .placeholder(R.drawable.ic_person_24)
                        //.circleCrop()
                        .into(mIvUserAvatar);

                if (includeQRCode) {


                }
                 */
            }
        }
    }

    public void reset() {
        mNodeUris = null;
        mIsQRCodeIncluded = false;
        mCurrentUriId = 0;
        mIvUserAvatar.setOnClickListener(null);
        mIvQRCode.setOnClickListener(null);
        mIvUserAvatar.setImageResource(R.drawable.ic_person_24);
    }

    private void showQRCode() {
        mIvQRCode.setElevation(2);
        mIvUserAvatar.setElevation(1);
    }

    private void showAvatar() {
        mIvQRCode.setElevation(1);
        mIvUserAvatar.setElevation(2);
    }

    public void setOnStateChangedListener(OnStateChangedListener listener) {
        mListener = listener;
    }

    public interface OnStateChangedListener {
        void onReveal();

        void onHide();
    }

    public LightningNodeUri getCurrentNodeIdentity() {
        if (mNodeUris == null) {
            return null;
        } else {
            return mNodeUris[mCurrentUriId];
        }
    }

    public boolean isCurrentNodeIdentityTor() {
        if (mNodeUris == null) {
            return false;
        } else {
            if (mNodeUris[mCurrentUriId].getHost() != null) {
                return mNodeUris[mCurrentUriId].isTorUri();
            } else {
                return false;
            }
        }
    }

    public boolean hasTorAndPublicIdentity() {
        if (mNodeUris == null) {
            return false;
        }
        if (mNodeUris.length > 1) {
            boolean hasPublic = false;
            boolean hasTor = false;
            for (LightningNodeUri nodeUri : mNodeUris) {
                if (nodeUri.getHost() != null && nodeUri.isTorUri()) {
                    hasTor = true;
                } else if (nodeUri.getHost() == null || !nodeUri.isTorUri()) {
                    hasPublic = true;
                }
            }
            return hasPublic && hasTor;
        } else {
            return false;
        }
    }
}
