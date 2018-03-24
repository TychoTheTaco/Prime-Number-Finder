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

    /**
     * The last time in milliseconds that the UI was updated.
     */
    private long lastUiUpdateTime = 0;

    @Nullable
    @Override
    public abstract View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState);

    @Override
    public void onTaskStarted() {
        super.onTaskStarted();
        if (uiUpdater.getState() == Task.State.NOT_STARTED){
            uiUpdater.startOnNewThread();
        }else{
            uiUpdater.resume();
        }
    }

    @Override
    public void onTaskPaused() {
        super.onTaskPaused();
        uiUpdater.pause(false);
    }

    @Override
    public void onTaskResumed() {
        super.onTaskResumed();
        uiUpdater.resume();
    }

    @Override
    public void onTaskStopped() {
        super.onTaskStopped();
        uiUpdater.pause(false);
    }

    protected void updateUi(){
        if (isAdded() && !isDetached()){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onUiUpdate();
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

                if (getTask() == null) pause(false);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateUi();
                    }
                });

                try {
                    Thread.sleep(1000 / 2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }

                tryPause();
            }
        }
    }
}
