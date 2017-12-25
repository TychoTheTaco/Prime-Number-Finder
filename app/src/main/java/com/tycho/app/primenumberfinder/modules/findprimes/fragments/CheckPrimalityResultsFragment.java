package com.tycho.app.primenumberfinder.modules.findprimes.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.ResultsFragment;
import com.tycho.app.primenumberfinder.modules.findprimes.CheckPrimalityTask;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import easytasks.Task;

/**
 * Created by tycho on 11/16/2017.
 */

public class CheckPrimalityResultsFragment extends ResultsFragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "CheckPrimalityResultsFgmnt";

    //Views
    private ViewGroup resultsView;
    private TextView noTaskView;
    private TextView title;
    private TextView subtitle;
    private ProgressBar progressBarInfinite;
    private TextView progress;
    private TextView result;

    final DecimalFormat decimalFormat = new DecimalFormat("##0.00");

    private long lastUiUpdateTime = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.check_primality_results_fragment, container, false);

        title = rootView.findViewById(R.id.title);
        subtitle = rootView.findViewById(R.id.subtitle);
        progressBarInfinite = rootView.findViewById(R.id.progressBar_infinite);
        resultsView = rootView.findViewById(R.id.results_view);
        noTaskView = rootView.findViewById(R.id.empty_message);
        progress = rootView.findViewById(R.id.textView_search_progress);
        result = rootView.findViewById(R.id.content);

        updateUi();

        init();

        return rootView;
    }

    //TODO: Maybe dont have a ui update method. Instead create methods like uiStoped(), uiRunning(), uiStarted() and just call those in the listeners.
    @Override
    protected void onUiUpdate() {
        if (getTask() != null) {

            //Update task state
            switch (getTask().getState()) {
                case RUNNING:
                    title.setText(getString(R.string.status_searching));
                    progressBarInfinite.setVisibility(View.VISIBLE);

                    //Set progress
                    progress.setVisibility(View.VISIBLE);
                    progress.setText(getString(R.string.task_progress, decimalFormat.format(getTask().getProgress() * 100)));
                    break;

                case PAUSED:
                    title.setText(getString(R.string.status_paused));
                    progressBarInfinite.setVisibility(View.GONE);

                    //Set progress
                    progress.setVisibility(View.VISIBLE);
                    progress.setText(getString(R.string.task_progress, decimalFormat.format(getTask().getProgress() * 100)));
                    break;

                case STOPPED:
                    progressBarInfinite.setVisibility(View.GONE);
                    title.setText(getString(R.string.status_finished));

                    //Set progress
                    progress.setVisibility(View.GONE);
                    break;
            }
        }
    }

    @Override
    public void onTaskStarted() {
        super.onTaskStarted();
        uiStarted();
    }

    @Override
    public void onTaskStopped() {
        super.onTaskStopped();
        uiStopped();
    }

    @Override
    public CheckPrimalityTask getTask() {
        return (CheckPrimalityTask) super.getTask();
    }

    @Override
    public void setTask(final Task task) {
        super.setTask(task);

        updateUi();

        try {
            init();
        } catch (NullPointerException e) {
        }
    }

    private void init() {
        if (getTask() != null) {

            //Make sure view is visible
            resultsView.setVisibility(View.VISIBLE);
            noTaskView.setVisibility(View.GONE);

            switch (getTask().getState()) {
                case RUNNING:
                    uiStarted();
                    break;

                case PAUSED:
                    uiStarted();
                    break;

                case STOPPED:
                    uiStopped();
                    break;
            }

        } else {
            resultsView.setVisibility(View.GONE);
            noTaskView.setVisibility(View.VISIBLE);
        }
    }

    private void uiStarted() {
        if (isAdded() && !isDetached()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    //Format subtitle
                    final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(getString(R.string.check_primality_task_status, NumberFormat.getInstance(Locale.getDefault()).format(getTask().getNumber())));
                    spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.purple_dark)), 0, spannableStringBuilder.length(), 0);
                    spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, spannableStringBuilder.length(), 0);
                    subtitle.setText(spannableStringBuilder);
                }
            });
        }
    }

    private void uiStopped() {
        if (isAdded() && !isDetached()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    //Format subtitle
                    final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(getString(R.string.check_primality_result, NumberFormat.getInstance(Locale.getDefault()).format(getTask().getNumber()), getTask().isPrime() ? "prime" : "not prime"));
                    spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.purple_dark)), 0, spannableStringBuilder.length(), 0);
                    spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, spannableStringBuilder.length(), 0);
                    subtitle.setText(spannableStringBuilder);
                }
            });
        }
    }
}
