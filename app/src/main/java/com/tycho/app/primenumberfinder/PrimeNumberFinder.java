package com.tycho.app.primenumberfinder;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.PreferenceManager;
import com.tycho.app.primenumberfinder.utils.TaskManager;

import io.fabric.sdk.android.Fabric;

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

    @Override
    public void onCreate() {
        super.onCreate();

        FileManager.init(this);
        taskManager = new TaskManager();
        PreferenceManager.initialize(this);

        //Initialize analytics
        if (PreferenceManager.getBoolean(PreferenceManager.Preference.ALLOW_ANALYTICS) && !BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
            Log.d(TAG, "Enabled Crashlytics");
        } else {
            Fabric.with(this, new Crashlytics.Builder().core(new CrashlyticsCore.Builder().disabled(true).build()).build());
            Log.d(TAG, "Disabled Crashlytics");
        }
    }

    public static String getVersionName(final Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error getting version name!");
            return "unknown";
        }
    }

    public static TaskManager getTaskManager(){
        return taskManager;
    }
}