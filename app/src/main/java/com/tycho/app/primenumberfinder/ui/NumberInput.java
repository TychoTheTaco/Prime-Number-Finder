package com.tycho.app.primenumberfinder.ui;

import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.lang.reflect.Field;
import java.util.Random;

public class NumberInput extends ValidEditText {

    private boolean showRandomHint = true;

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
        setBackgroundResource(R.drawable.number_input_background);
        setElevation(Utils.dpToPx(context, 2));
        setTextColor(ContextCompat.getColor(context, R.color.primary_text_light));
        setHintTextColor(ContextCompat.getColor(context, R.color.secondary_text_light));

        // Set cursor drawable. This requires reflection for API < 29
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            setTextCursorDrawable(R.drawable.cursor);
        }else{
            try {
                // https://github.com/android/platform_frameworks_base/blob/kitkat-release/core/java/android/widget/TextView.java#L562-564
                Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
                f.setAccessible(true);
                f.set(this, R.drawable.cursor);
            } catch (Exception ignored) {
            }
        }

        setGravity(Gravity.CENTER_HORIZONTAL);
        setInputType(InputType.TYPE_CLASS_NUMBER);
        setImeOptions(EditorInfo.IME_ACTION_DONE);

        // Set random hint whenever the text is cleared
        addTextChangedListener(new TextWatcher() {

            final Random random = new Random();
            final int min = 0;
            final int max = 1_000_000;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (showRandomHint){
                    if (s.length() == 0){
                        setHintNumber(min + random.nextInt(max - min));
                    }
                }
            }
        });
        setText(" ");
        setText("");

    }

    public void setShowRandomHint(boolean showRandomHint) {
        this.showRandomHint = showRandomHint;
    }
}
