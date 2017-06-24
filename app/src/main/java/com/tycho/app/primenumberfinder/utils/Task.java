package com.tycho.app.primenumberfinder.utils;

import android.util.Log;

import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.TaskListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A task is any runnable that takes time so complete. Tasks can have progress, or can be ongoing; stopping only when {@link #stop()} is called.
 *
 * @author Tycho Bellers
 *         Date Created: 11/12/2016
 */
public abstract class Task implements Runnable{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "Task";

    /**
     * Listeners for task events.
     */
    private final List<TaskListener> taskListeners = new ArrayList<>();

    /**
     * Possible task states.
     */
    public enum State{
        NOT_STARTED,
        RUNNING,
        PAUSED,
        STOPPED,
        FINISHED
    }

    /**
     * Current task state.
     */
    private State state = State.NOT_STARTED;

    /**
     * Total task progress. If ongoing task, progress will remain at 0.
     */
    private float progress = 0f;

    /**
     * Keep track of when the task was started and paused. These values are also used to calculate
     * the elapsed time.
     */
    private long startTime;
    private long lastPauseTime;
    private long totalPauseTime;
    private long endTime;

    /**
     * Lock used to pause the current thread when the task is paused.
     */
    protected final Object LOCK = new Object();

    protected boolean requestPause = false;
    protected boolean requestStop = false;

    //Utility methods

    protected void pauseThread(){
        synchronized (LOCK){
            try{

                //Pause
                dispatchPaused();
                Log.e(TAG, "Time elapsed pause: " + getElapsedTime());
                sendOnTaskPaused();

                //Wait until notified to resume
                LOCK.wait();

                //Resume
                dispatchResumed();

            }catch (InterruptedException e){
                if (PrimeNumberFinder.DEBUG)
                    Log.e(TAG, "Task was interrupted while paused!");
            }
        }
    }

    protected void tryPause(){
        if (requestPause){
            synchronized (LOCK){
                try{

                    //Pause
                    dispatchPaused();
                    sendOnTaskPaused();

                    //Wait until notified to resume
                    LOCK.wait();

                    //Resume
                    dispatchResumed();

                }catch (InterruptedException e){
                    if (PrimeNumberFinder.DEBUG)
                        Log.e(TAG, "Task was interrupted while paused!");
                }
            }
        }
    }

    protected boolean shouldStop(){
        return requestStop;
    }

    //Lifecycle methods

    public void start(){
        run();
    }

    protected void dispatchStarted(){
        this.startTime = System.currentTimeMillis();
        setState(State.RUNNING);
        sendOnTaskStarted();
    }

    public void pause(){
        requestPause = true;
    }

    private void dispatchPaused(){
        requestPause = false;
        lastPauseTime = System.currentTimeMillis();
        setState(State.PAUSED);
    }

    public void resume(){
        synchronized (LOCK){
            LOCK.notify();
        }
    }

    private void dispatchResumed(){
        totalPauseTime += (System.currentTimeMillis() - lastPauseTime);
        setState(State.RUNNING);
        sendOnTaskResumed();
    }

    protected void dispatchStopped(){
        setState(State.STOPPED);
    }

    protected void dispatchFinished(){
        setState(State.FINISHED);
    }

    public void stop(){
        requestStop = true;
    }

    //Getters and setters

    public void addTaskListener(final TaskListener taskListener){
        if (!taskListeners.contains(taskListener)) taskListeners.add(taskListener);
    }

    public long getElapsedTime(){
        if (startTime == 0){
            return 0;
        }
        if (state == State.PAUSED){
            return System.currentTimeMillis() - startTime - (System.currentTimeMillis() - lastPauseTime);
        }
        return System.currentTimeMillis() - startTime - totalPauseTime;
    }

    public float getProgress(){
        return progress;
    }

    public void setProgress(float progress){
        this.progress = progress;
        sendOnProgressChanged(progress);
    }

    public State getState(){
        return state;
    }

    private void setState(State state){
        this.state = state;
        switch (state){
            case PAUSED:
                sendOnTaskPaused();
                break;

            case STOPPED:
                sendOnTaskStopped();
                break;

            case FINISHED:
                sendOnTaskFinished();
                break;
        }
    }

    protected long getTotalPauseTime(){
        return totalPauseTime;
    }

    //Callbacks

    private void sendOnTaskStopped(){
        for (TaskListener taskListener : taskListeners){
            taskListener.onTaskStopped();
        }
    }

    private void sendOnTaskStarted(){
        for (TaskListener taskListener : taskListeners){
            taskListener.onTaskStarted();
        }
    }

    private void sendOnTaskPaused(){
        for (TaskListener taskListener : taskListeners){
            taskListener.onTaskPaused();
        }
    }

    private void sendOnTaskResumed(){
        for (TaskListener taskListener : taskListeners){
            taskListener.onTaskResumed();
        }
    }

    private void sendOnTaskFinished(){
        for (TaskListener taskListener : taskListeners){
            taskListener.onTaskFinished();
        }
    }

    protected void sendOnProgressChanged(final float percent){
        for (int i = 0; i < taskListeners.size(); i++){
            taskListeners.get(i).onProgressChanged(percent);
        }
        /*for (TaskListener taskListener : taskListeners){
            taskListener.onProgressChanged(percent);
        }*/
    }
}
