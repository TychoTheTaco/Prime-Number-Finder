package com.tycho.app.primenumberfinder.utils;

import com.tycho.app.primenumberfinder.NativeTaskInterface;

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

    private final Map<NativeTaskInterface, Task.State> tasks = new HashMap<>();

    public void registerTask(final NativeTaskInterface task) {
        tasks.put(task, task.getState());
    }

    public void unregisterTask(final NativeTaskInterface task) {
        tasks.remove(task);
    }

    public void saveTaskStates(){
        for (NativeTaskInterface task : tasks.keySet()){
            tasks.put(task, task.getState());
        }
    }

    /**
     * PAUSE all tasks.
     */
    public void pauseAllTasks() {
        for (NativeTaskInterface task : tasks.keySet()) {
            tasks.put(task, task.getState());
            task.pause();
        }
    }

    /**
     * Resume all tasks.
     */
    public void resumeAllTasks() {
        for (NativeTaskInterface task : tasks.keySet()) {
            if (tasks.get(task) != Task.State.PAUSED && tasks.get(task) != Task.State.STOPPED) {
                task.resume();
            }
        }
    }

    public Set<NativeTaskInterface> getTasks() {
        return tasks.keySet();
    }

    public NativeTaskInterface findTaskById(final UUID id) {
        for (NativeTaskInterface task : tasks.keySet()) {
            if (task.getId().equals(id)) {
                return task;
            }
        }
        return null;
    }
}
