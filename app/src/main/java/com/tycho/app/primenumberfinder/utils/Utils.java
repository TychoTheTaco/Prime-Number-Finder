package com.tycho.app.primenumberfinder.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.inputmethod.InputMethodManager;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains lots of random utility methods.
 * <p>
 * Created by tycho on 11/13/2017.
 */
public final class Utils {

    /*static {
        System.loadLibrary("native-utils");
    }

    public native static void compact();*/

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = Utils.class.getSimpleName();

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    /**
     * Convert a DP value to its pixel value.
     *
     * @param context Context used by display metrics.
     * @param dp      DP value to convert.
     * @return Equivalent value in pixels
     */
    public static float dpToPx(final Context context, final float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    /**
     * Map a value between a and b to a value between c and d.
     *
     * @param value The value to map.
     * @param a     Minimum value of original value's range.
     * @param b     Maximum value of original value's range.
     * @param c     Minimum value of new range.
     * @param d     Maximum value of new range.
     * @return The mapped value between c and d.
     */
    public static float map(float value, float a, float b, float c, float d) {
        return (value - a) / (b - a) * (d - c) + c;
    }

    /**
     * Format the time like a stopwatch.
     *
     * @param millis The time in milliseconds.
     * @return A string with the corresponding time formatted like a stopwatch.
     */
    public static String formatTime(final long millis) {

        if (millis == -1) {
            return "infinity";
        }

        final int milliseconds = (int) (millis % 1000);
        final int seconds = (int) ((millis / 1000) % 60);
        final int minutes = (int) ((millis / (1000 * 60)) % 60);
        final int hours = (int) ((millis / (1000 * 60 * 60)) % 24);
        final int days = (int) ((millis / (1000 * 60 * 60 * 24)) % 7);

        final String time;

        if (days > 0) {
            time = String.format(Locale.getDefault(), "%03d:%02d:%02d:%02d.%03d", days, hours, minutes, seconds, milliseconds);
        } else if (hours > 0) {
            time = String.format(Locale.getDefault(), "%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds);
        } else {
            time = String.format(Locale.getDefault(), "%02d:%02d.%03d", minutes, seconds, milliseconds);
        }

        return time;
    }

    public static String formatTimeHuman(final long millis, final int precision) {

        if (millis == -1) {
            return "infinite";
        }

        if (precision < 1){
            return "";
        }

        final int milliseconds = (int) (millis % 1000);
        final int seconds = (int) ((millis / 1000) % 60);
        final int minutes = (int) ((millis / (1000 * 60)) % 60);
        final int hours = (int) ((millis / (1000 * 60 * 60)) % 24);
        final int days = (int) ((millis / (1000 * 60 * 60 * 24)) % 7);

        final int[] times = new int[]{milliseconds, seconds, minutes, hours, days};
        final String[] parts = new String[]{milliseconds + " ms", seconds + " second", minutes + " minute", hours + " hour", days + " day"};

        final StringBuilder stringBuilder = new StringBuilder();
        int count = 0;
        for (int i = times.length - 1; i >= 0; i--){
            if (times[i] > 0){
                stringBuilder.append(parts[i]);
                if (i != 0 && times[i] != 1){
                    stringBuilder.append('s');
                }
                stringBuilder.append(' ');
                count++;
                if (count == precision){
                    break;
                }
            }else if (stringBuilder.length() > 0){
                break;
            }
        }

        if (stringBuilder.length() == 0){
            stringBuilder.append(parts[0]);
        }

        return stringBuilder.toString().trim();
    }

    /**
     * Append an ordinal indicator to the end of a number. This method will also format the number using {@linkplain NumberFormat#format(long)}.
     *
     * @param number The number to append to.
     * @return A string containing the formatted number with an ordinal indicator appended.
     */
    public static String formatNumberOrdinal(final long number) {
        String output = NumberFormat.getInstance(Locale.getDefault()).format(number);

        final long ones = number % 10;
        final long tens = number % 100;

        if (ones == 1 && tens != 11) {
            output += "st";
        } else if (ones == 2 && tens != 12) {
            output += "nd";
        } else if (ones == 3 && tens != 13) {
            output += "rd";
        } else {
            output += "th";
        }

        return output;
    }

    /**
     * Hide the Android virtual keyboard.
     *
     * @param context The activity context to use.
     */
    public static void hideKeyboard(final Context context) {
        try {
            final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(((Activity) context).getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sort a list of files by date.
     *
     * @param files
     * @param ascending
     */
    public static void sortByDate(final List<File> files, final boolean ascending) {
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File file0, File file1) {
                if (ascending) {
                    return Long.valueOf(file0.lastModified()).compareTo(file1.lastModified());
                } else {
                    return Long.valueOf(file1.lastModified()).compareTo(file0.lastModified());
                }
            }
        });
    }

    public static SpannableStringBuilder formatSpannable(final SpannableStringBuilder spannableStringBuilder, final String raw, final String[] content, final int color){
        final int[] spanPositions = getSpanPositions(spannableStringBuilder, raw, content);
        for (int i = 0; i < spanPositions.length; i += 2){
            spannableStringBuilder.setSpan(new ForegroundColorSpan(color), spanPositions[i], spanPositions[i + 1], Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), spanPositions[i], spanPositions[i + 1], Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return spannableStringBuilder;
    }

    public static SpannableStringBuilder formatSpannableColor(final SpannableStringBuilder spannableStringBuilder, final String raw, final String[] content, final int color){
        final int[] spanPositions = getSpanPositions(spannableStringBuilder, raw, content);
        for (int i = 0; i < spanPositions.length; i += 2){
            spannableStringBuilder.setSpan(new ForegroundColorSpan(color), spanPositions[i], spanPositions[i + 1], Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return spannableStringBuilder;
    }

    private static int[] getSpanPositions(final SpannableStringBuilder spannableStringBuilder, final String raw, final String[] content){
        spannableStringBuilder.clear();
        spannableStringBuilder.clearSpans();
        final String[] split = raw.split("%\\d\\$s");
        final int[] spanPositions = new int[content.length * 2];
        for (int i = 0; i < split.length; i++){
            spannableStringBuilder.append(split[i]);
            if (i < content.length){
                final int position = spannableStringBuilder.length();
                spannableStringBuilder.append(content[i]);
                spanPositions[i * 2] = position;
                spanPositions[i * 2 + 1] = spannableStringBuilder.length();
            }
        }
        return spanPositions;
    }

    public static void applyTheme(final AppCompatActivity appCompatActivity, final int statusBarColor, final int actionBarColor){
        appCompatActivity.getWindow().setStatusBarColor(statusBarColor);
        appCompatActivity.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(actionBarColor));
    }

    public static BigInteger textToNumber(String text){
        Crashlytics.log("raw input: '" + text + "'");

        //Extract all digits from the input
        final Pattern pattern = Pattern.compile("\\d+");
        final Matcher matcher = pattern.matcher(text);
        final StringBuilder numberString = new StringBuilder();
        while (matcher.find()){
            numberString.append(matcher.group());
        }

        Crashlytics.log("number: '" + numberString + "'");

        if (numberString.length() > 0){
            return new BigInteger(numberString.toString());
        }

        return BigInteger.ZERO;
    }

    public static BigDecimal textToDecimal(String text){
        Crashlytics.log("Raw input: '" + text + "'");

        if (text.length() == 0){
            return BigDecimal.ZERO;
        }

        return new BigDecimal(text);
    }

    public static int getAccentColor(final Context context) {
        TypedValue typedValue = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] { android.R.attr.colorAccent });
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }
}
