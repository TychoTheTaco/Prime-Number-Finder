package com.tycho.app.primenumberfinder.modules;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.modules.TaskFragment;

import java.text.DecimalFormat;

/**
 * Created by tycho on 11/19/2017.
 */

public abstract class ResultsFragment extends TaskFragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "ResultsFragment";

    protected final DecimalFormat decimalFormat = new DecimalFormat("##0.00");

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
        updateUi();
    }

    @Override
    public void onTaskPaused() {
        super.onTaskPaused();
        updateUi();
    }

    @Override
    public void onTaskResumed() {
        super.onTaskResumed();
        updateUi();
    }

    @Override
    public void onTaskStopped() {
        super.onTaskStopped();
        updateUi();
    }

    @Override
    public void onProgressChanged(float progress) {
        super.onProgressChanged(progress);
        requestUiUpdate();
    }

    protected void updateUi(){
        if (isAdded() && !isDetached()){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onUiUpdate();
                }
            });
        }
    }

    protected void requestUiUpdate(){
        if (System.currentTimeMillis() - lastUiUpdateTime >= PrimeNumberFinder.UPDATE_LIMIT_MS * 30){
            updateUi();
            lastUiUpdateTime = System.currentTimeMillis();
        }
    }

    protected abstract void onUiUpdate();
}
