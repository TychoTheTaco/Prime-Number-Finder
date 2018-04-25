package com.tycho.app.primenumberfinder;


import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Handler;

/**
 * Created by tycho on 2/16/2018.
 */

public class ValidEditText extends android.support.v7.widget.AppCompatEditText {

    private static final int[] STATE_VALID = {R.attr.valid};

    private boolean valid = true;

    private final CopyOnWriteArrayList<OnTouchListener> onTouchListeners = new CopyOnWriteArrayList<>();

    /**
     * {@code true} if the text should be cleared on a touch event.
     */
    private boolean clearOnTouch = true;

    public ValidEditText(final Context context){
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

    private void init(){
        setBackgroundTintList(getResources().getColorStateList(R.color.valid_edittext_background));
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Perform click
                performClick();

                //Clear text
                if (clearOnTouch){
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
        if (valid){
            mergeDrawableStates(drawableState, STATE_VALID);
        }
        return drawableState;
    }

    public void setValid(final boolean valid){
        this.valid = valid;
        refreshDrawableState();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public void addOnTouchListener(final OnTouchListener onTouchListener){
        if (!onTouchListeners.contains(onTouchListener)){
            onTouchListeners.add(onTouchListener);
        }
    }

    public boolean removeOnTouchListener(final OnTouchListener onTouchListener){
        return onTouchListeners.remove(onTouchListener);
    }

    private void sendOnTouch(View view, MotionEvent event){
        for (OnTouchListener onTouchListener : onTouchListeners){
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
