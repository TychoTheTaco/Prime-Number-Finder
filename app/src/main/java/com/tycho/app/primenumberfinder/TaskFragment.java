package com.tycho.app.primenumberfinder;

import android.app.Fragment;

import com.tycho.app.primenumberfinder.utils.Task;

/**
 * A task fragment is a {@link Fragment} that hosts a specific {@link Task}.
 *
 * @author Tycho Bellers
 *         Date Created: 11/12/2016
 */

public abstract class TaskFragment extends Fragment /*implements TaskListener*/{

    /**
     * The {@link Task} that belongs to this fragment.
     */
    private Task task;

    //Override methods

    /*@Override
    public void onTaskStarted(){

    }

    @Override
    public void onTaskPaused(){

    }

    @Override
    public void onTaskResumed(){

    }

    @Override
    public void onTaskFinished(){

    }

    @Override
    public void onTaskStopped(){

    }

    @Override
    public void onProgressChanged(float percent){

    }*/

    //Abstract methods

    protected abstract void reset();

    //Getters and setters

    public Task getTask(){
        return task;
    }

    public void setTask(Task task){
        this.task = task;
        /*if (this.task != null){
            this.task.addTaskListener(this);
        }*/
    }
}
