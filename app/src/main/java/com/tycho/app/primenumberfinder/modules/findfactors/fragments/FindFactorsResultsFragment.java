package com.tycho.app.primenumberfinder.modules.findfactors.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
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
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;
import com.tycho.app.primenumberfinder.modules.findfactors.adapters.FactorsListAdapter;
import com.tycho.app.primenumberfinder.modules.savedfiles.activities.DisplayFactorsActivity;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.File;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import easytasks.Task;

/**
 * Created by tycho on 11/19/2017.
 */

public class FindFactorsResultsFragment extends ResultsFragment{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "FindFactorsResultsFgmnt";

    //Views
    private ViewGroup resultsView;
    private TextView noTaskView;
    private TextView title;
    private TextView subtitleTextView;
    private ProgressBar progressBar;
    private TextView progress;
    private RecyclerView recyclerView;
    private ImageButton viewAllButton;
    private ImageButton saveButton;
    private ImageButton pauseButton;

    private View centerView;
    private TextView bodyTextView;

    //Statistics
    private ViewGroup statisticsLayout;
    private TextView timeElapsedTextView;
    private TextView etaTextView;
    private TextView numbersPerSecondTextView;

    private FactorsListAdapter adapter;

    private int lastAdapterSize = 0;

    private final RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.getDefault());

    private final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

    /**
     * This map holds the statistics for each task. When {@linkplain FindFactorsResultsFragment#setTask(Task)} is called,
     * the current task's statistics are saved to the map so that they can be used later when
     * {@linkplain FindFactorsResultsFragment#setTask(Task)} is called with the same task.
     */
    private final Map<FindFactorsTask, FindFactorsResultsFragment.Statistics> statisticsMap = new HashMap<>();

    /**
     * This class keeps the statistics for a task.
     */
    private class Statistics {
        private long lastCurrentValue;
        private long lastUpdateTime = -1000;
        private long finalNumbersPerSecond;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        adapter = new FactorsListAdapter(context);

        //Set up progress animation
        rotate.setDuration(3000);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setRepeatMode(Animation.INFINITE);
        rotate.setInterpolator(new LinearInterpolator());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.find_factors_results_fragment, container, false);

        //Set up recycler view
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(null);

        title = rootView.findViewById(R.id.title);
        subtitleTextView = rootView.findViewById(R.id.subtitle);
        progressBar = rootView.findViewById(R.id.progress_bar);
        resultsView = rootView.findViewById(R.id.results_view);
        noTaskView = rootView.findViewById(R.id.empty_message);
        progress = rootView.findViewById(R.id.textView_search_progress);
        viewAllButton = rootView.findViewById(R.id.view_all_button);
        saveButton = rootView.findViewById(R.id.save_button);
        centerView = rootView.findViewById(R.id.center);
        bodyTextView = rootView.findViewById(R.id.text);

        //Statistics
        statisticsLayout = rootView.findViewById(R.id.statistics_layout);
        timeElapsedTextView = rootView.findViewById(R.id.textView_elapsed_time);
        etaTextView = rootView.findViewById(R.id.textView_eta);
        numbersPerSecondTextView = rootView.findViewById(R.id.textView_numbers_per_second);

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
        }

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

        viewAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), getString(R.string.loading), Toast.LENGTH_SHORT).show();
                            }
                        });

                        final Task.State state = getTask().getState();
                        getTask().pause(true);
                        final File file = new File(getActivity().getFilesDir() + File.separator + "temp");
                        final boolean success = FileManager.getInstance().saveFactors(getTask().getFactors(), file);
                        if (!success){
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), getString(R.string.general_error), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        if (state == Task.State.RUNNING){
                            getTask().resume(false);
                        }

                        final Intent intent = new Intent(getActivity(), DisplayFactorsActivity.class);
                        intent.putExtra("filePath", file.getAbsolutePath());
                        intent.putExtra("title", false);
                        intent.putExtra("number", getTask().getNumber());
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
                        final boolean success = FileManager.getInstance().saveFactors(getTask().getFactors(), getTask().getNumber());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), success ? getString(R.string.successfully_saved_file) : getString(R.string.error_saving_file), Toast.LENGTH_SHORT).show();
                            }
                        });
                        progressDialog.dismiss();
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
        if (isAdded() && !isDetached() && getTask() != null){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    //Title
                    title.setText(getString(R.string.status_searching));
                    progressBar.startAnimation(rotate);

                    //Subtitle
                    subtitleTextView.setText(Utils.formatSpannable(spannableStringBuilder, getString(R.string.find_factors_subtitle), new String[]{NUMBER_FORMAT.format(getTask().getNumber())}, ContextCompat.getColor(getActivity(), R.color.orange_dark)));

                    //Buttons
                    final ViewGroup.LayoutParams layoutParams = centerView.getLayoutParams();
                    layoutParams.width = (int) Utils.dpToPx(getActivity(), 64);
                    centerView.setLayoutParams(layoutParams);
                    pauseButton.setVisibility(View.VISIBLE);
                    pauseButton.setEnabled(true);
                    pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
                    viewAllButton.setVisibility(View.VISIBLE);
                    saveButton.setVisibility(View.GONE);
                }
            });
        }
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
                    subtitleTextView.setText(Utils.formatSpannable(spannableStringBuilder, getString(R.string.find_factors_subtitle), new String[]{NUMBER_FORMAT.format(getTask().getNumber())}, ContextCompat.getColor(getActivity(), R.color.orange_dark)));

                    //Statistics
                    numbersPerSecondTextView.setText(Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.numbers_per_second), new String[]{NUMBER_FORMAT.format(statisticsMap.get(getTask()).finalNumbersPerSecond)}, ContextCompat.getColor(getActivity(), R.color.orange_dark)));

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
            }
        });
    }

    @Override
    public void onTaskPaused() {
        super.onTaskPaused();
        if (isAdded() && !isDetached() && getTask() != null){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    updateUi();

                    //Title
                    title.setText(getString(R.string.status_paused));
                    progressBar.clearAnimation();

                    //Subtitle
                    subtitleTextView.setText(Utils.formatSpannable(spannableStringBuilder, getString(R.string.find_factors_subtitle), new String[]{NUMBER_FORMAT.format(getTask().getNumber())}, ContextCompat.getColor(getActivity(), R.color.orange_dark)));

                    //Statistics
                    numbersPerSecondTextView.setText(Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.numbers_per_second), new String[]{NUMBER_FORMAT.format(statisticsMap.get(getTask()).finalNumbersPerSecond)}, ContextCompat.getColor(getActivity(), R.color.orange_dark)));

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
                    subtitleTextView.setText(Utils.formatSpannable(spannableStringBuilder, getString(R.string.find_factors_subtitle), new String[]{NUMBER_FORMAT.format(getTask().getNumber())}, ContextCompat.getColor(getActivity(), R.color.orange_dark)));

                    //Statistics
                    numbersPerSecondTextView.setText(Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.numbers_per_second), new String[]{NUMBER_FORMAT.format(statisticsMap.get(getTask()).finalNumbersPerSecond)}, ContextCompat.getColor(getActivity(), R.color.orange_dark)));

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
        if (isAdded() && !isDetached()&& getTask() != null){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    updateUi();

                    //Title
                    title.setText(getString(R.string.status_finished));
                    progressBar.clearAnimation();

                    //Subtitle
                    Utils.formatSpannable(spannableStringBuilder, getResources().getQuantityString(R.plurals.find_factors_subtitle_results, getTask().getFactors().size()), new String[]{NUMBER_FORMAT.format(getTask().getNumber()), NUMBER_FORMAT.format(getTask().getFactors().size())}, ContextCompat.getColor(getActivity(), R.color.orange_dark));
                    if (getTask().getFactors().size() != 2){
                        subtitleTextView.setText(spannableStringBuilder);
                    }else{
                        final SpannableStringBuilder ssb = new SpannableStringBuilder();
                        Utils.formatSpannable(ssb, getResources().getString(R.string.find_factors_subtitle_results_extension), new String[]{"prime"}, ContextCompat.getColor(getActivity(), R.color.orange_dark));
                        subtitleTextView.setText(TextUtils.concat(spannableStringBuilder, " ", ssb));
                    }

                    //Body
                    bodyTextView.setVisibility(View.GONE);

                    //Statistics
                    etaTextView.setVisibility(View.GONE);
                    double elapsed = (double) getTask().getElapsedTime() / 1000;
                    if (elapsed <= 0){
                        elapsed = 1;
                    }
                    numbersPerSecondTextView.setText(Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.average_numbers_per_second), new String[]{NUMBER_FORMAT.format((long) (getTask().getMaxValue() / elapsed))}, ContextCompat.getColor(getActivity(), R.color.orange_dark)));

                    //Buttons
                    final ViewGroup.LayoutParams layoutParams = centerView.getLayoutParams();
                    layoutParams.width = 0;
                    centerView.setLayoutParams(layoutParams);
                    pauseButton.setVisibility(View.GONE);
                    viewAllButton.setVisibility(View.VISIBLE);
                    saveButton.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    protected void onUiUpdate() {
        if (getTask() != null){

            //Update progress
            progress.setText(String.valueOf((int) (getTask().getProgress() * 100)));
            progressBar.setProgress((int) (getTask().getProgress() * 100));

            //Elapsed time
            timeElapsedTextView.setText(Utils.formatTimeHuman(getTask().getElapsedTime(), 2));

            //Body
            bodyTextView.setText(Utils.formatSpannable(spannableStringBuilder, getString(R.string.find_factors_body_text), new String[]{NUMBER_FORMAT.format(getTask().getFactors().size())}, ContextCompat.getColor(getActivity(), R.color.orange_dark)));

            //Time remaining
            etaTextView.setText(Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.time_remaining), new String[]{Utils.formatTimeHuman(getTask().getEstimatedTimeRemaining(), 1)}, ContextCompat.getColor(getActivity(), R.color.orange_dark)));

            //Update statistics every second
            if (getTask().getElapsedTime() - statisticsMap.get(getTask()).lastUpdateTime >= 1000) {

                //Numbers per second
                final long currentValue = getTask().getCurrentValue();
                statisticsMap.get(getTask()).finalNumbersPerSecond = currentValue - statisticsMap.get(getTask()).lastCurrentValue;
                numbersPerSecondTextView.setText(Utils.formatSpannableColor(spannableStringBuilder, getString(R.string.numbers_per_second), new String[]{NUMBER_FORMAT.format(currentValue - statisticsMap.get(getTask()).lastCurrentValue)}, ContextCompat.getColor(getActivity(), R.color.orange_dark)));
                statisticsMap.get(getTask()).lastCurrentValue = currentValue;

                statisticsMap.get(getTask()).lastUpdateTime = getTask().getElapsedTime();
            }

            //Update recyclerView
            if (lastAdapterSize != adapter.getItemCount()){
                try {
                    adapter.notifyItemRangeInserted(lastAdapterSize, adapter.getItemCount() - lastAdapterSize);
                }catch (IllegalStateException e) {}
                lastAdapterSize = adapter.getItemCount();
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }
        }
    }

    @Override
    public FindFactorsTask getTask() {
        return (FindFactorsTask) super.getTask();
    }

    @Override
    public void setTask(final Task task) {
        super.setTask(task);
        if (getTask() != null) {
            if (!statisticsMap.containsKey(getTask())) {
                statisticsMap.put(getTask(), new Statistics());
            }
        }
        if (getView() != null){
            init();
        }
    }

    private void init(){
        if (getTask() != null){

            //Reset view states
            resultsView.setVisibility(View.VISIBLE);
            noTaskView.setVisibility(View.GONE);
            bodyTextView.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            etaTextView.setVisibility(View.VISIBLE);

            //Add factors to the adapter
            adapter.setTask(getTask());
            adapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(adapter.getItemCount() - 1);

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

        }else{
            resultsView.setVisibility(View.GONE);
            noTaskView.setVisibility(View.VISIBLE);
        }
    }
}
