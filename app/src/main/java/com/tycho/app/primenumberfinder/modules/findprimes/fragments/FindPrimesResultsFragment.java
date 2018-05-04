package com.tycho.app.primenumberfinder.modules.findprimes.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.ProgressDialog;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.ResultsFragment;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;
import com.tycho.app.primenumberfinder.modules.savedfiles.activities.DisplayPrimesActivity;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import easytasks.Task;

import static com.tycho.app.primenumberfinder.utils.FileManager.EXTENSION;

/**
 * Created by tycho on 11/16/2017.
 */

public class FindPrimesResultsFragment extends ResultsFragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = FindPrimesResultsFragment.class.getSimpleName();

    //Views
    private ViewGroup resultsView;
    private TextView noTaskView;
    private TextView title;
    private TextView subtitleTextView;
    private TextView bodyTextView;
    private ProgressBar progressBar;
    private TextView progress;

    //Buttons
    private ImageButton pauseButton;
    private ImageButton viewAllButton;
    private ImageButton saveButton;
    private View centerView;

    //Statistics
    private ViewGroup statisticsLayout;
    private TextView timeElapsedTextView;
    private TextView etaTextView;
    private TextView numbersPerSecondTextView;
    private TextView primesPerSecondTextView;

    private final RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    private boolean showStatistics = true;

    final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

    /**
     * This map holds the statistics for each task. When {@linkplain FindPrimesResultsFragment#setTask(Task)} is called,
     * the current task's statistics are saved to the map so that they can be used later when
     * {@linkplain FindPrimesResultsFragment#setTask(Task)} is called with the same task.
     */
    private final Map<FindPrimesTask, Statistics> statisticsMap = new HashMap<>();

    /**
     * This class keeps the statistics for a task.
     */
    private class Statistics {
        private long lastCurrentValue;
        private long lastPrimeCount;
        private long lastUpdateTime = -1000;
        private long finalNumbersPerSecond;
        private long finalPrimesPerSecond;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        //Set up progress animation
        rotate.setDuration(3000);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setRepeatMode(Animation.INFINITE);
        rotate.setInterpolator(new LinearInterpolator());
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.find_primes_results_fragment, container, false);

        Log.d(TAG, "onCreateView: " + this);

        title = rootView.findViewById(R.id.title);
        subtitleTextView = rootView.findViewById(R.id.subtitle);
        progressBar = rootView.findViewById(R.id.progress_bar);
        resultsView = rootView.findViewById(R.id.results_view);
        noTaskView = rootView.findViewById(R.id.empty_message);
        progress = rootView.findViewById(R.id.textView_search_progress);
        bodyTextView = rootView.findViewById(R.id.text);

        //Statistics
        statisticsLayout = rootView.findViewById(R.id.statistics_layout);
        timeElapsedTextView = rootView.findViewById(R.id.textView_elapsed_time);
        etaTextView = rootView.findViewById(R.id.textView_eta);
        numbersPerSecondTextView = rootView.findViewById(R.id.textView_numbers_per_second);
        primesPerSecondTextView = rootView.findViewById(R.id.textView_primes_per_second);

        //Apply black tint to icons
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            for (Drawable drawable : etaTextView.getCompoundDrawables()){
                if (drawable != null){
                    drawable.mutate().setTint(ContextCompat.getColor(getActivity(), R.color.black));
                }
            }
            for (Drawable drawable : numbersPerSecondTextView.getCompoundDrawables()){
                if (drawable != null){
                    drawable.mutate().setTint(ContextCompat.getColor(getActivity(), R.color.black));
                }
            }
            for (Drawable drawable : primesPerSecondTextView.getCompoundDrawables()){
                if (drawable != null){
                    drawable.mutate().setTint(ContextCompat.getColor(getActivity(), R.color.black));
                }
            }
        }

        //Buttons
        pauseButton = rootView.findViewById(R.id.pause_button);
        viewAllButton = rootView.findViewById(R.id.view_all_button);
        saveButton = rootView.findViewById(R.id.save_button);
        centerView = rootView.findViewById(R.id.center);

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getTask().getState() == Task.State.RUNNING) {
                    getTask().pause(false);
                } else if (getTask().getState() == Task.State.PAUSED) {
                    getTask().resume(false);
                }
            }
        });

        viewAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setTitle("Loading...");
                progressDialog.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        //Pause the task
                        final Task.State state = getTask().getState();
                        getTask().pause(true);

                        final File file;

                        //Check if cached file exists
                        final File cached = new File(FileManager.getInstance().getTaskCacheDirectory(getTask()) + File.separator + "primes");
                        if (cached.exists() && getTask().getState() == Task.State.STOPPED) {
                            file = cached;
                        } else {
                            file = getTask().saveToFile();
                        }

                        //Resume the task
                        if (state == Task.State.RUNNING) {
                            getTask().resume(false);
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });

                        final Intent intent = new Intent(getActivity(), DisplayPrimesActivity.class);
                        intent.putExtra("filePath", file.getAbsolutePath());
                        intent.putExtra("enableSearch", true);
                        intent.putExtra("range", new long[]{getTask().getStartValue(), getTask().getState() == Task.State.STOPPED ? getTask().getEndValue() : getTask().getCurrentValue()});
                        intent.putExtra("title", false);
                        getActivity().startActivity(intent);
                    }
                }).start();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setTitle("Saving...");
                progressDialog.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            FileManager.copy(getTask().saveToFile(), new File(FileManager.getInstance().getSavedPrimesDirectory() + File.separator + "Prime numbers from " + getTask().getStartValue() + " to " + (getTask().getEndValue() == FindPrimesTask.INFINITY ? getTask().getCurrentValue() : getTask().getEndValue()) + EXTENSION));
                        } catch (IOException e) {
                            e.printStackTrace();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), "Error saving file!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        progressDialog.dismiss();

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), getString(R.string.successfully_saved_file), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }).start();
            }
        });

        init();

        return rootView;
    }

    @Override
    public void onTaskStarted() {
        super.onTaskStarted();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isAdded() && !isDetached() && getTask() != null) {
                    updateUi();

                    //Title
                    title.setText(getString(R.string.status_searching));
                    progressBar.startAnimation(rotate);

                    //Subtitle
                    subtitleTextView.setText(Utils.formatSpannable(spannableStringBuilder, getString(R.string.find_primes_subtitle), new String[]{
                            NUMBER_FORMAT.format(getTask().getStartValue()),
                            getTask().getEndValue() == FindPrimesTask.INFINITY ? getString(R.string.infinity_text) : NUMBER_FORMAT.format(getTask().getEndValue()),
                            getTask().getSearchOptions().getSearchMethod() == FindPrimesTask.SearchMethod.BRUTE_FORCE ? "brute force" : "the sieve of Eratosthenes"
                    }, ContextCompat.getColor(getActivity(), R.color.purple_dark)));

                    //Statistics
                    etaTextView.setVisibility((getTask().getEndValue() == FindPrimesTask.INFINITY) ? View.GONE : View.VISIBLE);

                    //Buttons
                    final ViewGroup.LayoutParams layoutParams = centerView.getLayoutParams();
                    layoutParams.width = (int) Utils.dpToPx(getActivity(), 64);
                    centerView.setLayoutParams(layoutParams);
                    pauseButton.setVisibility(View.VISIBLE);
                    pauseButton.setEnabled(true);
                    pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                    switch (getTask().getSearchOptions().getSearchMethod()) {
                        case BRUTE_FORCE:
                            viewAllButton.setVisibility(View.VISIBLE);
                            break;

                        case SIEVE_OF_ERATOSTHENES:
                            viewAllButton.setVisibility(View.GONE);
                            break;
                    }
                    saveButton.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onTaskPausing() {
        super.onTaskPausing();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isAdded() && !isDetached() && getTask() != null) {
                    updateUi();

                    //Title
                    title.setText(getString(R.string.state_pausing));

                    //Subtitle
                    subtitleTextView.setText(Utils.formatSpannable(spannableStringBuilder, getString(R.string.find_primes_subtitle), new String[]{
                            NUMBER_FORMAT.format(getTask().getStartValue()),
                            getTask().getEndValue() == FindPrimesTask.INFINITY ? getString(R.string.infinity_text) : NUMBER_FORMAT.format(getTask().getEndValue()),
                            getTask().getSearchOptions().getSearchMethod() == FindPrimesTask.SearchMethod.BRUTE_FORCE ? "brute force" : "the sieve of Eratosthenes"
                    }, ContextCompat.getColor(getActivity(), R.color.purple_dark)));

                    //Statistics
                    etaTextView.setVisibility((getTask().getEndValue() == FindPrimesTask.INFINITY) ? View.GONE : View.VISIBLE);
                    numbersPerSecondTextView.setText(Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.numbers_per_second), new String[]{NUMBER_FORMAT.format(statisticsMap.get(getTask()).finalNumbersPerSecond)}, ContextCompat.getColor(getActivity(), R.color.purple_dark)));
                    primesPerSecondTextView.setText(Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.primes_per_second), new String[]{NUMBER_FORMAT.format(statisticsMap.get(getTask()).finalPrimesPerSecond)}, ContextCompat.getColor(getActivity(), R.color.purple_dark)));

                    //Buttons
                    pauseButton.setEnabled(false);
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
            }
        });
    }

    @Override
    public void onTaskPaused() {
        super.onTaskPaused();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isAdded() && !isDetached() && getTask() != null) {
                    updateUi();

                    //Title
                    title.setText(getString(R.string.status_paused));
                    progressBar.clearAnimation();

                    //Subtitle
                    subtitleTextView.setText(Utils.formatSpannable(spannableStringBuilder, getString(R.string.find_primes_subtitle), new String[]{
                            NUMBER_FORMAT.format(getTask().getStartValue()),
                            getTask().getEndValue() == FindPrimesTask.INFINITY ? getString(R.string.infinity_text) : NUMBER_FORMAT.format(getTask().getEndValue()),
                            getTask().getSearchOptions().getSearchMethod() == FindPrimesTask.SearchMethod.BRUTE_FORCE ? "brute force" : "the sieve of Eratosthenes"
                    }, ContextCompat.getColor(getActivity(), R.color.purple_dark)));

                    //Statistics
                    etaTextView.setVisibility((getTask().getEndValue() == FindPrimesTask.INFINITY) ? View.GONE : View.VISIBLE);
                    numbersPerSecondTextView.setText(Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.numbers_per_second), new String[]{NUMBER_FORMAT.format(statisticsMap.get(getTask()).finalNumbersPerSecond)}, ContextCompat.getColor(getActivity(), R.color.purple_dark)));
                    primesPerSecondTextView.setText(Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.primes_per_second), new String[]{NUMBER_FORMAT.format(statisticsMap.get(getTask()).finalPrimesPerSecond)}, ContextCompat.getColor(getActivity(), R.color.purple_dark)));

                    //Buttons
                    final ViewGroup.LayoutParams layoutParams = centerView.getLayoutParams();
                    layoutParams.width = (int) Utils.dpToPx(getActivity(), 64);
                    centerView.setLayoutParams(layoutParams);
                    pauseButton.setEnabled(true);
                    pauseButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
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
            }
        });
    }

    @Override
    public void onTaskResuming() {
        super.onTaskResuming();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isAdded() && !isDetached() && getTask() != null) {
                    updateUi();

                    //Title
                    title.setText(getString(R.string.state_resuming));

                    //Subtitle
                    subtitleTextView.setText(Utils.formatSpannable(spannableStringBuilder, getString(R.string.find_primes_subtitle), new String[]{
                            NUMBER_FORMAT.format(getTask().getStartValue()),
                            getTask().getEndValue() == FindPrimesTask.INFINITY ? getString(R.string.infinity_text) : NUMBER_FORMAT.format(getTask().getEndValue()),
                            getTask().getSearchOptions().getSearchMethod() == FindPrimesTask.SearchMethod.BRUTE_FORCE ? "brute force" : "the sieve of Eratosthenes"
                    }, ContextCompat.getColor(getActivity(), R.color.purple_dark)));

                    //Statistics
                    etaTextView.setVisibility((getTask().getEndValue() == FindPrimesTask.INFINITY) ? View.GONE : View.VISIBLE);
                    numbersPerSecondTextView.setText(Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.numbers_per_second), new String[]{NUMBER_FORMAT.format(statisticsMap.get(getTask()).finalNumbersPerSecond)}, ContextCompat.getColor(getActivity(), R.color.purple_dark)));
                    primesPerSecondTextView.setText(Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.primes_per_second), new String[]{NUMBER_FORMAT.format(statisticsMap.get(getTask()).finalPrimesPerSecond)}, ContextCompat.getColor(getActivity(), R.color.purple_dark)));

                    //Buttons
                    pauseButton.setEnabled(false);
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
            }
        });
    }

    @Override
    public void onTaskResumed() {
        super.onTaskResumed();
        onTaskStarted();
    }

    @Override
    public void onTaskStopped() {
        super.onTaskStopped();
        if (isAdded() && !isDetached() && getTask() != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    updateUi();

                    //Title
                    title.setText(getString(R.string.status_finished));
                    progressBar.clearAnimation();

                    //Subtitle
                    subtitleTextView.setText(Utils.formatSpannable(spannableStringBuilder, getString(R.string.find_primes_subtitle_result), new String[]{
                            NUMBER_FORMAT.format(getTask().getPrimeCount()),
                            NUMBER_FORMAT.format(getTask().getStartValue()),
                            getTask().getEndValue() == FindPrimesTask.INFINITY ? getString(R.string.infinity_text) : NUMBER_FORMAT.format(getTask().getEndValue()),
                    }, ContextCompat.getColor(getActivity(), R.color.purple_dark)));

                    //Body
                    bodyTextView.setVisibility(View.GONE);

                    //Statistics
                    etaTextView.setVisibility(View.GONE);
                    double elapsed = (double) getTask().getElapsedTime() / 1000;
                    if (elapsed <= 0){
                        elapsed = 1;
                    }
                    numbersPerSecondTextView.setText(Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.average_numbers_per_second), new String[]{NUMBER_FORMAT.format((long) (getTask().getEndValue() / elapsed))}, ContextCompat.getColor(getActivity(), R.color.purple_dark)));
                    primesPerSecondTextView.setText(Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.average_primes_per_second), new String[]{NUMBER_FORMAT.format((long) (getTask().getPrimeCount() / elapsed))}, ContextCompat.getColor(getActivity(), R.color.purple_dark)));

                    //Buttons
                    centerView.getLayoutParams().width = 0;
                    pauseButton.setVisibility(View.GONE);
                    viewAllButton.setVisibility(View.VISIBLE);
                    saveButton.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    protected void onUiUpdate() {
        if (getTask() != null) {

            //Update progress
            if (getTask().getEndValue() != FindPrimesTask.INFINITY) {
                progress.setText(String.valueOf((int) (getTask().getProgress() * 100)));
                progressBar.setProgress((int) (getTask().getProgress() * 100));
            }else{
                progressBar.setProgress(50);
            }

            //Elapsed time
            timeElapsedTextView.setText(Utils.formatTimeHuman(getTask().getElapsedTime(), 2));

            //Body
            String text = "";
            String[] content = new String[]{NUMBER_FORMAT.format(getTask().getPrimeCount())};
            switch (getTask().getSearchOptions().getSearchMethod()) {
                case BRUTE_FORCE:
                    text = getString(R.string.find_primes_body_text);
                    break;

                case SIEVE_OF_ERATOSTHENES:
                    switch (getTask().getStatus()) {
                        default:
                            text = getString(R.string.find_primes_body_text);
                            break;

                        case "counting":
                            text = getString(R.string.find_primes_body_text_sieve_counting);
                            break;

                        case "searching":
                            text = "Marking all non-primes...";
                            content = new String[0];
                            break;
                    }
                    break;
            }
            bodyTextView.setText(Utils.formatSpannable(spannableStringBuilder, text, content, ContextCompat.getColor(getContext(), R.color.purple_dark)));

            //Time remaining
            if (etaTextView.getVisibility() == View.VISIBLE){
                etaTextView.setText(Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.time_remaining), new String[]{Utils.formatTimeHuman(getTask().getEstimatedTimeRemaining(), 1)}, ContextCompat.getColor(getActivity(), R.color.purple_dark)));
            }

            //Update statistics every second
            if (showStatistics && getTask().getElapsedTime() - statisticsMap.get(getTask()).lastUpdateTime >= 1000) {

                //Numbers per second
                final long currentValue = getTask().getCurrentValue();
                statisticsMap.get(getTask()).finalNumbersPerSecond = currentValue - statisticsMap.get(getTask()).lastCurrentValue;
                numbersPerSecondTextView.setText(Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.numbers_per_second), new String[]{NUMBER_FORMAT.format(currentValue - statisticsMap.get(getTask()).lastCurrentValue)}, ContextCompat.getColor(getActivity(), R.color.purple_dark)));
                statisticsMap.get(getTask()).lastCurrentValue = currentValue;

                //Primes per second
                final long primeCount = getTask().getPrimeCount();
                statisticsMap.get(getTask()).finalPrimesPerSecond = primeCount - statisticsMap.get(getTask()).lastPrimeCount;
                primesPerSecondTextView.setText(Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.primes_per_second), new String[]{NUMBER_FORMAT.format(primeCount - statisticsMap.get(getTask()).lastPrimeCount)}, ContextCompat.getColor(getActivity(), R.color.purple_dark)));
                statisticsMap.get(getTask()).lastPrimeCount = primeCount;

                statisticsMap.get(getTask()).lastUpdateTime = getTask().getElapsedTime();
            }
        }
    }

    @Override
    public FindPrimesTask getTask() {
        return (FindPrimesTask) super.getTask();
    }

    @Override
    public void setTask(final Task task) {
        super.setTask(task);
        if (getTask() != null) {
            if (!statisticsMap.containsKey(getTask())) {
                statisticsMap.put(getTask(), new Statistics());
            }
        }
        if (getView() != null) {
            init();
        }
    }

    private void init() {
        if (getTask() != null) {
            showStatistics = getTask().getSearchOptions().getSearchMethod() == FindPrimesTask.SearchMethod.BRUTE_FORCE;

            //Reset view states
            resultsView.setVisibility(View.VISIBLE);
            noTaskView.setVisibility(View.GONE);
            progress.setVisibility(getTask().getEndValue() == FindPrimesTask.INFINITY ? View.GONE : View.VISIBLE);
            bodyTextView.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            etaTextView.setVisibility(View.VISIBLE);
            statisticsLayout.setVisibility(showStatistics ? View.VISIBLE : View.GONE);

            switch (getTask().getState()) {
                case RUNNING:
                    onTaskStarted();
                    break;

                case PAUSING:
                    onTaskPausing();
                    break;

                case PAUSED:
                    onTaskPaused();
                    break;

                case RESUMING:
                    onTaskResuming();
                    break;

                case STOPPING:
                    onTaskStopping();
                    break;

                case STOPPED:
                    onTaskStopped();
                    break;
            }

        } else {
            noTaskView.setVisibility(View.VISIBLE);
            resultsView.setVisibility(View.GONE);
        }
    }
}
