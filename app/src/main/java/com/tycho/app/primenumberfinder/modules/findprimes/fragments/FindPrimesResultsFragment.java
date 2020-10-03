package com.tycho.app.primenumberfinder.modules.findprimes.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tycho.app.primenumberfinder.LongClickLinkMovementMethod;
import com.tycho.app.primenumberfinder.ProgressDialog;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.ResultsFragment;
import com.tycho.app.primenumberfinder.modules.findprimes.DisplayPrimesActivity;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;
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

        viewAllButton.setOnClickListener(v -> {
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Loading...");
            progressDialog.show();

            new Thread(() -> {

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
    }
}
