package com.tycho.app.primenumberfinder.modules.findprimes.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
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
import java.util.Locale;

import easytasks.Task;

import static com.tycho.app.primenumberfinder.utils.FileManager.EXTENSION;

/**
 * Created by tycho on 11/16/2017.
 */

public class FindPrimesResultsFragment extends ResultsFragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FindPrimesResultsFgmnt";

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

    private final String[] subtitleItems = new String[7];
    private final SpannableStringBuilder subtitleStringBuilder = new SpannableStringBuilder();

    private boolean showStatistics = true;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach()");

        final String[] splitSubtitle = getString(R.string.find_primes_subtitle_result).split("%\\d\\$.");
        subtitleItems[0] = splitSubtitle[0];
        subtitleItems[2] = splitSubtitle[1];
        subtitleItems[4] = splitSubtitle[2];
        subtitleItems[6] = splitSubtitle[3];

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

        Log.d(TAG, "onCreateView()");

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
                    getTask().resume();
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
                            getTask().resume();
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
        Log.d(TAG, "onTaskStarted()");
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isAdded() && !isDetached() && getTask() != null) {
                    Log.d(TAG, "onTaskStarted() handler posted");
                    onUiUpdate();

                    //Title
                    title.setText(getString(R.string.status_searching));
                    progressBar.startAnimation(rotate);

                    //Subtitle
                    subtitleTextView.setText(Utils.formatSpannable(spannableStringBuilder, getString(R.string.find_primes_subtitle), new String[]{
                            NUMBER_FORMAT.format(getTask().getStartValue()),
                            NUMBER_FORMAT.format(getTask().getEndValue()),
                            getTask().getSearchOptions().getSearchMethod() == FindPrimesTask.SearchMethod.BRUTE_FORCE ? "brute force" : "the sieve of Eratosthenes"
                    }, ContextCompat.getColor(getActivity(), R.color.purple_dark)));

                    //Buttons
                    centerView.getLayoutParams().width = (int) Utils.dpToPx(getActivity(), 64);
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
                    onUiUpdate();

                    //Title
                    title.setText(getString(R.string.state_pausing));

                    //Subtitle
                    subtitleTextView.setText(Utils.formatSpannable(spannableStringBuilder, getString(R.string.find_primes_subtitle), new String[]{
                            NUMBER_FORMAT.format(getTask().getStartValue()),
                            NUMBER_FORMAT.format(getTask().getEndValue()),
                            getTask().getSearchOptions().getSearchMethod() == FindPrimesTask.SearchMethod.BRUTE_FORCE ? "brute force" : "the sieve of Eratosthenes"
                    }, ContextCompat.getColor(getActivity(), R.color.purple_dark)));

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
                    onUiUpdate();

                    //Title
                    title.setText(getString(R.string.status_paused));
                    progressBar.clearAnimation();

                    //Buttons
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
        Log.d(TAG, "onTaskResuming()");
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isAdded() && !isDetached() && getTask() != null) {
                    Log.d(TAG, "onTaskResuming() handler posted");
                    title.setText(getString(R.string.state_resuming));

                    //Buttons
                    pauseButton.setEnabled(false);
                }
            }
        });
    }

    @Override
    public void onTaskResumed() {
        super.onTaskResumed();
        Log.d(TAG, "onTaskResumed()");
        onTaskStarted();
    }

    @Override
    public void onTaskStopped() {
        super.onTaskStopped();
        if (isAdded() && !isDetached() && getTask() != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Call onUiUpdate() one last time.");
                    onUiUpdate();

                    Log.d(TAG, "Begin final UI update.");

                    //Title
                    title.setText(getString(R.string.status_finished));
                    progressBar.clearAnimation();

                    formatSubtitle();
                    final String count = NUMBER_FORMAT.format(getTask().getPrimeCount());
                    subtitleStringBuilder.replace(subtitleItems[0].length(), subtitleItems[0].length() + subtitleItems[1].length(), count);
                    subtitleItems[1] = count;
                    subtitleTextView.setText(subtitleStringBuilder);

                    //Body
                    bodyTextView.setVisibility(View.GONE);

                    //Statistics
                    etaTextView.setVisibility(View.GONE);

                    //Buttons
                    centerView.getLayoutParams().width = 0;
                    pauseButton.setVisibility(View.GONE);
                    viewAllButton.setVisibility(View.VISIBLE);
                    saveButton.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private long lastCurrentValue;
    private long lastPrimeCount;
    private long lastUpdateTime = 0;

    final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
    final SpannableStringBuilder statusStringBuilder = new SpannableStringBuilder();

    @Override
    protected void onUiUpdate() {
        if (getTask() != null) {

            //Update progress
            if (getTask().getEndValue() != FindPrimesTask.INFINITY) {
                progress.setText(String.valueOf((int) (getTask().getProgress() * 100)));
                progressBar.setProgress((int) (getTask().getProgress() * 100));
            }

            //Elapsed time
            timeElapsedTextView.setText(Utils.formatTimeHuman(getTask().getElapsedTime(), 2));

            //Body
            spannableStringBuilder.clear();
            spannableStringBuilder.clearSpans();
            final String primes = NUMBER_FORMAT.format(getTask().getPrimeCount());
            switch (getTask().getSearchOptions().getSearchMethod()){
                case BRUTE_FORCE:
                    spannableStringBuilder.append(getString(R.string.find_primes_body_text, primes));
                    spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.purple_dark)), spannableStringBuilder.toString().indexOf(primes), spannableStringBuilder.toString().indexOf(primes) + primes.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), spannableStringBuilder.toString().indexOf(primes), spannableStringBuilder.toString().indexOf(primes) + primes.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    bodyTextView.setText(spannableStringBuilder);
                    break;

                case SIEVE_OF_ERATOSTHENES:
                    switch (getTask().getStatus()){
                        default:
                            spannableStringBuilder.append(getString(R.string.find_primes_body_text, primes));
                            spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.purple_dark)), spannableStringBuilder.toString().indexOf(primes), spannableStringBuilder.toString().indexOf(primes) + primes.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                            spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), spannableStringBuilder.toString().indexOf(primes), spannableStringBuilder.toString().indexOf(primes) + primes.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                            bodyTextView.setText(spannableStringBuilder);
                            break;

                        case "counting":
                            spannableStringBuilder.append(getString(R.string.find_primes_body_text_sieve_counting, primes));
                            spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.purple_dark)), spannableStringBuilder.toString().indexOf(primes), spannableStringBuilder.toString().indexOf(primes) + primes.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                            spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), spannableStringBuilder.toString().indexOf(primes), spannableStringBuilder.toString().indexOf(primes) + primes.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                            bodyTextView.setText(spannableStringBuilder);
                            break;

                        case "searching":
                            bodyTextView.setText("Marking all non-primes...");
                            break;
                    }
                    break;
            }

            //Time remaining
            spannableStringBuilder.clear();
            spannableStringBuilder.clearSpans();
            final String time = Utils.formatTimeHuman(getTask().getEstimatedTimeRemaining(), 1);
            spannableStringBuilder.append(time);
            spannableStringBuilder.append(" remaining");
            spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.purple_dark)), 0, time.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            etaTextView.setText(spannableStringBuilder);

            //Update statistics every second
            if (showStatistics && System.currentTimeMillis() - lastUpdateTime >= 1000) {

                //Numbers per second
                spannableStringBuilder.clear();
                spannableStringBuilder.clearSpans();
                final String nps = NUMBER_FORMAT.format(getTask().getCurrentValue() - lastCurrentValue);
                spannableStringBuilder.append(nps);
                spannableStringBuilder.append(" numbers per second");
                spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.purple_dark)), 0, nps.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                numbersPerSecondTextView.setText(spannableStringBuilder);
                lastCurrentValue = getTask().getCurrentValue();

                //Primes per second
                spannableStringBuilder.clear();
                spannableStringBuilder.clearSpans();
                spannableStringBuilder.append(NUMBER_FORMAT.format(getTask().getPrimeCount() - lastPrimeCount));
                spannableStringBuilder.append(" primes per second");
                spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.purple_dark)), 0, NUMBER_FORMAT.format(getTask().getPrimeCount() - lastPrimeCount).length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                primesPerSecondTextView.setText(spannableStringBuilder);
                lastPrimeCount = getTask().getPrimeCount();

                lastUpdateTime = System.currentTimeMillis();
            }
        }
    }

    private SpannableStringBuilder formatSubtitle() {
        final String count = NUMBER_FORMAT.format(getTask().getPrimeCount());
        final String start = NUMBER_FORMAT.format(getTask().getStartValue());
        String end = NUMBER_FORMAT.format(getTask().getEndValue());
        if (getTask().getEndValue() == FindPrimesTask.INFINITY) {
            end = getString(R.string.infinity_text);
        }

        subtitleItems[1] = count;
        subtitleItems[3] = start;
        subtitleItems[5] = end;
        subtitleStringBuilder.clear();
        for (int i = 0; i < subtitleItems.length; i++) {
            if (i % 2 != 0) {
                subtitleStringBuilder.append(subtitleItems[i], new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.purple_dark)), 0);
                subtitleStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), subtitleStringBuilder.length() - subtitleItems[i].length(), subtitleStringBuilder.length(), 0);
            } else {
                subtitleStringBuilder.append(subtitleItems[i]);
            }
        }
        return subtitleStringBuilder;
    }

    @Override
    public FindPrimesTask getTask() {
        return (FindPrimesTask) super.getTask();
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
            Log.d(TAG, "init()");

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

            //Required to init some fields
            formatSubtitle();

            //Reset statistics
            lastUpdateTime = 0;
            lastCurrentValue = 0;
            lastPrimeCount = 0;

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
            resultsView.setVisibility(View.GONE);
            noTaskView.setVisibility(View.VISIBLE);
        }
    }
}
