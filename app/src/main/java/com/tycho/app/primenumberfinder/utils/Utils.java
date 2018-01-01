package com.tycho.app.primenumberfinder.utils;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.inputmethod.InputMethodManager;

import java.util.Locale;

/**
 *
 * This class contains lots of random utility classes.
 *
 * Created by tycho on 11/13/2017.
 *
 */

public final class Utils {

    /**
     * Convert a DP value to its pixel value.
     * @param context Context used by display metrics.
     * @param dp DP value to convert.
     * @return Equivalent value in pixels
     */
    public static float dpToPx(final Context context, final float dp){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    /**
     * Map a value between a and b to a value between c and d.
     * @param value The value to map.
     * @param a Minimum value of original value's range.
     * @param b Maximum value of original value's range.
     * @param c Minimum value of new range.
     * @param d Maximum value of new range.
     * @return The mapped value between c and d.
     */
    public static float map(float value, float a, float b, float c, float d) {
        return (value - a) / (b - a) * (d - c) + c;
    }

    public static String formatTime(final long millis){

        if (millis == -1){
            return "infinity";
        }

        final int milliseconds = (int) (millis % 1000);
        final int seconds = (int) ((millis / 1000) % 60);
        final int minutes = (int) ((millis / (1000 * 60)) % 60);
        final int hours = (int) ((millis / (1000 * 60 * 60)) % 24);
        final int days = (int) ((millis / (1000 * 60 * 60 * 24)) % 7);

        final String time;

        if (days > 0){
            time = String.format(Locale.getDefault(),"%03d:%02d:%02d:%02d.%03d", days, hours, minutes, seconds, milliseconds);
        }else if (hours > 0){
            time = String.format(Locale.getDefault(),"%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds);
        }else{
            time = String.format(Locale.getDefault(),"%02d:%02d.%03d", minutes, seconds, milliseconds);
        }

        return time;
    }

    public static void hideKeyboard(final Context context) {
        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(((Activity) context).getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
