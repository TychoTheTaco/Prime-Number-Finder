package com.tycho.app.primenumberfinder.utils;

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

    private final Map<Task, Task.State> tasks = new HashMap<>();

    public void registerTask(final Task task) {
        tasks.put(task, task.getState());
    }

    public void unregisterTask(final Task task) {
        tasks.remove(task);
    }

    public void saveTaskStates(){
        for (Task task : tasks.keySet()){
            tasks.put(task, task.getState());
        }
    }

    /**
     * PAUSE all tasks.
     */
    public void pauseAllTasks() {
        for (Task task : tasks.keySet()) {
            tasks.put(task, task.getState());
            task.pause();
        }
    }

    /**
     * Resume all tasks.
     */
    public void resumeAllTasks() {
        for (Task task : tasks.keySet()) {
            if (tasks.get(task) != Task.State.PAUSED && tasks.get(task) != Task.State.STOPPED) {
                task.resume();
            }
        }
    }

    public Set<Task> getTasks() {
        return tasks.keySet();
    }

    public Task findTaskById(final UUID id) {
        for (Task task : tasks.keySet()) {
            if (task.getId().equals(id)) {
                return task;
            }
        }
        return null;
    }
}
