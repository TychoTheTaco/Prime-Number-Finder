package com.tycho.app.primenumberfinder.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

/**
 * Saves user preferences using Android's {@link SharedPreferences} implementation.
 *
 * @author Tycho Bellers
 *         Date Created: 3/3/2017
 */

public class PreferenceManager extends HashMap{

    /**
     * Tag used for logging and debugging
     */
    private static final String TAG = "PreferenceManager";

    private final SharedPreferences sharedPreferences;

    private final String name;

    private boolean allowBackgroundTasks;

    public PreferenceManager(final Context context, final String name){
        this.name = name;
        this.sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public String getName(){
        return name;
    }

    //Utility methods

    private void loadPreferences(){
        this.allowBackgroundTasks = sharedPreferences.getBoolean(Preference.ALLOW_BACKGROUND_TASKS.getKey(), (boolean) Preference.ALLOW_BACKGROUND_TASKS.getDefaultValue());
    }

    public void savePreferences(){
        sharedPreferences.edit()
                .putBoolean(Preference.ALLOW_BACKGROUND_TASKS.getKey(), this.allowBackgroundTasks)
                .apply();
    }

    //Preference

    public boolean isAllowBackgroundTasks(){
        return allowBackgroundTasks;
    }

    public void setAllowBackgroundTasks(boolean allowBackgroundTasks){
        this.allowBackgroundTasks = allowBackgroundTasks;
    }

    //Options

    public enum Preference{
        ALLOW_BACKGROUND_TASKS("allowBackgroundTasks", false);

        private final String key;
        private final Object defaultValue;

        Preference(final String key, final Object defaultValue){
            this.key = key;
            this.defaultValue = defaultValue;
        }

        public String getKey(){
            return key;
        }

        public Object getDefaultValue(){
            return defaultValue;
        }
    }

}
