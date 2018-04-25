package com.tycho.app.primenumberfinder.modules;

import android.content.Context;
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

    protected final UIUpdater uiUpdater = new UIUpdater();

    private volatile int updateRequests = 0;

    @Nullable
    @Override
    public abstract View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState);

    @Override
    public void onTaskStarted() {
        super.onTaskStarted();
        if (uiUpdater.getState() == Task.State.NOT_STARTED){
            uiUpdater.startOnNewThread();
        }else{
            uiUpdater.resume(false);
        }
    }

    @Override
    public void onTaskPausing() {
        super.onTaskPausing();
        uiUpdater.resume(false);
    }

    @Override
    public void onTaskPaused() {
        super.onTaskPaused();
        uiUpdater.pause(false);
    }

    @Override
    public void onTaskResuming() {
        super.onTaskResuming();
        uiUpdater.resume(false);
    }

    @Override
    public void onTaskResumed() {
        super.onTaskResumed();
        uiUpdater.resume(false);
    }

    @Override
    public void onTaskStopped() {
        super.onTaskStopped();
        uiUpdater.pause(false);
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
        if (task == null){
            uiUpdater.pause(false);
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
                        if (getView() == null){
                            Log.e(TAG, "ERROR: getView() was null before UI update!");
                        }
                        onUiUpdate();
                        updateRequests--;
                        if (updateRequests == 0){
                            LOCK.notify();
                        }
                    }
                }
            });
        }else{
            Log.w(TAG, "Skipping UI update! Fragment wasn't added or was detached!");
        }
    }

    protected abstract void onUiUpdate();

    protected final class UIUpdater extends Task{

        @Override
        protected void run() {
            while (true) {

                updateUi();

                try {
                    Thread.sleep(1000 / 25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }

                tryPause();
                if (shouldStop()){
                    break;
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUi();
    }
}
