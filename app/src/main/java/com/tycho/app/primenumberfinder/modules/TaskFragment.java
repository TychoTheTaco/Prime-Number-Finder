package com.tycho.app.primenumberfinder.modules;

import android.os.Handler;
import android.os.Looper;
import androidx.fragment.app.Fragment;
import android.util.Log;

import easytasks.ITask;
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
    private static final String TAG = TaskFragment.class.getSimpleName();

    /**
     * The {@link Task} that belongs to this fragment.
     */
    private ITask task;

    /**
     * All UI updates are posted to this {@link Handler} on the main thread.
     */
    protected final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onTaskStarted(final ITask task) {

    }

    @Override
    public void onTaskPausing(final ITask task) {

    }

    @Override
    public void onTaskPaused(final ITask task) {

    }

    @Override
    public void onTaskResuming(final ITask task) {

    }

    @Override
    public void onTaskResumed(final ITask task) {

    }

    @Override
    public void onTaskStopping(final ITask task) {

    }

    @Override
    public void onTaskStopped(final ITask task) {

    }

    public synchronized ITask getTask(){
        return task;
    }

    public synchronized void setTask(ITask task){
        //Remove task listener from previous task
        if (this.task != null){
            if (!this.task.removeTaskListener(this)){
                Log.w(TAG, "Failed to remove task listener from " + this.task);
            }
        }

        this.task = task;

        //Add task listener to new task
        if (task != null){
            task.addTaskListener(this);
        }
    }
}
