package com.tycho.app.primenumberfinder.activities;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.utils.PreferenceManager;

import java.text.NumberFormat;
import java.util.Locale;

public abstract class AbstractActivity extends AppCompatActivity {

    /**
     * Default {@linkplain NumberFormat} used for formatting numbers.
     */
    protected final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (PreferenceManager.getInt(PreferenceManager.Preference.THEME) == 1){
            setTheme(R.style.Dark_NoActionBar);
        }
    }

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
