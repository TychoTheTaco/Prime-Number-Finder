package com.tycho.app.primenumberfinder;

import android.support.v7.app.AppCompatActivity;

public abstract class AbstractActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
        PrimeNumberFinder.getTaskManager().resumeAllTasks();
    }

    @Override
    protected void onPause() {
        super.onPause();
        PrimeNumberFinder.getTaskManager().saveTaskStates();
        if (!PrimeNumberFinder.getPreferenceManager().isAllowBackgroundTasks()) {
            PrimeNumberFinder.getTaskManager().pauseAllTasks();
        }
    }
}
