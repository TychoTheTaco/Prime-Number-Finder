package com.tycho.app.primenumberfinder;

import android.support.v7.app.AppCompatActivity;

import com.tycho.app.primenumberfinder.utils.PreferenceManager;

import java.text.NumberFormat;
import java.util.Locale;

public abstract class AbstractActivity extends AppCompatActivity {

    /**
     * {@linkplain NumberFormat} used for formatting numbers.
     */
    protected final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    @Override
    protected void onResume() {
        super.onResume();
        PrimeNumberFinder.getTaskManager().resumeAllTasks();
    }

    @Override
    protected void onPause() {
        super.onPause();
        PrimeNumberFinder.getTaskManager().saveTaskStates();
        if (!PreferenceManager.getBoolean(PreferenceManager.Preference.ALLOW_BACKGROUND_TASKS)) {
            PrimeNumberFinder.getTaskManager().pauseAllTasks();
        }
    }
}
