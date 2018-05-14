package com.tycho.app.primenumberfinder.modules;

import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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

import com.tycho.app.primenumberfinder.R;

import java.text.NumberFormat;
import java.util.Locale;

import easytasks.Task;

/**
 * Created by tycho on 11/19/2017.
 */

public abstract class ResultsFragment extends TaskFragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = ResultsFragment.class.getSimpleName();

    /**
     * This UI updater is responsible for updating the UI. It its life cycle is managed by this
     * abstract class.
     */
    private final UIUpdater uiUpdater = new UIUpdater();

    //Views
    protected ViewGroup resultsView;
    protected TextView noTaskView;
    protected TextView title;
    protected TextView subtitleTextView;
    protected TextView bodyTextView;
    protected ProgressBar progressBar;
    protected TextView progress;

    //Buttons
    protected ImageButton pauseButton;
    protected ImageButton viewAllButton;
    protected ImageButton saveButton;
    protected View centerView;

    protected TextView timeElapsedTextView;

    protected final RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

    protected static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    @Nullable
    @Override
    public abstract View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    @Override
    public void onTaskStarted() {
        super.onTaskStarted();
        if (uiUpdater.getState() == Task.State.NOT_STARTED) {
            uiUpdater.startOnNewThread();
        } else {
            uiUpdater.resume(false);
        }
    }

    @Override
    public void onTaskPausing() {
        super.onTaskPausing();
        uiUpdater.resume(false);
    }

    @Override
    public void onTaskPaused() {
        super.onTaskPaused();
        uiUpdater.pause(false);
    }

    @Override
    public void onTaskResuming() {
        super.onTaskResuming();
        uiUpdater.resume(false);
    }

    @Override
    public void onTaskResumed() {
        super.onTaskResumed();
        uiUpdater.resume(false);
    }

    @Override
    public void onTaskStopped() {
        super.onTaskStopped();
        uiUpdater.pause(false);
    }

    /**
     * This method is synchronized to ensure that the task is not changed during a call to {@linkplain #updateUi()}.
     *
     * @param task
     */
    @Override
    public synchronized void setTask(Task task) {
        if (task == null || task.getState() != Task.State.RUNNING) {
            uiUpdater.pause(false);
        }
        super.setTask(task);
    }

    @Override
    public void onPause() {
        super.onPause();

        //Remove task listener
        if (getTask() != null) {
            getTask().removeTaskListener(this);
        }

        uiUpdater.pause(false);
    }

    @Override
    public void onResume() {
        super.onResume();

        //Add task listener
        if (getTask() != null) {
            getTask().addTaskListener(this);
        }

        updateUi();

        if (getTask() != null) {
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
        }
    }

    /**
     * Update the UI immediately if the fragment is added and not detached from it's context (typically an Activity).
     * This method is synchronized to ensure that {@linkplain #getTask()} returns the same task throughout the method.
     */
    protected synchronized void updateUi() {
        if (isAdded() && !isDetached()) {
            onUiUpdate();
        } else {
            Log.w(TAG, "Fragment not added or is detached! Dropping UI update: " + this);
        }
    }

    protected abstract void onUiUpdate();

    protected final class UIUpdater extends Task {

        @Override
        protected void run() {
            while (true) {

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateUi();
                    }
                });

                try {
                    Thread.sleep(1000 / 25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }

                tryPause();
                if (shouldStop()) {
                    break;
                }
            }
        }
    }

    protected void initStandardViews(final View rootView) {

        resultsView = rootView.findViewById(R.id.results_view);
        noTaskView = rootView.findViewById(R.id.empty_message);

        //Set up top bar with status, progress, and elapsed time
        title = rootView.findViewById(R.id.title);
        progressBar = rootView.findViewById(R.id.progress_bar);
        progress = rootView.findViewById(R.id.textView_search_progress);
        timeElapsedTextView = rootView.findViewById(R.id.textView_elapsed_time);

        //Set up progress animation
        rotate.setDuration(3000);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setRepeatMode(Animation.INFINITE);
        rotate.setInterpolator(new LinearInterpolator());

        //Buttons
        pauseButton = rootView.findViewById(R.id.pause_button);
        viewAllButton = rootView.findViewById(R.id.view_all_button);
        saveButton = rootView.findViewById(R.id.save_button);
        centerView = rootView.findViewById(R.id.center);

        //Fix button tint for API <22
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            final ColorStateList colorStateList = generateSimpleColorStateList(ContextCompat.getColor(getContext(), R.color.purple), ContextCompat.getColor(getContext(), R.color.gray));
            pauseButton.setBackgroundTintList(colorStateList);
            viewAllButton.setBackgroundTintList(colorStateList);
            saveButton.setBackgroundTintList(colorStateList);
            centerView.setBackgroundTintList(colorStateList);
        }

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
    }

    protected ColorStateList generateSimpleColorStateList(final int defaultColor, final int disabledColor) {
        return new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_enabled}, //Disabled
                        new int[]{} //Default
                },
                new int[]{
                        disabledColor,
                        defaultColor
                });
    }
}
