package com.tycho.app.primenumberfinder.utils;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import easytasks.Task;

/**
 * @author Tycho Bellers
 * Date Created: 12/26/2017
 */

public class TaskManager {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = TaskManager.class.getSimpleName();

    private final Map<Task, Task.State> taskStates = new HashMap<>();

    public void registerTask(final Task task) {
        taskStates.put(task, task.getState());
    }

    public void unregisterTask(final Task task) {
        taskStates.remove(task);
    }

    /**
     * Pause all tasks.
     */
    public void pauseAllTasks() {
        for (Task task : taskStates.keySet()) {
            taskStates.put(task, task.getState());
            task.pause(false);
        }
    }

    /**
     * Resume all tasks.
     */
    public void resumeAllTasks() {
        for (Task task : taskStates.keySet()) {
            if (taskStates.get(task) != Task.State.PAUSED && taskStates.get(task) != Task.State.STOPPED) {
                task.resume(false);
            }
        }
    }

    public Set<Task> getTaskStates() {
        return taskStates.keySet();
    }

    public Task findTaskById(final UUID id) {
        for (Task task : taskStates.keySet()) {
            if (task.getId().equals(id)) {
                return task;
            }
        }
        return null;
    }
}
