package com.tycho.app.primenumberfinder;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by tycho on 2/16/2018.
 */

public class ValidEditText extends android.support.v7.widget.AppCompatEditText {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = ValidEditText.class.getSimpleName();

    private static final int[] STATE_VALID = {R.attr.valid};

    private boolean valid = true;

    private final CopyOnWriteArrayList<OnTouchListener> onTouchListeners = new CopyOnWriteArrayList<>();

    /**
     * {@code true} if the text should be cleared on a touch event.
     */
    private boolean clearOnTouch = true;

    public ValidEditText(final Context context) {
        super(context);
        init();
    }

    public ValidEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ValidEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundTintList(getResources().getColorStateList(R.color.valid_edittext_background));
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Perform click
                performClick();

                //Clear text
                if (clearOnTouch) {
                    getText().clear();
                }

                //Notify listeners
                sendOnTouch(v, event);

                //Do not consume event
                return false;
            }
        });
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (valid) {
            mergeDrawableStates(drawableState, STATE_VALID);
        }
        return drawableState;
    }

    public void setValid(final boolean valid) {
        this.valid = valid;
        refreshDrawableState();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public void addOnTouchListener(final OnTouchListener onTouchListener) {
        if (!onTouchListeners.contains(onTouchListener)) {
            onTouchListeners.add(onTouchListener);
        }
    }

    public boolean removeOnTouchListener(final OnTouchListener onTouchListener) {
        return onTouchListeners.remove(onTouchListener);
    }

    private void sendOnTouch(View view, MotionEvent event) {
        for (OnTouchListener onTouchListener : onTouchListeners) {
            onTouchListener.onTouch(view, event);
        }
    }

    public boolean isClearOnTouch() {
        return clearOnTouch;
    }

    public void setClearOnTouch(boolean clearOnTouch) {
        this.clearOnTouch = clearOnTouch;
    }

    /**
     * Set the text and optionally retain the cursor position. The normal
     * {@linkplain #setText(CharSequence)} method places the cursor at the beginning of the text,
     * which is not always desired.
     *
     * @param text                  The text to set.
     * @param restoreCursorPosition {@code true} if the cursor position should be restored after the
     *                              text has been set.
     */
    public void setText(final String text, final boolean restoreCursorPosition) {
        final int oldCursorPosition = getSelectionStart();
        final int oldLength = getText().length();
        super.setText(text);
        if (restoreCursorPosition) {
            final int newCursorPosition = oldCursorPosition + (text.length() - oldLength);
            setSelection((newCursorPosition >= 0 && newCursorPosition <= length()) ? newCursorPosition : 0);
        } else {
            setSelection(length());
        }
    }
}
