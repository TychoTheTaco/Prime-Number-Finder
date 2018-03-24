package com.tycho.app.primenumberfinder.modules;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.Statistic;
import com.tycho.app.primenumberfinder.StatisticData;

import org.json.JSONException;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.TreeSet;

import easytasks.Task;
import easytasks.TaskAdapter;

/**
 * A {@linkplain StatisticsFragment} is a {@linkplain Fragment} that display statistics about a particular task
 * (such as time elapsed or numbers scanned per second).
 *
 * @author Tycho Bellers
 *         Date Created: 5/19/2017
 */
public abstract class StatisticsFragment extends TaskFragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "StatisticsFragment";

    /**
     * This key is used to store the {@link StatisticData} object when saving and restoring the
     * fragment state.
     */
    protected static final String KEY_STATISTIC_DATA = "statisticData";

    /**
     * This {@link StatisticData} holds all of the data for the statistics.
     */
    private StatisticData statisticData = new StatisticData();

    private final UIUpdater uiUpdater = new UIUpdater();

    protected static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    //Override methods

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (statisticData != null) {
            outState.putString(KEY_STATISTIC_DATA, statisticData.toString());
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (statisticData == null) {
            updateData(new StatisticData());
        } else {
            updateData(statisticData);
        }
    }

    @Override
    public void onTaskStarted() {
        super.onTaskStarted();
        if (uiUpdater.getState() == Task.State.NOT_STARTED) {
            uiUpdater.startOnNewThreadAndWait();
        } else {
            uiUpdater.resume();
        }
    }

    @Override
    public void onTaskPaused() {
        super.onTaskPaused();
        uiUpdater.pause(false);
        packStatistics(statisticData);
        handler.post(new Runnable() {
            @Override
            public void run() {
                updateData(statisticData);
            }
        });
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
        packStatistics(statisticData);
        handler.post(new Runnable() {
            @Override
            public void run() {
                updateData(statisticData);
            }
        });
    }

    //Abstract methods

    /**
     * Update the fragment with new data.
     *
     * @param data The new statistic data.
     */
    protected abstract void updateData(final StatisticData data);

    protected abstract void packStatistics(final StatisticData statisticData);

    //Getters and setters

    public StatisticData getStatisticData() {
        return statisticData;
    }

    public void setStatisticData(final StatisticData statisticData) {
        this.statisticData = statisticData;
        if (this.statisticData != null) {
            updateData(statisticData);
        }
    }

    protected class UIUpdater extends Task {

        @Override
        protected void run() {
            while (true) {
                if (getTask() != null) {
                    packStatistics(statisticData);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateData(statisticData);
                        }
                    });
                }

                tryPause();
                if (shouldStop()) {
                    break;
                }

                try {
                    Thread.sleep(PrimeNumberFinder.UPDATE_LIMIT_MS * 2);
                } catch (InterruptedException e) {
                    //Ignore exception
                }
            }
        }
    }
}
