package com.tycho.app.primenumberfinder.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormattedEditText extends android.support.v7.widget.AppCompatEditText {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = FormattedEditText.class.getSimpleName();

    /**
     * {@linkplain NumberFormat} instance used to format numbers with commas.
     */
    protected final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    /**
     * {@linkplain DecimalFormat} instance used to format numbers with a decimal point.
     */
    protected final DecimalFormat DECIMAL_FORMAT = (DecimalFormat) DecimalFormat.getInstance(Locale.getDefault());

    /**
     * If this is set to {@code true}, then this view will allow the user to enter the number '0'.
     * If this is set to {@code false}, then this view will prevent the user from entering '0', and
     * will instead just leave the view empty.
     */
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

    private void init(final AttributeSet attributeSet) {
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

        DECIMAL_FORMAT.setMaximumFractionDigits(1);
        DECIMAL_FORMAT.setRoundingMode(RoundingMode.DOWN);

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
            if (isDecimalInput()) {

                //Log.d(TAG, "Decimal separator is '" + DECIMAL_FORMAT.getDecimalFormatSymbols().getDecimalSeparator() + "'");

                //If the text ends with a decimal, don't modify it
                final Pattern pattern = Pattern.compile("[^\\d]$");
                final Matcher matcher = pattern.matcher(editable);
                if (matcher.find()) {
                    Log.w(TAG, "Ended with decimal, not modifying.");
                    return;
                }

                //If the text contains a decimal and ends with a zero, don't modify it
                if (getText().toString().contains(String.valueOf(DECIMAL_FORMAT.getDecimalFormatSymbols().getDecimalSeparator())) && getText().toString().endsWith("0")){
                    Log.w(TAG, "Ended with dot zero, not modifying.");
                    return;
                }

                //final String formatted = DECIMAL_FORMAT.format(Utils.textToDecimal(getText().toString(), DECIMAL_FORMAT.getDecimalFormatSymbols().getDecimalSeparator()));
                final BigDecimal bigDecimal = Utils.textToDecimal(getText().toString(), DECIMAL_FORMAT.getDecimalFormatSymbols().getDecimalSeparator());
                final String formatted = DECIMAL_FORMAT.format(bigDecimal);
                if (editable.length() > 0 && !editable.toString().equals(formatted)) {
                    Log.w(TAG, "Modified: " + formatted);
                    setText(formatted, formatted.length() > 1);
                } else if (!allowZeroInput && editable.toString().equals(DECIMAL_FORMAT.format(0))) {
                    getText().clear();
                }
            } else {
                final String formatted = NUMBER_FORMAT.format(Utils.textToNumber(getText().toString()));
                if (editable.length() > 0 && !editable.toString().equals(formatted)) {
                    setText(formatted, formatted.length() > 1);
                } else if (!allowZeroInput && editable.toString().equals(NUMBER_FORMAT.format(0))) {
                    getText().clear();
                }
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

    public void setHint(final Number number) {
        if (isDecimalInput()) {
            setHint(DECIMAL_FORMAT.format(number));
        } else {
            setHint(NUMBER_FORMAT.format(number));
        }
    }

    public void setHintNumber(final Number number) {
        setHint(number);
    }

    public void setNumber(final Number number) {
        if (isDecimalInput()) {
            setText(DECIMAL_FORMAT.format(number));
        } else {
            //TODO: Temporary fix
            setText(NUMBER_FORMAT.format(number.longValue()));
        }
    }

    public boolean isAllowZeroInput() {
        return allowZeroInput;
    }

    public void setAllowZeroInput(boolean allowZeroInput) {
        this.allowZeroInput = allowZeroInput;
    }

    public void overrideDefaultTextWatcher() {
        removeTextChangedListener(textWatcher);
    }

    public Number getNumberValue() {
        return Utils.textToNumber(getText().toString());
    }

    public Number getDecimalValue() {
        return Utils.textToDecimal(getText().toString(), DECIMAL_FORMAT.getDecimalFormatSymbols().getDecimalSeparator());
    }

    public int getIntValue() {
        return getNumberValue().intValue();
    }

    public float getFloatValue() {
        if (isDecimalInput()){
            return getDecimalValue().floatValue();
        }
        return getNumberValue().floatValue();
    }

    public long getLongValue() {
        return getNumberValue().longValue();
    }

    private boolean isDecimalInput() {
        return getInputType() == (InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }
}
