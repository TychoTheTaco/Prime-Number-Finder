package com.tycho.app.primenumberfinder.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.tycho.app.primenumberfinder.LongClickableSpan;
import com.tycho.app.primenumberfinder.ProgressDialog;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.Savable;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;
import com.tycho.app.primenumberfinder.modules.gcf.GreatestCommonFactorTask;
import com.tycho.app.primenumberfinder.modules.lcm.LeastCommonMultipleTask;
import com.tycho.app.primenumberfinder.modules.primefactorization.PrimeFactorizationTask;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import easytasks.ITask;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * This class contains lots of random utility methods.
 * <p>
 * Created by tycho on 11/13/2017.
 */
public final class Utils {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = Utils.class.getSimpleName();

    /**
     * Convert a DP value to its pixel value.
     *
     * @param context Context used by display metrics.
     * @param dp      DP value to convert.
     * @return Equivalent value in pixels.
     */
    public static float dpToPx(final Context context, final float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static int dpToPx(final Context context, final int dp) {
        return (int) dpToPx(context, (float) dp);
    }

    public static String formatTimeHuman(final long millis, final int precision) {

        if (millis == -1) {
            return "infinite time";
        }

        if (precision < 1) {
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
        for (int i = times.length - 1; i >= 0; i--) {
            if (times[i] > 0) {
                stringBuilder.append(parts[i]);
                if (i != 0 && times[i] != 1) {
                    stringBuilder.append('s');
                }
                stringBuilder.append(' ');
                count++;
                if (count == precision) {
                    break;
                }
            } else if (stringBuilder.length() > 0) {
                break;
            }
        }

        if (stringBuilder.length() == 0) {
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
        Collections.sort(files, (file0, file1) -> (ascending ? 1 : -1) * Long.compare(file0.lastModified(), file1.lastModified()));
    }

    public static void sortBySize(final List<File> files, final boolean ascending) {
        Collections.sort(files, (file0, file1) -> (ascending ? 1 : -1) * Long.compare(file0.length(), file1.length()));
    }

    public static SpannableStringBuilder formatSpannable(final SpannableStringBuilder spannableStringBuilder, final String raw, final String[] content, final int color) {
        final int[] spanPositions = getSpanPositions(spannableStringBuilder, raw, content);
        for (int i = 0; i < spanPositions.length; i += 2) {
            spannableStringBuilder.setSpan(new ForegroundColorSpan(color), spanPositions[i], spanPositions[i + 1], Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), spanPositions[i], spanPositions[i + 1], Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return spannableStringBuilder;
    }

    public static SpannableStringBuilder formatSpannable(final SpannableStringBuilder spannableStringBuilder, final String raw, final String[] content, final boolean[] applyCopySpan, final int color, final Context context) {
        final int[] spanPositions = getSpanPositions(spannableStringBuilder, raw, content);
        for (int i = 0; i < spanPositions.length; i += 2) {
            final int start = spanPositions[i];
            final int end = spanPositions[i + 1];
            spannableStringBuilder.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            if (applyCopySpan[i / 2]) applyCopySpan(spannableStringBuilder, start, end, context);
        }
        return spannableStringBuilder;
    }

    public static SpannableStringBuilder formatSpannableColor(final SpannableStringBuilder spannableStringBuilder, final String raw, final String[] content, final int color) {
        final int[] spanPositions = getSpanPositions(spannableStringBuilder, raw, content);
        for (int i = 0; i < spanPositions.length; i += 2) {
            spannableStringBuilder.setSpan(new ForegroundColorSpan(color), spanPositions[i], spanPositions[i + 1], Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return spannableStringBuilder;
    }

    private static int[] getSpanPositions(final SpannableStringBuilder spannableStringBuilder, final String raw, final String[] content) {
        spannableStringBuilder.clear();
        spannableStringBuilder.clearSpans();
        final String[] split = raw.split("%\\d\\$s");
        final int[] spanPositions = new int[content.length * 2];
        for (int i = 0; i < split.length; i++) {
            spannableStringBuilder.append(split[i]);
            if (i < content.length) {
                final int position = spannableStringBuilder.length();
                spannableStringBuilder.append(content[i]);
                spanPositions[i * 2] = position;
                spanPositions[i * 2 + 1] = spannableStringBuilder.length();
            }
        }
        return spanPositions;
    }

    public static void applyTheme(final AppCompatActivity activity, final int statusBarColor, final int actionBarColor) {
        activity.getWindow().setStatusBarColor(statusBarColor);
        activity.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(actionBarColor));
    }

    public static BigInteger textToNumber(String text) {
        //Extract all digits from the input
        final Pattern pattern = Pattern.compile("\\d+");
        final Matcher matcher = pattern.matcher(text);
        final StringBuilder numberString = new StringBuilder();
        while (matcher.find()) {
            numberString.append(matcher.group());
        }

        if (numberString.length() > 0) {
            return new BigInteger(numberString.toString());
        }

        return BigInteger.ZERO;
    }

    public static BigDecimal textToDecimal(String text, final char decimalSeparator) {
        if (text.length() == 0) {
            return BigDecimal.ZERO;
        }

        final Pattern pattern = Pattern.compile("\\d+|" + Pattern.quote(String.valueOf(decimalSeparator)));
        final Matcher matcher = pattern.matcher(text);
        final StringBuilder stringBuilder = new StringBuilder("0"); //Prepend 0 in case input starts with decimal point
        while (matcher.find()) {
            stringBuilder.append(matcher.group().replace(decimalSeparator, '.'));
        }
        BigDecimal bigDecimal = new BigDecimal(stringBuilder.toString());
        bigDecimal = bigDecimal.setScale(2, RoundingMode.FLOOR);
        return bigDecimal;
    }

    public static int getAccentColor(final Context context) {
        final TypedValue typedValue = new TypedValue();
        final TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{android.R.attr.colorAccent});
        final int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }

    public static int getColor(final int attr, final Context context) {
        final TypedValue typedValue = new TypedValue();
        final TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{attr});
        final int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "KMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static void reLayoutChildren(View view) {
        view.measure(
                View.MeasureSpec.makeMeasureSpec(view.getMeasuredWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(view.getMeasuredHeight(), View.MeasureSpec.EXACTLY));
        view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
    }

    public static void logTaskStarted(final Context context, final ITask task) {
        if (task instanceof FindPrimesTask) {
            final Bundle bundle = new Bundle();
            bundle.putLong("start", ((FindPrimesTask) task).getStartValue());
            bundle.putLong("end", ((FindPrimesTask) task).getEndValue());
            bundle.putString("method", ((FindPrimesTask) task).getSearchOptions().getSearchMethod().name());
            bundle.putInt("threads", ((FindPrimesTask) task).getThreadCount());
            FirebaseAnalytics.getInstance(context).logEvent("find_primes_task_started", bundle);
        } else if (task instanceof FindFactorsTask) {
            final Bundle bundle = new Bundle();
            bundle.putLong("number", ((FindFactorsTask) task).getNumber());
            FirebaseAnalytics.getInstance(context).logEvent("find_factors_task_started", bundle);
        } else if (task instanceof PrimeFactorizationTask) {
            final Bundle bundle = new Bundle();
            bundle.putLong("number", ((PrimeFactorizationTask) task).getNumber());
            FirebaseAnalytics.getInstance(context).logEvent("prime_factorization_task_started", bundle);
        }else if (task instanceof LeastCommonMultipleTask) {
            FirebaseAnalytics.getInstance(context).logEvent("lcm_task_started", null);
        }else if (task instanceof GreatestCommonFactorTask) {
            FirebaseAnalytics.getInstance(context).logEvent("gcf_task_started", null);
        }
    }

    public static ColorStateList generateColorStateList(final int[] states, final int[] colors) {
        final int[][] stateArray = new int[colors.length][];
        for (int i = 0; i < stateArray.length; i++) {
            if (i < states.length) {
                stateArray[i] = new int[]{states[i]};
            } else {
                stateArray[i] = new int[]{};
            }
        }
        return new ColorStateList(stateArray, colors);
    }

    public static String formatNumberList(final List<? extends Number> numbers, final NumberFormat numberFormat, final String separator) {
        final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        for (int i = 0; i < numbers.size(); i++) {
            spannableStringBuilder.append(numberFormat.format(numbers.get(i)));
            Utils.separateNumbers(spannableStringBuilder, numbers, i, separator);
        }
        return spannableStringBuilder.toString();
    }

    public static void separateNumbers(final SpannableStringBuilder spannableStringBuilder, final List<? extends Number> numbers, final int index, final String separator) {
        if (index == numbers.size() - 2) {
            if (numbers.size() > 2) spannableStringBuilder.append(separator);
            spannableStringBuilder.append(" and ");
        } else if (index != numbers.size() - 1) {
            spannableStringBuilder.append(separator).append(' ');
        }
    }

    public static void applyCopySpan(final SpannableStringBuilder spannableStringBuilder, final int start, final int end, final Context context) {
        final String original = spannableStringBuilder.toString();
        spannableStringBuilder.setSpan(new LongClickableSpan() {
            @Override
            public void onLongClick(View view, final int x, final int y) {
                //Make sure setting is enabled
                if (!PreferenceManager.getBoolean(PreferenceManager.Preference.QUICK_COPY)) {
                    return;
                }
                if (!original.equals(spannableStringBuilder.toString())) {
                    Log.w(TAG, "SpannableStringBuilder was modified since last touch!\nOriginal: " + original + "\nUpdated: " + spannableStringBuilder.toString());
                }

                final ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                final char[] chars = new char[end - start];
                spannableStringBuilder.getChars(start, end, chars, 0);
                String text = new String(chars);
                if (!PreferenceManager.getBoolean(PreferenceManager.Preference.QUICK_COPY_KEEP_FORMATTING)) {
                    text = textToNumber(text).toString();
                }
                final ClipData clip = ClipData.newPlainText(text, text);
                clipboard.setPrimaryClip(clip);

                //Show popup
                final PopupWindow popupWindow = new PopupWindow(LayoutInflater.from(context).inflate(R.layout.text_copied_popup, null, false), ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                    for (Drawable drawable : ((TextView) popupWindow.getContentView().findViewById(R.id.text)).getCompoundDrawables()){
                        drawable.mutate().setTint(Color.WHITE);
                    }
                }
                popupWindow.setAnimationStyle(R.style.PopupWindowAnimationStyle);
                popupWindow.setBackgroundDrawable(null);
                popupWindow.setElevation(Utils.dpToPx(context, 4));
                popupWindow.getContentView().measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                popupWindow.showAsDropDown(view, x - (popupWindow.getContentView().getMeasuredWidth() / 2), -view.getHeight() + y - popupWindow.getContentView().getMeasuredHeight() - Utils.dpToPx(context, 16));

                //Apply highlight span
                final UnderlineSpan span = new UnderlineSpan();
                spannableStringBuilder.setSpan(span, start, end, 0);
                ((TextView) view).setText(spannableStringBuilder);
                view.postDelayed(() -> {
                    spannableStringBuilder.removeSpan(span);
                    ((TextView) view).setText(spannableStringBuilder);
                    popupWindow.dismiss();
                }, 500);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        }, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    public static int applyAlpha(final int color, final float alpha){
        return ((color & 0x00FFFFFF) | ((int) (255 * alpha) << 24));
    }

    public static void save(final Savable task, final Context context) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Saving...");
        progressDialog.show();

        new Thread(() -> {
            final boolean saved = task.save();
            new Handler(Looper.getMainLooper()).post(() -> {Toast.makeText(context.getApplicationContext(), saved ? context.getString(R.string.successfully_saved_file) : context.getString(R.string.error_saving_file), Toast.LENGTH_SHORT).show();});
            progressDialog.dismiss();
        }).start();
    }
}
