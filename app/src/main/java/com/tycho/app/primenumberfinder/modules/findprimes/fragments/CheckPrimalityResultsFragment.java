package com.tycho.app.primenumberfinder.modules.findprimes.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
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
    private ProgressBar progressBar;
    private TextView progress;

    //Statistics
    private ViewGroup statisticsLayout;
    private TextView timeElapsedTextView;
    private TextView etaTextView;

    //Buttons
    private ImageButton pauseButton;

    private final RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    private final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        //Set up progress animation
        rotate.setDuration(3000);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setRepeatMode(Animation.INFINITE);
        rotate.setInterpolator(new LinearInterpolator());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.check_primality_results_fragment, container, false);

        title = rootView.findViewById(R.id.title);
        subtitle = rootView.findViewById(R.id.subtitle);
        progressBar = rootView.findViewById(R.id.progress_bar);
        resultsView = rootView.findViewById(R.id.results_view);
        noTaskView = rootView.findViewById(R.id.empty_message);
        progress = rootView.findViewById(R.id.textView_search_progress);

        //Statistics
        statisticsLayout = rootView.findViewById(R.id.statistics_layout);
        timeElapsedTextView = rootView.findViewById(R.id.textView_elapsed_time);
        etaTextView = rootView.findViewById(R.id.textView_eta);

        //Buttons
        pauseButton = rootView.findViewById(R.id.pause_button);

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

        init();

        return rootView;
    }

    @Override
    public void onTaskStarted() {
        super.onTaskStarted();
        if (isAdded() && !isDetached() && getTask() != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    updateUi();

                    //Title
                    title.setText(getString(R.string.status_searching));
                    progressBar.startAnimation(rotate);

                    //Format subtitle
                    subtitle.setText(Utils.formatSpannable(spannableStringBuilder, getString(R.string.check_primality_subtitle_searching), new String[]{NUMBER_FORMAT.format(getTask().getNumber())}, ContextCompat.getColor(getActivity(), R.color.purple_dark)));

                }
            });
        }
    }

    @Override
    public void onTaskPausing() {
        super.onTaskPausing();
        if (isAdded() && !isDetached() && getTask() != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    updateUi();

                    //Title
                    title.setText(getString(R.string.state_pausing));

                    //Format subtitle
                    subtitle.setText(Utils.formatSpannable(spannableStringBuilder, getString(R.string.check_primality_subtitle_searching), new String[]{NUMBER_FORMAT.format(getTask().getNumber())}, ContextCompat.getColor(getActivity(), R.color.purple_dark)));

                }
            });
        }
    }

    @Override
    public void onTaskPaused() {
        super.onTaskPaused();
        if (isAdded() && !isDetached() && getTask() != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    updateUi();

                    //Title
                    title.setText(getString(R.string.status_paused));
                    progressBar.clearAnimation();

                    //Buttons
                    pauseButton.setEnabled(true);
                    pauseButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                }
            });
        }
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

                    //Buttons
                    pauseButton.setEnabled(false);
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

                    //Format subtitle
                    subtitle.setText(Utils.formatSpannable(spannableStringBuilder, getString(R.string.check_primality_result), new String[]{
                            NUMBER_FORMAT.format(getTask().getNumber()),
                            getTask().isPrime() ? "prime" : "not prime"
                    }, ContextCompat.getColor(getActivity(), R.color.purple_dark)));

                    //Statistics
                    statisticsLayout.setVisibility(View.GONE);

                    //Buttons
                    pauseButton.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    protected void onUiUpdate() {
        if (getTask() != null) {
            //Update progress
            progress.setText(String.valueOf((int) (getTask().getProgress() * 100)));
            progressBar.setProgress((int) (getTask().getProgress() * 100));

            //Time elapsed
            timeElapsedTextView.setText(Utils.formatTimeHuman(getTask().getElapsedTime(), 2));

            //Time remaining
            etaTextView.setText(Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.time_remaining), new String[]{Utils.formatTimeHuman(getTask().getEstimatedTimeRemaining(), 1)}, ContextCompat.getColor(getActivity(), R.color.purple_dark)));
        }
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
