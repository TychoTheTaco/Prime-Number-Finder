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
import com.tycho.app.primenumberfinder.modules.StatisticsFragment;
import com.tycho.app.primenumberfinder.modules.TaskFragment;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;

import org.json.JSONException;

import java.text.NumberFormat;
import java.util.Locale;

import easytasks.Task;
import easytasks.TaskAdapter;
import easytasks.TaskListener;

import static com.tycho.app.primenumberfinder.utils.Utils.formatTime;

/**
 * Created by tycho on 10/6/2017.
 */

public class FindFactorsStatisticsFragment extends StatisticsFragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FindFactorsStatsFgmnt";

    private ViewGroup statisticsView;
    private TextView noTaskView;

    private TextView textViewElapsedTime;
    private TextView estimatedTimeRemaining;
    private TextView textViewNumbersPerSecond;

    private View estimatedTimeRemainingLayout;

    private long lastUpdate = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.find_factors_statistics_fragment, container, false);

        statisticsView = rootView.findViewById(R.id.statistics_view);
        noTaskView = rootView.findViewById(R.id.empty_message);

        textViewElapsedTime = rootView.findViewById(R.id.textView_elapsed_time);
        estimatedTimeRemaining = rootView.findViewById(R.id.textView_eta);
        textViewNumbersPerSecond = rootView.findViewById(R.id.textView_numbers_per_second);
        estimatedTimeRemainingLayout = rootView.findViewById(R.id.estimated_time_remaining_layout);

        init();

        return rootView;
    }

    public void updateData(StatisticData statisticData){
        //Log.d(TAG, "updateData " + getView());
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
        if (getView() != null) {
            init();
        }
    }

    private void init(){
        if (getTask() == null){
            statisticsView.setVisibility(View.GONE);
            noTaskView.setVisibility(View.VISIBLE);
        }else{
            statisticsView.setVisibility(View.VISIBLE);
            noTaskView.setVisibility(View.GONE);

            switch (getTask().getState()) {
                case RUNNING:
                    onTaskStarted();
                    break;

                case PAUSED:
                    onTaskPaused();
                    break;

                case STOPPED:
                    onTaskStopped();
                    break;
            }
        }
    }

    @Override
    public void onTaskStopped() {
        super.onTaskStopped();
        if (isAdded() && !isDetached()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    estimatedTimeRemainingLayout.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public FindFactorsTask getTask() {
        return (FindFactorsTask) super.getTask();
    }

    @Override
    protected void packStatistics(StatisticData statisticData) {
        statisticData.put(Statistic.TIME_ELAPSED, getTask().getElapsedTime());
        statisticData.put(Statistic.NUMBERS_PER_SECOND, getTask().getNumbersPerSecond());
        if (System.currentTimeMillis() - lastUpdate >= 1000){
            statisticData.put(Statistic.ESTIMATED_TIME_REMAINING, getTask().getEstimatedTimeRemaining());
            lastUpdate = System.currentTimeMillis();
        }
    }
}
