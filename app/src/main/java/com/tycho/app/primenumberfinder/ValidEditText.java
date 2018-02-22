package com.tycho.app.primenumberfinder;


import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import java.util.logging.Handler;

/**
 * Created by tycho on 2/16/2018.
 */

public class ValidEditText extends android.support.v7.widget.AppCompatEditText {

    private static final int[] STATE_VALID = {R.attr.valid};

    private boolean valid = true;

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
}
