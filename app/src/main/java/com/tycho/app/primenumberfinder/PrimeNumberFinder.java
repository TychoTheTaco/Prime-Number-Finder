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
    private static final String TAG = PrimeNumberFinder.class.getSimpleName();

    /**
     * Task manager to manage all application tasks.
     */
    private static TaskManager taskManager;

    /**
     * Holds all user preferences.
     */
    private static PreferenceManager preferenceManager;

    @Override
    public void onCreate() {
        super.onCreate();

        FileManager.init(this);
        taskManager = new TaskManager();
        preferenceManager = new PreferenceManager(this, "preferences");
    }

    public static String getVersionName(final Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error getting version name!");
            return "unknown";
        }
    }

    public static PreferenceManager getPreferenceManager() {
        return preferenceManager;
    }

    public static TaskManager getTaskManager(){
        return taskManager;
    }
}