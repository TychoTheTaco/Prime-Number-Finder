package com.tycho.app.primenumberfinder;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.util.TypedValue;

import com.tycho.app.primenumberfinder.utils.PreferenceManager;
import com.tycho.app.primenumberfinder.utils.Task;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tycho Bellers
 *         Date Created: 01/10/2016
 */
public class PrimeNumberFinder extends Application{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "PrimeNumberFinder";

    /**
     * Map of all tasks.
     */
    private static final Map<String, Task> tasks = new HashMap<>();

    /**
     * Holds all user preferences.
     */
    private static PreferenceManager preferenceManager;



    /**
     * Minimum delay between UI updates.
     */
    public static final int UPDATE_LIMIT_MS = (1000 / 60);

    private static final Task.State[] previousTaskStates = new Task.State[2];

    /**
     * If set to true, all logcat and debug messages will be shown.
     */
    public static final boolean DEBUG = true;

    //Override methods

    @Override
    public void onCreate(){
        super.onCreate();

        preferenceManager = new PreferenceManager(this, "preferences");
    }

    //Utility methods

    /**
     * Convert a DP value to its pixel value.
     * @param context Context used by display metrics.
     * @param dp DP value to convert.
     * @return Equivalent value in pixels
     */
    public static float dpToPx(final Context context, final float dp){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    /**
     * Pause all tasks.
     */
    public static void pauseAllTasks(){
        for (Task task : tasks.values()){
            task.pause();
        }
    }

    /**
     * Resume all tasks.
     */
    public static void resumeAllTasks(){
        for (Task task : tasks.values()){
            task.resume();
        }
    }

   //Utility methods

   public static String getVersionName(final Context context){
       try{
           return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
       }catch (PackageManager.NameNotFoundException e){
           if (PrimeNumberFinder.DEBUG) Log.e(TAG, "Error getting version name!");
           return "unknown";
       }
   }

    //Getters and setters

    public static Map<String, Task> getTasks(){
        return tasks;
    }

    public static PreferenceManager getPreferenceManager(){
        return preferenceManager;
    }
}