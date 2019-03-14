package com.tycho.app.primenumberfinder.modules.findprimes.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;

import com.tycho.app.primenumberfinder.LongClickLinkMovementMethod;
import com.tycho.app.primenumberfinder.ProgressDialog;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.ResultsFragment;
import com.tycho.app.primenumberfinder.modules.StatisticsLayout;
import com.tycho.app.primenumberfinder.modules.findprimes.DisplayPrimesActivity;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import easytasks.ITask;
import easytasks.Task;

import static com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask.SearchOptions.SearchMethod.BRUTE_FORCE;

/**
 * Created by tycho on 11/16/2017.
 */

public class FindPrimesResultsFragment extends ResultsFragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = FindPrimesResultsFragment.class.getSimpleName();

    //Views
    private TextView subtitleTextView;
    private TextView bodyTextView;

    //Statistics
    private StatisticsLayout statisticsLayout;

    private boolean showStatistics = true;

    /**
     * This {@link SpannableStringBuilder} is used to format any text displayed in
     * {@link FindPrimesResultsFragment#subtitleTextView}.
     */
    final SpannableStringBuilder subtitleStringBuilder = new SpannableStringBuilder();

    final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

    /**
     * This map holds the statistics for each task. When {@linkplain FindPrimesResultsFragment#setTask(ITask)} is called,
     * the current task's statistics are saved to the map so that they can be used later when
     * {@linkplain FindPrimesResultsFragment#setTask(ITask)} is called with the same task.
     */
    private final Map<FindPrimesTask, Statistics> statisticsMap = new HashMap<>();

    /**
     * This class keeps the statistics for a task.
     */
    private class Statistics {
        private long lastPrimeCount;
        private long lastUpdateTime = -1000;
        private long finalNumbersPerSecond;
        private long finalPrimesPerSecond;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.find_primes_results_fragment, container, false);

        initStandardViews(rootView);

        subtitleTextView = rootView.findViewById(R.id.subtitle);
        subtitleTextView.setMovementMethod(LongClickLinkMovementMethod.getInstance());
        bodyTextView = rootView.findViewById(R.id.text);

        //Statistics
        statisticsLayout = new StatisticsLayout(rootView.findViewById(R.id.statistics_layout));
        statisticsLayout.add("eta", R.drawable.ic_timer_white_24dp);
        statisticsLayout.add("nps", R.drawable.ic_trending_up_white_24dp);
        statisticsLayout.add("pps", R.drawable.ic_trending_up_white_24dp);
        statisticsLayout.inflate();
        statisticsLayout.hide("nps");

        viewAllButton.setOnClickListener(v -> {
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Loading...");
            progressDialog.show();

            new Thread(() -> {

                //Pause the task
                final Task.State state = getTask().getState();
                try {
                    getTask().pauseAndWait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //Save to file
                final File file;
                if (getTask().getState() == Task.State.STOPPED && getTask().isSaved()){
                    // Task is stopped and saved already, load saved file
                    file = FileManager.buildFile(getTask());
                }else{
                    // Task has not finished or is not saved, saved to temp file
                    file = new File(getTask().getCacheDirectory() + File.separator + "primes");
                    getTask().saveToFile(file);
                }

                //Restore task state
                if (state == Task.State.RUNNING) {
                    getTask().resume();
                }

                handler.post(progressDialog::dismiss);

                final Intent intent = new Intent(getActivity(), DisplayPrimesActivity.class);
                intent.putExtra("filePath", file.getAbsolutePath());
                intent.putExtra("enableSearch", true);
                getActivity().startActivity(intent);
            }).start();
        });

        Log.d(TAG, "onCreate(): init()");
        initDefaultState();

        return rootView;
    }

    @Override
    protected void postDefaults() {
        super.postDefaults();

        //Subtitle
        subtitleTextView.setText(Utils.formatSpannable(subtitleStringBuilder, getString(R.string.find_primes_subtitle), new String[]{
                NUMBER_FORMAT.format(getTask().getStartValue()),
                getTask().isEndless() ? getString(R.string.infinity_text) : NUMBER_FORMAT.format(getTask().getEndValue()),
                getTask().getSearchOptions().getSearchMethod() == BRUTE_FORCE ? "brute force" : "the sieve of Eratosthenes"
        }, new boolean[]{
                true,
                !getTask().isEndless(),
                false
        }, getTextHighlight(), getContext()));

        //Statistics
        if (getTask().isEndless()) statisticsLayout.hide("eta");
        statisticsLayout.hide("nps");
        statisticsLayout.set("pps", Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.primes_per_second), new String[]{NUMBER_FORMAT.format(statisticsMap.get(getTask()).finalPrimesPerSecond)}, getTextHighlight()));
    }

    @Override
    protected void onPostStarted() {
        super.onPostStarted();

        //Buttons
        switch (getTask().getSearchOptions().getSearchMethod()) {
            case BRUTE_FORCE:
                viewAllButton.setVisibility(View.VISIBLE);
                break;

            case SIEVE_OF_ERATOSTHENES:
                viewAllButton.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    protected void onPostPausing() {
        super.onPostPausing();

        //Buttons
        switch (getTask().getSearchOptions().getSearchMethod()) {
            case BRUTE_FORCE:
                saveButton.setVisibility(View.VISIBLE);
                viewAllButton.setVisibility(View.VISIBLE);
                break;

            case SIEVE_OF_ERATOSTHENES:
                saveButton.setVisibility(View.GONE);
                viewAllButton.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    protected void onPostPaused() {
        super.onPostPaused();

        //Buttons
        switch (getTask().getSearchOptions().getSearchMethod()) {
            case BRUTE_FORCE:
                saveButton.setVisibility(View.VISIBLE);
                viewAllButton.setVisibility(View.VISIBLE);
                break;

            case SIEVE_OF_ERATOSTHENES:
                saveButton.setVisibility(View.GONE);
                viewAllButton.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    protected void onPostResuming() {
        super.onPostResuming();

        //Buttons
        switch (getTask().getSearchOptions().getSearchMethod()) {
            case BRUTE_FORCE:
                saveButton.setVisibility(View.VISIBLE);
                viewAllButton.setVisibility(View.VISIBLE);
                break;

            case SIEVE_OF_ERATOSTHENES:
                saveButton.setVisibility(View.GONE);
                viewAllButton.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    protected void onPostResumed() {
        super.onPostResumed();
        onPostStarted();
    }

    @Override
    protected void onPostStopped() {
        super.onPostStopped();

        //Subtitle
        subtitleTextView.setText(Utils.formatSpannable(subtitleStringBuilder, getResources().getQuantityString(R.plurals.find_primes_subtitle_result, getTask().getPrimeCount()), new String[]{
                NUMBER_FORMAT.format(getTask().getPrimeCount()),
                NUMBER_FORMAT.format(getTask().getStartValue()),
                getTask().isEndless() ? getString(R.string.infinity_text) : NUMBER_FORMAT.format(getTask().getEndValue()),
        }, new boolean[]{
                true,
                true,
                !getTask().isEndless()
        }, getTextHighlight(), getContext()));

        //Body
        bodyTextView.setVisibility(View.GONE);

        //Statistics
        statisticsLayout.hide("eta");
        double elapsed = (double) getTask().getElapsedTime() / 1000;
        if (elapsed <= 0) {
            elapsed = 1;
        }

        statisticsLayout.show("nps");
        statisticsMap.get(getTask()).finalNumbersPerSecond = (long) ((getTask().getEndValue() - getTask().getStartValue()) / elapsed);
        statisticsLayout.set("nps", Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.average_numbers_per_second), new String[]{NUMBER_FORMAT.format(statisticsMap.get(getTask()).finalNumbersPerSecond)}, getTextHighlight()));
        statisticsLayout.set("pps", Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.average_primes_per_second), new String[]{NUMBER_FORMAT.format((long) (getTask().getPrimeCount() / elapsed))}, getTextHighlight()));
    }

    @Override
    protected void onUiUpdate() {
        if (getTask() != null) {

            //Update progress
            if (!getTask().isEndless()) {
                progress.setText(String.valueOf((int) (getTask().getProgress() * 100)));
                progressBar.setProgress((int) (getTask().getProgress() * 100));
            } else {
                progressBar.setProgress(50);
            }

            //Body
            switch (getTask().getSearchOptions().getSearchMethod()) {
                case BRUTE_FORCE:
                    bodyTextView.setText(Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.find_primes_body_text), new String[]{NUMBER_FORMAT.format(getTask().getPrimeCount())}, getTextHighlight()));
                    break;

                case SIEVE_OF_ERATOSTHENES:
                    switch (getTask().getStatus()) {
                        default:
                            bodyTextView.setText("Preparing...");
                            break;

                        case "counting":
                            bodyTextView.setText(Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.find_primes_body_text_sieve_counting), new String[]{NUMBER_FORMAT.format(getTask().getPrimeCount())}, getTextHighlight()));
                            break;

                        case "searching":
                            bodyTextView.setText(Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.find_primes_body_text_sieve_marking), new String[]{NUMBER_FORMAT.format(getTask().getCurrentFactor())}, getTextHighlight()));
                            break;
                    }
                    break;
            }

            //Time remaining
            if (statisticsLayout.isVisible("eta")) {
                statisticsLayout.set("eta", Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.time_remaining), new String[]{Utils.formatTimeHuman(getTask().getEstimatedTimeRemaining(), 1)}, getTextHighlight()));
            }

            //Update statistics every second
            if (showStatistics && getTask().getElapsedTime() - statisticsMap.get(getTask()).lastUpdateTime >= 1000) {

                //Primes per second
                final long primeCount = getTask().getPrimeCount();
                statisticsMap.get(getTask()).finalPrimesPerSecond = primeCount - statisticsMap.get(getTask()).lastPrimeCount;
                statisticsLayout.set("pps", Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.primes_per_second), new String[]{NUMBER_FORMAT.format(primeCount - statisticsMap.get(getTask()).lastPrimeCount)}, getTextHighlight()));
                statisticsMap.get(getTask()).lastPrimeCount = primeCount;

                statisticsMap.get(getTask()).lastUpdateTime = getTask().getElapsedTime();
            }
        }
    }

    @Override
    public synchronized FindPrimesTask getTask() {
        return (FindPrimesTask) super.getTask();
    }

    @Override
    public synchronized void setTask(final ITask task) {
        super.setTask(task);
        if (getTask() != null) {
            if (!statisticsMap.containsKey(getTask())) {
                statisticsMap.put(getTask(), new Statistics());
            }

            if (getView() != null) {
                initDefaultState();
            }
        }
    }

    @Override
    protected void onResetViews() {
        super.onResetViews();
        showStatistics = getTask().getSearchOptions().getSearchMethod() == BRUTE_FORCE;
        progress.setVisibility(getTask().isEndless() ? View.GONE : View.VISIBLE);
        bodyTextView.setVisibility(View.VISIBLE);
        statisticsLayout.show("eta");
        statisticsLayout.hide("nps");
        statisticsLayout.setVisibility(showStatistics ? View.VISIBLE : View.GONE);
    }
}
