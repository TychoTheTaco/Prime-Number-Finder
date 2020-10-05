package com.tycho.app.primenumberfinder.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;

import androidx.core.content.ContextCompat;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.utils.Utils;

public class NumberInput extends ValidEditText {

    public NumberInput(Context context) {
        super(context);
        init(context, null);
    }

    public NumberInput(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public NumberInput(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(final Context context, final AttributeSet attributeSet){
        // Get attributes from XML
        final int[] attributes = new int[]{
                android.R.attr.textSize
        };
        final TypedArray typedArray = context.obtainStyledAttributes(attributeSet, attributes);
        setTextSize(TypedValue.COMPLEX_UNIT_PX, typedArray.getDimensionPixelSize(0, spToPx(24, context)));
        typedArray.recycle();

        // Override properties
        setBackgroundResource(R.drawable.number_input_background);
        setElevation(Utils.dpToPx(context, 2));
        setTextColor(ContextCompat.getColor(context, R.color.primary_text_light));
        setHintTextColor(ContextCompat.getColor(context, R.color.primary_text_very_light));
        setTextCursorDrawable(R.drawable.cursor);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setInputType(InputType.TYPE_CLASS_NUMBER);
        setImeOptions(EditorInfo.IME_ACTION_DONE);
    }

    public static int spToPx(float sp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }
}
