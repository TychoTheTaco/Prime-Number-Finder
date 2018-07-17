package com.tycho.app.primenumberfinder.modules.lcm.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tycho.app.primenumberfinder.ProgressDialog;
import com.tycho.app.primenumberfinder.R;
import com.tycho.app.primenumberfinder.modules.ResultsFragment;
import com.tycho.app.primenumberfinder.modules.findfactors.DisplayFactorsActivity;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;
import com.tycho.app.primenumberfinder.modules.findfactors.adapters.FactorsListAdapter;
import com.tycho.app.primenumberfinder.modules.lcm.LeastCommonMultipleTask;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import easytasks.Task;

/**
 * Created by tycho on 11/19/2017.
 */

public class LeastCommonMultipleResultsFragment extends ResultsFragment {

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = LeastCommonMultipleResultsFragment.class.getSimpleName();

    //Views
    private TextView subtitleTextView;
    private TextView bodyTextView;

    //Statistics
    private ViewGroup statisticsLayout;
    private TextView etaTextView;
    private TextView numbersPerSecondTextView;

    private final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.lcm_results_fragment, container, false);

        initStandardViews(rootView);

        subtitleTextView = rootView.findViewById(R.id.subtitle);
        bodyTextView = rootView.findViewById(R.id.text);

        //Statistics
        statisticsLayout = rootView.findViewById(R.id.statistics_layout);
        etaTextView = rootView.findViewById(R.id.textView_eta);
        numbersPerSecondTextView = rootView.findViewById(R.id.textView_numbers_per_second);

        //Apply black tint to icons
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            for (Drawable drawable : etaTextView.getCompoundDrawables()) {
                if (drawable != null) {
                    drawable.mutate().setTint(ContextCompat.getColor(getActivity(), R.color.black));
                }
            }
        }

        /*saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTask(getTask(), getActivity());
            }
        });*/

        init();

        return rootView;
    }

    /*public void saveTask(final FindFactorsTask task, final Context context) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Saving...");
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (task.save()) {
                    progressDialog.dismiss();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Posted context: " + getContext() + " " + getActivity());
                            Toast.makeText(context.getApplicationContext(), context.getString(R.string.successfully_saved_file), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context.getApplicationContext(), context.getString(R.string.error_saving_file), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }*/

    @Override
    public void onTaskStarted() {
        super.onTaskStarted();
        handler.post(() -> {
            if (isAdded() && !isDetached() && getTask() != null) {
                updateUi();

                //Title
                title.setText(getString(R.string.status_searching));
                progressBar.startAnimation(rotateAnimation);

                //Subtitle
                String title = "";
                for (Long number : getTask().getNumbers()){
                    title += NUMBER_FORMAT.format(number) + "; ";
                }
                subtitleTextView.setText(Utils.formatSpannable(spannableStringBuilder, getString(R.string.lcm_subtitle), new String[]{title}, ContextCompat.getColor(getActivity(), R.color.yellow_dark)));

                //Buttons
                final ViewGroup.LayoutParams layoutParams = centerView.getLayoutParams();
                layoutParams.width = (int) Utils.dpToPx(getActivity(), 64);
                centerView.setLayoutParams(layoutParams);
                pauseButton.setVisibility(View.VISIBLE);
                pauseButton.setEnabled(true);
                pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                //viewAllButton.setVisibility(View.VISIBLE);
                saveButton.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onTaskPausing() {
        super.onTaskPausing();
        handler.post(() -> {
            if (isAdded() && !isDetached() && getTask() != null) {
                updateUi();

                //Title
                title.setText(getString(R.string.state_pausing));

                //Subtitle
                //subtitleTextView.setText(Utils.formatSpannable(spannableStringBuilder, getString(R.string.find_factors_subtitle), new String[]{NUMBER_FORMAT.format(getTask().getNumber())}, ContextCompat.getColor(getActivity(), R.color.orange_dark)));

                //Buttons
                final ViewGroup.LayoutParams layoutParams = centerView.getLayoutParams();
                layoutParams.width = (int) Utils.dpToPx(getActivity(), 64);
                centerView.setLayoutParams(layoutParams);
                pauseButton.setVisibility(View.VISIBLE);
                pauseButton.setEnabled(false);
                pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                viewAllButton.setVisibility(View.VISIBLE);
                saveButton.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onTaskPaused() {
        super.onTaskPaused();
        handler.post(() -> {
            if (isAdded() && !isDetached() && getTask() != null) {

                updateUi();

                //Title
                title.setText(getString(R.string.status_paused));
                progressBar.clearAnimation();

                //Subtitle
                //subtitleTextView.setText(Utils.formatSpannable(spannableStringBuilder, getString(R.string.find_factors_subtitle), new String[]{NUMBER_FORMAT.format(getTask().getNumber())}, ContextCompat.getColor(getActivity(), R.color.orange_dark)));

                //Buttons
                final ViewGroup.LayoutParams layoutParams = centerView.getLayoutParams();
                layoutParams.width = (int) Utils.dpToPx(getActivity(), 64);
                centerView.setLayoutParams(layoutParams);
                pauseButton.setVisibility(View.VISIBLE);
                pauseButton.setEnabled(true);
                pauseButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                viewAllButton.setVisibility(View.VISIBLE);
                saveButton.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onTaskResuming() {
        super.onTaskResuming();
        handler.post(() -> {
            if (isAdded() && !isDetached() && getTask() != null) {
                updateUi();

                //Title
                title.setText(getString(R.string.state_resuming));

                //Subtitle
                //subtitleTextView.setText(Utils.formatSpannable(spannableStringBuilder, getString(R.string.find_factors_subtitle), new String[]{NUMBER_FORMAT.format(getTask().getNumber())}, ContextCompat.getColor(getActivity(), R.color.orange_dark)));

                //Buttons
                final ViewGroup.LayoutParams layoutParams = centerView.getLayoutParams();
                layoutParams.width = (int) Utils.dpToPx(getActivity(), 64);
                centerView.setLayoutParams(layoutParams);
                pauseButton.setVisibility(View.VISIBLE);
                pauseButton.setEnabled(false);
                pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                viewAllButton.setVisibility(View.VISIBLE);
                saveButton.setVisibility(View.GONE);
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
            handler.post(() -> {
                updateUi();

                //Title
                title.setText(getString(R.string.status_finished));
                progressBar.clearAnimation();

                //Subtitle
                /*Utils.formatSpannable(spannableStringBuilder, getResources().getQuantityString(R.plurals.find_factors_subtitle_results, getTask().getFactors().size()), new String[]{NUMBER_FORMAT.format(getTask().getNumber()), NUMBER_FORMAT.format(getTask().getFactors().size())}, ContextCompat.getColor(getActivity(), R.color.orange_dark));
                if (getTask().getFactors().size() != 2) {
                    subtitleTextView.setText(spannableStringBuilder);
                } else {
                    final SpannableStringBuilder ssb = new SpannableStringBuilder();
                    Utils.formatSpannable(ssb, getResources().getString(R.string.find_factors_subtitle_results_extension), new String[]{"prime"}, ContextCompat.getColor(getActivity(), R.color.orange_dark));
                    subtitleTextView.setText(TextUtils.concat(spannableStringBuilder, " ", ssb));
                }*/

                //Body
                bodyTextView.setVisibility(View.GONE);

                //Statistics
                etaTextView.setVisibility(View.GONE);

                //Buttons
                final ViewGroup.LayoutParams layoutParams = centerView.getLayoutParams();
                layoutParams.width = 0;
                centerView.setLayoutParams(layoutParams);
                pauseButton.setVisibility(View.GONE);
                viewAllButton.setVisibility(View.VISIBLE);
                saveButton.setVisibility(View.VISIBLE);
            });
        }
    }

    @Override
    protected void onUiUpdate() {
        if (getTask() != null) {

            //Update progress
            progress.setText(String.valueOf((int) (getTask().getProgress() * 100)));
            progressBar.setProgress((int) (getTask().getProgress() * 100));

            //Elapsed time
            timeElapsedTextView.setText(Utils.formatTimeHuman(getTask().getElapsedTime(), 2));

            //Body
            //bodyTextView.setText(Utils.formatSpannable(spannableStringBuilder, getString(R.string.find_factors_body_text), new String[]{NUMBER_FORMAT.format(getTask().getFactors().size())}, ContextCompat.getColor(getActivity(), R.color.orange_dark)));

            //Time remaining
            etaTextView.setText(Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.time_remaining), new String[]{Utils.formatTimeHuman(getTask().getEstimatedTimeRemaining(), 1)}, ContextCompat.getColor(getActivity(), R.color.orange_dark)));
        }
    }

    @Override
    public synchronized LeastCommonMultipleTask getTask() {
        return (LeastCommonMultipleTask) super.getTask();
    }

    @Override
    public synchronized void setTask(final Task task) {
        super.setTask(task);
        if (getView() != null) {
            init();
        }
    }

    @Override
    protected void onResetViews() {
        super.onResetViews();
        bodyTextView.setVisibility(View.VISIBLE);
        etaTextView.setVisibility(View.VISIBLE);
    }
}
