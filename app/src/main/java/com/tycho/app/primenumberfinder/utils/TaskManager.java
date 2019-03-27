package com.tycho.app.primenumberfinder.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import easytasks.ITask;
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

    private final Map<ITask, Task.State> tasks = new HashMap<>();

    public void registerTask(final ITask task) {
        tasks.put(task, task.getState());
    }

    public void unregisterTask(final ITask task) {
        tasks.remove(task);
    }

    public void saveTaskStates(){
        for (ITask task : tasks.keySet()){
            tasks.put(task, task.getState());
        }
    }

    /**
     * Pause all tasks.
     */
    public void pauseAllTasks() {
        for (ITask task : tasks.keySet()) {
            tasks.put(task, task.getState());
            task.pause();
        }
    }

    /**
     * Resume all tasks.
     */
    public void resumeAllTasks() {
        for (ITask task : tasks.keySet()) {
            if (tasks.get(task) != Task.State.PAUSED && tasks.get(task) != Task.State.STOPPED) {
                task.resume();
            }
        }
    }

    public Set<ITask> getTasks() {
        return tasks.keySet();
    }

    public ITask findTaskById(final UUID id) {
        for (ITask task : tasks.keySet()) {
            if (task.getId().equals(id)) {
                return task;
            }
        }
        return null;
    }
}
