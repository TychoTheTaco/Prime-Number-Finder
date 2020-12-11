package com.tycho.app.primenumberfinder.ui;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.tycho.app.primenumberfinder.R;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by tycho on 2/16/2018.
 */

public class ValidEditText extends FormattedEditText {

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
        init(context, null);
    }

    public ValidEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ValidEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(final Context context, final AttributeSet attributeSet) {
        setOnTouchListener((v, event) -> {
            //Clear text
            if (clearOnTouch) {
                getText().clear();
            }

            //Notify listeners
            sendOnTouch(v, event);

            // Perform click
            if (event.getAction() == MotionEvent.ACTION_UP){
                performClick();
            }

            return false;
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

    public boolean isValid() {
        return valid;
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
}
