package com.tycho.app.primenumberfinder;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

import com.tycho.app.primenumberfinder.utils.Utils;

import java.text.NumberFormat;
import java.util.Locale;

public class FormattedEditText extends android.support.v7.widget.AppCompatEditText{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = FormattedEditText.class.getSimpleName();

    /**
     * {@linkplain NumberFormat} instance used to format numbers with commas.
     */
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    private boolean allowZeroInput;

    public FormattedEditText(Context context) {
        super(context);
        init(null);
    }

    public FormattedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public FormattedEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(final AttributeSet attributeSet){
        //Get xml attributes
        TypedArray typedArray = getContext().obtainStyledAttributes(
                attributeSet,
                R.styleable.FormattedEditText,
                0, 0);

        try {
            allowZeroInput = typedArray.getBoolean(R.styleable.FormattedEditText_allowZeroInput, false);
        } finally {
            typedArray.recycle();
        }

        addTextChangedListener(textWatcher);
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            //Format text
            final String formatted = NUMBER_FORMAT.format(Utils.textToNumber(getText().toString()));
            if (editable.length() > 0 && !editable.toString().equals(formatted)) {
                setText(formatted, formatted.length() > 1);
            } else if (!allowZeroInput && editable.toString().equals(NUMBER_FORMAT.format(0))) {
                getText().clear();
            }
        }
    };

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

    public void setText(final Number number){
        setText(NUMBER_FORMAT.format(number));
    }

    public boolean isAllowZeroInput() {
        return allowZeroInput;
    }

    public void setAllowZeroInput(boolean allowZeroInput) {
        this.allowZeroInput = allowZeroInput;
    }
}
