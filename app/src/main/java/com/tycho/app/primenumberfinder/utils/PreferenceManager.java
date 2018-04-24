package com.tycho.app.primenumberfinder.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

import static com.tycho.app.primenumberfinder.utils.PreferenceManager.Preference.FILE_VERSION;

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

    private int fileVersion;

    public static final int CURRENT_VERSION = 1;

    public PreferenceManager(final Context context, final String name){
        this.name = name;
        this.sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        loadPreferences();
    }

    public String getName(){
        return name;
    }

    //Utility methods

    private void loadPreferences(){
        this.allowBackgroundTasks = sharedPreferences.getBoolean(Preference.ALLOW_BACKGROUND_TASKS.getKey(), (boolean) Preference.ALLOW_BACKGROUND_TASKS.getDefaultValue());
        this.fileVersion = sharedPreferences.getInt(FILE_VERSION.getKey(), (int) FILE_VERSION.getDefaultValue());
    }

    public void savePreferences(){
        sharedPreferences.edit()
                .putBoolean(Preference.ALLOW_BACKGROUND_TASKS.getKey(), this.allowBackgroundTasks)
                .putInt(FILE_VERSION.getKey(), this.fileVersion)
                .apply();
    }

    //Preference

    public boolean isAllowBackgroundTasks(){
        return allowBackgroundTasks;
    }

    public void setAllowBackgroundTasks(boolean allowBackgroundTasks){
        this.allowBackgroundTasks = allowBackgroundTasks;
    }

    public void setFileVersion(final int version){
        this.fileVersion = version;
    }

    public int getFileVersion(){
        return this.fileVersion;
    }

    //Options

    public enum Preference{
        ALLOW_BACKGROUND_TASKS("allowBackgroundTasks", false),
        FILE_VERSION("fileVersion", 0);

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
