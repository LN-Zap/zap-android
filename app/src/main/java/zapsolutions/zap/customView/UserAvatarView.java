package zapsolutions.zap.customView;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.utils.widget.ImageFilterView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import net.glxn.qrgen.android.QRCode;

import zapsolutions.zap.R;

public class UserAvatarView extends ConstraintLayout {

    private ImageView mIvQRCode;
    private ImageFilterView mIvUserAvatar;
    private String mNodePubKey;
    private OnStateChangedListener mListener;

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

    public void setupWithNodePubKey(@NonNull String nodePubKey, boolean includeQRCode) {
        mNodePubKey = nodePubKey;
        showAvatar();

        if (mNodePubKey != null) {
            if (!mNodePubKey.isEmpty()) {
                if (includeQRCode) {
                    // Generate "QR-Code"
                    Bitmap bmpQRCode = QRCode
                            .from(mNodePubKey)
                            .withSize(750, 750)
                            .withErrorCorrection(ErrorCorrectionLevel.L)
                            .bitmap();
                    mIvQRCode.setImageBitmap(bmpQRCode);
                    /*
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

                     */
                }
                
                // Load user Avatar
                /*
                Glide.with(getContext())
                        .setDefaultRequestOptions(new RequestOptions().timeout(15000))
                        .load(UserAvatarUtil.getAvatarUrl(mNodePubKey))
                        .placeholder(R.drawable.ic_person_24)
                        //.circleCrop()
                        .into(mIvUserAvatar);

                if (includeQRCode) {

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
            }
        }
    }

    public void reset() {
        mNodePubKey = null;
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
}
