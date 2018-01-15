package com.tycho.app.primenumberfinder.modules;

import android.app.Fragment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import easytasks.Task;
import easytasks.TaskListener;


/**
 * A {@linkplain Fragment} that hosts a specific {@linkplain Task}. A {@linkplain TaskFragment} can only host 1 task at a time.
 *
 * @author Tycho Bellers
 *         Date Created: 11/12/2016
 */
public abstract class TaskFragment extends Fragment implements TaskListener {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "TaskFragment";

    /**
     * The {@link Task} that belongs to this fragment.
     */
    private Task task;

    /**
     * All UI updates are posted to this {@link Handler} on the main thread.
     */
    protected final Handler handler = new Handler(Looper.getMainLooper());

    //Override methods

    @Override
    public void onTaskStarted() {

    }

    @Override
    public void onTaskPaused() {

    }

    @Override
    public void onTaskResumed() {

    }

    @Override
    public void onTaskStopped() {

    }

    @Override
    public void onProgressChanged(float progress) {

    }

    //Getters and setters

    public Task getTask(){
        return task;
    }

    public void setTask(Task task){

        //Remove task listener from previous task
        if (this.task != null){
            if (!this.task.removeTaskListener(this)){
                Log.d(TAG, "Failed to remove task listener from " + task);
            }
        }

        this.task = task;

        //Add task listener to new task
        if (this.task != null){
            this.task.addTaskListener(this);
        }
    }
}