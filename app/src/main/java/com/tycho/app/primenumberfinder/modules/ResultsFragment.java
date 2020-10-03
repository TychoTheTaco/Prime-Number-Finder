package com.tycho.app.primenumberfinder.modules;

import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.Savable;
import com.tycho.app.primenumberfinder.modules.findfactors.fragments.FindFactorsResultsFragment;
import com.tycho.app.primenumberfinder.modules.findprimes.fragments.CheckPrimalityResultsFragment;
import com.tycho.app.primenumberfinder.modules.findprimes.fragments.FindPrimesResultsFragment;
import com.tycho.app.primenumberfinder.modules.gcf.fragments.GreatestCommonFactorResultsFragment;
import com.tycho.app.primenumberfinder.modules.lcm.fragments.LeastCommonMultipleResultsFragment;
import com.tycho.app.primenumberfinder.modules.primefactorization.fragments.PrimeFactorizationResultsFragment;
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
    protected TextView noTaskView;
    protected TextView title;
    protected ProgressBar progressBar;
    protected TextView progress;
    protected TextView timeElapsedTextView;
    protected ViewGroup resultsView;

    //Buttons
    protected ImageButton pauseButton;
    protected ImageButton viewAllButton;
    protected ImageButton saveButton;
    protected View centerView;

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

    protected static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    @Nullable
    @Override
    public abstract View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

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
                if (centerView != null){
                    final ViewGroup.LayoutParams layoutParams = centerView.getLayoutParams();
                    layoutParams.width = Utils.dpToPx(getContext(), 64);
                    centerView.setLayoutParams(layoutParams);
                }
                if (pauseButton != null){
                    pauseButton.setVisibility(View.VISIBLE);
                    pauseButton.setEnabled(true);
                    pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                }
                if (viewAllButton != null){
                    viewAllButton.setVisibility(View.VISIBLE);
                }
                if (saveButton != null){
                    saveButton.setVisibility(View.GONE);
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
                if (centerView != null){
                    final ViewGroup.LayoutParams layoutParams = centerView.getLayoutParams();
                    layoutParams.width = Utils.dpToPx(getContext(), 64);
                    centerView.setLayoutParams(layoutParams);
                }
                if (pauseButton != null){
                    pauseButton.setVisibility(View.VISIBLE);
                    pauseButton.setEnabled(false);
                    pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                }
                if (viewAllButton != null){
                    viewAllButton.setVisibility(View.VISIBLE);
                }
                if (saveButton != null){
                    saveButton.setVisibility(View.GONE);
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
                if (centerView != null){
                    final ViewGroup.LayoutParams layoutParams = centerView.getLayoutParams();
                    layoutParams.width = Utils.dpToPx(getContext(), 64);
                    centerView.setLayoutParams(layoutParams);
                }
                if (pauseButton != null){
                    pauseButton.setVisibility(View.VISIBLE);
                    pauseButton.setEnabled(true);
                    pauseButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                }
                if (viewAllButton != null){
                    viewAllButton.setVisibility(View.VISIBLE);
                }
                if (saveButton != null){
                    saveButton.setVisibility(View.GONE);
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
                if (centerView != null){
                    final ViewGroup.LayoutParams layoutParams = centerView.getLayoutParams();
                    layoutParams.width = Utils.dpToPx(getContext(), 64);
                    centerView.setLayoutParams(layoutParams);
                }
                if (pauseButton != null){
                    pauseButton.setVisibility(View.VISIBLE);
                    pauseButton.setEnabled(false);
                    pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                }
                if (viewAllButton != null){
                    viewAllButton.setVisibility(View.VISIBLE);
                }
                if (saveButton != null){
                    saveButton.setVisibility(View.GONE);
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
                if (centerView != null){
                    final ViewGroup.LayoutParams layoutParams = centerView.getLayoutParams();
                    layoutParams.width = Utils.dpToPx(getContext(), 64);
                    centerView.setLayoutParams(layoutParams);
                }
                if (pauseButton != null){
                    pauseButton.setVisibility(View.VISIBLE);
                    pauseButton.setEnabled(true);
                    pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                }
                if (viewAllButton != null){
                    viewAllButton.setVisibility(View.VISIBLE);
                }
                if (saveButton != null){
                    saveButton.setVisibility(View.GONE);
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
                //progressBar.startAnimation(rotateAnimation);

                //Buttons
                if (centerView != null){
                    final ViewGroup.LayoutParams layoutParams = centerView.getLayoutParams();
                    layoutParams.width = Utils.dpToPx(getContext(), 64);
                    centerView.setLayoutParams(layoutParams);
                }
                if (pauseButton != null){
                    pauseButton.setVisibility(View.VISIBLE);
                    pauseButton.setEnabled(false);
                    pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                }
                if (viewAllButton != null){
                    viewAllButton.setVisibility(View.VISIBLE);
                }
                if (saveButton != null){
                    saveButton.setVisibility(View.GONE);
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

                //Buttons
                if (centerView != null){
                    final ViewGroup.LayoutParams layoutParams = centerView.getLayoutParams();
                    layoutParams.width = 0;
                    centerView.setLayoutParams(layoutParams);
                }
                if (pauseButton != null){
                    pauseButton.setVisibility(View.GONE);
                }
                if (viewAllButton != null){
                    viewAllButton.setVisibility(View.VISIBLE);
                }
                if (saveButton != null){
                    saveButton.setVisibility(View.VISIBLE);
                }

                onPostStopped();
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
        noTaskView = rootView.findViewById(R.id.empty_message);

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
        viewAllButton = rootView.findViewById(R.id.view_all_button);
        saveButton = rootView.findViewById(R.id.save_button);
        centerView = rootView.findViewById(R.id.center);

        //Fix button tint for API <22
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            final ColorStateList colorStateList = createSimpleColorStateList(Utils.getAccentColor(rootView.getContext()), ContextCompat.getColor(getContext(), R.color.gray));
            pauseButton.setBackgroundTintList(colorStateList);
            if (viewAllButton != null) viewAllButton.setBackgroundTintList(colorStateList);
            if (saveButton != null) saveButton.setBackgroundTintList(colorStateList);
        }

        //Set up pause button
        pauseButton.setOnClickListener(v -> {
            if (getTask().getState() == Task.State.RUNNING) {
                getTask().pause();
            } else if (getTask().getState() == Task.State.PAUSED) {
                getTask().resume();
            }
        });

        //Set up save button
        if (saveButton != null){
            saveButton.setOnClickListener((view)->{if (getTask() instanceof Savable) Utils.save((Savable) getTask(), getActivity());});
        }
    }

    protected final void initDefaultState(){
        if (getTask() != null) {
            onResetViews();
            switchState();
        } else {
            noTaskView.setVisibility(View.VISIBLE);
            resultsView.setVisibility(View.GONE);
        }
    }

    protected void onResetViews(){
        resultsView.setVisibility(View.VISIBLE);
        noTaskView.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.VISIBLE);
        if (saveButton != null) saveButton.setVisibility(View.VISIBLE);
    }

    protected ColorStateList createSimpleColorStateList(final int defaultColor, final int disabledColor) {
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

    protected int getTextHighlight(){
        switch (PreferenceManager.getInt(PreferenceManager.Preference.THEME)){
            default:
            case 0:
                if (this instanceof FindPrimesResultsFragment || this instanceof CheckPrimalityResultsFragment){
                    return ContextCompat.getColor(getContext(), R.color.purple_dark);
                }else if (this instanceof FindFactorsResultsFragment){
                    return ContextCompat.getColor(getContext(), R.color.orange_dark);
                }else if (this instanceof PrimeFactorizationResultsFragment){
                    return ContextCompat.getColor(getContext(), R.color.green_dark);
                }else if (this instanceof LeastCommonMultipleResultsFragment){
                    return ContextCompat.getColor(getContext(), R.color.yellow_dark);
                }else if (this instanceof GreatestCommonFactorResultsFragment){
                    return ContextCompat.getColor(getContext(), R.color.blue_dark);
                }
                break;

            case 1:
                if (this instanceof FindPrimesResultsFragment || this instanceof CheckPrimalityResultsFragment){
                    return ContextCompat.getColor(getContext(), R.color.purple_light);
                }else if (this instanceof FindFactorsResultsFragment){
                    return ContextCompat.getColor(getContext(), R.color.orange_light);
                }else if (this instanceof PrimeFactorizationResultsFragment){
                    return ContextCompat.getColor(getContext(), R.color.green_light);
                }else if (this instanceof LeastCommonMultipleResultsFragment){
                    return ContextCompat.getColor(getContext(), R.color.yellow_light);
                }else if (this instanceof GreatestCommonFactorResultsFragment){
                    return ContextCompat.getColor(getContext(), R.color.blue_light);
                }
                break;
        }
        return 0;
    }
}
