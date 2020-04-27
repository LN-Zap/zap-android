package zapsolutions.zap.customView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.constraintlayout.widget.ConstraintLayout;

import zapsolutions.zap.R;

public class NumpadView extends ConstraintLayout {

    private Button[] mBtnNumpad = new Button[10];
    private Button mBtnNumpadDot;
    private ImageButton mBtnNumpadBack;
    private EditText mEditText;

    public NumpadView(Context context) {
        super(context);
        init();
    }

    public NumpadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NumpadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        View view = inflate(getContext(), R.layout.view_numpad, this);

        // Get numpad buttons
        mBtnNumpad[0] = view.findViewById(R.id.Numpad1);
        mBtnNumpad[1] = view.findViewById(R.id.Numpad2);
        mBtnNumpad[2] = view.findViewById(R.id.Numpad3);
        mBtnNumpad[3] = view.findViewById(R.id.Numpad4);
        mBtnNumpad[4] = view.findViewById(R.id.Numpad5);
        mBtnNumpad[5] = view.findViewById(R.id.Numpad6);
        mBtnNumpad[6] = view.findViewById(R.id.Numpad7);
        mBtnNumpad[7] = view.findViewById(R.id.Numpad8);
        mBtnNumpad[8] = view.findViewById(R.id.Numpad9);
        mBtnNumpad[9] = view.findViewById(R.id.Numpad0);
        mBtnNumpadDot = view.findViewById(R.id.NumpadDot);
        mBtnNumpadBack = view.findViewById(R.id.NumpadBack);


        // Set action for numpad number buttons
        for (Button btn : mBtnNumpad) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mEditText != null) {
                        // Add input
                        int start = Math.max(mEditText.getSelectionStart(), 0);
                        int end = Math.max(mEditText.getSelectionEnd(), 0);
                        mEditText.getText().replace(Math.min(start, end), Math.max(start, end),
                                btn.getText(), 0, btn.getText().length());
                    }
                }
            });
        }

        // Set action for numpad "." button
        mBtnNumpadDot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditText != null) {
                    // Add input
                    int start = Math.max(mEditText.getSelectionStart(), 0);
                    int end = Math.max(mEditText.getSelectionEnd(), 0);
                    mEditText.getText().replace(Math.min(start, end), Math.max(start, end),
                            mBtnNumpadDot.getText(), 0, mBtnNumpadDot.getText().length());
                }
            }
        });

        // Set action for numpad "delete" button
        mBtnNumpadBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // remove Input
                removeOneDigit();
            }
        });

        // Set action for long klick on numpad "delete" button
        mBtnNumpadBack.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mEditText != null) {
                    mEditText.getEditableText().clear();
                }
                return false;
            }
        });
    }

    public void removeOneDigit() {
        if (mEditText != null) {
            boolean selection = mEditText.getSelectionStart() != mEditText.getSelectionEnd();

            int start = Math.max(mEditText.getSelectionStart(), 0);
            int end = Math.max(mEditText.getSelectionEnd(), 0);

            String before = mEditText.getText().toString().substring(0, start);
            String after = mEditText.getText().toString().substring(end);

            if (selection) {
                String outputText = before + after;
                mEditText.setText(outputText);
                mEditText.setSelection(start);
            } else {
                if (before.length() >= 1) {
                    String newBefore = before.substring(0, before.length() - 1);
                    String outputText = newBefore + after;
                    mEditText.setText(outputText);
                    mEditText.setSelection(start - 1);
                }
            }
        }
    }

    public void bindEditText(EditText editText) {
        mEditText = editText;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (Button btn : mBtnNumpad) {
            btn.setEnabled(enabled);
        }
        mBtnNumpadBack.setEnabled(enabled);
        mBtnNumpadDot.setEnabled(enabled);
    }
}
