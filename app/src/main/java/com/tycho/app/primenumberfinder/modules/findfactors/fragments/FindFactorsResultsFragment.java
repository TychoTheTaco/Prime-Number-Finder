package com.tycho.app.primenumberfinder.modules.findfactors.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.tycho.app.primenumberfinder.modules.findfactors.adapters.FactorsListAdapter;
import com.tycho.app.primenumberfinder.modules.ResultsFragment;
import com.tycho.app.primenumberfinder.modules.findfactors.FindFactorsTask;
import com.tycho.app.primenumberfinder.modules.findprimes.FindPrimesTask;
import com.tycho.app.primenumberfinder.modules.savedfiles.activities.DisplayFactorsActivity;
import com.tycho.app.primenumberfinder.utils.FileManager;
import com.tycho.app.primenumberfinder.utils.Utils;

import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;

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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        adapter = new FactorsListAdapter(activity);

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

        pauseButton = rootView.findViewById(R.id.pause_button);
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
                            getTask().resume();
                        }

                        final Intent intent = new Intent(getActivity(), DisplayFactorsActivity.class);
                        intent.putExtra("filePath", file.getAbsolutePath());
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
        if (isAdded() && !isDetached()){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    //Title
                    title.setText(getString(R.string.status_searching));
                    progressBar.startAnimation(rotate);

                    //Buttons
                    centerView.getLayoutParams().width = (int) Utils.dpToPx(getActivity(), 64);
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
                    //Title
                    title.setText(getString(R.string.state_pausing));

                    //Buttons
                    pauseButton.setEnabled(false);
                }
            }
        });
    }

    @Override
    public void onTaskPaused() {
        super.onTaskPaused();
        if (isAdded() && !isDetached()){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onUiUpdate();

                    //Title
                    title.setText(getString(R.string.status_paused));
                    progressBar.clearAnimation();

                    //Buttons
                    pauseButton.setEnabled(true);
                    pauseButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                    saveButton.setVisibility(View.VISIBLE);
                    viewAllButton.setVisibility(View.VISIBLE);
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
        if (isAdded() && !isDetached()&& getTask() != null){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Call onUiUpdate() one last time.");
                    onUiUpdate();

                    Log.d(TAG, "Begin final UI update.");

                    //Title
                    title.setText(getString(R.string.status_finished));
                    progressBar.clearAnimation();

                    //Subtitle
                    spannableStringBuilder.clear();
                    spannableStringBuilder.clearSpans();
                    final String number = NUMBER_FORMAT.format(getTask().getNumber());
                    final String factors = NUMBER_FORMAT.format(getTask().getFactors().size());
                    spannableStringBuilder.append(getResources().getQuantityString(R.plurals.find_factors_subtitle_results, getTask().getFactors().size(), number, factors));
                    spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.orange_dark)), spannableStringBuilder.toString().indexOf(number), spannableStringBuilder.toString().indexOf(number) + number.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), spannableStringBuilder.toString().indexOf(number), spannableStringBuilder.toString().indexOf(number) + number.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.orange_dark)), spannableStringBuilder.toString().indexOf(factors), spannableStringBuilder.toString().indexOf(factors) + factors.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), spannableStringBuilder.toString().indexOf(factors), spannableStringBuilder.toString().indexOf(factors) + factors.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    subtitleTextView.setText(spannableStringBuilder);

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

    private final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
    private long lastUpdateTime;

    @Override
    protected void onUiUpdate() {
        if (getTask() != null){

            //Update progress
            progress.setText(String.valueOf((int) (getTask().getProgress() * 100)));
            progressBar.setProgress((int) (getTask().getProgress() * 100));

            //Elapsed time
            timeElapsedTextView.setText(Utils.formatTimeHuman(getTask().getElapsedTime(), 2));

            //Subtitle
            Log.d(TAG, "onUiUpdate()");
            spannableStringBuilder.clear();
            spannableStringBuilder.clearSpans();
            final String number = NUMBER_FORMAT.format(getTask().getNumber());
            spannableStringBuilder.append(getString(R.string.find_factors_subtitle, number));
            spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.orange_dark)), spannableStringBuilder.toString().indexOf(number), spannableStringBuilder.toString().indexOf(number) + number.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), spannableStringBuilder.toString().indexOf(number), spannableStringBuilder.toString().indexOf(number) + number.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            subtitleTextView.setText(spannableStringBuilder);

            //Body
            spannableStringBuilder.clear();
            spannableStringBuilder.clearSpans();
            final String factors = NUMBER_FORMAT.format(getTask().getFactors().size());
            spannableStringBuilder.append(getString(R.string.find_factors_body_text, factors));
            spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.orange_dark)), spannableStringBuilder.toString().indexOf(factors), spannableStringBuilder.toString().indexOf(factors) + factors.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), spannableStringBuilder.toString().indexOf(factors), spannableStringBuilder.toString().indexOf(factors) + factors.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            bodyTextView.setText(spannableStringBuilder);

            //Time remaining
            spannableStringBuilder.clear();
            spannableStringBuilder.clearSpans();
            final String time = Utils.formatTimeHuman(getTask().getEstimatedTimeRemaining(), 1);
            spannableStringBuilder.append(time);
            spannableStringBuilder.append(" remaining");
            spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.orange_dark)), 0, time.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            etaTextView.setText(spannableStringBuilder);

            //Update statistics every second
            if (System.currentTimeMillis() - lastUpdateTime >= 1000) {

                //Numbers per second
                spannableStringBuilder.clear();
                spannableStringBuilder.clearSpans();
                final String nps = NUMBER_FORMAT.format(getTask().getCurrentValue() - lastCurrentValue);
                spannableStringBuilder.append(nps);
                spannableStringBuilder.append(" numbers per second");
                spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.orange_dark)), 0, nps.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                numbersPerSecondTextView.setText(spannableStringBuilder);
                lastCurrentValue = getTask().getCurrentValue();

                lastUpdateTime = System.currentTimeMillis();
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

            //Reset statistics
            lastUpdateTime = 0;
            lastCurrentValue = 0;

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
