package com.tycho.app.primenumberfinder.modules;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.findfactors.fragments.FindFactorsResultsFragment;
import com.tycho.app.primenumberfinder.modules.findprimes.fragments.CheckPrimalityResultsFragment;
import com.tycho.app.primenumberfinder.modules.findprimes.fragments.FindPrimesResultsFragment;
import com.tycho.app.primenumberfinder.modules.gcf.fragments.GreatestCommonFactorResultsFragment;
import com.tycho.app.primenumberfinder.modules.lcm.fragments.LeastCommonMultipleResultsFragment;
import com.tycho.app.primenumberfinder.modules.primefactorization.fragments.PrimeFactorizationResultsFragment;
import com.tycho.app.primenumberfinder.ui.TaskControlBubble;
import com.tycho.app.primenumberfinder.utils.PreferenceManager;
import com.tycho.app.primenumberfinder.utils.UIUpdater;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.text.NumberFormat;
import java.util.Locale;

import easytasks.ITask;
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
     * This UI updater is responsible for updating the UI. Its life cycle is managed by this
     * abstract class.
     */
    private final UIUpdater uiUpdater = new UIUpdater(handler){
        @Override
        protected void update() {
            updateUi();
        }
    };

    //Views
    protected TextView title;
    protected ProgressBar progressBar;
    protected TextView progress;
    protected TextView timeElapsedTextView;
    protected ViewGroup resultsView;

    // Task controls
    protected TaskControlBubble taskControlBubble;
    protected ImageButton pauseButton;

    /**
     * Rotate animation for the circular progress bar.
     */
    private final RotateAnimation rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f){

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            rotation = 0 + (360 - 0) * interpolatedTime;
            super.applyTransformation(interpolatedTime, t);
        }
    };

    /**
     * Current rotation, in degrees, of the progress bar.
     */
    private float rotation;

    protected final NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up task controls
        taskControlBubble = view.findViewById(R.id.task_control_bubble);
        taskControlBubble.setVisibility(View.GONE);
    }

    @Override
    public void onTaskStarted(final ITask task) {
        super.onTaskStarted(task);
        if (uiUpdater.getState() == Task.State.NOT_STARTED) {
            uiUpdater.startOnNewThread();
        } else {
            uiUpdater.resume();
        }
        handler.post(() -> {
            if (isAdded() && !isDetached() && getTask() != null) {
                updateUi();

                //Title
                title.setText(getString(R.string.status_searching));
                progressBar.startAnimation(rotateAnimation);

                //Buttons
                if (pauseButton != null){
                    pauseButton.setVisibility(View.VISIBLE);
                    pauseButton.setEnabled(true);
                    pauseButton.setImageResource(R.drawable.ic_pause);
                }

                onPostStarted();
            }
        });
    }

    @Override
    public void onTaskPausing(final ITask task) {
        super.onTaskPausing(task);
        uiUpdater.resume();
        handler.post(() -> {
            if (isAdded() && !isDetached() && getTask() != null) {
                updateUi();

                //Title
                title.setText(getString(R.string.state_pausing));

                //Buttons
                if (pauseButton != null){
                    pauseButton.setVisibility(View.VISIBLE);
                    pauseButton.setEnabled(false);
                    pauseButton.setImageResource(R.drawable.ic_pause);
                }

                onPostPausing();
            }
        });
    }

    @Override
    public void onTaskPaused(final ITask task) {
        super.onTaskPaused(task);
        uiUpdater.pause();
        handler.post(() -> {
            if (isAdded() && !isDetached() && getTask() != null) {
                updateUi();

                //Title
                title.setText(getString(R.string.status_paused));
                progressBar.clearAnimation();

                //Buttons
                if (pauseButton != null){
                    pauseButton.setVisibility(View.VISIBLE);
                    pauseButton.setEnabled(true);
                    pauseButton.setImageResource(R.drawable.ic_play);
                }

                onPostPaused();
            }
        });
    }

    @Override
    public void onTaskResuming(final ITask task) {
        super.onTaskResuming(task);
        uiUpdater.resume();
        handler.post(() -> {
            if (isAdded() && !isDetached() && getTask() != null) {
                updateUi();

                //Title
                title.setText(getString(R.string.state_resuming));

                //Buttons
                if (pauseButton != null){
                    pauseButton.setVisibility(View.VISIBLE);
                    pauseButton.setEnabled(false);
                    pauseButton.setImageResource(R.drawable.ic_pause);
                }

                onPostResuming();
            }
        });
    }

    @Override
    public void onTaskResumed(final ITask task) {
        super.onTaskResumed(task);
        uiUpdater.resume();
        handler.post(() -> {
            if (isAdded() && !isDetached() && getTask() != null) {
                updateUi();

                //Title
                title.setText(getString(R.string.status_searching));
                progressBar.startAnimation(rotateAnimation);

                //Buttons
                if (pauseButton != null){
                    pauseButton.setVisibility(View.VISIBLE);
                    pauseButton.setEnabled(true);
                    pauseButton.setImageResource(R.drawable.ic_pause);
                }

                onPostResumed();
            }
        });
    }

    @Override
    public void onTaskStopping(final ITask task) {
        super.onTaskStopping(task);
        uiUpdater.resume();
        handler.post(() -> {
            if (isAdded() && !isDetached() && getTask() != null) {
                updateUi();

                //Title
                title.setText(getString(R.string.status_stopping));

                //Buttons
                if (pauseButton != null){
                    pauseButton.setVisibility(View.VISIBLE);
                    pauseButton.setEnabled(false);
                    pauseButton.setImageResource(R.drawable.ic_pause);
                }

                onPostStopping();
            }
        });
    }

    @Override
    public void onTaskStopped(final ITask task) {
        super.onTaskStopped(task);
        uiUpdater.pause();
        handler.post(() -> {
            if (isAdded() && !isDetached() && getTask() != null) {
                updateUi();

                //Title
                title.setText(getString(R.string.status_finished));
                progressBar.clearAnimation();

                onPostStopped();

                // Task controls
                taskControlBubble.setFinished(true);
                if (taskControlBubble.getLeftView().getVisibility() == View.GONE && taskControlBubble.getRightView().getVisibility() == View.GONE){
                    taskControlBubble.setVisibility(View.GONE);
                }
            }
        });
    }

    protected void onPostStarted(){
        postDefaults();
    }

    protected void onPostPausing(){
        postDefaults();
    }

    protected void onPostPaused(){
        postDefaults();
    }

    protected void onPostResuming(){
        postDefaults();
    }

    protected void onPostResumed(){
        postDefaults();
    }

    protected void onPostStopping(){
        postDefaults();
    }

    protected void onPostStopped(){
        postDefaults();
    }

    protected void postDefaults(){
        taskControlBubble.setVisibility(View.VISIBLE);
        taskControlBubble.hideRight(true);
    }

    /**
     * This method is synchronized to ensure that the task is not changed during a call to {@linkplain #updateUi()}.
     *
     * @param task
     */
    @Override
    public synchronized void setTask(ITask task) {
        super.setTask(task);
        switchState();
    }

    @Override
    public void onPause() {
        super.onPause();

        //Remove task listener
        if (getTask() != null) {
            getTask().removeTaskListener(this);
        }

        uiUpdater.pause();
    }

    @Override
    public void onResume() {
        super.onResume();

        //Add task listener
        if (getTask() != null) {
            getTask().addTaskListener(this);
        }

        updateUi();

        switchState();
    }

    private void switchState(){
        if (getTask() != null) {
            switch (getTask().getState()) {
                case RUNNING:
                    onTaskStarted(getTask());
                    break;

                case PAUSING:
                    onTaskPausing(getTask());
                    break;

                case PAUSED:
                    onTaskPaused(getTask());
                    break;

                case RESUMING:
                    onTaskResuming(getTask());
                    break;

                case STOPPING:
                    onTaskStopping(getTask());
                    break;

                case STOPPED:
                    onTaskStopped(getTask());
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
            if (getTask() != null){
                //Elapsed time
                timeElapsedTextView.setText(Utils.formatTimeHuman(getTask().getElapsedTime(), 2));
            }
            
            onUiUpdate();
        } else {
            //TODO: This happens sometimes and its not good
            Log.w(TAG, "Fragment not added or is detached! Dropping UI update: " + this);
        }
    }

    protected abstract void onUiUpdate();

    protected void initStandardViews(final View rootView) {

        resultsView = rootView.findViewById(R.id.results_view);

        //Set up top bar with status, progress, and elapsed time
        title = rootView.findViewById(R.id.title);
        progressBar = rootView.findViewById(R.id.progress_bar);
        progress = rootView.findViewById(R.id.textView_search_progress);
        timeElapsedTextView = rootView.findViewById(R.id.textView_elapsed_time);

        //Set up progress animation
        rotateAnimation.setDuration(3000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setRepeatMode(Animation.INFINITE);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                progressBar.setRotation(progressBar.getRotation() + rotation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        //Buttons
        pauseButton = rootView.findViewById(R.id.pause_button);

        //Set up pause button
        pauseButton.setOnClickListener(v -> {
            if (getTask().getState() == Task.State.RUNNING) {
                getTask().pause();
            } else if (getTask().getState() == Task.State.PAUSED) {
                getTask().resume();
            }
        });
    }

    protected final void initDefaultState(){
        if (getTask() != null) {
            onResetViews();
            switchState();
        } else {
            resultsView.setVisibility(View.GONE);
        }
    }

    protected void onResetViews(){
        resultsView.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.VISIBLE);
    }

    protected int getTextHighlight(){
        switch (PreferenceManager.getInt(PreferenceManager.Preference.THEME)){
            default:
            case 0:
                if (this instanceof FindPrimesResultsFragment || this instanceof CheckPrimalityResultsFragment){
                    return ContextCompat.getColor(requireContext(), R.color.purple_dark);
                }else if (this instanceof FindFactorsResultsFragment){
                    return ContextCompat.getColor(requireContext(), R.color.orange_dark);
                }else if (this instanceof PrimeFactorizationResultsFragment){
                    return ContextCompat.getColor(requireContext(), R.color.green_dark);
                }else if (this instanceof LeastCommonMultipleResultsFragment){
                    return ContextCompat.getColor(requireContext(), R.color.yellow_dark);
                }else if (this instanceof GreatestCommonFactorResultsFragment){
                    return ContextCompat.getColor(requireContext(), R.color.blue_dark);
                }
                break;

            case 1:
                if (this instanceof FindPrimesResultsFragment || this instanceof CheckPrimalityResultsFragment){
                    return ContextCompat.getColor(requireContext(), R.color.purple_light);
                }else if (this instanceof FindFactorsResultsFragment){
                    return ContextCompat.getColor(requireContext(), R.color.orange_light);
                }else if (this instanceof PrimeFactorizationResultsFragment){
                    return ContextCompat.getColor(requireContext(), R.color.green_light);
                }else if (this instanceof LeastCommonMultipleResultsFragment){
                    return ContextCompat.getColor(requireContext(), R.color.yellow_light);
                }else if (this instanceof GreatestCommonFactorResultsFragment){
                    return ContextCompat.getColor(requireContext(), R.color.blue_light);
                }
                break;
        }
        return 0;
    }
}
