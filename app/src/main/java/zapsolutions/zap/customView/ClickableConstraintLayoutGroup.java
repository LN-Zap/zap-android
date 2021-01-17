package zapsolutions.zap.customView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.constraintlayout.widget.Group;

public class ClickableConstraintLayoutGroup extends Group {


    public ClickableConstraintLayoutGroup(Context context) {
        super(context);
    }

    public ClickableConstraintLayoutGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClickableConstraintLayoutGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnAllClickListener(View.OnClickListener listener) {
        for (int id : getReferencedIds()) {
            getRootView().findViewById(id).setOnClickListener(listener);
        }
    }
}
