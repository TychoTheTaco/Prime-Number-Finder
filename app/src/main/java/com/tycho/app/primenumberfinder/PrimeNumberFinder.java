package com.tycho.app.primenumberfinder;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
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

    @Override
    public void onCreate() {
        super.onCreate();

        FileManager.init(this);
        taskManager = new TaskManager();
        PreferenceManager.initialize(this);

        //Disable Crashlytics for debug builds
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        //Initialize analytics
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(PreferenceManager.getBoolean(PreferenceManager.Preference.ALLOW_ANALYTICS) && !BuildConfig.DEBUG);
    }

    public static String getVersionName(final Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "Unknown";
        }
    }

    public static void reloadTheme(final ContextWrapper contextWrapper){
        final Intent intent = contextWrapper.getBaseContext().getPackageManager().getLaunchIntentForPackage(contextWrapper.getBaseContext().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        contextWrapper.startActivity(intent);
    }

    public static TaskManager getTaskManager(){
        return taskManager;
    }
}