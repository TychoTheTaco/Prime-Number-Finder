package com.tycho.app.primenumberfinder.modules.findprimes.fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
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
import com.tycho.app.primenumberfinder.modules.StatisticsFragment;
import com.tycho.app.primenumberfinder.modules.TaskFragment;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;

import org.json.JSONException;

import java.text.NumberFormat;
import java.util.Locale;

import easytasks.Task;
import easytasks.TaskAdapter;
import easytasks.TaskListener;

import static com.tycho.app.primenumberfinder.utils.Utils.formatTime;

/**
 * Created by tycho on 10/19/2017.
 */

public class FindPrimesStatisticsFragment extends StatisticsFragment{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FindPrimesStatsFgmnt";

    private ViewGroup statisticsView;
    private TextView noTaskView;

    private View estimatedTimeRemainingLayout;

    private TextView textViewElapsedTime;
    private TextView estimatedTimeRemaining;
    private TextView textViewNumbersPerSecond;
    private TextView textViewPrimesPerSecond;

    private final UiUpdater uiUpdater = new UiUpdater();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.find_primes_statistics_fragment, container, false);

        statisticsView = rootView.findViewById(R.id.statistics_view);
        noTaskView = rootView.findViewById(R.id.empty_message);

        textViewElapsedTime = rootView.findViewById(R.id.textView_elapsed_time);
        estimatedTimeRemaining = rootView.findViewById(R.id.textView_eta);
        textViewNumbersPerSecond = rootView.findViewById(R.id.textView_numbers_per_second);
        textViewPrimesPerSecond = rootView.findViewById(R.id.textView_primes_per_second);

        estimatedTimeRemainingLayout = rootView.findViewById(R.id.estimated_time_remaining_layout);

        try {
            init();
        }catch (NullPointerException e){}

        return rootView;
    }

    public void updateData(final StatisticData statisticData){
        if (getView() != null){
            setTimeElapsed(statisticData.optLong(Statistic.TIME_ELAPSED));
            textViewNumbersPerSecond.setText(NumberFormat.getInstance().format(statisticData.optInt(Statistic.NUMBERS_PER_SECOND)));
            textViewPrimesPerSecond.setText(NumberFormat.getInstance().format(statisticData.optInt(Statistic.PRIMES_PER_SECOND)));

            //Display estimated time remaining
            final String string = formatTime(statisticData.optLong(Statistic.ESTIMATED_TIME_REMAINING));
            if (string.equals("infinity")){
                estimatedTimeRemaining.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_infinity_white_24dp, 0);

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                    for (Drawable drawable : estimatedTimeRemaining.getCompoundDrawables()) {
                        if (drawable != null) {
                            drawable.setColorFilter(new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN));
                        }
                    }
                }

                estimatedTimeRemaining.setText("");
            }else{
                estimatedTimeRemaining.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                estimatedTimeRemaining.setText(string);
            }
        }
    }

    public void setTimeElapsed(final long millis){
        textViewElapsedTime.setText(formatTime(millis));
    }

    @Override
    public void setTask(Task task) {
        super.setTask(task);

        try {
            init();
        }catch (NullPointerException e){}

        if (task != null){
            //Start UI updater
            if (uiUpdater.getState() == Task.State.NOT_STARTED) {
                uiUpdater.addTaskListener(new TaskAdapter() {
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
                });
                uiUpdater.startOnNewThread();
            }

            switch (getTask().getState()){
                case RUNNING:
                    uiUpdater.resume();
                    break;

                case PAUSED:
                case STOPPED:
                    uiUpdater.pause();
                    break;
            }
        }

    }

    private void init(){
        if (getTask() == null){
            statisticsView.setVisibility(View.GONE);
            noTaskView.setVisibility(View.VISIBLE);
            if (uiUpdater.getState() != Task.State.NOT_STARTED) uiUpdater.pause();
        }else{
            statisticsView.setVisibility(View.VISIBLE);
            noTaskView.setVisibility(View.GONE);

            estimatedTimeRemainingLayout.setVisibility((getTask().getEndValue() == FindPrimesTask.INFINITY) ? View.GONE : View.VISIBLE);

            final StatisticData statisticData = new StatisticData();
            try {
                statisticData.put(Statistic.TIME_ELAPSED, getTask().getElapsedTime());
                statisticData.put(Statistic.NUMBERS_PER_SECOND, getTask().getNumbersPerSecond());
                statisticData.put(Statistic.PRIMES_PER_SECOND, getTask().getPrimesPerSecond());
                statisticData.put(Statistic.ESTIMATED_TIME_REMAINING, getTask().getEstimatedTimeRemaining());
            }catch (JSONException e){
                e.printStackTrace();
            }
            setStatisticData(statisticData);
        }
    }

    @Override
    public void onTaskStarted() {
        super.onTaskStarted();

        //Start UI updater
        if (uiUpdater.getState() == Task.State.NOT_STARTED) {
            uiUpdater.addTaskListener(new TaskAdapter() {
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
            });
            uiUpdater.startOnNewThread();
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
    public void onTaskStopped() {
        super.onTaskStopped();
        uiUpdater.pause();
        if (getTask() != null){
            try {
                final StatisticData statisticData = new StatisticData();
                statisticData.put(Statistic.TIME_ELAPSED, getTask().getElapsedTime());
                statisticData.put(Statistic.ESTIMATED_TIME_REMAINING, getTask().getEstimatedTimeRemaining());
                statisticData.put(Statistic.NUMBERS_PER_SECOND, getTask().getNumbersPerSecond());
                statisticData.put(Statistic.PRIMES_PER_SECOND, getTask().getPrimesPerSecond());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateData(statisticData);
                    }
                });
            }catch (JSONException e){}
        }
    }

    @Override
    public FindPrimesTask getTask() {
        return (FindPrimesTask) super.getTask();
    }

    private class UiUpdater extends Task {

        private StatisticData statisticData = new StatisticData();

        private long lastUpdate;

        @Override
        protected void run() {
            while (true) {

                if (getTask() != null){
                    try {
                        statisticData.put(Statistic.TIME_ELAPSED, getTask().getElapsedTime());
                        statisticData.put(Statistic.NUMBERS_PER_SECOND, getTask().getNumbersPerSecond());
                        statisticData.put(Statistic.PRIMES_PER_SECOND, getTask().getPrimesPerSecond());
                        if (System.currentTimeMillis() - lastUpdate >= 1000){
                            statisticData.put(Statistic.ESTIMATED_TIME_REMAINING, getTask().getEstimatedTimeRemaining());
                            lastUpdate = System.currentTimeMillis();
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (getState() != State.PAUSED){
                                updateData(statisticData);
                            }
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
