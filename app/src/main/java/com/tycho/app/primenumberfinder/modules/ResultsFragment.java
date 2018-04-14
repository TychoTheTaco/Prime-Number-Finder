package com.tycho.app.primenumberfinder.modules;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.modules.TaskFragment;

import java.text.DecimalFormat;

import easytasks.Task;
import easytasks.TaskAdapter;

/**
 * Created by tycho on 11/19/2017.
 */

public abstract class ResultsFragment extends TaskFragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "ResultsFragment";

    /**
     * Decimal format used for displaying task progress.
     */
    protected static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##0.00");

    protected final UIUpdater uiUpdater = new UIUpdater();

    private volatile int updateRequests = 0;

    @Nullable
    @Override
    public abstract View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState);

    @Override
    public void onTaskStarted() {
        super.onTaskStarted(); //This sometimes gets called twice but only in FindFactorsResults fragment and PrimeFactorization fragment
        Log.d(TAG, "onTaskStarted(): " + getTask());
        if (uiUpdater.getState() == Task.State.NOT_STARTED){
            Log.w(TAG, "Starting task " + uiUpdater + " for " + this + " on new thread with state: " + uiUpdater.getState());
            uiUpdater.startOnNewThread();
            uiUpdater.addTaskListener(new TaskAdapter(){
                @Override
                public void onTaskPaused() {
                    Log.d(TAG, "UI Updater paused: " + uiUpdater);
                }

                @Override
                public void onTaskResumed() {
                    Log.d(TAG, "UI Updater resumed: " + uiUpdater);
                }
            });
        }else{
            uiUpdater.resume();
        }
    }

    @Override
    public void onTaskPausing() {
        super.onTaskPausing();
        uiUpdater.resume();
    }

    @Override
    public void onTaskPaused() {
        super.onTaskPaused();
        uiUpdater.pause(false);
    }

    @Override
    public void onTaskResuming() {
        super.onTaskResuming();
        uiUpdater.resume();
    }

    @Override
    public void onTaskResumed() {
        super.onTaskResumed();
        uiUpdater.resume();
    }

    @Override
    public void onTaskStopped() {
        super.onTaskStopped();
        Log.d(TAG, "onTaskStopped(): " + getTask());
        uiUpdater.pause(true);
        Log.d(TAG, "After pause returned.");
    }

    /**
     * Make sure the task isn't changed while the UI is updating.
     * @param task
     */
    @Override
    public void setTask(Task task) {
        synchronized (LOCK){
            try {
                if (updateRequests > 0){
                    LOCK.wait();
                }
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        super.setTask(task);
    }

    private static final Object LOCK = new Object();

    protected void updateUi(){
        if (isAdded() && !isDetached()){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    synchronized (LOCK){
                        updateRequests++;
                        onUiUpdate();
                        updateRequests--;
                        if (updateRequests == 0){
                            LOCK.notify();
                        }
                    }
                }
            });
        }else{
            Log.d(TAG, "Wasn't added or was detached!");
        }
    }

    protected abstract void onUiUpdate();

    protected final class UIUpdater extends Task{

        @Override
        protected void run() {
            while (true) {

                if (getTask() == null){
                    Log.w(TAG, "Pausing because task was null");
                    pause(true);
                }

                //Log.d(TAG, "Sending UI update! State: " + getState());
                updateUi();

                try {
                    Thread.sleep(1000 / 25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }

                tryPause();
            }
        }
    }
}
