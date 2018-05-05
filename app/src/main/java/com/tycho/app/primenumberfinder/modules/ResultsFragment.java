package com.tycho.app.primenumberfinder.modules;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import easytasks.Task;

/**
 * Created by tycho on 11/19/2017.
 */

public abstract class ResultsFragment extends TaskFragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = ResultsFragment.class.getSimpleName();

    protected final UIUpdater uiUpdater = new UIUpdater();

    @Nullable
    @Override
    public abstract View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState);

    @Override
    public void onTaskStarted() {
        super.onTaskStarted();
        Log.e(TAG, "onTaskStarted(): " + this);
        if (uiUpdater.getState() == Task.State.NOT_STARTED) {
            uiUpdater.startOnNewThread();
        } else {
            uiUpdater.resume(false);
        }
    }

    @Override
    public void onTaskPausing() {
        super.onTaskPausing();
        Log.e(TAG, "onTaskPausing(): " + this);
        uiUpdater.resume(false);
    }

    @Override
    public void onTaskPaused() {
        super.onTaskPaused();
        Log.e(TAG, "onTaskPaused(): " + this);
        uiUpdater.pause(false);
    }

    @Override
    public void onTaskResuming() {
        super.onTaskResuming();
        Log.e(TAG, "onTaskResuming(): " + this);
        uiUpdater.resume(false);
    }

    @Override
    public void onTaskResumed() {
        super.onTaskResumed();
        Log.e(TAG, "onTaskResumed(): " + this);
        uiUpdater.resume(false);
    }

    @Override
    public void onTaskStopped() {
        super.onTaskStopped();
        Log.e(TAG, "onTaskStopped(): " + this);
        uiUpdater.pause(false);
    }

    /**
     * This method is synchronized to ensure that the task is not changed during a call to {@linkplain #updateUi()}.
     * @param task
     */
    @Override
    public synchronized void setTask(Task task) {
        if (task == null || task.getState() != Task.State.RUNNING) {
            uiUpdater.pause(false);
        }
        super.setTask(task);
    }

    /**
     * Update the UI immediately if the fragment is added and not detached from it's context (typically an Activity).
     * This method is synchronized to ensure that {@linkplain #getTask()} returns the same task throughout the method.
     */
    protected synchronized void updateUi() {
        if (isAdded() && !isDetached()) {
            onUiUpdate();
        } else {
            Log.w(TAG, "Fragment not added or is detached! Dropping UI update: " + this);
        }
    }

    protected abstract void onUiUpdate();

    protected final class UIUpdater extends Task {

        @Override
        protected void run() {
            while (true) {

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateUi();
                    }
                });

                try {
                    Thread.sleep(1000 / 25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }

                tryPause();
                if (shouldStop()) {
                    break;
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause(): " + this);

        //Remove task listener
        if (getTask() != null) {
            getTask().removeTaskListener(this);
        }

        uiUpdater.pause(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume(): " + this);

        //Add task listener
        if (getTask() != null) {
            getTask().addTaskListener(this);
        }

        updateUi();

        if (getTask() != null) {
            switch (getTask().getState()) {
                case RUNNING:
                    onTaskStarted();
                    break;

                case PAUSING:
                    onTaskPausing();
                    break;

                case PAUSED:
                    onTaskPaused();
                    break;

                case RESUMING:
                    onTaskResuming();
                    break;

                case STOPPING:
                    onTaskStopping();
                    break;

                case STOPPED:
                    onTaskStopped();
                    break;
            }
        }
    }
}
