package com.tycho.app.primenumberfinder.modules.findprimes.fragments;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.renderscript.ScriptIntrinsicYuvToRGB;
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
    private View numbersPerSecondLayout;
    private View primesPerSecondLayout;

    private TextView textViewElapsedTime;
    private TextView estimatedTimeRemaining;
    private TextView textViewNumbersPerSecond;
    private TextView textViewPrimesPerSecond;

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
        numbersPerSecondLayout = rootView.findViewById(R.id.numbers_per_second_layout);
        primesPerSecondLayout = rootView.findViewById(R.id.primes_per_second_layout);

        init();

        return rootView;
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

    public void updateData(final StatisticData statisticData){
        if (getView() != null){
            //Log.d(TAG, "updateData " + getView());
            setTimeElapsed(statisticData.optLong(Statistic.TIME_ELAPSED));
            textViewNumbersPerSecond.setText(NUMBER_FORMAT.format(statisticData.optInt(Statistic.NUMBERS_PER_SECOND)));
            textViewPrimesPerSecond.setText(NUMBER_FORMAT.format(statisticData.optInt(Statistic.PRIMES_PER_SECOND)));

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
        this.task = task;
        Log.d(TAG, "Set task: " + task);
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

            estimatedTimeRemainingLayout.setVisibility((getTask().getEndValue() == FindPrimesTask.INFINITY) ? View.GONE : View.VISIBLE);
            numbersPerSecondLayout.setVisibility(getTask().getSearchOptions().getSearchMethod() == FindPrimesTask.SearchMethod.BRUTE_FORCE ? View.VISIBLE : View.GONE);
            primesPerSecondLayout.setVisibility(getTask().getSearchOptions().getSearchMethod() == FindPrimesTask.SearchMethod.BRUTE_FORCE ? View.VISIBLE : View.GONE);

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
    public FindPrimesTask getTask() {
        return (FindPrimesTask) super.getTask();
    }

    private long lastCurrentNumber = 0;
    private int lastPrimeCount = 0;
    private long lastUpdate = 0;
    private Task task;

    @Override
    protected void packStatistics(StatisticData statisticData) {
        statisticData.put(Statistic.TIME_ELAPSED, getTask().getElapsedTime());

        if (task != getTask()){
            task = getTask();
            lastCurrentNumber = 0;
            lastPrimeCount = 0;
        }

        //Update statistics
        if (System.currentTimeMillis() - lastUpdate >= 1000){
            statisticData.put(Statistic.ESTIMATED_TIME_REMAINING, getTask().getEstimatedTimeRemaining());
            statisticData.put(Statistic.NUMBERS_PER_SECOND, getTask().getCurrentValue() - lastCurrentNumber);
            statisticData.put(Statistic.PRIMES_PER_SECOND, getTask().getPrimeCount() - lastPrimeCount);
            lastCurrentNumber = getTask().getCurrentValue();
            lastPrimeCount = getTask().getPrimeCount();
            lastUpdate = System.currentTimeMillis();
        }
    }
}
