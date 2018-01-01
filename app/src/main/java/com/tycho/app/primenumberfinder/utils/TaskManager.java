package com.tycho.app.primenumberfinder.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import easytasks.Task;

/**
 * @author Tycho Bellers
 *         Date Created: 12/26/2017
 */

public class TaskManager{

    private final Map<Task, Task.State> tasks = new HashMap<>();

    public void registerTask(final Task task) {
        tasks.put(task, task.getState());
    }

    public void unregisterTask(final Task task) {
        tasks.remove(task);
    }

    /**
     * Pause all tasks.
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
            if (tasks.get(task) == Task.State.RUNNING){
                task.resume();
            }
        }
    }

    public Set<Task> getTasks(){
        return tasks.keySet();
    }
}
