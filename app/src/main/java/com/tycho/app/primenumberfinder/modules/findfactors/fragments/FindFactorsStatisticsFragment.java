package com.tycho.app.primenumberfinder.modules.findfactors.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.PrimeNumberFinder;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.Statistic;
import com.tycho.app.primenumberfinder.StatisticData;
import com.tycho.app.primenumberfinder.modules.TaskFragment;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;

import org.json.JSONException;

import java.text.NumberFormat;
import java.util.Locale;

import easytasks.Task;
import easytasks.TaskListener;

import static com.tycho.app.primenumberfinder.utils.Utils.formatTime;

/**
 * Created by tycho on 10/6/2017.
 */

public class FindFactorsStatisticsFragment extends TaskFragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FindFactorsStatsFgmnt";

    private ViewGroup statisticsView;
    private TextView noTaskView;

    private TextView textViewElapsedTime;
    private TextView estimatedTimeRemaining;
    private TextView textViewNumbersPerSecond;

    private final UiUpdater uiUpdater = new UiUpdater();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.find_factors_statistics_fragment, container, false);

        statisticsView = rootView.findViewById(R.id.statistics_view);
        noTaskView = rootView.findViewById(R.id.empty_message);

        textViewElapsedTime = rootView.findViewById(R.id.textView_elapsed_time);
        estimatedTimeRemaining = rootView.findViewById(R.id.textView_eta);
        textViewNumbersPerSecond = rootView.findViewById(R.id.textView_numbers_per_second);

        return rootView;
    }

    public void updateData(StatisticData statisticData){
        if (getView() != null){
            setTimeElapsed(statisticData.optLong(Statistic.TIME_ELAPSED));
            textViewNumbersPerSecond.setText(NumberFormat.getInstance().format(statisticData.optInt(Statistic.NUMBERS_PER_SECOND)));
            estimatedTimeRemaining.setText(formatTime(statisticData.optLong(Statistic.ESTIMATED_TIME_REMAINING)));
        }
    }

    public void setTimeElapsed(final long millis){
        textViewElapsedTime.setText(formatTime(millis));
    }

    @Override
    public void setTask(Task task) {
        super.setTask(task);

        if (task == null){
            statisticsView.setVisibility(View.GONE);
            noTaskView.setVisibility(View.VISIBLE);
            uiUpdater.pause();
        }else{
            statisticsView.setVisibility(View.VISIBLE);
            noTaskView.setVisibility(View.GONE);

            final StatisticData statisticData = new StatisticData();
            try {
                statisticData.put(Statistic.TIME_ELAPSED, getTask().getElapsedTime());
                statisticData.put(Statistic.NUMBERS_PER_SECOND, getTask().getNumbersPerSecond());
                statisticData.put(Statistic.ESTIMATED_TIME_REMAINING, getTask().getEstimatedTimeRemaining());
            }catch (JSONException e){}
            updateData(statisticData);

            //Start UI updater
            if (uiUpdater.getState() == Task.State.NOT_STARTED) {
                uiUpdater.addTaskListener(new TaskListener() {
                    @Override
                    public void onTaskStarted() {
                        Log.d(TAG, "UI updater started");
                    }

                    @Override
                    public void onTaskPaused() {
                        Log.d(TAG, "UI updater paused");
                    }

                    @Override
                    public void onTaskResumed() {
                        Log.d(TAG, "UI updater resumed");
                    }

                    @Override
                    public void onTaskStopped() {
                        Log.d(TAG, "UI updater stopped");
                    }

                    @Override
                    public void onProgressChanged(float v) {

                    }
                });
                uiUpdater.startOnNewThread();
            }

            switch (task.getState()){
                case RUNNING:
                    uiUpdater.resume();
                    break;

                case PAUSED:
                    uiUpdater.pause();
                    break;

                case STOPPED:
                    uiUpdater.pause();
                    break;
            }

        }
    }

    @Override
    public void onTaskPaused() {
        super.onTaskPaused();
        uiUpdater.pause();
    }

    @Override
    public void onTaskResumed() {
        super.onTaskResumed();
        uiUpdater.resume();
    }

    @Override
    public FindFactorsTask getTask() {
        return (FindFactorsTask) super.getTask();
    }

    private class UiUpdater extends Task {

        private StatisticData statisticData = new StatisticData();

        private long lastUpdate;

        @Override
        protected void run() {
            while (true) {

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (getTask() != null){
                            try {
                                statisticData.put(Statistic.TIME_ELAPSED, getTask().getElapsedTime());
                                statisticData.put(Statistic.NUMBERS_PER_SECOND, getTask().getNumbersPerSecond());
                                if (System.currentTimeMillis() - lastUpdate >= 1000){
                                    statisticData.put(Statistic.ESTIMATED_TIME_REMAINING, getTask().getEstimatedTimeRemaining());
                                    lastUpdate = System.currentTimeMillis();
                                }
                            }catch (JSONException e){}

                            updateData(statisticData);
                        }
                    }
                });

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
