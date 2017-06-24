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
        this.allowBackgroundTasks = sharedPreferences.getBoolean(Preferences.ALLOW_BACKGROUND_TASKS.getKey(), (boolean) Preferences.ALLOW_BACKGROUND_TASKS.getDefaultValue());
    }

    public void savePreferences(){
        sharedPreferences.edit()
                .putBoolean(Preferences.ALLOW_BACKGROUND_TASKS.getKey(), this.allowBackgroundTasks)
                .apply();
    }

    //Preferences

    public boolean isAllowBackgroundTasks(){
        return allowBackgroundTasks;
    }

    public void setAllowBackgroundTasks(boolean allowBackgroundTasks){
        this.allowBackgroundTasks = allowBackgroundTasks;
    }

    //Options

    public enum Preferences{
        ALLOW_BACKGROUND_TASKS("allowBackgroundTasks", false);

        private final String key;
        private final Object defaultValue;

        Preferences(final String key, final Object defaultValue){
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
