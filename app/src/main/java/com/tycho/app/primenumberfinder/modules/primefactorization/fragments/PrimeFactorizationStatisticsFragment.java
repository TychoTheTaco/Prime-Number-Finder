package com.tycho.app.primenumberfinder.modules.primefactorization.fragments;

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
import com.tycho.app.primenumberfinder.modules.primefactorization.PrimeFactorizationTask;

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

public class PrimeFactorizationStatisticsFragment extends StatisticsFragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FindPrimesStatsFgmnt";

    private ViewGroup statisticsView;
    private TextView noTaskView;

    private TextView textViewElapsedTime;
    private TextView estimatedTimeRemainingTextView;

    private long lastUpdate = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.prime_factorization_statistics_fragment, container, false);

        statisticsView = rootView.findViewById(R.id.statistics_view);
        noTaskView = rootView.findViewById(R.id.empty_message);

        textViewElapsedTime = rootView.findViewById(R.id.textView_elapsed_time);
        estimatedTimeRemainingTextView = rootView.findViewById(R.id.textView_eta);

        init();

        return rootView;
    }

    public void updateData(StatisticData statisticData) {
        if (getView() != null) {
            setTimeElapsed(statisticData.optLong(Statistic.TIME_ELAPSED));

            final String string = formatTime(statisticData.optLong(Statistic.ESTIMATED_TIME_REMAINING));
            if (string.equals("infinity")) {
                estimatedTimeRemainingTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_infinity_white_24dp, 0);

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    for (Drawable drawable : estimatedTimeRemainingTextView.getCompoundDrawables()) {
                        if (drawable != null) {
                            drawable.setColorFilter(new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN));
                        }
                    }
                }

                estimatedTimeRemainingTextView.setText("");
            } else {
                estimatedTimeRemainingTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                estimatedTimeRemainingTextView.setText(string);
            }
        }
    }

    public void setTimeElapsed(final long millis) {
        textViewElapsedTime.setText(formatTime(millis));
    }

    @Override
    public void setTask(Task task) {
        super.setTask(task);
        if (getView() != null) {
            init();
        }
    }

    private void init() {
        if (getTask() == null) {
            statisticsView.setVisibility(View.GONE);
            noTaskView.setVisibility(View.VISIBLE);
        } else {
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
    protected void packStatistics(StatisticData statisticData) {
        statisticData.put(Statistic.TIME_ELAPSED, getTask().getElapsedTime());
        if (System.currentTimeMillis() - lastUpdate >= 1000) {
            statisticData.put(Statistic.ESTIMATED_TIME_REMAINING, getTask().getEstimatedTimeRemaining());
            lastUpdate = System.currentTimeMillis();
        }
    }

    @Override
    public PrimeFactorizationTask getTask() {
        return (PrimeFactorizationTask) super.getTask();
    }
}
