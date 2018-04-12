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
import com.tycho.app.primenumberfinder.utils.Utils;

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
    private static final String TAG = "ChkPrimalityRsltsFgmnt";

    //Views
    private ViewGroup resultsView;
    private TextView noTaskView;
    private TextView title;
    private TextView subtitle;
    private ProgressBar progressBarInfinite;
    private TextView progress;
    private TextView elapsedTimeTextView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.check_primality_results_fragment, container, false);

        title = rootView.findViewById(R.id.title);
        subtitle = rootView.findViewById(R.id.subtitle);
        progressBarInfinite = rootView.findViewById(R.id.progress_bar);
        resultsView = rootView.findViewById(R.id.results_view);
        noTaskView = rootView.findViewById(R.id.empty_message);
        progress = rootView.findViewById(R.id.textView_search_progress);
        elapsedTimeTextView = rootView.findViewById(R.id.textView_elapsed_time);

        init();

        return rootView;
    }

    @Override
    public void onTaskStarted() {
        super.onTaskStarted();
        if (isAdded() && !isDetached()) {
            handler.post(new Runnable() {
                @Override
                public void run() {

                    title.setText(getString(R.string.status_searching));
                    //progressBarInfinite.setVisibility(View.VISIBLE);

                    //Format subtitle
                    final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(getString(R.string.check_primality_task_status, NumberFormat.getInstance(Locale.getDefault()).format(getTask().getNumber())));
                    spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.purple_dark)), 0, spannableStringBuilder.length(), 0);
                    spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, spannableStringBuilder.length(), 0);
                    subtitle.setText(spannableStringBuilder);
                }
            });
        }
    }

    @Override
    public void onTaskPaused() {
        super.onTaskPaused();
        if (isAdded() && !isDetached()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    title.setText(getString(R.string.status_paused));
                    //progressBarInfinite.setVisibility(View.GONE);

                    //Set progress
                    progress.setText(getString(R.string.task_progress, DECIMAL_FORMAT.format(getTask().getProgress() * 100)));
                }
            });
        }
    }

    @Override
    public void onTaskResumed() {
        super.onTaskResumed();
        onTaskStarted();
    }

    @Override
    public void onTaskStopped() {
        super.onTaskStopped();
        updateUi();
        if (isAdded() && !isDetached()) {
            handler.post(new Runnable() {
                @Override
                public void run() {

                    title.setText(getString(R.string.status_finished));
                    //progressBarInfinite.setVisibility(View.GONE);

                    //Set progress
                    //progress.setVisibility(View.GONE);

                    //Format subtitle
                    final String[] splitSubtitle = getString(R.string.check_primality_result).split("%\\d\\$.");
                    final String[] subtitleItems = new String[4 + 1];
                    subtitleItems[0] = splitSubtitle[0];
                    subtitleItems[1] = NumberFormat.getInstance(Locale.getDefault()).format(getTask().getNumber());
                    subtitleItems[2] = splitSubtitle[1];
                    subtitleItems[3] = getTask().isPrime() ? "prime" : "not prime";
                    subtitleItems[4] = splitSubtitle[2];
                    final SpannableStringBuilder subtitleStringBuilder = new SpannableStringBuilder();
                    for (int i = 0; i < subtitleItems.length; i++) {
                        if (i % 2 != 0) {
                            subtitleStringBuilder.append(subtitleItems[i], new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.purple_dark)), 0);
                            subtitleStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), subtitleStringBuilder.length() - subtitleItems[i].length(), subtitleStringBuilder.length(), 0);
                        } else {
                            subtitleStringBuilder.append(subtitleItems[i]);
                        }
                    }
                    subtitle.setText(subtitleStringBuilder);
                }
            });
        }
    }

    @Override
    protected void onUiUpdate() {
        //Update progress
        progress.setText(getString(R.string.task_progress, DECIMAL_FORMAT.format(getTask().getProgress() * 100)));
        progressBarInfinite.setMax(10_000);
        progressBarInfinite.setProgress((int) (getTask().getProgress() * 10_000));

        elapsedTimeTextView.setText(Utils.formatTimeHuman(getTask().getElapsedTime(), 2));
    }

    @Override
    public CheckPrimalityTask getTask() {
        return (CheckPrimalityTask) super.getTask();
    }

    @Override
    public void setTask(final Task task) {
        super.setTask(task);
        if (getView() != null) {
            init();
        }
    }

    private void init() {
        if (getTask() != null) {

            //Make sure view is visible
            resultsView.setVisibility(View.VISIBLE);
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

        } else {
            resultsView.setVisibility(View.GONE);
            noTaskView.setVisibility(View.VISIBLE);
        }
    }
}
