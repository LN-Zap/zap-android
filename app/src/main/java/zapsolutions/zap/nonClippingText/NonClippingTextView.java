package zapsolutions.zap.nonClippingText;

import android.content.Context;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * This class is used to display text with italic formatting without clipping at the end.
 */
public class NonClippingTextView extends AppCompatTextView {


    public NonClippingTextView(Context context) {
        super(context);
    }

    public NonClippingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NonClippingTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int measuredWidth = getMeasuredWidth();
        final int extendAmount = (int) (getTextSize() / 10);
        final int newWidth = measuredWidth + extendAmount;

        setMeasuredDimension(newWidth, getMeasuredHeight());
    }
}
