package com.tycho.app.primenumberfinder.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Saves user preferences using Android's {@link SharedPreferences} implementation.
 *
 * @author Tycho Bellers
 * Date Created: 3/3/2017
 */

public class PreferenceManager {

    /**
     * Tag used for logging and debugging
     */
    private static final String TAG = PreferenceManager.class.getSimpleName();

    private static SharedPreferences sharedPreferences;

    private boolean allowBackgroundTasks;
    private int fileVersion;
    private boolean allowAnalytics;

    public static final int CURRENT_VERSION = 1;

    public static void initialize(final Context context) {
        sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        loadPreferences();
    }

    private static void loadPreferences() {
        for (Preference preference : Preference.values()){
            if (preference.getDefaultValue() instanceof Integer){
                preference.setValue(sharedPreferences.getInt(preference.getKey(), (Integer) preference.getDefaultValue()));
            }else if (preference.getDefaultValue() instanceof Boolean){
                preference.setValue(sharedPreferences.getBoolean(preference.getKey(), (Boolean) preference.getDefaultValue()));
            }
        }
    }

    public static void set(Preference preference, Object value) {
        preference.setValue(value);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        if (value instanceof Boolean) {
            editor.putBoolean(preference.getKey(), (Boolean) value);
        } else if (value instanceof String) {
            editor.putString(preference.getKey(), (String) value);
        } else if (value instanceof Integer) {
            editor.putInt(preference.getKey(), (Integer) value);
        } else if (value instanceof Long) {
            editor.putLong(preference.getKey(), (Long) value);
        } else if (value instanceof Float) {
            editor.putFloat(preference.getKey(), (Float) value);
        }
        editor.apply();
    }

    public static boolean getBoolean(Preference preference) {
        return preference.getBooleanValue();
    }

    public static int getInt(Preference preference) {
        return preference.getIntValue();
    }

    public enum Preference {
        ALLOW_BACKGROUND_TASKS("allowBackgroundTasks", false),
        FILE_VERSION("fileVersion", 0),
        ALLOW_ANALYTICS("allowAnalytics", true),
        QUICK_COPY("quickCopy", true),
        QUICK_COPY_KEEP_FORMATTING("quickCopyKeepFormatting", false),
        THEME("theme", 1);

        private final String key;

        private final Object defaultValue;

        private Object value;

        Preference(final String key, final Object defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
            this.value = defaultValue;
        }

        public String getKey() {
            return key;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public Object getValue() {
            return value;
        }

        public boolean getBooleanValue() {
            return (Boolean) value;
        }

        public int getIntValue() {
            return (Integer) value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }

}
