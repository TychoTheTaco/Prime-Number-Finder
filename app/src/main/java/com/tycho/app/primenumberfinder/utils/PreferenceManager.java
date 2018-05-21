package com.tycho.app.primenumberfinder.utils;

import android.content.Context;
import android.content.SharedPreferences;

import static com.tycho.app.primenumberfinder.utils.PreferenceManager.Preference.ALLOW_ANALYTICS;
import static com.tycho.app.primenumberfinder.utils.PreferenceManager.Preference.FILE_VERSION;

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
        Preference.FILE_VERSION.setValue(sharedPreferences.getInt(Preference.FILE_VERSION.getKey(), (Integer) Preference.FILE_VERSION.getDefaultValue()));
        Preference.ALLOW_BACKGROUND_TASKS.setValue(sharedPreferences.getBoolean(Preference.ALLOW_BACKGROUND_TASKS.getKey(), (Boolean) Preference.ALLOW_BACKGROUND_TASKS.getDefaultValue()));
        Preference.ALLOW_ANALYTICS.setValue(sharedPreferences.getBoolean(Preference.ALLOW_ANALYTICS.getKey(), (Boolean) Preference.ALLOW_ANALYTICS.getDefaultValue()));
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
        ALLOW_ANALYTICS("allowAnalytics", true);

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
