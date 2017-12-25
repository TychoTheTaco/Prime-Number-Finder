package com.tycho.app.primenumberfinder;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.util.TypedValue;

import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.PreferenceManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import easytasks.Task;

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
     * List of all tasks.
     */
    private static final List<Task> tasks = new ArrayList<>();

    private static final Map<Task, Task.State> previousTaskStates = new HashMap<>();

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

        //Initialize file manager and preference manager
        FileManager.init(this);
        preferenceManager = new PreferenceManager(this, "preferences");
    }

    //Utility methods

    /**
     * Pause all tasks.
     */
    public static void pauseAllTasks() {
        for (Task task : tasks) {
            previousTaskStates.put(task, task.getState());
            task.pause();
        }
        Log.d(TAG, "Tasks paused");
    }

    /**
     * Resume all tasks.
     */
    public static void resumeAllTasks() {
        for (Task task : tasks) {
            if (previousTaskStates.get(task) == Task.State.RUNNING) {
                task.resume();
            }
        }
        Log.d(TAG, "Tasks resumed");
    }

    public static List<Task> getTasks() {
        return tasks;
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

    public static void registerTask(final Task task) {
        if (!tasks.contains(task)) {
            tasks.add(task);
        } else {
            Log.w(TAG, "Task already registered!");
        }
    }

    public static boolean unregisterTask(final Task task) {
        return tasks.remove(task);
    }

    //Getters and setters

    public static PreferenceManager getPreferenceManager() {
        return preferenceManager;
    }
}