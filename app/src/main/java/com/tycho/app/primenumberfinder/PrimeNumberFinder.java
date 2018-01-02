package com.tycho.app.primenumberfinder;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.PreferenceManager;
import com.tycho.app.primenumberfinder.utils.TaskManager;

/**
 * @author Tycho Bellers
 *         Date Created: 01/10/2016
 */
public class PrimeNumberFinder extends Application {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "PrimeNumberFinder";

    /**
     * Task manager to manage all application tasks.
     */
    private static TaskManager taskManager;

    /**
     * Holds all user preferences.
     */
    private static PreferenceManager preferenceManager;

    /**
     * Minimum delay between UI updates.
     */
    public static final int UPDATE_LIMIT_MS = (1000 / 60);

    //Override methods

    @Override
    public void onCreate() {
        super.onCreate();

        FileManager.init(this);
        FileManager.getInstance().updateFileSystem(this);
        taskManager = new TaskManager();
        preferenceManager = new PreferenceManager(this, "preferences");
    }

    //Utility methods

    public static String getVersionName(final Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error getting version name!");
            return "unknown";
        }
    }

    //Getters and setters

    public static PreferenceManager getPreferenceManager() {
        return preferenceManager;
    }

    public static TaskManager getTaskManager(){
        return taskManager;
    }
}